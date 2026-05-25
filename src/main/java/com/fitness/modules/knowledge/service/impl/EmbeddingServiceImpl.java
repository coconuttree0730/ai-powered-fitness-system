package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.knowledge.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    @Qualifier("ioTaskExecutor")
    private final Executor ioTaskExecutor;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.embedding.model:embeddinggemma:300m}")
    private String embeddingModel;

    @Override
    public float[] embed(String text) {
        if (StrUtil.isBlank(text)) {
            return new float[0];
        }

        try {
            String url = ollamaBaseUrl + "/api/embeddings";

            JSONObject requestBody = new JSONObject();
            requestBody.set("model", embeddingModel);
            requestBody.set("prompt", text);

            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            if (!response.isOk()) {
                log.error("Ollama embedding 请求失败: {}", response.body());
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "向量嵌入服务调用失败");
            }

            JSONObject result = JSONUtil.parseObj(response.body());
            JSONArray embeddingArray = result.getJSONArray("embedding");

            float[] embedding = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = embeddingArray.getFloat(i);
            }

            return embedding;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 Ollama embedding 服务失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "向量嵌入服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public float[][] embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new float[0][];
        }

        // Semaphore限制并发数，防止Ollama过载
        Semaphore semaphore = new Semaphore(5);
        List<CompletableFuture<float[]>> futures = new ArrayList<>();

        for (String text : texts) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        return embed(text);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }, ioTaskExecutor));
        }

        float[][] embeddings = new float[texts.size()][];
        for (int i = 0; i < futures.size(); i++) {
            embeddings[i] = futures.get(i).join();
        }
        return embeddings;
    }

    @Override
    public int getDimension() {
        return 768;
    }
}

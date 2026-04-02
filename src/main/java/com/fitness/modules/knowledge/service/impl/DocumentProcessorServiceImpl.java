package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import com.fitness.modules.knowledge.service.DocumentProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DocumentProcessorServiceImpl implements DocumentProcessorService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_CHUNK_OVERLAP = 50;
    private static final int MIN_CHUNK_SIZE = 100;

    @Value("${knowledge.chunk.size:500}")
    private int chunkSize;

    @Value("${knowledge.chunk.overlap:50}")
    private int chunkOverlap;

    @Override
    public String parseFile(String fileUrl, String fileType) {
        if (StrUtil.isBlank(fileUrl)) {
            return "";
        }

        try {
            String content = HttpRequest.get(fileUrl)
                    .timeout(30000)
                    .execute()
                    .body();

            content = cleanContent(content);

            log.info("文件解析成功，文件类型: {}, 内容长度: {}", fileType, content.length());
            return content;
        } catch (Exception e) {
            log.error("文件解析失败，文件URL: {}", fileUrl, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "文件解析失败: " + e.getMessage());
        }
    }

    @Override
    public List<KnowledgeChunk> chunkDocument(KnowledgeDocument document, String content) {
        List<KnowledgeChunk> chunks = new ArrayList<>();

        if (StrUtil.isBlank(content)) {
            return chunks;
        }

        List<String> textChunks = splitText(content);

        for (int i = 0; i < textChunks.size(); i++) {
            String chunkContent = textChunks.get(i);

            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunkContent);
            chunk.setContentHash(DigestUtil.md5Hex(chunkContent));
            chunk.setCharCount(chunkContent.length());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_id", document.getId());
            metadata.put("document_title", document.getTitle());
            metadata.put("chunk_index", i);
            chunk.setMetadata(metadata);

            chunks.add(chunk);
        }

        log.info("文档切片完成，文档ID: {}, 切片数量: {}", document.getId(), chunks.size());
        return chunks;
    }

    @Override
    public String generateSummary(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }

        String cleanContent = cleanContent(content);

        if (cleanContent.length() <= 200) {
            return cleanContent;
        }

        int end = Math.min(200, cleanContent.length());
        int lastPeriod = cleanContent.lastIndexOf("。", end);
        int lastQuestion = cleanContent.lastIndexOf("？", end);
        int lastExclamation = cleanContent.lastIndexOf("！", end);

        int breakPoint = Math.max(Math.max(lastPeriod, lastQuestion), lastExclamation);
        if (breakPoint > 100) {
            return cleanContent.substring(0, breakPoint + 1);
        }

        return cleanContent.substring(0, end) + "...";
    }

    @Override
    public String extractTitle(String content, String fileName) {
        if (StrUtil.isNotBlank(content)) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("# ")) {
                    return line.substring(2).trim();
                }
                if (StrUtil.isNotBlank(line) && line.length() <= 100) {
                    return line;
                }
            }
        }

        if (StrUtil.isNotBlank(fileName)) {
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                return fileName.substring(0, dotIndex);
            }
            return fileName;
        }

        return "未命名文档";
    }

    private List<String> splitText(String content) {
        List<String> chunks = new ArrayList<>();

        String[] paragraphs = content.split("\n\n+");

        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (StrUtil.isBlank(paragraph)) {
                continue;
            }

            if (paragraph.length() > chunkSize) {
                if (currentSize > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentSize = 0;
                }

                List<String> subChunks = splitLongParagraph(paragraph);
                chunks.addAll(subChunks);
            } else {
                if (currentSize + paragraph.length() + 1 > chunkSize && currentSize >= MIN_CHUNK_SIZE) {
                    chunks.add(currentChunk.toString().trim());

                    String overlapText = getOverlapText(currentChunk.toString());
                    currentChunk = new StringBuilder(overlapText);
                    currentSize = overlapText.length();
                }

                if (currentSize > 0) {
                    currentChunk.append("\n\n");
                    currentSize += 2;
                }
                currentChunk.append(paragraph);
                currentSize += paragraph.length();
            }
        }

        if (currentSize > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> splitLongParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();

        String[] sentences = paragraph.split("(?<=[。！？.!?])");

        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (StrUtil.isBlank(sentence)) {
                continue;
            }

            if (currentSize + sentence.length() > chunkSize && currentSize >= MIN_CHUNK_SIZE) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                currentSize = 0;
            }

            currentChunk.append(sentence);
            currentSize += sentence.length();
        }

        if (currentSize > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private String getOverlapText(String text) {
        if (text.length() <= chunkOverlap) {
            return text;
        }

        int start = text.length() - chunkOverlap;
        int breakPoint = text.indexOf("\n", start);
        if (breakPoint > 0 && breakPoint < text.length() - 10) {
            return text.substring(breakPoint + 1);
        }

        return text.substring(start);
    }

    private String cleanContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }

        content = content.replaceAll("\r\n", "\n");
        content = content.replaceAll("\r", "\n");
        content = content.replaceAll("[ \t]+", " ");
        content = content.replaceAll("\n{3,}", "\n\n");
        content = content.trim();

        return content;
    }
}

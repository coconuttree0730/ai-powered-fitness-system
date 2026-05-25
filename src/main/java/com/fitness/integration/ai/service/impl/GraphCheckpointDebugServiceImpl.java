package com.fitness.integration.ai.service.impl;

import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.serializer.check_point.CheckPointSerializer;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.model.vo.GraphCheckpointDetailVO;
import com.fitness.integration.ai.service.GraphCheckpointDebugService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphCheckpointDebugServiceImpl implements GraphCheckpointDebugService {

    private static final String CHECKPOINT_PREFIX = "graph:checkpoint:content:";
    private static final String THREAD_META_PREFIX = "graph:thread:meta:";
    private static final String THREAD_REVERSE_PREFIX = "graph:thread:reverse:";
    private static final String FIELD_THREAD_ID = "thread_id";
    private static final String FIELD_THREAD_NAME = "thread_name";
    private static final String FIELD_IS_RELEASED = "is_released";

    private final RedissonClient redissonClient;

    @Override
    public GraphCheckpointDetailVO getThreadCheckpointDetail(String threadName) {
        if (!StringUtils.hasText(threadName)) {
            throw new BusinessException("threadId 不能为空");
        }

        String metaKey = THREAD_META_PREFIX + threadName;
        RMap<String, String> meta = redissonClient.getMap(metaKey);
        String activeThreadId = meta.get(FIELD_THREAD_ID);
        if (!StringUtils.hasText(activeThreadId)) {
            throw new BusinessException("未找到对应的 graph checkpoint 线程");
        }

        String reverseKey = THREAD_REVERSE_PREFIX + activeThreadId;
        RMap<String, String> reverse = redissonClient.getMap(reverseKey);

        String contentKey = CHECKPOINT_PREFIX + activeThreadId;
        RBucket<String> bucket = redissonClient.getBucket(contentKey);
        GraphCheckpointDetailVO detail = buildDetail(decodeContent(bucket.get()));
        detail.setThreadName(threadName);
        detail.setActiveThreadId(activeThreadId);
        detail.setReleased(isReleased(meta.get(FIELD_IS_RELEASED)));
        detail.setMetaKey(metaKey);
        detail.setReverseKey(reverseKey);
        detail.setContentKey(contentKey);
        detail.setMeta(buildThreadMeta(meta.get(FIELD_THREAD_ID), meta.get(FIELD_IS_RELEASED)));
        detail.setReverse(buildReverseMeta(reverse.get(FIELD_THREAD_NAME), reverse.get(FIELD_IS_RELEASED)));
        return detail;
    }

    @Override
    public GraphCheckpointDetailVO decodeCheckpointContent(String content) {
        GraphCheckpointDetailVO detail = buildDetail(decodeContent(content));
        detail.setContentKey("raw-content");
        return detail;
    }

    private LinkedList<Checkpoint> decodeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return new LinkedList<>();
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(content);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                int size = objectInputStream.readInt();
                LinkedList<Checkpoint> checkpoints = new LinkedList<>();
                CheckPointSerializer serializer = new CheckPointSerializer(StateGraph.DEFAULT_JACKSON_SERIALIZER);
                for (int i = 0; i < size; i++) {
                    checkpoints.add(serializer.read(objectInputStream));
                }
                return checkpoints;
            }
        } catch (IllegalArgumentException | IOException | ClassNotFoundException ex) {
            throw new BusinessException("Checkpoint 内容解码失败: " + ex.getMessage());
        }
    }

    private GraphCheckpointDetailVO buildDetail(List<Checkpoint> checkpoints) {
        GraphCheckpointDetailVO detail = new GraphCheckpointDetailVO();
        List<GraphCheckpointDetailVO.CheckpointVO> items = new ArrayList<>(checkpoints.size());
        for (Checkpoint checkpoint : checkpoints) {
            GraphCheckpointDetailVO.CheckpointVO item = new GraphCheckpointDetailVO.CheckpointVO();
            item.setId(checkpoint.getId());
            item.setNodeId(checkpoint.getNodeId());
            item.setNextNodeId(checkpoint.getNextNodeId());
            item.setState(checkpoint.getState());
            items.add(item);
        }
        detail.setCheckpointCount(items.size());
        detail.setCheckpoints(items);
        return detail;
    }

    private Map<String, String> buildThreadMeta(String threadId, String isReleased) {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put(FIELD_THREAD_ID, threadId);
        meta.put(FIELD_IS_RELEASED, isReleased);
        return meta;
    }

    private Map<String, String> buildReverseMeta(String threadName, String isReleased) {
        Map<String, String> reverse = new LinkedHashMap<>();
        reverse.put(FIELD_THREAD_NAME, threadName);
        reverse.put(FIELD_IS_RELEASED, isReleased);
        return reverse;
    }

    private boolean isReleased(String value) {
        return "true".equalsIgnoreCase(value);
    }
}

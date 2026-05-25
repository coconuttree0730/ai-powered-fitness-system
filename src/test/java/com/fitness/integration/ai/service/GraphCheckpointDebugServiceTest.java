package com.fitness.integration.ai.service;

import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.serializer.check_point.CheckPointSerializer;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.ai.model.vo.GraphCheckpointDetailVO;
import com.fitness.integration.ai.service.impl.GraphCheckpointDebugServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphCheckpointDebugServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RMap<String, String> metaMap;

    @Mock
    private RMap<String, String> reverseMap;

    @Mock
    private RBucket<String> bucket;

    @Test
    void getThreadCheckpointDetailShouldDecodeRedisCheckpointContent() throws IOException {
        GraphCheckpointDebugService service = new GraphCheckpointDebugServiceImpl(redissonClient);
        String threadName = "session-1001";
        String runtimeThreadId = "runtime-thread-uuid";

        doReturn(metaMap).when(redissonClient).getMap("graph:thread:meta:" + threadName);
        when(metaMap.get("thread_id")).thenReturn(runtimeThreadId);
        when(metaMap.get("is_released")).thenReturn("false");

        doReturn(reverseMap).when(redissonClient).getMap("graph:thread:reverse:" + runtimeThreadId);
        when(reverseMap.get("thread_name")).thenReturn(threadName);
        when(reverseMap.get("is_released")).thenReturn("false");

        doReturn(bucket).when(redissonClient).getBucket("graph:checkpoint:content:" + runtimeThreadId);
        when(bucket.get()).thenReturn(serialize(List.of(
                Checkpoint.builder()
                        .id("cp-1")
                        .nodeId("planner")
                        .nextNodeId("END")
                        .state(Map.of("step", "collect_profile", "count", 1))
                        .build()
        )));

        GraphCheckpointDetailVO detail = service.getThreadCheckpointDetail(threadName);

        assertEquals(threadName, detail.getThreadName());
        assertEquals(runtimeThreadId, detail.getActiveThreadId());
        assertEquals(1, detail.getCheckpointCount());
        assertEquals("cp-1", detail.getCheckpoints().get(0).getId());
        assertEquals("planner", detail.getCheckpoints().get(0).getNodeId());
        assertEquals("collect_profile", detail.getCheckpoints().get(0).getState().get("step"));
    }

    @Test
    void decodeCheckpointContentShouldReturnStructuredCheckpoints() throws IOException {
        GraphCheckpointDebugService service = new GraphCheckpointDebugServiceImpl(redissonClient);
        String content = serialize(List.of(
                Checkpoint.builder()
                        .id("cp-2")
                        .nodeId("tool")
                        .nextNodeId("answer")
                        .state(Map.of("status", "running"))
                        .build(),
                Checkpoint.builder()
                        .id("cp-1")
                        .nodeId("start")
                        .nextNodeId("tool")
                        .state(Map.of("status", "ready"))
                        .build()
        ));

        GraphCheckpointDetailVO detail = service.decodeCheckpointContent(content);

        assertEquals(2, detail.getCheckpointCount());
        assertEquals("cp-2", detail.getCheckpoints().get(0).getId());
        assertEquals("running", detail.getCheckpoints().get(0).getState().get("status"));
        assertEquals("cp-1", detail.getCheckpoints().get(1).getId());
    }

    @Test
    void decodeCheckpointContentShouldRejectInvalidContent() {
        GraphCheckpointDebugService service = new GraphCheckpointDebugServiceImpl(redissonClient);

        assertThrows(BusinessException.class, () -> service.decodeCheckpointContent("not-base64"));
    }

    private String serialize(List<Checkpoint> checkpoints) throws IOException {
        CheckPointSerializer serializer = new CheckPointSerializer(StateGraph.DEFAULT_JACKSON_SERIALIZER);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeInt(checkpoints.size());
            for (Checkpoint checkpoint : checkpoints) {
                serializer.write(checkpoint, objectOutputStream);
            }
            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }
}

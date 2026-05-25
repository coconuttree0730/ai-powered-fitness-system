package com.fitness.integration.ai.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphCheckpointDetailVO {

    private String threadName;

    private String activeThreadId;

    private Boolean released;

    private String metaKey;

    private String reverseKey;

    private String contentKey;

    private Integer checkpointCount;

    private Map<String, String> meta;

    private Map<String, String> reverse;

    private List<CheckpointVO> checkpoints;

    @Data
    public static class CheckpointVO {

        private String id;

        private String nodeId;

        private String nextNodeId;

        private Map<String, Object> state;
    }
}

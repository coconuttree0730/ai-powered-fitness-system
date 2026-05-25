package com.fitness.integration.ai.service;

import com.fitness.integration.ai.model.vo.GraphCheckpointDetailVO;

public interface GraphCheckpointDebugService {

    GraphCheckpointDetailVO getThreadCheckpointDetail(String threadName);

    GraphCheckpointDetailVO decodeCheckpointContent(String content);
}

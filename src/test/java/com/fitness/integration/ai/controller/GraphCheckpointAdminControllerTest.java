package com.fitness.integration.ai.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.ai.model.vo.GraphCheckpointDetailVO;
import com.fitness.integration.ai.service.GraphCheckpointDebugService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphCheckpointAdminControllerTest {

    @Test
    void getCheckpointDetailShouldReturnWrappedResult() {
        GraphCheckpointDebugService debugService = mock(GraphCheckpointDebugService.class);
        GraphCheckpointAdminController controller = new GraphCheckpointAdminController(debugService);
        GraphCheckpointDetailVO detail = new GraphCheckpointDetailVO();
        detail.setThreadName("session-1");
        when(debugService.getThreadCheckpointDetail("session-1")).thenReturn(detail);

        Result<GraphCheckpointDetailVO> result = controller.getCheckpointDetail("session-1");

        assertSame(detail, result.getData());
    }

    @Test
    void decodeCheckpointContentShouldReturnWrappedResult() {
        GraphCheckpointDebugService debugService = mock(GraphCheckpointDebugService.class);
        GraphCheckpointAdminController controller = new GraphCheckpointAdminController(debugService);
        GraphCheckpointDetailVO detail = new GraphCheckpointDetailVO();
        GraphCheckpointAdminController.DecodeCheckpointRequest request =
                new GraphCheckpointAdminController.DecodeCheckpointRequest();
        request.setContent("encoded-content");
        when(debugService.decodeCheckpointContent("encoded-content")).thenReturn(detail);

        Result<GraphCheckpointDetailVO> result = controller.decodeCheckpointContent(request);

        assertSame(detail, result.getData());
    }
}

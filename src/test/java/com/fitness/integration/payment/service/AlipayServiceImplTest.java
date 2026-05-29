package com.fitness.integration.payment.service;

import com.fitness.integration.payment.gateway.AlipayGateway;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.service.impl.AlipayServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlipayServiceImplTest {

    @Mock
    private AlipayGateway alipayGateway;

    @InjectMocks
    private AlipayServiceImpl service;

    @Test
    void createPayOrderShouldReturnGatewayBody() {
        when(alipayGateway.createWapOrder("P20260513001", new BigDecimal("99.00"), "会员卡", "年卡"))
                .thenReturn(new AlipayCreateOrderResult(true, "<form>pay</form>", null, null));

        String html = service.createPayOrder("P20260513001", new BigDecimal("99.00"), "会员卡", "年卡");

        assertEquals("<form>pay</form>", html);
    }
}

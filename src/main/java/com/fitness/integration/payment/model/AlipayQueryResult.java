package com.fitness.integration.payment.model;

public record AlipayQueryResult(
        boolean success,
        String tradeStatus,
        String code,
        String message
) {
}

package com.fitness.integration.payment.model;

public record AlipayCreateOrderResult(
        boolean success,
        String body,
        String code,
        String message
) {
}

package com.fitness.integration.payment.model;

public record AlipayRefundResult(
        boolean success,
        String code,
        String message
) {
}

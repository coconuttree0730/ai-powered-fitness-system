package com.fitness.modules.membership.model.vo;

import lombok.Data;

@Data
public class AlipayPayVO {

    private String orderNo;
    private String payForm;
    private String qrCode;
    private String payUrl;
}

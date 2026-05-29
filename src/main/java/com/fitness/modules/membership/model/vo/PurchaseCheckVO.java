package com.fitness.modules.membership.model.vo;

import lombok.Data;

@Data
public class PurchaseCheckVO {

    private Boolean hasExistingMembership;

    private String membershipType;

    private Integer remainingDays;

    private String warningMessage;
}
package com.fitness.integration.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanGenerationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long planId;
}
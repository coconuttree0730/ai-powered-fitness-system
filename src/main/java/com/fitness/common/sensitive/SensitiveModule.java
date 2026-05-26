package com.fitness.common.sensitive;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module，注册 SensitiveSerializer
 */
public class SensitiveModule extends SimpleModule {

    public SensitiveModule() {
        addSerializer(String.class, new SensitiveSerializer());
    }
}

package com.fitness.common.cache;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class CacheKeyBuilder {

    private CacheKeyBuilder() {
    }

    public static String join(Object... parts) {
        return Arrays.stream(parts)
                .map(part -> part == null ? "null" : part.toString())
                .collect(Collectors.joining("::"));
    }
}

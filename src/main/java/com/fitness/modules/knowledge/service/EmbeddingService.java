package com.fitness.modules.knowledge.service;

import java.util.List;

public interface EmbeddingService {

    float[] embed(String text);

    float[][] embedBatch(List<String> texts);

    int getDimension();
}

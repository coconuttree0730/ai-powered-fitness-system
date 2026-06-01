package com.fitness.modules.knowledge.service.impl;

import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentProcessorServiceImplTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(DocumentProcessorServiceImpl.class);

    @Test
    void defaultChunkConfigShouldMatchApplicationYamlDefaults() {
        contextRunner.run(context -> {
            DocumentProcessorServiceImpl service = context.getBean(DocumentProcessorServiceImpl.class);
            KnowledgeDocument document = new KnowledgeDocument();
            document.setId(1L);

            List<KnowledgeChunk> chunks = service.chunkDocument(document, "测试内容。".repeat(220));

            assertEquals(500, chunks.get(0).getMetadata().get("chunk_size"));
            assertEquals(50, chunks.get(0).getMetadata().get("chunk_overlap"));
        });
    }
}

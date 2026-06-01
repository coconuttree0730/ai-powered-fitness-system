package com.fitness.modules.knowledge.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeChunkMapperXmlTest {

    @Test
    void vectorAndKeywordSearchShouldFilterByCategoryWhenProvided() throws Exception {
        String xml = new ClassPathResource("mapper/knowledge/KnowledgeChunkMapper.xml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(xml.contains("<if test=\"categoryId != null\">"));
        assertTrue(xml.contains("AND kd.category_id = #{categoryId}"));
    }
}

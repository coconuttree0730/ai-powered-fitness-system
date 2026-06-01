package com.fitness.modules.knowledge.rewrite;

import com.fitness.modules.knowledge.rewrite.impl.NoopQueryRewriteServiceImpl;
import com.fitness.modules.knowledge.rewrite.impl.SimpleQueryRewriteServiceImpl;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteRequest;
import com.fitness.modules.knowledge.rewrite.model.QueryRewriteResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryRewriteServiceTest {

    @Test
    void noopRewriteShouldReturnOriginalQueryOnly() {
        QueryRewriteService service = new NoopQueryRewriteServiceImpl();

        QueryRewriteResult result = service.rewrite(request("有哪些课程"));

        assertEquals(List.of("有哪些课程"), result.getQueries());
        assertEquals("noop", result.getStrategy());
    }

    @Test
    void simpleRewriteShouldExpandCourseQuery() {
        QueryRewriteService service = new SimpleQueryRewriteServiceImpl();

        QueryRewriteResult result = service.rewrite(request("有哪些课程"));

        assertEquals("simple", result.getStrategy());
        assertTrue(result.getQueries().contains("有哪些课程"));
        assertTrue(result.getQueries().contains("团课 私教 课程 预约"));
        assertTrue(result.getQueries().contains("健身课程 类型 适合人群"));
    }

    @Test
    void simpleRewriteShouldLimitDuplicateQueries() {
        QueryRewriteService service = new SimpleQueryRewriteServiceImpl();

        QueryRewriteResult result = service.rewrite(request("会员卡可以借给朋友用吗"));

        assertEquals(result.getQueries().size(), result.getQueries().stream().distinct().count());
        assertTrue(result.getQueries().size() <= 3);
    }

    private QueryRewriteRequest request(String query) {
        QueryRewriteRequest request = new QueryRewriteRequest();
        request.setQuery(query);
        request.setMaxQueries(3);
        return request;
    }
}

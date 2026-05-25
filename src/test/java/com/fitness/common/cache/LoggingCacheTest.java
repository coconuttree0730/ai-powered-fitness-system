package com.fitness.common.cache;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingCacheTest {

    @Test
    void cacheGetWithLoaderLogsMissThenHit() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingCache.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);

        try {
            LoggingCache cache = new LoggingCache(new ConcurrentMapCache("test-cache"));
            AtomicInteger loaderCalls = new AtomicInteger();

            String first = cache.get("k1", () -> {
                loaderCalls.incrementAndGet();
                return "value";
            });
            String second = cache.get("k1", () -> {
                loaderCalls.incrementAndGet();
                return "other";
            });

            assertEquals("value", first);
            assertEquals("value", second);
            assertEquals(1, loaderCalls.get());
            assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("[CACHE MISS]")));
            assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("[CACHE HIT]")));
        } finally {
            logger.detachAppender(appender);
        }
    }
}

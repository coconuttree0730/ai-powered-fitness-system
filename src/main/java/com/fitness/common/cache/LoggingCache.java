package com.fitness.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class LoggingCache implements Cache {

    private static final int MAX_TEXT_LENGTH = 120;

    private final Cache delegate;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = delegate.get(key);
        logAccess(valueWrapper == null ? "MISS" : "HIT", key, valueWrapper == null ? null : valueWrapper.get());
        return valueWrapper;
    }

    @Override
    @Nullable
    public <T> T get(Object key, @Nullable Class<T> type) {
        T value = delegate.get(key, type);
        logAccess(value == null ? "MISS" : "HIT", key, value);
        return value;
    }

    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper existing = delegate.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            T value = (T) existing.get();
            logAccess("HIT", key, value);
            return value;
        }

        logAccess("MISS", key, null);
        T loaded = delegate.get(key, valueLoader);
        logWrite("LOAD", key, loaded);
        return loaded;
    }

    @Override
    @Nullable
    public CompletableFuture<?> retrieve(Object key) {
        CompletableFuture<?> future = delegate.retrieve(key);
        log.debug("[CACHE RETRIEVE] cache={}, key={}", getName(), abbreviate(key));
        return future;
    }

    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
        ValueWrapper existing = delegate.get(key);
        if (existing != null) {
            @SuppressWarnings("unchecked")
            T value = (T) existing.get();
            logAccess("HIT", key, value);
        } else {
            logAccess("MISS", key, null);
        }
        return delegate.retrieve(key, valueLoader);
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        delegate.put(key, value);
        logWrite("PUT", key, value);
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        ValueWrapper existing = delegate.putIfAbsent(key, value);
        if (existing == null) {
            logWrite("PUT_IF_ABSENT", key, value);
        } else {
            logAccess("HIT", key, existing.get());
        }
        return existing;
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
        log.debug("[CACHE EVICT] cache={}, key={}", getName(), abbreviate(key));
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean removed = delegate.evictIfPresent(key);
        log.debug("[CACHE EVICT_IF_PRESENT] cache={}, key={}, removed={}", getName(), abbreviate(key), removed);
        return removed;
    }

    @Override
    public void clear() {
        delegate.clear();
        log.debug("[CACHE CLEAR] cache={}", getName());
    }

    @Override
    public boolean invalidate() {
        boolean invalidated = delegate.invalidate();
        log.debug("[CACHE INVALIDATE] cache={}, invalidated={}", getName(), invalidated);
        return invalidated;
    }

    private void logAccess(String action, Object key, @Nullable Object value) {
        if (value == null) {
            log.debug("[CACHE {}] cache={}, key={}", action, getName(), abbreviate(key));
            return;
        }
        log.debug("[CACHE {}] cache={}, key={}, value={}", action, getName(), abbreviate(key), abbreviate(value));
    }

    private void logWrite(String action, Object key, @Nullable Object value) {
        if (value == null) {
            log.debug("[CACHE {}] cache={}, key={}", action, getName(), abbreviate(key));
            return;
        }
        log.debug("[CACHE {}] cache={}, key={}, value={}", action, getName(), abbreviate(key), abbreviate(value));
    }

    private String abbreviate(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        String text = Objects.toString(value);
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH) + "...";
    }
}

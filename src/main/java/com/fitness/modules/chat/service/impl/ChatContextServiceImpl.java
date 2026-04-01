package com.fitness.modules.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.model.entity.ChatMessage;
import com.fitness.modules.chat.service.ChatContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatContextServiceImpl implements ChatContextService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageMapper chatMessageMapper;

    private static final String CHAT_CONTEXT_KEY_PREFIX = "chat:context:";
    private static final Duration CONTEXT_EXPIRE_DURATION = Duration.ofHours(2);

    @Override
    public void addMessage(Long sessionId, ChatMessage message) {
        String key = CHAT_CONTEXT_KEY_PREFIX + sessionId;
        
        redisTemplate.opsForList().rightPush(key, message);
        
        redisTemplate.expire(key, CONTEXT_EXPIRE_DURATION);
        
        log.debug("添加消息到会话上下文: sessionId={}, role={}", sessionId, message.getRole());
    }

    @Override
    public List<ChatMessage> getContext(Long sessionId, int maxMessages) {
        String key = CHAT_CONTEXT_KEY_PREFIX + sessionId;
        
        List<Object> objects = redisTemplate.opsForList().range(key, -maxMessages, -1);
        
        if (CollUtil.isEmpty(objects)) {
            log.debug("Redis中无上下文，从数据库加载: sessionId={}", sessionId);
            return loadFromDatabase(sessionId, maxMessages);
        }
        
        List<ChatMessage> messages = new ArrayList<>();
        for (Object obj : objects) {
            if (obj instanceof ChatMessage) {
                messages.add((ChatMessage) obj);
            }
        }
        
        log.debug("获取会话上下文: sessionId={}, 消息数={}", sessionId, messages.size());
        return messages;
    }

    @Override
    public void clearContext(Long sessionId) {
        String key = CHAT_CONTEXT_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.debug("清除会话上下文: sessionId={}", sessionId);
    }

    @Override
    public void saveMessageToDatabase(ChatMessage message) {
        chatMessageMapper.insert(message);
        log.debug("保存消息到数据库: id={}, sessionId={}", message.getId(), message.getSessionId());
    }

    private List<ChatMessage> loadFromDatabase(Long sessionId, int limit) {
        List<ChatMessage> messages = chatMessageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit)
        );
        
        if (CollUtil.isNotEmpty(messages)) {
            String key = CHAT_CONTEXT_KEY_PREFIX + sessionId;
            for (ChatMessage msg : messages) {
                redisTemplate.opsForList().rightPush(key, msg);
            }
            redisTemplate.expire(key, CONTEXT_EXPIRE_DURATION);
        }
        
        return CollUtil.isEmpty(messages) ? new ArrayList<>() : messages;
    }
}

package com.fitness.integration.ai.agent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentPrompts;
import com.fitness.modules.chat.tools.CoachQueryTools;
import com.fitness.modules.chat.tools.CourseQueryTools;
import com.fitness.modules.chat.tools.LocationQueryTools;
import com.fitness.modules.chat.tools.MembershipQueryTools;
import com.fitness.modules.chat.tools.ProductQueryTools;
import com.fitness.modules.chat.tools.ProfileQueryTools;
import com.fitness.modules.chat.tools.RagQueryTool;
import com.fitness.modules.chat.tools.WeatherQueryTools;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class AgentRuntimeConfig {

    private final ChatModel chatModel;
    private final DataSource dataSource;
    private final JianXiaoZhuAgentPrompts prompts;
    private final MembershipQueryTools membershipQueryTools;
    private final CourseQueryTools courseQueryTools;
    private final ProductQueryTools productQueryTools;
    private final CoachQueryTools coachQueryTools;
    private final ProfileQueryTools profileQueryTools;
    private final WeatherQueryTools weatherQueryTools;
    private final LocationQueryTools locationQueryTools;
    private final RagQueryTool ragQueryTool;
    private final RedisProperties redisProperties;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String host = redisProperties.getHost() != null ? redisProperties.getHost() : "localhost";
        int port = redisProperties.getPort() != 0 ? redisProperties.getPort() : 6379;
        String password = redisProperties.getPassword();

        // 调试日志
        System.out.println("[RedissonConfig] Redis Host: " + host);
        System.out.println("[RedissonConfig] Redis Port: " + port);
        System.out.println("[RedissonConfig] Redis Password: '" + password + "'");
        System.out.println("[RedissonConfig] Redis Password length: " + (password != null ? password.length() : 0));

        String address = "redis://" + host + ":" + port;
        if (password == null || password.isBlank()) {
            System.out.println("[RedissonConfig] Connecting to Redis without password");
            config.useSingleServer().setAddress(address);
        } else {
            System.out.println("[RedissonConfig] Connecting to Redis with password");
            config.useSingleServer().setAddress(address).setPassword(password);
        }
        return Redisson.create(config);
    }

    @Bean
    public RedisSaver redisSaver(RedissonClient redissonClient) {
        return RedisSaver.builder()
                .redisson(redissonClient)
                .build();
    }

    @Bean
    public DatabaseStore databaseStore() {
        return new DatabaseStore(dataSource, "chat_memory_store");
    }

    @Bean
    public SummarizationHook summarizationHook() {
        return SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(1000) // 触发摘要的token阈值
                .messagesToKeep(3) // 保留最近3条消息
                .build();
    }

    @Bean
    public ReactAgent jianXiaoZhuReactAgent(RedisSaver redisSaver, SummarizationHook summarizationHook) {
        return ReactAgent.builder()
                .name("jian_xiao_zhu_agent")
                .model(chatModel)
                .instruction(prompts.systemPrompt())
                .methodTools(
                        membershipQueryTools,
                        courseQueryTools,
                        productQueryTools,
                        coachQueryTools,
                        profileQueryTools,
                        weatherQueryTools,
                        locationQueryTools,
                        ragQueryTool
                )
                .hooks(summarizationHook)
                .parallelToolExecution(false)
                .saver(redisSaver)
                .build();
    }
}

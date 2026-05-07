package com.fitness.modules.chat.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WeatherQueryTools {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WeatherQueryTools() {
        this.restClient = RestClient.builder()
                .baseUrl("https://wttr.in")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Tool(description = "查询城市天气，返回天气状态、温度、风速")
    public String getWeather(String city) {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri("/" + city + "?format=j1")
                    .retrieve()
                    .toEntity(String.class);

            JsonNode current = objectMapper.readTree(response.getBody())
                    .path("current_condition").get(0);

            String desc = current.path("weatherDesc").get(0).path("value").asText();
            String temp = current.path("temp_C").asText();
            String wind = current.path("windspeedKmph").asText();

            String result = String.format("天气：%s，温度：%s°C，风速：%skm/h", desc, temp, wind);
            log.info("\n========== toolmessage ==========\n工具: 天气查询\n查询城市: {}\n返回结果:\n{}\n========== toolmessage end ==========\n", city, result);
            return result;

        } catch (Exception e) {
            String errorResult = "天气查询失败";
            log.error("\n========== toolmessage ==========\n工具: 天气查询\n查询城市: {}\n异常: {}\n返回结果:\n{}\n========== toolmessage end ==========\n", city, e.getMessage(), errorResult);
            return errorResult;
        }
    }
}

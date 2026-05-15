package com.fitness.modules.chat.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class LocationQueryTools {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${chat.tools.location.default-city:北京}")
    private String defaultCity;

    public LocationQueryTools() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl("https://ipapi.co")
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Tool(description = """
            获取用户当前的地理位置（城市名称）。
            当用户询问天气但没有指定城市时，必须先调用此工具获取当前位置，然后再调用天气查询工具。
            例如用户问"今天什么天气？"、"现在天气怎么样？"等没有指定城市的问题时，必须先获取位置。
            返回当前城市名称，如"上海"、"北京"等。
            """)
    public String getCurrentLocation() {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri("/json/")
                    .retrieve()
                    .toEntity(String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String city = root.path("city").asText();
            String region = root.path("region").asText();
            String countryName = root.path("country_name").asText();

            if (city == null || city.isBlank()) {
                log.warn("\n========== toolmessage ==========\n工具: 位置查询\n返回结果:\n{}\n========== toolmessage end ==========\n", defaultCity);
                return defaultCity;
            }

            String result = String.format("城市: %s, 省份: %s, 国家: %s", city, region, countryName);
            log.info("\n========== toolmessage ==========\n工具: 位置查询\n返回结果:\n{}\n========== toolmessage end ==========\n", result);
            return city;

        } catch (Exception e) {
            log.error("\n========== toolmessage ==========\n工具: 位置查询\n异常: {}\n返回结果:\n{}\n========== toolmessage end ==========\n", e.getMessage(), defaultCity);
            return defaultCity;
        }
    }
}
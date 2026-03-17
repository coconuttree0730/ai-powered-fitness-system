package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.fitness.**.mapper")
@EnableAsync
public class FitnessApplication {

    public static void main(String[] args) {
        //System.setProperty("nacos.logging.default.config.enabled", "false");
        SpringApplication.run(FitnessApplication.class, args);
    }

}

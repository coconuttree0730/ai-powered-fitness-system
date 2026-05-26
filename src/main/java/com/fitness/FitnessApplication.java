package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.fitness.**.mapper")
public class FitnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessApplication.class, args);
    }

}

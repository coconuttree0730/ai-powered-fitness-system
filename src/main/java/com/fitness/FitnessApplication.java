package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Paths;

@SpringBootApplication
@MapperScan("com.fitness.**.mapper")
@EnableAsync
public class FitnessApplication {

    public static void main(String[] args) {
        EnvFilePropertyLoader.load(Paths.get(".env"));
        SpringApplication.run(FitnessApplication.class, args);
    }

}

package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
@MapperScan("com.fitness.**.mapper")
public class FitnessApplication {

    public static void main(String[] args) {
        EnvFilePropertyLoader.load(Paths.get(".env"));
        SpringApplication.run(FitnessApplication.class, args);
    }

}

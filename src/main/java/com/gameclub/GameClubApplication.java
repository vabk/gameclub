package com.gameclub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.gameclub.mapper")
public class GameClubApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameClubApplication.class, args);
    }
}


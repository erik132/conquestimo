package com.conquestimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConquestimoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConquestimoApplication.class, args);
    }
}

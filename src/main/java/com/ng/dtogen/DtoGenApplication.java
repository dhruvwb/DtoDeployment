package com.ng.dtogen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DtoGenApplication {
    public static void main(String[] args) {
          System.out.println("Starting DTO Generator...****::");
        SpringApplication.run(DtoGenApplication.class, args);
    }
}
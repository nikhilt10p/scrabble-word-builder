package com.hasbropulse.scrabble;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// entry point — Spring Boot handles the rest from here
@SpringBootApplication
public class ScrabbleWordBuilderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScrabbleWordBuilderApplication.class, args);
    }
}

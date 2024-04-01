package com.janbabak.noqlbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }) // because postgres db has not been set up yet
@SpringBootApplication
public class NoQLBackendApplication {

    @SuppressWarnings("unused")
    @GetMapping
    public static String healthCheck() {
        return "NoQL backend is running";
    }

    public static void main(String[] args) {
        SpringApplication.run(NoQLBackendApplication.class, args);
    }
}
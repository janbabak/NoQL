package com.janbabak.noqlbakend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class NoqlBakendApplication {

	@GetMapping
	public static String healthCheck() {
		return "NoQL backend is running";
	}

	public static void main(String[] args) {
		SpringApplication.run(NoqlBakendApplication.class, args);
	}
}

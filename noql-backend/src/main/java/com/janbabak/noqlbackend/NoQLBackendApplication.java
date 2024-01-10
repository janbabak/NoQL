package com.janbabak.noqlbackend;

import com.janbabak.noqlbackend.repository.DatabaseInfo;
import com.janbabak.noqlbackend.repository.PostgresInfo;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }) // because postgres db has not been set up yet
public class NoQLBackendApplication {

    @GetMapping
    @SuppressWarnings("unused")
    public static String healthCheck() {
        return "NoQL backend is running";
    }

    public static void testRequest(String[] args) {
        SpringApplication.run(NoQLBackendApplication.class, args);
        try {
            QueryApi gptApi = new GptApi();
            System.out.println(gptApi.queryModel("What is the most common name in the USA."));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void getDbSchema() {
        System.out.println("Schema");
        PostgresInfo dbInfo = new PostgresInfo();
        try {
            dbInfo.getSchema();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        getDbSchema();
    }
}
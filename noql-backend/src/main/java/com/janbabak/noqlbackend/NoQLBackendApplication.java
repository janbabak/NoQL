package com.janbabak.noqlbackend;

import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.model.database.Database;
import com.janbabak.noqlbackend.service.database.PostgresService;
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

    @SuppressWarnings("unused")
    public static void testRequest(String[] args) {
        SpringApplication.run(NoQLBackendApplication.class, args);
        try {
            QueryApi gptApi = new GptApi();
            System.out.println(gptApi.queryModel("What is the most common name in the USA."));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createQuery() {
        PostgresService dbInfo = new PostgresService();
        try {
            Database db = dbInfo.retrieveSchema();

            String NLQuery = "Find all users that are males.";
            String query = QueryService.createQuery(NLQuery, db.generateCreateScript(), "Postgres", true);

            System.out.println(query);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        createQuery();
    }
}
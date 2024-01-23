package com.janbabak.noqlbackend;

import com.janbabak.noqlbackend.model.UserQueryRequest;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.api.GptApi;
import com.janbabak.noqlbackend.service.api.QueryApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@RestController
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class }) // because postgres db has not been set up yet
@SpringBootApplication
public class NoQLBackendApplication {

    public static QueryService queryService = new QueryService();

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

    @SuppressWarnings("unused")
    public static void testQuery() {
        try {
            ResultSet resultSet = queryService.handleQuery
                    (new UserQueryRequest("id", "Find all users that are males."));

            // print result
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(NoQLBackendApplication.class, args);
    }
}
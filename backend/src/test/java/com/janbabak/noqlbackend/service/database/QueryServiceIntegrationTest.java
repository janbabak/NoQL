package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.dao.PostgresTest;
import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.service.QueryService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class QueryServiceIntegrationTest extends PostgresTest  {

    @Autowired
    private QueryService queryService;

    @Autowired
    private DatabaseEntityService databaseService;

    @Override
    protected String getCreateScript() {
        return loadScriptFromFile("./src/test/resources/dbInsertScripts/postgresUsers.sql");
    }

    @Test
    @DisplayName("Test execute query language query")
    void testExecuteQueryLanguageQuery()
            throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {

        // given
        postgresDatabase = databaseService.create(postgresDatabase);
        UUID databaseId = postgresDatabase.getId();
        Integer page = 1;
        Integer pageSize = 5;
        // language=SQL
        String query = "SELECT id, name, age, sex, email FROM public.user ORDER BY name;";

        QueryResponse expectedResponse = new QueryResponse(
                new QueryResponse.RetrievedData(
                        List.of("id", "name", "age", "sex", "email"),
                        List.of(List.of("10", "David Taylor", "45", "M         ", "david.taylor@example.com"),
                                List.of("19", "Ella Thomas", "24", "F         ", "ella.thomas@example.com"),
                                List.of("5", "Emily Johnson", "40", "F         ", "emily.johnson@example.com"),
                                List.of("17", "Emma Scott", "30", "F         ", "emma.scott@example.com"),
                                List.of("21", "Grace Miller", "34", "F         ", "grace.miller@example.com"))),
                22L,
                null,
                null);

        // when
        QueryResponse queryResponse = queryService.executeQueryLanguageSelectQuery(databaseId, query, page, pageSize);

        // then
        assertEquals(pageSize, queryResponse.getData().getRows().size()); // page size
        assertEquals(22, queryResponse.getTotalCount());
        assertEquals(expectedResponse, queryResponse);
    }
}

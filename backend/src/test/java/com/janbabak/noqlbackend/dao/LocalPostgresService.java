package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.service.DockerService;
import com.janbabak.noqlbackend.service.DockerService.RunContainerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Service for managing a local Postgres container for testing purposes.
 */
@Service
@SpringBootTest
@ActiveProfiles("test")
public class LocalPostgresService {

    public final String POSTGRES_USER = "test-user";
    public final String POSTGRES_DB = "test-database";
    public final String POSTGRES_PASSWORD = "test-password";
    public final Integer POSTGRES_PORT = 5433;

    @SuppressWarnings("FieldCanBeLocal")
    private final String POSTGRES_CONTAINER_NAME = "local-testing-postgres";

    @SuppressWarnings("FieldCanBeLocal")
    private final String POSTGRES_IMAGE_NAME = "postgres:16-alpine";

    @Autowired
    private DockerService dockerService;

    private String containerId;

    /**
     * Starts a local Postgres container with the specified configuration
     */
    public void startPostgres() {
        RunContainerRequest request = DockerService.RunContainerRequest.builder()
                .imageName(POSTGRES_IMAGE_NAME)
                .containerName(POSTGRES_CONTAINER_NAME)
                .portMappings(List.of(new DockerService.PortMapping(POSTGRES_PORT, 5432)))
                .environmentVariables(List.of(
                        new DockerService.EnvironmentVariableMapping("POSTGRES_USER", POSTGRES_USER),
                        new DockerService.EnvironmentVariableMapping("POSTGRES_PASSWORD", POSTGRES_PASSWORD),
                        new DockerService.EnvironmentVariableMapping("POSTGRES_DB", POSTGRES_DB)))
                .build();

        containerId = dockerService.runContainer(request);
    }

    /**
     * Stops the local Postgres container
     */
    public void stopPostgres() {
        dockerService.stopContainer(containerId);
    }
}

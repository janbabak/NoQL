package com.janbabak.noqlbackend.dao;

import com.janbabak.noqlbackend.service.DockerService;
import com.janbabak.noqlbackend.service.DockerService.RunContainerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Service for managing a local Postgres container for testing purposes.
 */
@Service
@SpringBootTest
@ActiveProfiles("test")
public class LocalPostgresService {

    public static final String POSTGRES_USER = "test-user";
    public static final String POSTGRES_DB = "test-database";
    public static final String POSTGRES_PASSWORD = "test-password";
    public static final Integer POSTGRES_PORT = 5433;
    public static final String POSTGRES_HOST = "localhost";

    @SuppressWarnings("FieldCanBeLocal")
    private final String POSTGRES_CONTAINER_NAME = "local-testing-postgres";

    @SuppressWarnings("FieldCanBeLocal")
    private final String POSTGRES_IMAGE_NAME = "postgres:16-alpine";

    @Autowired
    private DockerService dockerService;

    private String containerId;

    /**
     * Starts a local Postgres container with the specified configuration. Wait 5s for the container to start.
     */
    public void startPostgres() throws InterruptedException {
        startPostgres(5000);
    }

    /**
     * Starts a local Postgres container with the specified configuration
     *
     * @param waitMillis time to wait for the container to start
     */
    public void startPostgres(Integer waitMillis) throws InterruptedException {
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
        sleep(waitMillis); // wait for the container to start
    }

    /**
     * Stops the local Postgres container
     */
    public void stopPostgres() {
        dockerService.stopContainer(containerId);
    }
}

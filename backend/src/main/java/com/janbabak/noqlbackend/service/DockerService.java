package com.janbabak.noqlbackend.service;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@Slf4j
@Service
public class DockerService {

    @SuppressWarnings("FieldCanBeLocal")
    private final long SECONDS_TIMEOUT = 60 * 5;


    /**
     * Represents a request to run a container
     *
     * @param imageName            name of the image including the version
     * @param containerName        name of the container to be created.
     *                             If container with such name already exists, it will be removed.
     * @param portMappings         mappings between the host and the container ports
     * @param environmentVariables environment variables to be passed to the container
     */
    @Builder
    public record RunContainerRequest(
            @NotNull String imageName,
            String containerName,
            List<PortMapping> portMappings,
            List<EnvironmentVariableMapping> environmentVariables
    ) {
    }

    /**
     * Represents a port mapping between the host and the container
     */
    public record PortMapping(int hostPort, int containerPort) {
    }

    /**
     * Represents an environment variable mapping
     */
    public record EnvironmentVariableMapping(String key, String value) {
    }


    /**
     * Runs a container with the specified configuration in detached mode.
     * If container with the same name already exists, it will be removed.
     *
     * @param request configuration
     * @return id of the created container
     */
    public String runContainer(RunContainerRequest request) {
        removeContainer(request.containerName);
        pullImage(request.imageName);

        StringBuilder commandBuilder = new StringBuilder("docker run -d");
        if (request.containerName != null) {
            commandBuilder.append(" --name ").append(request.containerName);
        }
        if (request.portMappings != null) {
            for (PortMapping portMapping : request.portMappings) {
                commandBuilder
                        .append(" -p ").append(portMapping.hostPort).append(":").append(portMapping.containerPort);
            }
        }
        if (request.environmentVariables != null) {
            for (EnvironmentVariableMapping envMapping : request.environmentVariables) {
                commandBuilder.append(" -e ").append(envMapping.key).append("=").append(envMapping.value);
            }
        }
        commandBuilder.append(" ").append(request.imageName);

        String containerId = executeCommand(commandBuilder.toString()).trim();
        log.info("Container ID: {}", containerId);
        return containerId;
    }

    /**
     * Stops a container
     *
     * @param container name or id of the container to be stopped
     */
    public void stopContainer(String container) {
        String command = "docker stop " + container;
        executeCommand(command);
    }

    /**
     * Pulls an image from the Docker Hub
     *
     * @param imageName name of the image to be pulled
     */
    public void pullImage(String imageName) {
        String command = "docker pull " + imageName;
        executeCommand(command);
    }

    /**
     * Removes a container
     *
     * @param containerName name of the container to be removed
     */
    public void removeContainer(String containerName) {
        String command = "docker rm -f " + containerName;
        executeCommand(command);
    }

    /**
     * Executes a command in the shell
     *
     * @param command command to be executed
     * @return output of the command
     */
    private String executeCommand(String command) {
        Process process;
        StringBuilder output = new StringBuilder();
        try {
            process = new ProcessBuilder("sh", "-c", command).start();
            process.waitFor(SECONDS_TIMEOUT, TimeUnit.SECONDS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Command execution failed: '{}'", command);
            throw new RuntimeException(e);
        }
        if (process.exitValue() != 0) {
            log.error("Command failed: '{}'", command);
            throw new RuntimeException("Command failed with exit code: " + process.exitValue());
        }
        log.error("Command executed: '{}'", command);
        return output.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        DockerService dockerService = new DockerService();
        dockerService.runContainer(RunContainerRequest.builder()
                .imageName("mysql")
                .containerName("muj-mysql")
                .portMappings(List.of(new PortMapping(27017, 27017)))
                .environmentVariables(List.of(
                        new EnvironmentVariableMapping("MYSQL_ROOT_PASSWORD", "root"),
                        new EnvironmentVariableMapping("MYSQL_ALLOW_EMPTY_PASSWORD", "yes"),
                        new EnvironmentVariableMapping("MYSQL_ROOT_PASSWORD", "secret")))
                .build());
        sleep(10);
        dockerService.stopContainer("muj-mysql");
    }
}


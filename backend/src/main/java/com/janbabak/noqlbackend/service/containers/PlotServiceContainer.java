package com.janbabak.noqlbackend.service.containers;

import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.service.containers.DockerService.RunContainerRequest;
import com.janbabak.noqlbackend.service.containers.DockerService.VolumeMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlotServiceContainer {

    private static final String IMAGE_NAME = "janbabak/plot-service:1.0.0";
    private static final int WAIT_TO_START_MILLIS = 5000; // waits 5s for the container to start

    private final DockerService dockerService;
    private final Settings settings;

    @SuppressWarnings("all")
    private String containerId;

    /**
     * Starts the plot service container
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @SuppressWarnings("unused")
    public void start() throws InterruptedException {
        start(WAIT_TO_START_MILLIS);
    }

    /**
     * Starts the plot service container and waits for the specified time.
     *
     * @param waitMillis time to wait for the container to start
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void start(int waitMillis) throws InterruptedException {
        log.info("Starting Plot Service Container;");

        RunContainerRequest request = RunContainerRequest.builder()
                .imageName(IMAGE_NAME)
                .containerName(settings.plotServiceContainerName)
                .volumeMappings(List.of(new VolumeMapping("`pwd`/plotService", "/plotService")))
                .detachedMode(true)
                .interactiveMode(true)
                .build();

        containerId = dockerService.runContainer(request);
        sleep(waitMillis);
    }
}
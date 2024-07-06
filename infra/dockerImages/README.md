# Docker images

## Plot service

- **Docker container used by the `PlotService.java` in the backend to generate plots in an isolated environment.**


- Build image for ARM and x86:
  ```bash
  docker build -t janbabak/plot-service-arm64:1.0.0 -f ./infra/dockerImages/plotService.Dockerfile --platform linux/arm64 .
  docker build -t janbabak/plot-service-amd64:1.0.0 -f ./infra/dockerImages/plotService.Dockerfile --platform linux/amd64 .
  ```

- Push images:
  ```bash
  docker push janbabak/plot-service-arm64:1.0.0
  docker push janbabak/plot-service-amd64:1.0.0
  ```

- Create manifest:
  ```bash
  docker manifest create janbabak/plot-service:1.0.0  \
    janbabak/plot-service-arm64:1.0.0 \
    janbabak/plot-service-amd64:1.0.0
  ```

- Inspect manifest:
  ```bash
  docker manifest inspect janbabak/plot-service:1.0.0
  ```

- Push manifest:
  ```bash
  docker manifest push janbabak/plot-service:1.0.0
  ```

- Run the container:
  ```bash
  docker run -d -it --name plot-service -v `pwd`/plotService:/plotService janbabak/plot-service:1.0.0
  ```
- Generate plot:
  ```bash
  docker exec plot-service python plotService/plot.py
  ```

## Backend

- Docker container that runs the backend service.
- Build the image:
  ```bash
  docker build -t janbabak/noql-backend:0.0.1 -f ./infra/dockerImages/backend.Dockerfile .
  ```

- Run the container:
  ```bash
    docker run -d --name noql-backend -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock janbabak/noql-backend:0.0.1
    ```
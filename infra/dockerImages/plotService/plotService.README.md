# Plot Service Docker Image

- **Docker container used by the `PlotService.java` in the backend to generate plots in an isolated environment.**

- Build image for ARM and x86:
  ```bash
  docker build \
    -t janbabak/plot-service-arm64:0.0.1
    -f ./infra/dockerImages/plotService/plotService.Dockerfile
    --platform linux/arm64 .
  
  docker build \
    -t janbabak/plot-service-amd64:0.0.1 \
    -f ./infra/dockerImages/plotService/plotService.Dockerfile \
    --platform linux/amd64 .
  ```

- Push images:
  ```bash
  docker push janbabak/plot-service-arm64:0.0.1
  docker push janbabak/plot-service-amd64:0.0.1
  ```

- Create manifest:
  ```bash
  docker manifest create janbabak/plot-service:0.0.1  \
    --amend janbabak/plot-service-arm64:0.0.1 \
    --amend janbabak/plot-service-amd64:0.0.1
  ```

- Inspect manifest:
  ```bash
  docker manifest inspect janbabak/plot-service:0.0.1
  ```

- Push manifest:
  ```bash
  docker manifest push janbabak/plot-service:0.0.1
  ```

- Run the container:
  ```bash
  docker run -d -it --name plot-service -v `pwd`/plotService:/plotService janbabak/plot-service:1.0.0
  ```
- Generate plot:
  ```bash
  docker exec plot-service python plotService/plot.py
  ```
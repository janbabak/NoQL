# Docker images

## Plot service

- Docker container used by the `PlotService.java` in the backend to generate plots in an isolated environment.
- Build the image:
  ```bash
  # change the version if needed
  docker build -t plot-service:1.0.0 -f ./infra/dockerImages/plotService.Dockerfile .
  docker tag plot-service:1.0.0 janbabak/plot-service:1.0.0
  docker push janbabak/plot-service:1.0.0
  ````
- Run the container:
  ```bash
  docker run -d -it --name plot-service -v `pwd`/plotService:/plotService janbabak/plot-service:1.0.0
  ```
- Generate plot:
  ```bash
  docker exec plot-service python plotService/plot.py
  ```
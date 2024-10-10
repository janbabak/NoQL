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

- Build image for ARM and x86:
  ```bash
  docker build -t janbabak/noql-backend-arm64:0.0.1 -f ./infra/dockerImages/backend.Dockerfile --platform linux/arm64 .
  docker build -t janbabak/noql-backend-amd64:0.0.1 -f ./infra/dockerImages/backend.Dockerfile --platform linux/amd64 .
  ```

- Push images:
  ```bash
  docker push janbabak/noql-backend-arm64:0.0.1
  docker push janbabak/noql-backend-amd64:0.0.1
  ```

- Create manifest:
  ```bash
  docker manifest create janbabak/noql-backend:0.0.1  \
    janbabak/noql-backend-arm64:0.0.1 \
    janbabak/noql-backend-amd64:0.0.1
  ```

- Inspect manifest:
  ```bash
  docker manifest inspect janbabak/noql-backend:0.0.1
  ```

- Push manifest:
  ```bash
  docker manifest push janbabak/noql-backend:0.0.1
  ```

- Run the container:
  ```bash
  docker run -d -it --name noql-backend -p 80:3000 janbabak/noql-backend:0.0.1
  ```

## Frontend
- Build image for ARM and x86:
  ```bash
  docker build -t janbabak/noql-frontend-arm64:0.0.1 -f ./infra/dockerImages/frontend.Dockerfile --platform linux/arm64 .
  docker build -t janbabak/noql-frontend-amd64:0.0.1 -f ./infra/dockerImages/frontend.Dockerfile --platform linux/amd64 .
  ```

- Push images:
  ```bash
  docker push janbabak/noql-frontend-arm64:0.0.1
  docker push janbabak/noql-frontend-amd64:0.0.1
  ```

- Create manifest:
  ```bash
  docker manifest create janbabak/noql-frontend:0.0.1  \
    janbabak/noql-frontend-arm64:0.0.1 \
    janbabak/noql-frontend-amd64:0.0.1
  ```

- Inspect manifest:
  ```bash
  docker manifest inspect janbabak/noql-frontend:0.0.1
  ```

- Push manifest:
  ```bash
  docker manifest push janbabak/noql-frontend:0.0.1
  ```

- Run the container:
  ```bash
  docker run -d -it --name noql-frontend -p 80:3000 janbabak/noql-frontend:0.0.1
  ```
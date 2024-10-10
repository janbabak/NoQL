# Frontend Docker Image
- Build image for ARM and x86:
  ```bash
  docker build -t janbabak/noql-frontend-arm64:0.0.1 -f ./infra/dockerImages/frontend/frontend.Dockerfile --platform linux/arm64 .
  docker build -t janbabak/noql-frontend-amd64:0.0.1 -f ./infra/dockerImages/frontend/frontend.Dockerfile --platform linux/amd64 .
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
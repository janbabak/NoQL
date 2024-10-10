# Backend Docker Image

- Build image for ARM and x86:
  ```bash
  docker build \
    -t janbabak/noql-backend-arm64:0.0.1
    -f ./infra/dockerImages/backend/backend.Dockerfile
    --platform linux/arm64 .
  
  docker build \
    -t janbabak/noql-backend-amd64:0.0.1 \
    -f ./infra/dockerImages/backend/backend.Dockerfile \
    --platform linux/amd64 .
  ```

- Push images:
  ```bash
  docker push janbabak/noql-backend-arm64:0.0.1
  docker push janbabak/noql-backend-amd64:0.0.1
  ```

- Create manifest:
  ```bash
  docker manifest create janbabak/noql-backend:0.0.1  \
    --amend janbabak/noql-backend-arm64:0.0.1 \
    --amend janbabak/noql-backend-amd64:0.0.1
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
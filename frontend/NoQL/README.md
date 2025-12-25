# NoQL Frontend

## Frontend docker image

- **Build** frontend docker image
    - Version is taken from `package.json` `version` attribute
    ```bash
    npm run dockerBuildFrontend
    ```

- **Build** and **Push** frontend docker image
    - Version is taken from `package.json` `version` attribute
    ```bash
    npm run dockerPushFrontend
    ```

- **Run** frontend docker container
    - Portforward port desired port to `80` in container
    - Define necessary environment variables
    ```bash
    docker run -p 80:80 \                                                                                       ✔  25s   10:13:02  
    --env-file ./frontend/NoQL/.env \
    --name noql-frontend \
    janbabak/noql-frontend:0.0.1
    ```
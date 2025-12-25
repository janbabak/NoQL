# NoQL backend

## Configuration

- App can be configured by environment variables in the `backend/.env.local` file.

### Environment variables

**Pagination**

- `PAGINATION_MAX_PAGE_SIZE` - Maximum page size of automatically paginated query result.
- `PAGINATION_DEFAULT_PAGE_SIZE` - Default page size of automatically paginated query result.

**Database connection**

- `NOQL_DB_NAME` - (required) Database name
- `NOQL_DB_HOST` - (required) Database host
- `NOQL_DB_PORT` - (required) Database port
- `NOQL_DB_USERNAME` - (required) Database username
- `NOQL_DB_PASSWORD` - (required) Database password.

**Local Database (for testing)**

- should be the same as the database above (or there has to be another db running)
- `POSTGRES_PASSWORD` - (required) Database password
- `POSTGRES_USER` - (required) Database username
- `POSTGRES_DB` - (required) Database name

**Other**

- `PLOT_SERVICE_CONTAINER_NAME` - (required) Name of the container running the plot service
- `DEFAULT_USER_QUERY_LIMIT` - Default number of queries that can be executed by newly registered user.

**Security**

- `JWT_SECRET` - (required) Secret key for JWT token generation (at least 512 bits)
- `JWT_EXPIRATION` - Expiration time of JWT token in seconds. (Default is 1 day)
- `JWT_REFRESH_EXPIRATION` - Expiration time of JWT refresh token in seconds. (Default is 7 days)
- `DATA_ENCRYPTION_KEY` - (required) Key for data encryption (256 bits encoded in base64)

**Api keys to external services**

- `GEMINI_API_KEY` - (required) Google Gemini model API key
- `OPEN_AI_API_KEY` - (required) OpenAI API key - for GPT models
- `CLAUDE_API_KEY` - (required) for Claude models

## Gradle Tasks

- `showLogs` - Show logs of tests in the console. Default is `false`.
     ```bash
    ./gradlew test -PshowLogs
    ```

## Backend docker image

- **Build** backend docker image
    - Version is taken from `build.gradle` `version` attribute
    ```bash
    ./gradlew dockerBuildBackend -Ppush=false df
    ```

- **Build** and **Push** backend docker image
    - Version is taken from `build.gradle` `version` attribute
    ```bash
    ./gradlew dockerBuildBackend
    ```

- **Run** backend docker container
    - Portforward port desired port to `8080` in container
    - Map plot service directory to `/app/plotService` in container to store plot images
    - Map docker socket to `/var/run/docker.sock` in container
    - Define necessary environment variables
    ```bash
    docker run -d \
      --name noql-backend-test \            
      -p 8080:8080 \              
      -v "./plotService:/app/plotService" \
      -v /var/run/docker.sock:/var/run/docker.sock \
      --env-file ./backend/.env.local \
      backend-test:0.0.1
    ```

## Plot service docker image

- **Build** plot service docker image
    - Version is taken from `build.gradle` `ext.docker.plotServiceVersion` attribute
    ```bash
    ./gradlew dockerBuildPlotService -Ppush=false
    ```

- **Build** and **Push** plot service docker image
    - Version is taken from `build.gradle` `ext.docker.plotServiceVersion` attribute
    ```bash
    ./gradlew dockerBuildPlotService
    ```

- **Run** plot service docker container
    - Map plot service directory to `/app/plotService` in container to generate plot images
    ```bash
    docker run -d -it \
     --name plot-service \
     -v `pwd`/../plotService:/app/plotService \
     janbabak/noql-plot-service:0.0.1
    ```

- **Generate plot** on running image
    ```bash
     docker exec plot-service python ./plotService/plot.py
    ```
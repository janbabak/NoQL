# NoQL Backend

## Pipeline (CI/CD)

- There are two pipelines - CI pipeline and Deployment pipeline

### [CI pipeline](https://github.com/janbabak/NoQL/actions/workflows/backend.yaml)

- Triggered by pull request to main branch action happens or manually started.
- Jobs:
    - **Validate:**
        - Detect changes in the `/backend` directory.
        - If changes have been detected, verifies backend version has been increased and lint backend code.
    - **Test:**
        - Started if backend changes have been detected, PR has been merged, or on manual dispatch
        - Executes unit/integration tests with coverage.
        - Coverage can be found in the job summary output
        - When triggered by PR merge, update `backend_coverage.json` file in `coverage-badge` branch which is used by
          the coverage batch in the [../README.md](../README.md)
    - **Build:**
        - Builds backend jar
        - If triggered manually with input `push_docker=true`, build and push docker image `janbabak/noql-backend` to
          [hub.docker.com/r/janbabak/noql-backend](https://hub.docker.com/r/janbabak/noql-backend). Image tag
          matches backend version.

## Configuration

The backend application is configured using environment variables.
For local development, variables can be defined in `backend/.env.local`.

## Environment Variables

### Pagination

| Variable                       | Description                                                |
|--------------------------------|------------------------------------------------------------|
| `PAGINATION_MAX_PAGE_SIZE`     | Maximum page size of automatically paginated query results |
| `PAGINATION_DEFAULT_PAGE_SIZE` | Default page size of automatically paginated query results |

### Database Connection

All database-related variables are **required**.

| Variable           | Description       |
|--------------------|-------------------|
| `NOQL_DB_NAME`     | Database name     |
| `NOQL_DB_HOST`     | Database host     |
| `NOQL_DB_PORT`     | Database port     |
| `NOQL_DB_USERNAME` | Database username |
| `NOQL_DB_PASSWORD` | Database password |

### Local Database (Testing Only)

Used for running the application locally with a test database.
The configuration should match the primary database settings unless a separate database is used.

| Variable            | Description       |
|---------------------|-------------------|
| `POSTGRES_DB`       | Database name     |
| `POSTGRES_USER`     | Database username |
| `POSTGRES_PASSWORD` | Database password |

### Other Configuration

| Variable                      | Description                                                   |
|-------------------------------|---------------------------------------------------------------|
| `PLOT_SERVICE_CONTAINER_NAME` | Name of the container running the plot service                |
| `DEFAULT_USER_QUERY_LIMIT`    | Default number of queries allowed for a newly registered user |

### Security

All security-related variables are **required** unless stated otherwise.

| Variable                 | Description                                                    |
|--------------------------|----------------------------------------------------------------|
| `JWT_SECRET`             | Secret key for JWT token generation (minimum 512 bits)         |
| `JWT_EXPIRATION`         | JWT access token expiration time in seconds (default: 1 day)   |
| `JWT_REFRESH_EXPIRATION` | JWT refresh token expiration time in seconds (default: 7 days) |
| `DATA_ENCRYPTION_KEY`    | Data encryption key (256 bits, Base64-encoded)                 |

### API Keys (External Services)

All API keys listed below are **required**.

| Variable          | Description                 |
|-------------------|-----------------------------|
| `GEMINI_API_KEY`  | Google Gemini API key       |
| `OPEN_AI_API_KEY` | OpenAI API key (GPT models) |
| `CLAUDE_API_KEY`  | Anthropic Claude API key    |

## Gradle Tasks

## Lint

Lint main source code. Rules can be modified at [pmd-main.xml](./config/pmd/pmd-main.xml)

```bash
./gradlew pmdMain 
```

Lint test source code. Rules can be modified at [pmd-test.xml](./config/pmd/pmd-test.xml)

```bash
./gradlew pmdTest 
```

## Test Coverage

Generate JaCoco report

```bash
./gradlew jacocoTestReport 
```

Get total coverage

```bash
./gradlew computeTotalTestCoverage
```

### Show Test Logs

By default, test logs are hidden. To enable log output:

```bash
./gradlew test -PshowLogs
```

## Backend Docker Image

### Build Backend Image (Local)

Builds the backend Docker image locally without pushing it.
The image version is taken from the `version` field in `build.gradle`.

```bash
./gradlew dockerBuildBackend -Ppush=false
```

---

### Build and Push Backend Image

Builds and pushes the backend Docker image to the registry.
The image version is taken from the `version` field in `build.gradle`.

```bash
./gradlew dockerBuildBackend
```

---

### Run Backend Container

- Expose container port `8080`
- Mount plot service output directory
- Mount Docker socket for container orchestration
- Provide required environment variables

```bash
docker run -d \
  --name noql-backend-test \
  -p 8080:8080 \
  -v "./plotService:/app/plotService" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --env-file ./backend/.env.local \
  backend-test:0.0.1
```

---

## Plot Service Docker Image

### Build Plot Service Image (Local)

The image version is taken from `ext.docker.plotServiceVersion` in `build.gradle`.

```bash
./gradlew dockerBuildPlotService -Ppush=false
```

---

### Build and Push Plot Service Image

```bash
./gradlew dockerBuildPlotService
```

---

### Run Plot Service Container

Mount the plot service directory to store generated plot images.

```bash
docker run -d -it \
  --name plot-service \
  -v "$(pwd)/../plotService:/app/plotService" \
  janbabak/noql-plot-service:0.0.1
```

---

### Generate Plot on Running Container

```bash
docker exec plot-service python ./plotService/plot.py
```
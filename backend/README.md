# NoQL Backend

## Pipeline (CI/CD)

The project consists of:
- **CI pipelines** for individual components (backend, frontend, and plot service),
- A **Deployment pipeline** that deploys all components together,
- A **Tear-down pipeline** that removes the AWS infrastructure.

---

## CI Pipelines

CI pipelines are triggered automatically on **pull requests to the `main` branch** or can be
**started manually**.

The backend, frontend, and plot service each have their **own dedicated CI pipeline**.

---

### [Backend CI Pipeline](../.github/workflows/backend.yaml)

#### Jobs

- **Validate**
    - Detects changes in the [`/backend`](./) directory.
    - If changes are found:
        - Verifies that the backend version has been incremented.
        - Runs linting on the backend code.

- **Test**
    - Runs when:
        - Backend changes were detected in the *Validate* job, or
        - A pull request has been merged, or
        - The workflow is manually triggered.
    - Executes **unit and integration tests** with coverage reporting.
    - Coverage results are available in the **job summary**.
    - When triggered by a **PR merge**, updates `backend_coverage.json` in the `coverage-badge` branch.  
      This file is used by the **coverage badge** displayed in the [main README](../README.md).

- **Build**
    - Builds the backend JAR file.
    - If the workflow is manually triggered with `push_docker=true`:
        - Builds and pushes the Docker image `janbabak/noql-backend`
          to [Docker Hub](https://hub.docker.com/r/janbabak/noql-backend).
        - The Docker image tag matches the backend version.

---

### [Plot Service CI Pipeline](../.github/workflows/plotservice.yaml)

#### Jobs

- **Validate**
    - Detects changes in [`plotService.Dockerfile`](../backend/docker/plotService.Dockerfile).
    - If changes are found:
        - Verifies that the plot service version has been incremented.

- **Build**
    - Runs when the workflow is manually triggered with `push_docker=true`.
    - Builds and pushes the Docker image `janbabak/noql-plot-service`
      to [Docker Hub](https://hub.docker.com/r/janbabak/noql-plot-service).
    - The Docker image tag matches the plot service version.

---

## [Deployment Pipeline](../.github/workflows/stack-deploy.yaml)

The deployment pipeline deploys **all system components** (backend, frontend, and plot service)
to the AWS infrastructure.

The pipeline can be triggered **only manually**.

#### Jobs

- **Deploy Infrastructure**
    - Authenticates with AWS.
    - Deploys infrastructure defined in the CloudFormation template
      [`infra.yaml`](../infra/prod-stack/infra.yaml).

- **Deploy Docker Compose**
    - Sets up environment variables from:
        - [`.env.backend-prod`](../infra/local-stack/.env.backend-prod)
        - [`.env.frontend-prod`](../infra/local-stack/.env.frontend-prod)  
          (More details on environment variable configuration can be found [here](#configuration))
    - Starts Docker Compose, which pulls Docker images from
      [Docker Hub](https://hub.docker.com/u/janbabak).
    - The frontend IP address is available in the output of the **Print stack URL** step.

---

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
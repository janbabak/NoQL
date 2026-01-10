# NoQL Backend

![Backend Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/janbabak/NoQL/coverage-badge/backend_coverage.json)

[![Java](https://img.shields.io/badge/Java-e4292d.svg?style=flat&logo=java&logoColor=white&color=f1931c)](https://www.java.com/en/)
[![Spring](https://img.shields.io/badge/Spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io)
[![JUnit](https://img.shields.io/badge/JUnit5-25A162.svg?style=flat&logo=JUnit5&logoColor=white)](https://junit.org/junit5/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white)](https://www.docker.com)
![ChatGPT](https://img.shields.io/badge/ChatGPT-000000.svg?style=flat&logo=OpenAI&logoColor=white)
![Anthropic](https://img.shields.io/badge/Anthropic-e3dacc.svg?style=flat)
![AWS](https://img.shields.io/badge/AWS-f89500.svg?style=flat&logo=amazonaws&logoColor=white)

---

## 🗂️ Table of Contents

- [Description](#-description)
- [Project Structure](#-project-structure)
- [Software Requirements](#-software-requirements)
- [How to Run](#-how-to-run)
- [Gradle Tasks](#gradle-tasks)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [Backend Docker Image](#backend-docker-image)
- [Plot Service Docker Image](#plot-service-docker-image)

---

## 📝 Description

The **NoQL Backend** is a stateful (“thick”) server implemented in **Java** using the **Spring Framework**.

Its responsibilities include:
- Processing user queries
- Communicating with LLM providers (OpenAI, Claude, ...)
- Orchestrating the **Plot Service**, which executes dynamically generated Python scripts inside an isolated Docker environment

---

## 🌲 Project Structure

```text
backend/                                  # Backend root
├─ config/
│  └─ pmd/                                # PMD lint configuration
├─ docker/                                # Docker-related resources
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/janbabak/noqlbackend/
│  │  │     ├─ authentication/            # Authentication & authorization
│  │  │     ├─ config/                    # Spring and app configuration
│  │  │     ├─ controller/                # REST controllers
│  │  │     ├─ dao/                       # DAOs and repositories
│  │  │     ├─ error/                     # Exception handling
│  │  │     ├─ model/                     # Domain models
│  │  │     ├─ service/                   # Business logic
│  │  │     └─ validation/                # Input validation
│  │  └─ resources/
│  │     ├─ static/                       # Static resources
│  │     ├─ application.yaml              # Spring configuration
│  │     └─ logback.xml                   # Logging configuration
│  └─ test/
│     ├─ java/
│     │  └─ com/janbabak/noqlbackend/      # Test sources
│     └─ resources/
│        ├─ dbScripts/                    # DB init & cleanup scripts
│        ├─ llmResponses/                 # Sample LLM plot responses
│        └─ application.yaml              # Test configuration
├─ swagger/                               # OpenAPI / Swagger documentation
├─ .env.local                             # Local environment variables
├─ lombok.config                          # Lombok configuration
└─ README.md                              # Documentation
```

---

## ✅ Software Requirements

The following software is required for local development, building, and deployment of the backend service:

### Backend

- **Java 17** (required)
- **Gradle** (or use the included Gradle Wrapper)
- **Docker**
- **Docker Compose**
- **Postgres** (could be run as a Docker container)

---

## 🏃 How to Run

You can run the backend either **natively** on your machine or **locally using Docker Compose**.

### Run Natively

**Build the application**

```bash
./gradlew clean build
```

**Build without running test**
```shell
./gradlew clean build -x test
```

**Start the application**
```shell
./gradlew bootRun
```

### Run locally with Docker Compose**

Instructions: [`../infra/local-stack/README.md`](../infra/local-stack/README.md)

---

## Gradle Tasks

### Lint

**Lint main source code**

Rules can be modified at [pmd-main.xml](./config/pmd/pmd-main.xml)

```bash
./gradlew pmdMain 
```

**Lint test source code**

Rules can be modified at [pmd-test.xml](./config/pmd/pmd-test.xml)

```bash
./gradlew pmdTest 
```

### Tests

**Execute unit integration tests**

```shell
./gradlew clean test
```

**Generate JaCoco test coverage report**

```shell
./gradlew jacocoTestReport 
```

**Get total coverage**

```shell
./gradlew computeTotalTestCoverage
```

**Show test logs**

By default, test logs are hidden. To enable log output:

```shell
./gradlew test -PshowLogs
```

---

## Configuration

**Spring Configuration**

- App is configured using [src/main/resources/application.yml](src/main/resources/application.yml) file.
- Tests use configurationt from [src/test/resources/application.yml](src/main/resources/application.yml) file
- In this file you can change settings, port, apiKeys, secrets ...

---

## Environment Variables

The backend application is configured using environment variables.
For local development, variables can be defined in `backend/.env.local`.

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

---

## Backend Docker Image

### Build Backend Image (Local)

Builds the backend Docker image locally without pushing it.
The image version is taken from the `version` field in `build.gradle`.

```bash
./gradlew dockerBuildBackend -Ppush=false
```

### Build and Push Backend Image

Builds and pushes the backend Docker image to the registry.
The image version is taken from the `version` field in `build.gradle`.

```bash
./gradlew dockerBuildBackend
```

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

### Build and Push Plot Service Image

```bash
./gradlew dockerBuildPlotService
```

### Run Plot Service Container

Mount the plot service directory to store generated plot images.

```shell
docker run -d -it \
  --name plot-service \
  -v "$(pwd)/../plotService:/app/plotService" \
  janbabak/noql-plot-service:0.0.1
```

### Generate Plot on Running Container

```shell
docker exec plot-service python ./plotService/plot.py
```
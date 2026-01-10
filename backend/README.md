# NoQL Backend

![Backend Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/janbabak/NoQL/coverage-badge/backend_coverage.json)

[![Java](https://img.shields.io/badge/Java-e4292d.svg?style=flat&logo=java&logoColor=white&color=f1931c)](https://www.java.com/en/)
[![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io)
[![Junit](https://img.shields.io/badge/JUnit5-25A162.svg?style=flat&logo=JUnit5&logoColor=white)](https://junit.org/junit5/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org)
[![Postgres](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white)](https://www.docker.com)
![Chat GPT](https://img.shields.io/badge/ChatGPT-000000.svg?style=flat&logo=ChatBot&logoColor=white)
![Anthropic](https://img.shields.io/badge/Anthropic-e3dacc.svg?style=flat&logo=ChatBot&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-f89500.svg?style=flat&logo=ownCloud&logoColor=white)

## 🗂️ Table of Contents

- [Description](#-description)
- [Project Structure](#-project-structure)
- [Software Requirements](#-software-requirements)
- [How to Run](#-how-to-run)
- [Gradle Tasks](#gradle-tasks)
- [Configuration](#configuration)
- [Environment Variables](#environment-variables)
- [Backend docker image](#backend-docker-image)
- [Plot Service docker image](#plot-service-docker-image)

---

## 📝 Description

NoQL backend is a thick server written in Java with the Spring framework. It uses Plot service - service that executes
generated python scripts in separated docker environment.

---

## 🌲 Project Structure

```text
backend/                                  ... backend root
├─ config
|  ├─ pmd                                 ... PMD lint config files
├─ docker                                 ... Docker containers used in backend
├─ src/                                   ... source code
│  ├─ main/                               ... main code
│  │  ├─ java.
|  |  |  ├─ com.janbabak.noqlbackend/   
│  │  │  |  ├─ authentication/            ... authentication
│  │  │  |  ├─ config/                    ... various configurations
│  │  │  |  ├─ controller/                ... REST controllers
│  │  │  |  ├─ dao/                       ... data access objects, repositories
│  │  │  |  ├─ error/                     ... errors and exceptions handling
│  │  │  |  ├─ model/                     ... data models
│  │  │  |  ├─ service/                   ... services
│  │  │  |  ├─ validation/                ... input validations
|  |  |  ├─ resources
|  |  |  |  ├─ static                     ... serves static resources
|  |  |  |  | application.yaml            ... spring configuration 
|  |  |  |  | logback.xml                 ... logger config
│  ├─ test/                               ... unit/integration tests
│  │  ├─ java.
|  |  |  ├─ com.janbabak.noqlbackend/     ... tests source code
|  |  |  ├─ resources
|  |  |  |  ├─ dbScripts                  ... SQL scripts to initiate and clean up test databases
|  |  |  |  ├─ llmResponses               ... Sample LLM responses - plots
|  |  |  |  | application.yaml            ... spring test configuration 
├─ swagger/                               ... API documentation
| .env.local                              ... Environment variables used for local development
| lombok.config                           ... Lombok configuration
| README.md                               ... documentation
```

## ✅ Software Requirements

The following software must be installed for development, building, and deployment.

- **Backend**
    - Java 17
    - Gradle (or use the Gradle wrapper)
    - Docker
    - Docker Compose

## 🏃 How to Run

### Run natively

**Build**

```shell
./gradlew clean build
```

**Build without test**

```shell
./gradlew clean build -x test
```

**Run**

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
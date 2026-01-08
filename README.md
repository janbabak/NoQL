# NoQL - No Query Language

![Backend Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/janbabak/NoQL/coverage-badge/backend_coverage.json)


[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=java&logoColor=white&color=f1931c)](https://www.java.com/en/)
[![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io)
[![Junit](https://img.shields.io/badge/JUnit5-25A162.svg?style=flat&logo=JUnit5&logoColor=white)](https://junit.org/junit5/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org)
[![Postgres](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6.svg?style=flat&logo=TypeScript&logoColor=white)](https://www.typescriptlang.org)
[![React](https://img.shields.io/badge/React-61DAFB.svg?style=flat&logo=React&logoColor=black)](https://react.dev)
[![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white)](https://www.docker.com)
![Chatbot](https://img.shields.io/badge/ChatBot-0066FF.svg?style=flat&logo=ChatBot&logoColor=white)
[![Gemini](https://img.shields.io/badge/Google%20Gemini-8E75B2.svg?style=flat&logo=Google-Gemini&logoColor=white)](https://deepmind.google/technologies/gemini/)

## ✏️ Description

NoSQL is a tool that lets users query databases using natural language. It's designed for those who aren't developers
and don't know query language syntax such as SQL.

## 🌲Project structure

```text
.github/                                  ... github actions (pipelines)
├─ workflows/
backend/                                  ... backend app root
|  README.md                            .  .. backend documentation
├─ src/                                   ... source code
│  ├─ main/                               ... main code
│  │  ├─ java.com.janbabak.noqlbackend/   
│  │  │  ├─ authentication/               ... authentication
│  │  │  ├─ config/                       ... spring configurations
│  │  │  ├─ controller/                   ... REST controllers
│  │  │  ├─ dao/                          ... data access objects
│  │  │  ├─ error/                        ... error handling and exceptions
│  │  │  ├─ model/                        ... data models
│  │  │  ├─ service/                      ... services
│  │  │  ├─ validation/                   ... validation
│  ├─ test/                               ... unit/integration tests
│  swagger/                               ... swagger API documentation
customModel/                              ... custom LLM api (separate project - gpt proxy)
exampleDatabase/                          ... old example database
frontend/                                 ... frontend app root
├─ NoQL/                 
|  | README.md                            ... frontend documentation
│  ├─ public/                             ... public files
│  ├─ src/                                ... source code
│  │  ├─ assets/
│  │  ├─ components/                      ... reusable components
│  │  ├─ pages/                           ... pages
│  │  ├─ routes/                          ... routing
│  │  ├─ services/                        ... services (api, logging,...)
│  │  ├─ state/                           ... state management (Redux Toolkit)
│  │  ├─ types/                           ... types
infra/                                    ... infrastructure
├─ dev-stack/                             ... development stack (for backend)
├─ local-stack/                           ... local stack (for frontend, runs backend in docker)
├─ prod-stack/                            ... production stack
├─ scripts/                               ... scripts for building containers
llmBenchmarks/                            ... benchmarks of LLM APIs
.dockerignore
.gitignore
README.md
```

## 🧑‍🔬 Technologies

- [Java 17](https://www.java.com/en/)
- [Spring](https://spring.io)
- [Gradle](https://gradle.org)
- [Docker](https://www.docker.com)
- [Docker compose](https://docs.docker.com/compose/)
- [Postgres](https://www.postgresql.org)
- [git](https://git-scm.com)
- [TypeScript](https://www.typescriptlang.org)
- [ReactJs](https://react.dev)
- [Material UI](https://mui.com)

## ✅ Software requirements

- Java 17
- Docker, Docker compose

## 🎬 How to run

### Clone repository

```bash
git clone https://github.com/janbabak/NoQL.git
cd NoQL/
```

### Backend

#### Local stack

- Local stack is used for frontend development. It uses a local database and a local backend running in docker containers (like docker-compose
  services).
- Details are in the [Local stack](infra/local-stack/README.md).

#### Development stack

- Development stack is used for backend development and frontend development. It uses a local database and other
  dependencies running in docker containers (like docker-compose services) and backend running locally on the host machine.
- It is necessary to create a `NoQL/backend/.env.local` file with the following content:
  ```dotenv
  # external services/apis
  OPEN_AI_API_KEY="" # input your api key
  GEMINI_API_KEY="" # input your api key
  CLAUDE_API_KEY="" # input your api key
  
  NOQL_DB_NAME="database"
  NOQL_DB_HOST="localhost"
  NOQL_DB_PORT="5432"
  NOQL_DB_USERNAME="user"
  NOQL_DB_PASSWORD="password"
  
  # Local databases - should match the NOQL_DB_xxx credentials
  POSTGRES_PASSWORD="password"
  POSTGRES_USER="user"
  POSTGRES_DB="database"
  
  # settings
  PAGINATION_MAX_PAGE_SIZE=100
  PAGINATION_DEFAULT_PAGE_SIZE=20
  
  PLOT_SERVICE_CONTAINER_NAME="plot-service-database-stack"
  DEFAULT_USER_QUERY_LIMIT=150
  
  # security
  JWT_SECRET="fkajlak4jt34ktj34t98vu44d5d4p54o5d45m34ik5n345m34wm5l431145l434u64bgjsuicvkaplcvqyevasswilmvbti09478jujhhdsbfasdhfbu4" # could be changed
  # 30 min
  JWT_EXPIRATION=1800
  # 7 days
  JWT_REFRESH_EXPIRATION=604800
  DATA_ENCRYPTION_KEY="djfasgu98g438yth43iuhg34jnfmf343nfij34fij43fm34fnij34fi34f"
  ```

- Start [Dev stack](infra/local-stack/README.md).
- Export environment variables.
  ```text
  set -o allexport
  source backend/.env.local
  set +o allexport
  ```
- Run the backend
  ```bash
  ./backend/gradlew -p backend bootRun
  ```
  
### Frontend
- Install the frontend dependencies
  ```bash
  cd frontend/NoQL 
  npm install
  ```
- Start the frontend
  ```bash
  npm run dev
  ```

## Pipelines (CI/CD)

The project uses a multi-pipeline CI/CD setup consisting of:

- **Component-specific CI pipelines** for the backend, frontend, and plot service
- A **deployment pipeline** that provisions infrastructure and deploys all components
- A **tear-down pipeline** that removes the AWS infrastructure

---

## CI Pipelines

CI pipelines are triggered automatically on **pull requests targeting the `main` branch** and can
also be **run manually**.

Each component (backend, frontend, and plot service) has its **own dedicated CI pipeline**, allowing
independent validation, testing, and builds.

<details>
<summary><b>Backend CI Pipeline</b></summary>

### [Backend CI Pipeline](.github/workflows/backend.yaml)

#### Jobs

- **Validate**
    - Detects changes in the [`backend`](./) directory.
    - If changes are detected:
        - Verifies that the backend version has been incremented.
        - Runs backend linting checks.

- **Test**
    - Runs when:
        - Backend changes were detected in the **Validate** job, or
        - A pull request has been merged, or
        - The workflow is manually triggered.
    - Executes **unit and integration tests** with coverage reporting.
    - Coverage results are published in the **job summary**.
    - When triggered by a **PR merge**, updates `backend_coverage.json` in the
      `coverage-badge` branch.  
      This file is used to generate the **coverage badge** displayed at the top of this file.

- **Build**
    - Builds the backend JAR artifact.
    - If the workflow is manually triggered with `push_docker=true`:
        - Builds and pushes the Docker image `janbabak/noql-backend`
          to [Docker Hub](https://hub.docker.com/r/janbabak/noql-backend).
        - The Docker image tag matches the backend version.

</details>

<details>
<summary><b>Plot Service CI Pipeline</b></summary>

### [Plot Service CI Pipeline](.github/workflows/plotservice.yaml)

#### Jobs

- **Validate**
    - Detects changes in [`plotService.Dockerfile`](backend/docker/plotService.Dockerfile).
    - If changes are detected:
        - Verifies that the plot service version has been incremented.

- **Build**
    - Runs only when the workflow is manually triggered with `push_docker=true`.
    - Builds and pushes the Docker image `janbabak/noql-plot-service`
      to [Docker Hub](https://hub.docker.com/r/janbabak/noql-plot-service).
    - The Docker image tag matches the plot service version.

</details>

<details>
<summary><b>Frontend CI Pipeline</b></summary>

### [Frontend CI Pipeline](.github/workflows/frontend.yaml)

#### Jobs

- **Validate**
    - Detects changes in the [`frontend`](frontend) directory.
    - If changes are detected:
        - Verifies that the frontend version has been incremented.

- **Build**
    - Builds and packages the frontend application.
    - If the workflow is manually triggered with `push_docker=true`:
        - Builds and pushes the Docker image `janbabak/noql-frontend`
          to [Docker Hub](https://hub.docker.com/r/janbabak/noql-frontend).
        - The Docker image tag matches the frontend version.

</details>

---

## Deployment Pipeline

### [Deployment Pipeline](.github/workflows/stack-deploy.yaml)

The deployment pipeline deploys **all system components** (backend, frontend, and plot service)
to AWS.

- The pipeline can be triggered **only manually**.
- It accepts a single input parameter: **stack ID**.

#### Jobs

- **Deploy Infrastructure**
    - Authenticates with AWS.
    - Deploys infrastructure defined in the CloudFormation template:
        - [`infra.yaml`](infra/prod-stack/infra.yaml)

- **Deploy Application (Docker Compose)**
    - Loads environment variables from:
        - [`.env.backend-prod`](infra/local-stack/.env.backend-prod)
        -  [`.env.frontend-prod`](infra/local-stack/.env.frontend-prod)
    - Starts Docker Compose, pulling Docker images from Docker Hub.
    - The frontend URL is printed in the output of the **Print stack URL** step.

---

## Tear-down Pipeline

### [Tear-down Pipeline](.github/workflows/stack-tear-down.yaml)

The tear-down pipeline removes the AWS infrastructure.

- The pipeline can be triggered **only manually**.
- It accepts a single input parameter: **stack ID**.

#### Jobs

- **Tear Down Infrastructure**
    - Authenticates with AWS.
    - Deletes infrastructure defined in the CloudFormation template:
        - [`infra.yaml`](infra/prod-stack/infra.yaml)
  
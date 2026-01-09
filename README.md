# NoQL - No Query Language

![Backend Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/janbabak/NoQL/coverage-badge/backend_coverage.json)

[![Java](https://img.shields.io/badge/Java-e4292d.svg?style=flat&logo=java&logoColor=white&color=f1931c)](https://www.java.com/en/)
[![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io)
[![Junit](https://img.shields.io/badge/JUnit5-25A162.svg?style=flat&logo=JUnit5&logoColor=white)](https://junit.org/junit5/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org)
[![Postgres](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6.svg?style=flat&logo=TypeScript&logoColor=white)](https://www.typescriptlang.org)
[![React](https://img.shields.io/badge/React-61DAFB.svg?style=flat&logo=React&logoColor=black)](https://react.dev)
[![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white)](https://www.docker.com)
![Chat GPT](https://img.shields.io/badge/ChatGPT-000000.svg?style=flat&logo=ChatBot&logoColor=white)
![Anthropic](https://img.shields.io/badge/Anthropic-e3dacc.svg?style=flat&logo=ChatBot&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-f89500.svg?style=flat&logo=ownCloud&logoColor=white)

## ✏️ Description

NoQL (No Query Language) is an **AI** tool for **data analysis**. It lets users connect to data sources like PostgreSQL,
**query them using natural language**, and visualize results with tables and charts.

The backend is built in **Java** with the **Spring framework** and uses **PostgreSQL** for data persistence. It
integrates various **LLMs**, including **OpenAI GPT**, and **Claude Haiku** fron Anthropic. The frontend is a
single-page app built with **TypeScript, React.js**, and **Material UI**, connected via a **REST API.**

## 🧑‍🔬 Tech Stack

### Backend

- [Java 17](https://www.java.com/en/)
- [Spring](https://spring.io)
- [Gradle](https://gradle.org)
- [PostgreSQL](https://www.postgresql.org)
- [MySQL](https://www.mysql.com)
- [Docker](https://www.docker.com)
- [Docker Compose](https://docs.docker.com/compose/)
- [Swagger](https://swagger.io)
- [Python](https://www.python.org)
- [Matplotlib](https://matplotlib.org)
- [GPT API](https://openai.com/api/)
- [Anthropic API](https://www.anthropic.com/learn/build-with-claude)

### Frontend

- [React](https://react.dev)
- [TypeScript](https://www.typescriptlang.org)
- [Vite](https://vite.dev)
- [Material UI](https://mui.com)
- [Nginx](https://nginx.org)

### Infrastructure & Others

- [AWS](https://aws.amazon.com/console/)
- [CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html)
- [Make](https://cs.wikipedia.org/wiki/Make)
- [Git](https://git-scm.com)
- [GitHub Actions](https://github.com/features/actions)
- [Bash](https://cs.wikipedia.org/wiki/Bash)

---

## ✅ Software Requirements

The following software must be installed for development, building, and deployment.

- **Backend**
    - Java 17
    - Gradle (or use the Gradle wrapper)
    - Docker
    - Docker Compose

- **Frontend**
    - Node.js 18+
    - Vite (installed via `npm install` as devDependency)
    - Docker
    - Docker Compose

- **Deployment**
    - AWS CLI
    - Make

---

## 🏗️ Architecture

The application is composed of the following main components:

- **Backend** – a thick server responsible for business logic, and API exposure
- **Frontend** – a thin client responsible for user interaction
- **Plot Service** – a dedicated service for plot generation
- **PostgreSQL** – persistence layer for application data
- **LLM(s)** – external AI agents that invoke backend functions

The overall system structure and request flow are illustrated in the diagrams below:

![NoQL architecture](images/architecture%20diagram.png)

Query request flow:

![Request flow](images/flow-diagram.png)

---

## 🌲Project structure

```text
.github/
├─ workflows/                             ... GitHub workflows (pipelines)
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
| README.md                               ... backend documentation
ci/
├─ scripts                                ... scripts used in pipelines
customModel/                              ... [Deprecated ]custom LLM api (separate project - gpt proxy)
exampleDatabase/                          ... [Deprecated] old example database
frontend/                                 ... frontend app root
├─ NoQL/                 
│  ├─ public/                             ... public files
│  ├─ src/                                ... source code
│  │  ├─ assets/                          ... static assets such as images
│  │  ├─ components/                      ... reusable components
│  │  ├─ pages/                           ... pages
│  │  ├─ routes/                          ... routing
│  │  ├─ services/                        ... services (api, logging,...)
│  │  ├─ state/                           ... state management (Redux Toolkit)
│  │  ├─ types/                           ... types
|  | .env                                 ... environment variables for local development
|  | frontend.Dockerfile                  ... front end docker image
|  | nginx.conf                           ... Nginx configuration
|  | README.md                            ... frontend documentation
infra/                                    ... infrastructure
├─ local-stack/                           ... docker compose stacks, files, ...
|  ├─ scripts                             ... scripts
|  ├─ stack-date                          ... persists data from docker containers
├─ prod-stack/                            ... AWS stack
|  ├─ infra.yaml                          ... AWS infrastructure cloud formation
|  ├─ Makefile                            ... deployment tasks
|  ├─ README.md                           ... deployment documentation
├─ scripts/                               ... infra related scripts
README.md                                 ... Documentation
```

---

## ▶️ How to Run

The application can be run in several ways, depending on your needs:

- **Deploy to AWS (production-like)**
    - Using GitHub Actions: see the **Deployment Pipeline** section
    - Manual deployment: [`infra/prod-stack/README.md`](infra/prod-stack/README.md)

- **Run locally with Docker Compose**
    - Instructions: [`infra/local-stack/README.md`](infra/local-stack/README.md)

- **Run components natively (development)**
    - Backend: [`backend/README.md`](backend/README.md)
    - Frontend: [`frontend/NoQL/README.md`](frontend/NoQL/README.md)

---

## 🔁 Pipelines (CI/CD)

The project uses a multi-pipeline CI/CD setup consisting of:

- **Component-specific CI pipelines** for the backend, frontend, and plot service
- A **deployment pipeline** that provisions infrastructure and deploys all components
- A **tear-down pipeline** that removes the AWS infrastructure

### 🔐 Environment Variables and Secrets

The following **secrets and variables** must be configured in **GitHub** for the CI/CD pipelines.

#### Secrets

| Name                    | Description                                                                                                                      |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| `AWS_ACCESS_KEY_ID`     | AWS access key ID used by deployment and tear-down pipelines                                                                     |
| `AWS_SECRET_ACCESS_KEY` | AWS secret access key used by deployment and tear-down pipelines                                                                 |
| `AWS_SSH_KEY_VALUE`     | Private SSH key used to access the AWS EC2 instance                                                                              |
| `BACKEND_DOT_ENV`       | Backend `.env` file content for the production stack, variables description: [backend/README.md](backend/README.md)              |
| `FRONTEND_DOT_ENV`      | Frontend `.env` file content for the production stack, variables description: [frontend/NoQL/README.md](frontend/NoQL/README.md) |
| `DOCKERHUB_USERNAME`    | Docker Hub username                                                                                                              |
| `DOCKERHUB_TOKEN`       | Docker Hub access token                                                                                                          |
| `GH_ACCESS_TOKEN`       | GitHub access token with read & write repository access                                                                          |

#### Variables

| Name               | Description                                                                                          |
|--------------------|------------------------------------------------------------------------------------------------------|
| `AWS_SSH_KEY_NAME` | Name of the EC2 SSH key pair how to get it: [infra/prod-stack/README.md](infra/prod-stack/README.md) |
| `NOQL_AWS_REGION`  | AWS region where the infrastructure is deployed                                                      |

---

### 📦 CI Pipelines

CI pipelines are triggered automatically on **pull requests targeting the `main` branch** and can
also be **run manually**.

Each component (backend, frontend, and plot service) has its **own dedicated CI pipeline**, allowing
independent validation, testing, and builds.

<details>
<summary><b>Backend CI Pipeline</b></summary>

#### [Backend CI Pipeline](.github/workflows/backend.yaml)

▶️ [GitHub trigger](https://github.com/janbabak/NoQL/actions/workflows/backend.yaml)

##### Jobs

- **Validate**
    - Detects changes in the [`backend`](backend) directory.
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

#### [Plot Service CI Pipeline](.github/workflows/plotservice.yaml)

▶️ [GitHub trigger](https://github.com/janbabak/NoQL/actions/workflows/plotservice.yaml)

##### Jobs

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

#### [Frontend CI Pipeline](.github/workflows/frontend.yaml)

▶️ [GitHub trigger](https://github.com/janbabak/NoQL/actions/workflows/frontend.yaml)

##### Jobs

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

### ,🚀 Deployment Pipeline

#### [Deployment Pipeline](.github/workflows/stack-deploy.yaml)

▶️ [GitHub trigger](https://github.com/janbabak/NoQL/actions/workflows/stack-deploy.yaml)

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
        - [`.env.frontend-prod`](infra/local-stack/.env.frontend-prod)
    - Starts Docker Compose, pulling Docker images from Docker Hub.
    - The frontend URL is printed in the output of the **Print stack URL** step.

---

### 🧹 Tear-down Pipeline

#### [Tear-down Pipeline](.github/workflows/stack-tear-down.yaml)

▶️ [GitHub trigger](https://github.com/janbabak/NoQL/actions/workflows/stack-tear-down.yaml)

The tear-down pipeline removes the AWS infrastructure.

- The pipeline can be triggered **only manually**.
- It accepts a single input parameter: **stack ID**.

#### Jobs

- **Tear Down Infrastructure**
    - Authenticates with AWS.
    - Deletes infrastructure defined in the CloudFormation template:
        - [`infra.yaml`](infra/prod-stack/infra.yaml)
  
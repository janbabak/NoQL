# NoQL - No Query Language

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
│  ├─ test/
│  swagger/                               ... swagger API documentation
customModel/                              ... custom LLM api (separate project - gpt proxy)
exampleDatabase/                          ... old example database
frontend/                                 ... frontend app root
├─ NoQL/                 
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
├─ dockerImages/                          ... docker images
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
  OPEN_AI_API_URL="https://api.openai.com/v1/chat/completions"
  OPEN_AI_API_KEY="" # input your api key
  
  GEMINI_API_URL="https://generativelanguage.googleapis.com/v1beta/models"
  GEMINI_API_KEY="" # input your api key
  
  LLAMA_API_URL="https://api.llama-api.com/chat/completions"
  LLAMA_API_KEY="" # input your api key
  
  CLAUDE_API_URL="https://api.anthropic.com/v1/messages"
  CLAUDE_API_KEY="" # input your api key
  ANTHROPIC_VERSION="2023-06-01"
  
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
  
  TRANSLATION_RETRIES=1
  PLOT_SERVICE_CONTAINER_NAME="plot-service-dev-stack"
  DEFAULT_USER_QUERY_LIMIT=150
  
  # security
  JWT_SECRET="fkajlak4jt34ktj34t98vu44d5d4p54o5d45m34ik5n345m34wm5l431145l434u64bgjsuicvkaplcvqyevasswilmvbti09478jujhhdsbfasdhfbu4" # could be changed
  # 30 min
  JWT_EXPIRATION=1800
  # 7 days
  JWT_REFRESH_EXPIRATION=604800
  DATA_ENCRYPTION_KEY="djfasgu98g438yth43iuhg34jnfmf343nfij34fij43fm34fnij34fi34f"
  ```

- Start [Dev stack](infra/dev-stack/README.md).
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
  
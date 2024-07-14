# NoQL - No Query Language

[![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=java&logoColor=white&color=f1931c)](https://www.java.com/en/)
[![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white)](https://gradle.org)
[![Postgres](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)

## ✏️ Description

NoSQL is a tool that lets users query databases using natural language. It's designed for those who aren't developers
and don't know query language syntax like SQL.

## 🧑‍🔬 Technologies

- [Java 17](https://www.java.com/en/)
- [Spring](https://spring.io)
- [Gradle](https://gradle.org)
- [Docker](https://www.docker.com)
- [Postgres](https://www.postgresql.org)
- [git](https://git-scm.com)

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

- Local stack is used for frontend development. It uses a local database and a local backend running like docker-compose
  services.
- Details are in the [Local stack](infra/local-stack/README.md).

#### Development stack

- Development stack is used for backend development and frontend development. It uses a local database and other
  dependencies running like docker-compose services and backend running on the host machine.
- It is necessary to create a `NoQL/backend/.env.local` file with the following content:

  ```dotenv
    API_KEY="xxx" # change
    LLAMA_AUTH_TOKEN="xxx" # change
    AI_STUDIO_API_KEY="xxx" # change

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

    TRANSLATION_RETRIES=3
    ```
- Start [Dev stack](infra/dev-stack/README.md).
- Set the development environment. The app will connect to the local database.
    ```bash
    export spring_profiles_active=local # TODO: create local profile
    ```
- Start the backend app:
- Run the backend
  ```bash
  cd backend
  ./gradlew bootRun
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

## AWS

- Connect to the AWS EC2 instance over SSH
  ```bash
  ssh ec2-user@3.68.195.75
  ```
- Install java
  ```bash
  sudo yum update
  sudo yum install java-17-amazon-corretto-headless.x86_64
  ```
- Copy the jar
  ```bash
  scp build/libs/backend-0.0.1-SNAPSHOT.jar ec2-user@3.68.195.75:/home/ec2-user
  ```
- Run the jar
  ```bash
  java -jar backend-0.0.1-SNAPSHOT.jar # the app ends when the session ends
  java -jar backend-0.0.1-SNAPSHOT.jar > noql.log 2>&1 & # detached with logging
  ```
  
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

### Start database

```bash
docker compose up -d # TODO: create docker compose file (instead of using example db)
```

### Start the app

- Set the development environment. The app will connect to the local database.
  ```bash
  export spring_profiles_active=local # TODO: create local profile
  ```

- Run the backend
  ```bash
  cd backend
  TODO: solve gradlew wrapper issue https://support.snyk.io/hc/en-us/articles/360007745957-Snyk-test-Could-not-find-or-load-main-class-org-gradle-wrapper-GradleWrapperMain
  ```


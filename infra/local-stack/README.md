# Local stacks

- There are 3 docker compose stacs which differ in components.
- All stacks persist database data in [./stack-data/**](./stack-data)

## Backend Dev Stack

- Used for **frontend development**.
- Runs backend with all dependencies.
- Environment variables come from [.env.local](../../backend/.env.local) file.
- **Docker compose stack contains:**
    - Backend
    - Plot service
    - Backend database
    - Example Postgres database

## Database Dev Stack

- Used for **backend development**.
- Runs all dependencies of backend (only databases so far).
- Environment variables come from [.env.local](../../backend/.env.local) file.
- **Docker compose stack contains:**
    - Plot service
    - Backend database
    - Example Postgres database
    - Example MySQL database

## Prod Stack

- Run all components that run in production.
- Environment variables come from [.env.backend-prod](.env.backend-prod) and [.env.frontend-prod](.env.backend-prod)
  files.
- **Docker compose stack contains:**
    - Backend
    - Frontend
    - Plot service
    - Backend database

## Usage

- Creation of backend stack automatically register new user and associate it with the example postgres database.

- **Create stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh create <databse|backend|prod>
    ```

- **Start existing stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh start <databse|backend|prod> 
    ```

- **Stop stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh stop <databse|backend|prod> 
    ```

- **Remove stack** (keep data):
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh remove <databse|backend|prod>
    ```
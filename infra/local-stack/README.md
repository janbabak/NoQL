# Dev stacks

## Backend Dev Stack

- **Docker compose stack containing:**
  - Backend server
  - Plot service
  - Backend database
  - Example Postgres database

## Database Dev Stack

- **Docker compose stack containing:**
    - Plot service
    - Backend database
    - Example Postgres database
    - Example MySQL database
  
## Usage

- **Create stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh create <databse|backend>
    ```

- **Start existing stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh start <databse|backend> 
    ```

- **Stop stack:**
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh stop <databse|backend> 
    ```

- **Remove stack** (keep data):
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh remove <databse|backend>
    ```
# Backend Dev Stack

- **Starts Docker compose stack containing:**
  - Backend server
  - Plot service
  - Backend database
  - Example Postgres database
  

- Create stack:
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh create  
    ```

- Start existing stack:
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh start  
    ```

- Stop stack:
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh stop  
    ```

- Remove stack (keep data):
    ```bash
    ./infra/local-stack/scripts/backend-stack.sh remove  
    ```
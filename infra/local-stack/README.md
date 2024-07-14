# Local Stack

- Run backend locally with all dependencies on a single click.
- Can be used for frontend development.
- The script starts backend in a docker container with its dependencies (database, plot service) also as separate containers, and it starts example database and insert sample data.
- Run all commands from the NoQL root directory!

## Local variables
It is necessary to create a `NoQL/backend/.env.local` file with the following content:
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

## Create and start local stack
```bash
chmod +x ./infra/local-stack/scripts/create-local-stack.sh
./infra/local-stack/scripts/create-local-stack.sh
```

## Start local stack
```bash
docker compose -f infra/local-stack/local-stack.docker-compose.yaml up -d
```

## Stop local stack
```bash
docker compose -f infra/local-stack/local-stack.docker-compose.yaml stop
```

## Build new version of local stack
```bash
chmod +x infra/local-stack/rebuild-stack.sh 
./infra/local-stack/scripts/rebuild-stack.sh
```
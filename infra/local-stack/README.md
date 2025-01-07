# Local Stack

- Run backend locally with all dependencies on a single click.
- Can be used for frontend development.
- The script starts backend in a docker container with its dependencies (database, plot service) also as separate containers, and it starts example database and insert sample data.
- Run all commands from the NoQL root directory!

## Local variables
It is necessary to create a `NoQL/backend/.env.local` file with the following content:
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
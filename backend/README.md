# NoQL backend

## Configuration

- App can be configured by environment variables in the `backend/.env.local` file.

### Environment variables

**Pagination**

- `PAGINATION_MAX_PAGE_SIZE` - Maximum page size of automatically paginated query result.
- `PAGINATION_DEFAULT_PAGE_SIZE` - Default page size of automatically paginated query result.

**Database connection**

- `NOQL_DB_NAME` - (required) Database name
- `NOQL_DB_HOST` - (required) Database host
- `NOQL_DB_PORT` - (required) Database port
- `NOQL_DB_USERNAME` - (required) Database username
- `NOQL_DB_PASSWORD` - (required) Database password.

**Local Database (for testing)**
- should be the same as the database above (or there has to be another db running)
- `POSTGRES_PASSWORD` - (required) Database password
- `POSTGRES_USER` - (required) Database username
- `POSTGRES_DB` - (required) Database name

**Other**

- `TRANSLATION_RETRIES` - (required) Number of retries when translated query fails due to a syntax error
- `PLOT_SERVICE_CONTAINER_NAME` - (required) Name of the container running the plot service
- `DEFAULT_USER_QUERY_LIMIT` - Default number of queries that can be executed by newly registered user.

**Security**

- `JWT_SECRET` - (required) Secret key for JWT token generation (at least 512 bits)
- `JWT_EXPIRATION` - Expiration time of JWT token in seconds. (Default is 1 day)
- `JWT_REFRESH_EXPIRATION` - Expiration time of JWT refresh token in seconds. (Default is 7 days)

**Api keys to external services**

- `GEMINI_API_KEY` - (required) Google Gemini model API key
- `GEMINI_API_URL` - (required) Google Gemini model API URL
- `OPEN_AI_API_KEY` - (required) OpenAI API key - for GPT models
- `OPEN_AI_API_URL` - (required) OpenAI API URL
- `LLAMA_API_KEY` - (required) for Llama models
- `LLAMA_API_URL` - (required) for Llama models
- `CLAUDE_API_KEY` - (required) for Claude models
- `CLAUDE_API_URL` - (required) for Claude models
- `ANTHROPIC_VERSION` - (required) Anthropic API version

## Gradle properties

- `showLogs` - Show logs of tests in the console. Default is `false`.
     ```bash
    ./gradlew test -PshowLogs
    ````
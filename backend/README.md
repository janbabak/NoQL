# NoQL backend

## Configuration

- App can be configured by environment variables in the `backend/.env.local` file.

### Envoronment variables

**Pagination**

- `PAGINATION_MAX_PAGE_SIZE` - Maximum page size of automatically paginated query result.
- `PAGINATION_DEFAULT_PAGE_SIZE` - Default page size of automatically paginated query result.

**Database connection**

- `NOQL_DB_NAME` - (required) Database name
- `NOQL_DB_HOST` - (required) Database host
- `NOQL_DB_PORT` - (required) Database port
- `NOQL_DB_USERNAME` - (required) Database username
- `NOQL_DB_PASSWORD` - (required) Database password.

**Other**

- `TRANSLATION_RETRIES` - (required) Number of retries when translated query fails due to a syntax error
- `DEFAULT_USER_QUERY_LIMIT` - Default number of queries that can be executed by newly registered user.

**Security**

- `JWT_SECRET` - (required) Secret key for JWT token generation (at least 512 bits)
- `JWT_EXPIRATION` - Expiration time of JWT token in seconds. (Default is 1 day)
- `JWT_REFRESH_EXPIRATION` - Expiration time of JWT refresh token in seconds. (Default is 7 days)

## Gradle properties

- `showLogs` - Show logs of tests in the console. Default is `false`.
     ```bash
    ./gradlew test -PshowLogs
    ````
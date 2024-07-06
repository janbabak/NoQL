# Dev Stack

- Run all backend dependencies (database, plot service, example databases) locally in docker containers.
- Used for backend development.
- When local stack is running, backend can be run locally and connect to it.
- Run all commands from the root of the NoQL project.

## Start Dev Stack

```bash
docker compose -f infra/dev-stack/dev-stack.docker-compose.yaml up -d
```

## Stop Dev Stack

```bash
docker compose -f infra/dev-stack/dev-stack.docker-compose.yaml down
```
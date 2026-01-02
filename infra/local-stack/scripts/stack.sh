#!/bin/bash

set -euo pipefail

# This script manages backend-stack (stack containing backend,  plot service, database, and example database)

BACKEND_URL="http://localhost:8080"
STACK_NAME=""
STACK_TYPE=""

DOCKER_COMPOSE_FILE=""
BACKEND_DEV_STACK_DOCKER_COMPOSE_FILE="infra/local-stack/backend-stack.docker-compose.yaml"
DATABASE_DEV_STACK_DOCKER_COMPOSE_FILE="infra/local-stack/databases-stack.docker-compose.yaml"
PROD_STACK_DOCKER_COMPOSE_FILE="infra/local-stack/prod-stack.docker-compose.yaml"

ENV_FILE="" # backend env file used to do database health check
BACKEND_DEV_STACK_ENV_FILE="backend/.env.local"
DATABASE_DEV_STACK_ENV_FILE="backend/.env.local"
PROD_STACK_ENV_FILE="./infra/local-stack/.env.backend-prod"


EXAMPLE_DB_SQL_INIT_SCRIPT="./sample-data.sql"
EXAMPLE_DB_CONTAINER=""
EXAMPLE_DB_NAME="database"
EXAMPLE_DB_USER="user"
EXAMPLE_DB_PASSWORD="password"
EXAMPLE_DB_PORT=5432

USER_EMAIL="user@email.com"
USER_PASSWORD="12345678"
USER_ID=""
AUTH_TOKEN=""

createStack() {
    echo "Creating ${STACK_TYPE}"
    runDockerCompose
    insertSampleData

    if [[ "$STACK_TYPE" == "backend" ]]; then
        registerNewUser
        authenticateUser
        registerNewDatabase
    fi
}

startLocalStack() {
    echo "Starting ${STACK_TYPE}"
    docker compose --file $DOCKER_COMPOSE_FILE --project-name ${STACK_NAME} start
}

stopLocalStack() {
    echo "Stopping ${STACK_TYPE}"
    docker compose --file ${DOCKER_COMPOSE_FILE} --project-name ${STACK_NAME} stop
}

removeLocalStack() {
    echo "Removing local ${STACK_TYPE}"
    docker compose --file ${DOCKER_COMPOSE_FILE} --project-name ${STACK_NAME} down
}

usage() {
    echo "Usage: $0 {create|start|stop|remove} {backend|database|prod}"
    echo
    echo "Commands:"
    echo "  create    Create the local stack, insert sample data, register user and database"
    echo "  start     Start existing containers"
    echo "  stop      Stop running containers"
    echo "  remove    Stop and remove containers and network"
    echo "  database  Use database stack"
    echo "  backend   Use backend stack"
}

runDockerCompose() {
    # env file is referenced because of the health check
    docker compose \
        --file ${DOCKER_COMPOSE_FILE} \
        --env-file ${ENV_FILE} \
        --project-name ${STACK_NAME} \
        up --detach

    sleep 20
    echo "Local stack started"
}

insertSampleData() {
    echo "Inserting sample data into the example database..."

    docker exec -i "$EXAMPLE_DB_CONTAINER" \
    psql -U "$EXAMPLE_DB_USER" -d "$EXAMPLE_DB_NAME" -f "$EXAMPLE_DB_SQL_INIT_SCRIPT"

    sleep 20
    echo "Init script executed"
}

registerNewUser() {
    RESPONSE=$(curl --location --silent --show-error --write-out "HTTPSTATUS:%{http_code}" \
        --request POST "${BACKEND_URL}/auth/register" \
        --header "Content-Type: application/json" \
        --data-raw "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\",
            \"firstName\": \"user\",
            \"lastName\": \"user\"
        }")

    # Separate body and status code
    HTTP_BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS\:.*//g')
    HTTP_STATUS=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')

    if [ "$HTTP_STATUS" -eq 201 ]; then
        echo "User registered successfully."
    else
        echo "Failed to register user. Server response:"
        echo "$HTTP_BODY"
        echo "HTTP code: $HTTP_STATUS"
        exit 1
    fi
}

authenticateUser() {
    RESPONSE=$(curl --location --silent --show-error --write-out "HTTPSTATUS:%{http_code}" \
        --request POST "${BACKEND_URL}/auth/authenticate" \
        --header "Content-Type: application/json" \
        --data-raw "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\"
        }")

    # Separate body and status code
    HTTP_BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS\:.*//g')
    HTTP_STATUS=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')

    if [ "$HTTP_STATUS" -eq 200 ]; then
        # Extract accessToken and userId using jq
        AUTH_TOKEN=$(echo "$HTTP_BODY" | jq -r '.accessToken')
        USER_ID=$(echo "$HTTP_BODY" | jq -r '.user.id')
        echo "Authentication successful."
        echo "UserId: $USER_ID"
        echo "Access token: $AUTH_TOKEN"
    else
        echo "Authentication failed. Server response:"
        echo "$HTTP_BODY"
        echo "HTTP code: $HTTP_STATUS"
        exit 2
    fi
}

registerNewDatabase() {
    RESPONSE=$(curl --location --silent --show-error --write-out "HTTPSTATUS:%{http_code}" \
        --request POST "${BACKEND_URL}/database" \
        --header "Content-Type: application/json" \
        --header "Authorization: Bearer ${AUTH_TOKEN}" \
        --data-raw "{
            \"name\" : \"Example Postgres\",
            \"host\" : \"postgres\",
            \"port\" : ${EXAMPLE_DB_PORT},
            \"database\" : \"${EXAMPLE_DB_NAME}\",
            \"userName\" : \"${EXAMPLE_DB_USER}\",
            \"password\" : \"${EXAMPLE_DB_PASSWORD}\",
            \"engine\" : \"POSTGRES\",
            \"userId\" : \"${USER_ID}\",
            \"createDefaultChat\" : true
        }")

    # Separate body and status code
    HTTP_BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS\:.*//g')
    HTTP_STATUS=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')

    if [ "$HTTP_STATUS" -eq 201 ]; then
        echo "Database registered successfully."
    else
        echo "Failed to register database. Server response:"
        echo "$HTTP_BODY"
        echo "HTTP code: $HTTP_STATUS"
        exit 3
    fi
}

if [ $# -le 1 ]; then
    usage
    exit 1
fi

case "$2" in
    database)
        STACK_TYPE=$2
        STACK_NAME="${STACK_TYPE}-dev-stack"
        DOCKER_COMPOSE_FILE=$DATABASE_DEV_STACK_DOCKER_COMPOSE_FILE
        ENV_FILE=$DATABASE_DEV_STACK_ENV_FILE
        EXAMPLE_DB_CONTAINER="example-postgres-${STACK_TYPE}-stack"
    ;;
    backend)
        STACK_TYPE=$2
        STACK_NAME="${STACK_TYPE}-dev-stack"
        DOCKER_COMPOSE_FILE=$BACKEND_DEV_STACK_DOCKER_COMPOSE_FILE
        ENV_FILE=$BACKEND_DEV_STACK_ENV_FILE
        EXAMPLE_DB_CONTAINER="example-postgres-${STACK_TYPE}-stack"
    ;;
    prod)
        STACK_TYPE=$2
        STACK_NAME="${STACK_TYPE}-stack"
        DOCKER_COMPOSE_FILE=$PROD_STACK_DOCKER_COMPOSE_FILE
        ENV_FILE=$PROD_STACK_ENV_FILE
    ;;
    *)
        usage
        exit 1
esac


case "$1" in
  create)
    createStack
    ;;
  start)
    startLocalStack
    ;;
  stop)
    stopLocalStack
    ;;
  remove)
    removeLocalStack
    ;;
  *)
    usage
    exit 2
    ;;
esac

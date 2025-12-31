#!/bin/bash

set -euo pipefail

# This script manages backend-stack (stack containing backend,  plot service, database, and example database)

STACK_NAME="backend-dev-stack"
BACKEND_URL="http://localhost:8080"
DOCKER_COMPOSE_FILE="infra/local-stack/backend-dev.docker-compose.yaml"

EXAMPLE_DB_SQL_INIT_SCRIPT="./sample-data.sql"
EXAMPLE_DB_CONTAINER="example-postgres-local-stack"
EXAMPLE_DB_NAME="database"
EXAMPLE_DB_USER="user"
EXAMPLE_DB_PASSWORD="password"
EXAMPLE_DB_PORT=5432

USER_EMAIL="user@email.com"
USER_PASSWORD="12345678"
USER_ID=""
AUTH_TOKEN=""

createStack() {
    echo "Creating a local stack"
    runDockerCompose
    insertSampleData
    registerNewUser
    authenticateUser
    registerNewDatabase
}

startLocalStack() {
    echo "Starting local stack"
    docker compose --file $DOCKER_COMPOSE_FILE --project-name ${STACK_NAME} start
}

stopLocalStack() {
    echo "Stopping local stack"
    docker compose --file ${DOCKER_COMPOSE_FILE} --project-name ${STACK_NAME} stop
}

removeLocalStack() {
    echo "Removing local stack"
    docker compose --file ${DOCKER_COMPOSE_FILE} --project-name ${STACK_NAME} down
}

usage() {
    echo "Usage: $0 {create|start|stop|remove}"
    echo
    echo "Commands:"
    echo "  create  Create the local stack, insert sample data, register user and database"
    echo "  start   Start existing containers"
    echo "  stop    Stop running containers"
    echo "  remove  Stop and remove containers and network"
}

runDockerCompose() {
    docker compose \
        --file infra/local-stack/backend-dev.docker-compose.yaml \
        --env-file  backend/.env.local \
        --project-name ${STACK_NAME} \
        up --detach

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

if [ $# -eq 0 ]; then
    usage
    exit 1
fi

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
    exit 1
    ;;
esac
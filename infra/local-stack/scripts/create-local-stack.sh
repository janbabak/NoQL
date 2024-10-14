#! /bin/bash

# This script creates a local stack - starts backend in a docker container and its dependencies as docker containers
# and inserts sample data into the example database. It also creates a new database record in the backend.

createLocalStack() {
    echo "Creating a local stack..."

    docker compose -f infra/local-stack/local-stack.docker-compose.yaml up -d
}

insertSampleData() {
    echo "Inserting sample data into the example database..."

    local CONTAINER="example-postgres-local-stack"
    local SQL_SCRIPT_PATH="./sample-data.sql"
    local DATABASE_NAME="database"
    local USER="user"

    docker exec -i "$CONTAINER" psql -U "$USER" -d "$DATABASE_NAME" -f "$SQL_SCRIPT_PATH"
}

createNewDatabaseRecordInBackend() {
    echo "Creating a new database record in the backend..."

    # TODO: create new user and database for him

    curl --location 'http://localhost:8080/database' \
    --header 'Content-Type: application/json' \
    --data '{
        "name" : "Local postgres",
        "host" : "example-postgres",
        "port" : "5432",
        "database" : "database",
        "userName" : "user",
        "password" : "password",
        "engine" : "POSTGRES"
    }'
}

createLocalStack
sleep 15
insertSampleData
sleep 15
createNewDatabaseRecordInBackend

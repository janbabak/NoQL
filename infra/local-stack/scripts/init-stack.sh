#!/bin/bash

USER_EMAIL="user@email.com"
USER_PASSWORD="12345678"
BACKEND_URL="http://localhost:8080"
AUTH_TOKEN=""

registerNewUser() {
    RESPONSE=$(curl --write-out "%{http_code}" --silent --output /dev/null \
        --location "${BACKEND_URL}/auth/register" \
        --header 'Content-Type: application/json' \
        --data-raw "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\",
            \"firstName\": \"user\",
            \"lastName\": \"user\"
        }")

    if [ "$RESPONSE" -eq 201 ]; then
        echo "User registered successfully."
    elif [ "$RESPONSE" -eq 409 ]; then
        echo "User with email $USER_EMAIL already exists."
    else
        echo "Failed to register user. HTTP code: $RESPONSE"
    fi
}

authenticateUser() {
    RESPONSE=$(curl --write-out "%{http_code}" --silent --output /dev/null \
    --location "${BACKEND_URL}/auth/authenticate" \
    --header 'Content-Type: application/json' \
    --data-raw "{
        \"email\": \"${USER_EMAIL}\",
        \"password\": \"${USER_PASSWORD}\"
    }")
    echo $RESPONSE
}

authenticateUser
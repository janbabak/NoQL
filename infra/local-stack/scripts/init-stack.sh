#!/bin/bash

USER_EMAIL="user@email.com"
USER_PASSWORD="12345678"
BACKEND_URL="http://localhost:8080"
AUTH_TOKEN=""
USER_ID=""

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

    if [ "$RESPONSE" -eq 200 ]; then
        echo "User registered successfully."
    elif [ "$RESPONSE" -eq 409 ]; then
        echo "User with email $USER_EMAIL already exists."
    else
        echo "Failed to register user. HTTP code: $RESPONSE"
    fi
}

authenticateUser() {
    RESPONSE=$(curl --silent \
        --location "${BACKEND_URL}/auth/authenticate" \
        --header "Content-Type: application/json" \
        --data-raw "{
            \"email\": \"${USER_EMAIL}\",
            \"password\": \"${USER_PASSWORD}\"
        }")

    # Extract accessToken using jq
    AUTH_TOKEN=$(echo "$RESPONSE" | jq -r '.accessToken')
    USER_ID=$(echo $RESPONSE | jq -r '.user.id')

    if [ "$AUTH_TOKEN" != "null" ] && [ -n "$AUTH_TOKEN" ]; then
        echo "Authentication successful."
        echo "UserId: $USER_ID"
        echo "Access token: $AUTH_TOKEN"
    else
        echo "Authentication failed. Response:"
        echo "$RESPONSE"
    fi
}

registerNewDatabase() {
    RESPONSE=$(curl --silent \
        --location "${BACKEND_URL}/database" \
        --header "Content-Type: application/json" \
        --header "Authorization: Bearer ${AUTH_TOKEN}" \
        --data "{
        \"name\" : \"Example Postgres\",
        \"host\" : \"postgres\",
        \"port\" : \"5432\",
        \"database\" : \"database\",
        \"userName\" : \"user\",
        \"password\" : \"password\",
        \"engine\" : \"POSTGRES\",
        \"userId\" : \"${USER_ID}\",
        \"createDefaultChat\" : true
    }")

    echo $RESPONSE
}

# registerNewUser
authenticateUser
registerNewDatabase
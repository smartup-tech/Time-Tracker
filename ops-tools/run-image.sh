#!/usr/bin/env bash

ARTIFACT_VERSION="${ARTIFACT_VERSION:-latest}"
REPOSITORY="time-tracker-backend"
DB_PASSWORD="${DB_PASSWORD:-YOUR_DB_PASS}"
EMAIL_NAME="${EMAIL_NAME:-YOUR_EMAIL_NAME}"
EMAIL_PASSWORD="${EMAIL_PASSWORD:-YOUR_EMAIL_PASSWORD}"

# Option 1: With .env file
cat << EOF > .env
SPRING_PROFILE=prod

FLYWAY_DB_URL=jdbc:postgresql://postgres:5432/timetracker
FLYWAY_DB_USER=timetracker
FLYWAY_DB_PASSWORD=${DB_PASSWORD}

DATASOURCE_DB_URL=jdbc:postgresql://postgres:5432/timetracker
DATASOURCE_DB_USER=timetracker
DATASOURCE_DB_PASSWORD=${DB_PASSWORD}

EMAIL_USERNAME=${EMAIL_NAME}
EMAIL_PASSWORD=${EMAIL_PASSWORD}

DOMAIN_NAME=timetracker

ALLOWED_ORIGIN=http://localhost:8082

MM_NOTIFICATION_URL: ${MM_NOTIFICATION_URL}
TIME_TRACKER_URL: https://${DOMAIN_NAME}/
EOF

docker run --rm -it -p 8080:8080/tcp --env-file ".env" ${REPOSITORY}:${ARTIFACT_VERSION}

# Option 2: With env params
#docker run --rm -it -p 8080:8080/tcp \
#    --env "SPRING_PROFILE=prod" \
#    --env "FLYWAY_DB_URL=jdbc:postgresql://postgres:5432/timetracker" \
#    --env "FLYWAY_DB_USER=timetracker" \
#    --env "FLYWAY_DB_PASSWORD=${DB_PASSWORD}" \
#    --env "DATASOURCE_DB_URL=jdbc:postgresql://postgres:5432/timetracker" \
#    --env "EMAIL_USERNAME=${EMAIL_NAME}" \
#    --env "EMAIL_PASSWORD=${EMAIL_PASSWORD}" \
#    --env "DATASOURCE_DB_USER=timetracker" \
#    --env "DATASOURCE_DB_PASSWORD=${DB_PASSWORD}" \
#    --env "ALLOWED_ORIGIN=http://localhost:8082" \
#    --env "DOMAIN_NAME=timetracker" \
#    ${REPOSITORY}:${ARTIFACT_VERSION}
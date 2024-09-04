#!/usr/bin/env bash

ARTIFACT_VERSION="${ARTIFACT_VERSION:-latest}"
REPOSITORY="time-tracker-backend"

# Option 1: With .env file
cat << EOF > .env
SPRING_PROFILE=prod

FLYWAY_DB_URL=jdbc:postgresql://postgres:5432/timetracker
FLYWAY_DB_USER=timetracker
FLYWAY_DB_PASSWORD=timetracker

DATASOURCE_DB_URL=jdbc:postgresql://postgres:5432/timetracker
DATASOURCE_DB_USER=timetracker
DATASOURCE_DB_PASSWORD=timetracker

EMAIL_USERNAME=no-reply-time-tracker@gamil.com
EMAIL_PASSWORD=timetracker

DOMAIN_NAME=timetracker

ALLOWED_ORIGIN=http://localhost:8082
EOF

docker run --rm -it -p 8080:8080/tcp --env-file ".env" ${REPOSITORY}:${ARTIFACT_VERSION}

# Option 2: With env params
#docker run --rm -it -p 8080:8080/tcp \
#    --env "SPRING_PROFILE=prod" \
#    --env "FLYWAY_DB_URL=jdbc:postgresql://postgres:5432/timetracker" \
#    --env "FLYWAY_DB_USER=timetracker" \
#    --env "FLYWAY_DB_PASSWORD=timetracker" \
#    --env "DATASOURCE_DB_URL=jdbc:postgresql://postgres:5432/timetracker" \
#    --env "EMAIL_USERNAME=no-reply-time-tracker@smartup.ru" \
#    --env "EMAIL_PASSWORD=zxsoioaxzlzqlplu" \
#    --env "DATASOURCE_DB_USER=timetracker" \
#    --env "DATASOURCE_DB_PASSWORD=timetracker" \
#    --env "ALLOWED_ORIGIN=http://localhost:8082" \
#    --env "DOMAIN_NAME=timetracker" \
#    ${REPOSITORY}:${ARTIFACT_VERSION}
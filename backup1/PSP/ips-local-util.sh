#!/bin/bash

# Environment variables (set these)
echo "Required dependencies: mvn, psql, podman"
echo "Required env variables: IPS_REPO_PATH, IPS_NAMESPACE"

# export IPS_REPO_PATH=
# export IPS_NAMESPACE=

# Step 0: Check if required env variables are set
if [ -z "$IPS_REPO_PATH" ] || [ -z "$IPS_NAMESPACE" ]; then
    echo "ERROR: Both IPS_REPO_PATH and IPS_NAMESPACE environment variables need to be set."
    exit 1
fi

# Step 0: Check if required env variables are set
if [ -z "$IPS_SERVICE_VERSION" ]; then
    echo "IPS_SERVICE_VERSION not set, will default to 20.0.21"
    export IPS_SERVICE_VERSION=20.0.21
fi

if [ -z "$IPS_POD_MEMORY" ]; then
    echo "IPS_POD_MEMORY not set, will default to 1024m"
    export IPS_POD_MEMORY=1024m
fi

if [ -z "$PGPASSWORD" ]; then
    export PGPASSWORD=$(uuidgen)
    echo "PGPASSWORD not set, will default to '$PGPASSWORD'"
fi

# create and initialize podman machine
podman machine init
podman machine start


# Step 1: Build the IPS Managed Model
echo "Building IPS Managed Model..."
cd $IPS_REPO_PATH
mvn clean install

if [ $? -ne 0 ]; then
    echo "ERROR: IPS Managed Model build failed."
    exit 1
fi
echo "IPS Managed Model built successfully."

# Step 2: Stop and remove PostgreSQL container if running
echo "Stopping PostgreSQL container if it's running..."
podman stop postgresql
podman rm postgresql

# Step 3: Run the PostgreSQL container in the background
echo "Starting PostgreSQL container in the background..."
podman run --name postgresql -d -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=$PGPASSWORD -p 5434:5432 postgres

# Give PostgreSQL some time to initialize
echo "Waiting for PostgreSQL to initialize..."
sleep 5

# Check PostgreSQL readiness
until psql -h localhost -U postgres -p 5434 -d postgres -c '\q'; do
  >&2 echo "Postgres is still unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up and running"

# Step 4: Create database and schema
echo "Creating IPS database and schema..."
psql "host=localhost port=5434 dbname=postgres user=postgres password=$PGPASSWORD" <<EOF

create database imr;
\c imr
create schema $IPS_NAMESPACE;
set schema '$IPS_NAMESPACE';
\i $IPS_REPO_PATH/target/classes/META-INF/$IPS_NAMESPACE/sql/create.sql
EOF


# Step 5: Run the IPS Devbox
echo "Running IPS Devbox..."
podman run --replace --name ips \
    -m $IPS_POD_MEMORY \
    -v $IPS_REPO_PATH/target:/app/ips/psl \
    -p 8008:8008 \
    -e SPRING_PROFILES_ACTIVE=devbox \
    -e LOADER_PATH=/app/ips/psl \
    -e NAMESPACE=$IPS_NAMESPACE \
    -e SPRING_DATASOURCE_PASSWORD=$PGPASSWORD \
    docker.intuit.com/data-dataplatform/ips-mgd-relational-svc/service/ips-mgd-relational-svc:$IPS_SERVICE_VERSION

echo "IPS Devbox is running, you can now access it at http://localhost:8008"

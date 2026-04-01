# PSP DATABASE DOCKERFILES PROJECT
# --------------------------
# This is the Dockerfile for PSP Database
#
# HOW TO BUILD THIS IMAGE
# -----------------------
# Put all downloaded files in the same directory as this Dockerfile
# Run:
#      $ docker build -t psp/database:0.0.1 . --force-rm=true
#
# Pull base image
# ---------------
#FROM store/oracle/database-enterprise:12.2.0.1
FROM docker.intuit.com/small-business/onlinepayroll/cicd/oracle/database-enterprise:12.2.0.1

# Argument from Command Line
#---------------------------
ARG PROJECT_VERSION
ARG NEXUS_URL

# Environment variables required for this build (do NOT change)
# -------------------------------------------------------------
ENV INSTALL_DIR=/home/oracle/install \
    SETUP_DIR=/home/oracle/setup \
    MODEL_DIR=PSE/domain/src/main/model \
    CONFIG_DIR=docker/config \
    SCRIPTS_DIR=docker/scripts \
    TZ=America/Los_Angeles \
    ARTIFACT_VERSION=${PROJECT_VERSION} \
    NEXUS_URL_VALUE=${NEXUS_URL}

RUN mkdir -p $INSTALL_DIR

# Copy files needed during scheme creation
# -----------------------------------------
COPY --chown=oracle:oinstall $CONFIG_DIR/configDockerDB.sh $SETUP_DIR
COPY --chown=oracle:oinstall $CONFIG_DIR/configDB.sh $SETUP_DIR
COPY --chown=oracle:oinstall $CONFIG_DIR/pspDB.sh $SETUP_DIR
COPY --chown=oracle:oinstall $SCRIPTS_DIR/User.sql $INSTALL_DIR
COPY --chown=oracle:oinstall $MODEL_DIR/CreateUser.sql $INSTALL_DIR
COPY --chown=oracle:oinstall $MODEL_DIR/SAP_InsertTestUsers.sql $INSTALL_DIR

# Download DB-Install files from Nexus
RUN curl -o /$INSTALL_DIR/psp-dbinstall-$ARTIFACT_VERSION.zip "$NEXUS_URL_VALUE&a=psp-dbinstall&v=$ARTIFACT_VERSION&e=zip"
RUN curl -o /$INSTALL_DIR/psp-dbinstall-secondary-$ARTIFACT_VERSION.zip "$NEXUS_URL_VALUE&a=psp-dbinstall-secondary&v=$ARTIFACT_VERSION&e=zip"

RUN chmod ug+x $SETUP_DIR/*.sh
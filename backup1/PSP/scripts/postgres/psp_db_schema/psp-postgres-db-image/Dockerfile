FROM docker.intuit.com/docker-rmt/postgres:15.4-bullseye

ENV POSTGRES_DB psp
ENV POSTGRES_USER PSPADM
ENV POSTGRES_PASSWORD PSPADM

#Main Shell Script to setup DB
COPY psp-postgres-db-image/script/setup-db.sh /docker-entrypoint-initdb.d/

#Copy all sql files
ADD psp-postgres-db-image/db-init-setup-sql/ /tmp/

#Not giving entrypoint or cmd, as it will use from base image
#Scripts present in /docker-entrypoint-initdb.d directory will be exected i.e. setup-db.sh

##script to setup pg_hint_plan , auto_explain and logging in postgres image
#RUN apt-get update -y
#RUN apt-get install -y alien
#RUN apt-get install -y wget
#RUN wget https://github.com/ossc-db/pg_hint_plan/releases/download/REL15_1_5_0/pg_hint_plan15-1.5-1.el7.x86_64.rpm
#RUN alien ./pg_hint_plan*.rpm
#RUN dpkg -i pg-hint-plan*.deb
#RUN cp /usr/pgsql-15/share/extension/pg_hint_plan.control /usr/share/postgresql/15/extension
#RUN cp /usr/pgsql-15/share/extension/pg_hint_plan--1.4.1--1.5.sql /usr/share/postgresql/15/extension
#RUN cp /usr/pgsql-15/lib/pg_hint_plan.so /usr/lib/postgresql/15/lib
#
##copy local postgres postgresql.conf configuration
#COPY psp-postgres-db-image/configuration/postgresql.conf /tmp/

FROM docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc/psp-base-img:0.0.8

ARG build
ARG nextVersion
ARG dbDeployment

ARG DOCKER_TAGS=latest
# ARG JIRA_PROJECT=https://jira.intuit.com/projects/<CHANGE_ME>
ARG DOCKER_IMAGE_NAME=docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc:${DOCKER_TAGS}
ARG SERVICE_LINK=https://devportal.intuit.com/app/dp/resource/6847740825712235356

LABEL maintainer=dhanendra_jain@intuit.com \
      app=dt-payroll-svc \
      app-scope=runtime \
      build=${build}

#VMP
COPY app/artifacts/wars/CdmAdapter-${nextVersion}.war /app/artifacts/CdmAdapter.war
#SAP
COPY app/artifacts/wars/SAP-${nextVersion}.war /app/artifacts/SAP.war
COPY app/artifacts/wars/psp-test-ws-${nextVersion}.war /app/artifacts/test-ws.war
COPY app/artifacts/wars/testtools-${nextVersion}.war /app/artifacts/testtools.war


#batch-jobs
COPY app/artifacts/wars/batch-jobs-${nextVersion}.war  /app/artifacts/batch-jobs.war

#QBDT
COPY app/artifacts/wars/psp-qbdt-adapter-${nextVersion}.war /app/artifacts/payroll.war
COPY app/artifacts/wars/qbdtws-${nextVersion}.war /app/artifacts/qbdtws.war
COPY app/artifacts/wars/Keynote-Adapter-${nextVersion}.war /app/artifacts/Keynote-Adapter.war

#RAT - NOT_IN_USE
#COPY app/artifacts/wars/DataAdapter-${nextVersion}.war /app/artifacts/DataAdapter.war

#Adapter
COPY app/artifacts/wars/EWSAdapter-${nextVersion}.war  /app/artifacts/EWSAdapter.war
COPY app/artifacts/wars/cep-${nextVersion}.war /app/artifacts/cep.war
COPY app/artifacts/wars/DISAdapter-${nextVersion}.war /app/artifacts/DISAdapter.war
COPY app/artifacts/wars/ptcadapter-${nextVersion}.war /app/artifacts/ptcadapter.war
COPY app/artifacts/wars/BRMAdapter-${nextVersion}.war /app/artifacts/BRMAdapter.war


#Copy configurations
COPY app/artifacts/config/psp-configuration-${nextVersion}.zip /app/artifacts/psp-configuration.zip
COPY app/artifacts/zips/psp-batchjobs-${nextVersion}-batchdeploy.zip /app/artifacts/psp-batchjobs-batchdeploy.zip
COPY app/artifacts/zips/psp-batchjobs-${nextVersion}-batchdeploy-jss.zip /app/artifacts/psp-batchjobs-batchdeploy-jss.zip
COPY app/artifacts/zips/psp-batchjobs-${nextVersion}-spcfdeploy.zip /app/artifacts/batch-jobs-spcfdeploy.zip


# Adding pem file to see if this is able to connect to IDPS without needing the policy ID. Remove it later.
#COPY --from=build /usr/src/app/PSE/configuration/src/main/resources/templates/key_v2-b95a8179f0779.pem /app/key_v2-b95a8179f0779.pem

# Download latest contrast.jar
RUN curl -o /app/contrast/javaagent/contrast.jar https://artifact.intuit.com/artifactory/generic-local/dev/security/ssdlc/contrast/java/latest/contrast.jar

# Download latest aspectjweaver.jar
RUN curl -o /app/contrast/javaagent/aspectjweaver.jar https://artifact.intuit.com/artifactory/maven-proxy-cache/org/aspectj/aspectjweaver/1.8.13/aspectjweaver-1.8.13.jar

# Tomcat HPA Configuration : https://devportal.intuit.com/app/dp/capability/CAP-1321/capabilityDocs/main/docs/usecases/hpa/tomcat-hpa.md?searchTerm=hpa
# https://github.intuit.com/pages/kubernetes/modern-saas-docs/iks/iks_metrics/
# Setup Tomcat Metrics : https://github.intuit.com/cto-dev-fdp-common/fdp-metrics-tomcat/blob/master/README.md
# 1.0.0 does not support enable.memory.metrics, so using 1.0.1
# Commenting it out below, as that metrics-tomcat-1.0.1.war file is bringing critical vulnerabilities.
RUN curl -L -o /usr/local/tomcat/webapps/metrics.war https://artifact.intuit.com/nexus/content/groups/public/com/intuit/sbg/psp/psp-tomcat-metrics-core/0.0.2/psp-tomcat-metrics-core-0.0.2.war

COPY package/build.params.json /build.params.json
RUN chmod 644 /build.params.json

COPY package/entry.sh /app/entry.sh
RUN chmod 644 /app/entry.sh

COPY package/pspHealthCheck.sh /app/pspHealthCheck.sh
RUN chmod 644 /app/pspHealthCheck.sh

COPY package/jss-scheduler-entry.sh /app/jss-scheduler-entry.sh
RUN chmod 644 /app/jss-scheduler-entry.sh

COPY package/db-update.sh /app/db-update.sh
RUN chmod 644 /app/db-update.sh

# Script to create Directory Structure inside Container for STG JSS Pods as they do not have EFS Volume
COPY package/create-jss-efs-directory-structure.sh /app/create-jss-efs-directory-structure.sh
RUN chmod 644 /app/create-jss-efs-directory-structure.sh


#RUN mkdir /app/tmp  The Base tomcat image has this package already present in it.
RUN chown appuser:appuser /app -R
RUN chown appuser:appuser /usr/local/tomcat -R

RUN echo ${nextVersion} > /app/version.txt
RUN echo ${dbDeployment} > /app/dbDeployment.txt

# Remove unnecessary tools
#RUN ["/home/appuser/post_harden.sh"]

USER appuser
CMD ["/bin/sh", "/app/entry.sh"]

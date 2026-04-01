#!/bin/bash

echo "***** STARTING HEALTH CHECK *****"

echo "Script Name: $0"


checkHealth(){
    health_value=$1
    module=$2
    if test -n "${health_value}"; then
        echo "HealthCheck: Module=${module} Status=up. AppVersion=${health_value}"
    else
        echo "HealthCheck: Module=${module} Status=down. AppVersion=NA"
    fi
}

if [ -f BRMAdapter.war ]; then
    brm_health_value=$(curl -s http://localhost:8080/BRMAdapter/appversion.html)
    checkHealth "${brm_health_value}" "BRM_Adapter"
fi

if [ -f cep.war ]; then
    ade_health_value=$(curl -s http://localhost:8080/cep/appversion.html)
    checkHealth ${ade_health_value} "ADE_Adapter"
fi

if [ -f DISAdapter.war ]; then
    dis_health_value=$(curl -s http://localhost:8080/DISAdapter/appversion.html)
    checkHealth ${dis_health_value} "DIS_Adapter"
fi

if [ -f EWSAdapter.war ]; then
    ews_health_value=$(curl -s http://localhost:8080/EWSAdapter/appversion.html)
    checkHealth ${ews_health_value} "EWS_Adapter"
fi

if [ -f ptcadapter.war ]; then
    ptc_health_value=$(curl -s http://localhost:8080/ptcadapter/appversion.html)
    checkHealth ${ptc_health_value} "PTC_Adapter"
fi

if [ -f CdmAdapter.war ]; then
    cdm_health_value=$(curl -s http://localhost:8080/CdmAdapter/appversion.html)
    checkHealth ${cdm_health_value} "CDM_Adapter"
fi


echo "***** HEALTH CHECK DONE *****"
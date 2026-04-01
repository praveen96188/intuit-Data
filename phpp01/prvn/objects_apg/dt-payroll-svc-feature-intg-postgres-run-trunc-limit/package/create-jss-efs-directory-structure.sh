#!/bin/bash

echo "Starting creation of JSS EFS Directories inside Pod"

#Changing ownership of /apps/batch/flux/work/ directory to appuser
sudo chown appuser:appuser /apps/batch/flux/work/

#ade/results/request, archive, backup, beds, email-error-logs,
#messages, mount, mtl/work, paycycle/eftps,
#RAFResponse/archive, receive, send, temp
mkdir -p -v /apps/batch/flux/work/{ade/results/request,archive,backup,beds,email-error-logs,messages,mount,mtl/work,paycycle/eftps,RAFResponse/archive,receive,send,temp}

#ACH Enrollment
mkdir -p -v /apps/batch/flux/work/achenrollment/{archive,error,receive,send,stage}

#BRM and BRMS3
mkdir -p -v /apps/batch/flux/work/brm/{archive,error,recv,stage}
mkdir -p -v /apps/batch/flux/work/brms3/{archive,error,recv,stage}

#EDI
mkdir -p -v /apps/batch/flux/work/edi/{archive,as400,error,stage,van,vanhold}

#EFTPS
mkdir -p -v /apps/batch/flux/work/eftps/{archive,as400,error,stage,support,tfa}

#FSET
mkdir -p -v /apps/batch/flux/work/fset/{archive,error,receive,send,stage}

echo "Completed creation of JSS EFS Directories inside Pod"

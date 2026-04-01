cp /u01/ogg/scripts/orcl /l/orcl
. /l/orcl
cp /u01/ogg/scripts/tnsnames.ora $ORACLE_HOME/network/admin/


if [ -d /dev/shm/goldengate/db ]; then
        cp /u01/tmp/password_file /dev/shm/goldengate/db
else
        mkdir -p /dev/shm/goldengate/db
        cp /u01/tmp/password_file /dev/shm/goldengate/db
fi

if [ -d /dev/shm/goldengate/$RDS_SID ]; then
        cp /u01/tmp/dircrd_cwallet.sso /dev/shm/goldengate/$RDS_SID/dircrd/cwallet.sso
        cp /u01/tmp/dirwlt_cwallet.sso /dev/shm/goldengate/$RDS_SID/dirwlt/cwallet.sso
else
        mkdir -p /dev/shm/goldengate/$RDS_SID
        cp /u01/tmp/dircrd_cwallet.sso /dev/shm/goldengate/$RDS_SID/dircrd/cwallet.sso
        cp /u01/tmp/dirwlt_cwallet.sso /dev/shm/goldengate/$RDS_SID/dirwlt/cwallet.sso
fi

## start weblogic
cd $MW_HOME/user_projects/domains/base_domain/bin/
nohup ./startWebLogic.sh > startWebLogic.log 2>&1 &

sleep 120
## start veridata server
cd $MW_HOME/user_projects/domains/base_domain/veridata/bin
nohup ./veridataServer.sh start > veridataServer.log 2>&1 &


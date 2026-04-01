

gghup for west:
ggpsppf501.sbg-psp-ppd.a.intuit.com

gghub for east:
 ggpspe2eue.sbg-psp-ppd.a.intuit.com 

--to check status 
info all

--to check extract 
view param extractname

--credentials info

INFO CREDENTIALSTORE

--abended to start
ALTER EXTRACT EPSPWPD1, BEGIN NOW

 GGS/GGT password for PDS : fv9YU#4yb

 GGS/''pspe2euw.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com

 GGS/"fy9YU4#yb"@'pspe2euw.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:1521/pspe2euw'


 intuadmin/"changeme"@'pspe2euw.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:1521/pspe2euw'


start replicat rpspwpd1, atcsn 8376682036107



select to_char(resetlogs_change#) from v$database_incarnation where incarnation# = 1;



 SELECT username, account_status
FROM dba_users  where username in ('GGS','GGT');


fy9YU4#yb


ALTER CREDENTIALSTORE DELETE USER ggsource_pds


ALTER CREDENTIALSTORE DELETE USER ggtarget_pds


ALTER CREDENTIALSTORE ADD USER ggs@pspe2eme PASSWORD fy9YU4#yb ALIAS ggsource_pds

ALTER CREDENTIALSTORE ADD USER ggt@pspe2eme PASSWORD fy9YU4#yb ALIAS ggtarget_pds



DBLOGIN USERIDALIAS ggsource_pds

pspe2eme =
  (description =
    (address_list =
      (address = (protocol = tcp)(host = pspe2eme.cxph5rnzesrt.us-east-2.rds.amazonaws.com)(port = 1521))
    )
    (connect_data =
      (sid = pspe2eme)
    )
  )

monitor_ggserr.sh, monitor_gg_sync_mon.sh, gg_status.sh


cp /dev/shm/goldengate/psppf501/password_file  /u01/tmp/password_file 

scp -i id_rsa -o "proxycommand ssh -W %h:%p -i id_rsa pnarlagalla@ec2-3-133-253-70.us-east-2.compute.amazonaws.com" cwallet.sso pnarlagalla@ggpspe2eue.sbg-psp-ppd.a.intuit.com:/tmp/





scp -i id_rsa -o "proxycommand ssh -W %h:%p -i id_rsa pnarlagalla@ec2-52-38-34-141.us-west-2.compute.amazonaws.com" oracle@ggpsppf501.sbg-psp-ppd.a.intuit.com:/u01/tmp/cwallet.sso  cwallet.sso 


Copy file from local to machine:
scp -i id_rsa -o "proxycommand ssh -W %h:%p -i id_rsa aagarwal25@ec2-52-38-34-141.us-west-2.compute.amazonaws.com" dbsolo_nojre.sh aagarwal25@ggppspdbs2.sbg-psp-ppd.a.intuit.com:/tmp/ 
Copy file from machine to local:
scp -i id_rsa -o "proxycommand ssh -W %h:%p -i id_rsa aagarwal25@ec2-52-38-34-141.us-west-2.compute.amazonaws.com" aagarwal25@ggppspdbs2.sbg-psp-ppd.a.intuit.com:/u01/ogg/scripts/dbsolo5/results/results.html results.html 





## Goldengate monitoring
#*/30 * * * * cd /u01/ogg/scripts; ./monitor_ggserr.sh aws 1>/dev/null 2>&1
#*/30 * * * * cd /u01/ogg/scripts; ./monitor_gg_sync_mon.sh 10 pite2euw  PPSPEPD1 1>/dev/null 2>&1

## collect GG lag data
#*/5 * * * * /u01/ogg/scripts/gg_status.sh > /dev/null 2>&1



ops_user /"TLx#3Jp)zv"@'pspe2eme.cxph5rnzesrt.us-east-2.rds.amazonaws.com:1521/pspe2eme'


west GG gghub:

bation: ec2-52-38-34-141.us-west-2.compute.amazonaws.com 
GGhub:  ggpsppf501.sbg-psp-ppd.a.intuit.com 


West Oracle RDS:

Host: pite2euw.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com
port: 1521
service: pite2euw

east gghub:
bation: ec2-3-133-253-70.us-east-2.compute.amazonaws.com
GGhub:ggpspe2eue.sbg-psp-ppd.a.intuit.com 


East Oracle RDS:

Host: pspe2eme.cxph5rnzesrt.us-east-2.rds.amazonaws.com
port: 1521
service: pspe2eme
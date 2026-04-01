aws secretsmanager create-secret \
    --name oracle_licensing_ppspsp01 \
    --secret-string file://ppspsp01.json

aws secretsmanager create-secret \
    --name oracle_licensing_pspparhdg \
    --secret-string file://pspparhdg.json


aws secretsmanager create-secret \
    --name oracle_licensing_psphdg01 \
    --secret-string file://psphdg01.json


aws secretsmanager create-secret \
    --name oracle_licensing_psphdg02 \
    --secret-string file://psphdg02.json

psphpp02,pspparhdg,psphdg01,psphdg02,pspparhs,psphpp05,pspvd1pt


# /tmp/secret.json

{
  "engine": "oracle-ee",
  "host": "ppspsp01.cxph5rnzesrt.us-east-2.rds.amazonaws.com",
  "username": "INTU_LICENSING",
  "password": "INp3zu#JA7M5)p",
  "dbname": "PPSPHP01",
  "port": "1521"
}


{
  "engine": "oracle-ee",
  "host": "pspparhdg.cjls0bohfgpq.us-west-2.rds.amazonaws.com",
  "username": "INTU_LICENSING",
  "password": "INp3zu#JA7M5)p",
  "dbname": "PSPPARHS",
  "port": "2632"
}



{
  "engine": "oracle-ee",
  "host": "psphdg01.cjls0bohfgpq.us-west-2.rds.amazonaws.com",
  "username": "INTU_LICENSING",
  "password": "INp3zu#JA7M5)p",
  "dbname": "PSPHPP02",
  "port": "1521"
}

{
  "engine": "oracle-ee",
  "host": "psphdg02.cjls0bohfgpq.us-west-2.rds.amazonaws.com",
  "username": "INTU_LICENSING",
  "password": "INp3zu#JA7M5)p",
  "dbname": "PSPHPP02",
  "port": "1521"
}
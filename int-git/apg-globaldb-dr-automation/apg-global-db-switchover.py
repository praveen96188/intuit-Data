import time

import boto3, json
import logging
from botocore.exceptions import ClientError
import os, sys, pprint
import pg8000
from datetime import datetime, timezone
import postgres_utils

os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

logger = logging.getLogger()
logger.setLevel(logging.INFO)

rdsclient_ue2 = boto3.client('rds', region_name='us-east-2')
rdsclient = boto3.client('rds', region_name=os.environ['AWS_DEFAULT_REGION'])


def lambda_handler(event, context):
    responseData = {}
    if len(event.keys()) < 6 or len(event.keys()) > 7:
        print(
            'Usage: python3 apg-global-db-switchover.py <AppEnv> <db_type> <global_cluster_name> <from_region> <to_region> <switch_option>')
        print("      where <AppEnv> should be sys, pds, arc, prod")
        print("      <db_type>  should be monolith, audit")
        print("      <global_cluster_name> is the Aurora Global Cluster")
        print("      <From_Region> should be us-west-2, us-east-2")
        print("      <To_Region> should be us-west-2, us-east-2")
        print("      <switch_option> should be switchover, failover, prechecks")
        print('  Example:')
        print(
            '  python3 apg-global-db-switchover.py pds monolith ppsp-pds-db-global us-west-2 us-east-2 switchover')
        print('')
        sys.exit('[Error] Missing or invalid parameters')
    else:
        app_env = event['AppEnv']
        if app_env == 'sys' or app_env == 'pds' or app_env == 'arc' or app_env == 'prod' or app_env == 'test':
            print("Application Environment provided: ", app_env)
        else:
            print("Incorrect environment name provided. Please provide from sys, pds, arc, prod")
        db_type = event['db_type']
        if db_type == 'monolith' or db_type == 'audit':
            print("Database Type provided: ", db_type)
        else:
            print("Not a valid database type. Please provide valid db type either monolith or audit")
            exit()

        global_cluster_name = event['global_cluster_name']
        g_clu_info = getglobalclusterdetails(global_cluster_name)
        p_clu_info = getclusterdetails(g_clu_info['prim_clu_name'], g_clu_info['prim_region'])
        s_clu_info = getclusterdetails(g_clu_info['sec_clu_name'], g_clu_info['sec_region'])
        from_region = event['from_region']
        if app_env == 'pds' and db_type == 'monolith':
            db_name = "ppdspg02"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'pds' and db_type == 'audit':
            db_name = "pdsibobdb"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'sys' and db_type == 'monolith':
            db_name = "psyspg01"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'sys' and db_type == 'audit':
            db_name = "sysibobdb"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'arc' and db_type == 'monolith':
            db_name = "psppdarc"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'test' and db_type == 'monolith':
            db_name = "tempdb"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'prod' and db_type == 'monolith':
            db_name = "pspapg02"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        elif app_env == 'prod' and db_type == 'audit':
            db_name = "prodapgib"
            print("Database name is : " + db_name + " for " + app_env + " , " + db_type)
        else:
            print("Not a valid database name")
            exit()
        if from_region == 'us-west-2' or from_region == 'us-east-2':
            print("Source region provided:" + from_region)
        else:
            print("Please provide valid value for from_region")
            exit()
        to_region = event['to_region']
        if to_region == 'us-west-2' or to_region == 'us-east-2':
            print("Target region provided:" + to_region)
        else:
            print("Please provide valid value for to_region")
            exit()
        if from_region == to_region:
            print("from_region and to_region cannot be same. Please provide correct values for both")
            exit()
        switch_option = event['switch_option']
        if switch_option == 'switchover':
            switchover_status = {}
            switchover_status['data'] = perform_switchover(app_env, global_cluster_name, g_clu_info['prim_clu_name'],
                                                           g_clu_info['sec_clu_name'], g_clu_info['sec_clu_arn'],
                                                           from_region, to_region, db_type, db_name)
            return switchover_status
        elif switch_option == 'failover':
            if len(event.keys()) != 7:
                print(
                    'Usage: python3 apg-global-db-switchover.py <AppEnv> <db_type> <global_cluster_name> <from_region> <to_region> <switch_option> <to_cluster>')
                print("      where <AppEnv> should be sys, pds, arc, prod")
                print("      <db_type>  should be monolith, audit")
                print("      <global_cluster_name> is the Aurora Global Cluster")
                print("      <From_Region> should be us-west-2, us-east-2")
                print("      <To_Region> should be us-west-2, us-east-2")
                print("      <switch_option> should be switchover, failover, prechecks")
                print("      <to_cluster> should be target cluster name for failover")
                print('  Example:')
                print(
                    '  python3 apg-global-db-switchover.py pds monolith ppsp-pds-db-global us-west-2 us-east-2 failover ppsp-pds-uw02dr')
                print('')
                sys.exit('[Error] Missing or invalid parameters')
            else:
                failover_status = {}
                to_cluster = event['to_cluster']
                to_cluster_info = getclusterdetails(to_cluster, to_region)
                to_cluster_arn = to_cluster_info['cluarn']
                failover_status['data'] = perform_failover(app_env, global_cluster_name, to_cluster, to_cluster_arn, to_region)
                return failover_status
        elif switch_option == 'prechecks':
            print("Performing pre-check for aurora global cluster " + global_cluster_name)
            switchover_prechecks_status = {}
            switchover_prechecks_status['data'] = switchover_prechecks(app_env, db_type, global_cluster_name,
                                                                       from_region, to_region, db_name)
            return switchover_prechecks_status
        elif switch_option == 'updateroute53':
            print("Calling update_route53_record function...")
            update_route53_record(app_env, to_region)
        else:
            print("Please enter valid operation name from switchover, failover, prechecks")


def getclusterdetails(cluster_name, region):
    if region == 'us-east-2':
        response = rdsclient_ue2.describe_db_clusters(
            DBClusterIdentifier=cluster_name
        )
    else:
        response = rdsclient.describe_db_clusters(
            DBClusterIdentifier=cluster_name
        )
    logger.info("Getting DB Cluster details for " + cluster_name)
    instance_details = response['DBClusters'][0]['DBClusterMembers']
    # print(instance_details)
    clu_info = dict()
    clu_info['status'] = response['DBClusters'][0]['Status']
    clu_info['endpoint'] = response['DBClusters'][0]['Endpoint']
    clu_info['cluarn'] = response['DBClusters'][0]['DBClusterArn']
    # # clu_status = cludetails['DBClusters'][0]['Status']
    # print(clu_info)
    # for dic in cludetails:
    #     print(dic)
    #     print(dic['DBInstanceIdentifier'])
    #     print(dic['IsClusterWriter'])
    #     if str(dic['IsClusterWriter']) == "True":
    #         result = 'This is Primary Cluster'
    #         print(result)
    #         print('')
    #     elif str(dic['IsClusterWriter']) == 'False':
    #         result = 'This is Secondary Cluster'
    #         print(result)
    return clu_info


def getglobalclusterdetails(global_cluster_name):
    logger.info("Getting Global DB Cluster details")
    try:
        response = rdsclient.describe_global_clusters(
            GlobalClusterIdentifier=global_cluster_name
        )
    except rdsclient.exceptions.GlobalClusterNotFoundFault as err:
        print(global_cluster_name + " is not an Aurora Global Cluster. Please check.")
        exit()
    gl_clu_members = response['GlobalClusters'][0]['GlobalClusterMembers']
    # print(gl_clu_members)
    gc = dict()
    for dic in gl_clu_members:
        # print(dic)
        # print(dic['DBClusterArn'])
        # print(dic['IsWriter'])
        if str(dic['IsWriter']) == "True":
            prim_clu_arn = dic['DBClusterArn']
            dbcluarnlist = dic['DBClusterArn'].split(":")
            prim_region = dbcluarnlist[3]
            # print(prim_region)
            # print(dbcluarnlist)
            prim_clu_name = dbcluarnlist[6]
            result = prim_clu_name + ' is Primary Cluster'
            # print(result)
            gc['prim_region'] = dbcluarnlist[3]
            gc['prim_clu_name'] = dbcluarnlist[6]
            gc['prim_clu_arn'] = dic['DBClusterArn']
            # print("primary cluster ARN:" + gc['prim_clu_arn'])
            print('')
        elif str(dic['IsWriter']) == 'False':
            sec_clu_arn = dic['DBClusterArn']
            dbcluarnlist = dic['DBClusterArn'].split(":")
            sec_region = dbcluarnlist[3]
            # print(sec_region)
            sec_clu_name = dbcluarnlist[6]
            result = sec_clu_name + ' is Secondary Cluster'
            # sc = dict()
            gc['sec_region'] = dbcluarnlist[3]
            gc['sec_clu_name'] = dbcluarnlist[6]
            gc['sec_clu_arn'] = dic['DBClusterArn']
            # print("secondary cluster ARN:" + gc['sec_clu_arn'])
            # print(gc)
    return gc


def apg_gl_db_lag_check(app_env, db_type, to_clu_endpoint, db_name, from_region, to_region):
    credential = {}
    if app_env == 'arc':
        secret_name = "switch-ppspppdarcSecret"
    elif app_env == 'test':
        secret_name = "test-mon-user"
    elif app_env == 'pds' and db_type == 'monolith':
        secret_name = "ppsppdsuw02Secret0353C786-M6hnVOZZFL7E"
    elif app_env == 'pds' and db_type == 'audit':
        secret_name = "ppsppdsdbSecret5672512C-9Fy2Z7rZz0wZ"
    elif app_env == 'prod' and db_type == 'monolith':
        secret_name = "monolith-mon-user"
    elif app_env == 'prod' and db_type == 'audit':
        secret_name = "audit-mon-user"
    if to_region == 'us-east-2':
        client_sm = boto3.client('secretsmanager', region_name=to_region)
    else:
        client_sm = boto3.client('secretsmanager', region_name=from_region)
    get_secret_value_response = client_sm.get_secret_value(
        SecretId=secret_name
    )
    secret = json.loads(get_secret_value_response['SecretString'])
    credential['USERNAME'] = secret['username']
    credential['PASSWORD'] = secret['password']

    # connect to db
    # ENDPOINT = "127.0.0.1"
    ENDPOINT = to_clu_endpoint
    PORT = 5432
    # PORT = "11532"
    USER = credential['USERNAME']
    PWD = credential['PASSWORD']
    DBNAME = db_name

    conn = None
    try:
        print('Connecting to the Aurora PostgreSQL database...', to_clu_endpoint)
        conn = pg8000.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USER, password=PWD,
                              ssl_context=True)
        cur = conn.cursor()
        sql = "select durability_lag_in_msec/1000 from (SELECT CASE WHEN '-1' = durability_lag_in_msec THEN 'Primary' ELSE 'Secondary' END AS global_role,durability_lag_in_msec,rpo_lag_in_msec FROM aurora_global_db_status()) a where global_role='Secondary'"

        cur.execute(sql)
        conn.commit()
        query_results = cur.fetchall()
        # print(query_results)
        for r in query_results:
            rep_lag = r[0]
            # print(rep_lag)
        cur.close()
        print("Aurora Global Database " + DBNAME + " replication Lag is ", rep_lag, "seconds")
        return rep_lag

    except Exception as e:
        print("Database connection failed due to {}".format(e))
    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')


def switchover_prechecks(app_env, db_type, global_cluster_name, from_region, to_region, db_name):
    resp_switchover_prechecks = {}
    resp_switchover_prechecks['prechecks_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": APG Cluster Prechecks Started..."
    print(" ")
    print("#####################################")
    print("Switchover Pre-checks: APG Cluster Status Check")
    print("#####################################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting APG Cluster Status Check ...")
    gl_clu_info = getglobalclusterdetails(global_cluster_name)
    # print(gl_clu_info)
    prim_clu_info = getclusterdetails(gl_clu_info['prim_clu_name'], gl_clu_info['prim_region'])
    # print(prim_clu_info)
    sec_clu_info = getclusterdetails(gl_clu_info['sec_clu_name'], gl_clu_info['sec_region'])
    # print(sec_clu_info)
    # Both Primary & DR APG Clusters should be in Available state for Switchover to work
    if prim_clu_info['status'] == 'available':
        print(gl_clu_info['prim_clu_name'] + ' is Primary Cluster in ' + prim_clu_info['status'] + ' state in ' +
              gl_clu_info['prim_region'])
    else:
        print(gl_clu_info['prim_clu_name'] + "primary cluster is not in available state. Please check. Exiting...")
        exit()
    if sec_clu_info['status'] == 'available':
        print(gl_clu_info['sec_clu_name'] + ' is Secondary Cluster in ' + sec_clu_info['status'] + ' state in ' +
              gl_clu_info['sec_region'])
    else:
        print(gl_clu_info['sec_clu_name'] + "secondary cluster is not in available state. Please check. Exiting...")
        exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": APG Cluster Status Check Completed")
    resp_switchover_prechecks['prechecks_end'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": APG Cluster Prechecks Completed..."

    # Both Primary & DR Cluster databases should be in the correct DB mode before the Switchover
    resp_switchover_prechecks['db_prechecks_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Database Mode Check Started..."
    print(" ")
    print("###############################")
    print("Switchover Pre-checks: Database Mode Check")
    print("###############################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Database Mode Check ...")
    if gl_clu_info['prim_region'] == from_region:
        print("Provided " + from_region + " source region is correct. Proceeding with next steps..")
    else:
        print("Provided source region is not correct as " + gl_clu_info[
            'prim_clu_name'] + " is not primary in " + from_region + ". Please check")
        exit()
    if gl_clu_info['sec_region'] == to_region:
        print("Provided " + to_region + " target region is correct. Proceeding with next steps..")
    else:
        print("Provided target region is not correct as " + gl_clu_info[
            'sec_clu_name'] + "is not secondary in " + to_region + ". Please check")
        exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Database Mode Check Completed")
    resp_switchover_prechecks['db_prechecks_end'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Database Mode Check Completed..."

    # DR lag should be < 5 mins ( or less than the given min_lag_seconds ) before the switchover starts
    resp_switchover_prechecks['lag_prechecks_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Replication Lag Check Started..."
    print(" ")
    print("#############################")
    print("DBPreCheck: DR Lag Check")
    print("#############################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Aurora Global Replication Lag Check ...")
    retry_count = 0
    while True:
        gl_rep_lag_sec = apg_gl_db_lag_check(app_env, db_type, sec_clu_info['endpoint'], db_name, from_region,
                                             to_region)
        if gl_rep_lag_sec > 120:
            print(datetime.now().strftime("%Y-%m-%d %H:%M:%S"), " Cluster=",
                  sec_clu_info['endpoint'] + " having lag of " + gl_rep_lag_sec + "seconds")
        else:
            break
        retry_count += 1
        time.sleep(10)
        if retry_count > 30:
            print("Global Replication lag is over " + str(
                gl_rep_lag_sec) + " seconds for last 5 mins. Switchover has not started. Please check ")
            exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Aurora Global Replication Lag Check Completed.")
    resp_switchover_prechecks['lag_prechecks_end'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Replication Lag Check Completed..."
    return resp_switchover_prechecks


def lock_unlock_kill_app_users(app_env, from_cluster, to_cluster, from_region, to_region, db_type, db_name,
                               lock_option):
    credential = {}
    if app_env == 'arc':
        secret_name = "switch-ppspppdarcSecret"
    elif app_env == 'test':
        secret_name = "test-mon-user"
    elif app_env == 'pds' and db_type == 'monolith':
        secret_name = "ppsppdsuw02Secret0353C786-M6hnVOZZFL7E"
    elif app_env == 'pds' and db_type == 'audit':
        secret_name = "ppsppdsdbSecret5672512C-9Fy2Z7rZz0wZ"
    elif app_env == 'prod' and db_type == 'monolith':
        secret_name = "switch-ppspppdarcSecret"
    elif app_env == 'prod' and db_type == 'audit':
        secret_name = "switch-ppspppdarcSecret"
    if to_region == 'us-east-2':
        client_sm = boto3.client('secretsmanager', region_name=to_region)
    else:
        client_sm = boto3.client('secretsmanager', region_name=from_region)
    get_secret_value_response = client_sm.get_secret_value(
        SecretId=secret_name
    )
    secret = json.loads(get_secret_value_response['SecretString'])
    credential['USERNAME'] = secret['username']
    credential['PASSWORD'] = secret['password']

    # gl_clu_info = getglobalclusterdetails(global_cluster_name)
    # prim_clu_info = getclusterdetails(gl_clu_info['prim_clu_name'], gl_clu_info['prim_region'])
    # sec_clu_info = getclusterdetails(gl_clu_info['sec_clu_name'], gl_clu_info['sec_region'])
    from_clu_info = getclusterdetails(from_cluster, from_region)
    to_clu_info = getclusterdetails(to_cluster, to_region)
    from_clu_endpoint = from_clu_info['endpoint']
    to_clu_endpoint = to_clu_info['endpoint']
    # connect to db
    # ENDPOINT = "127.0.0.1"
    ENDPOINT = to_clu_endpoint
    PORT = 5432
    # PORT = "11532"
    USER = credential['USERNAME']
    PWD = credential['PASSWORD']
    DBNAME = db_name

    if (app_env == 'pds' and db_type == 'monolith') or (app_env == 'prod' and db_type == 'monolith'):
        mon_app_user_lock_sqls = ["alter user pspapp with NOLOGIN",
                                  "alter user pspbatch_rw_user with NOLOGIN",
                                  "alter user pspbatch_ro_user with NOLOGIN",
                                  "alter user pspread with NOLOGIN",
                                  "alter user psprjf with NOLOGIN",
                                  "alter user psp_payroll_dm with NOLOGIN"]
        mon_app_user_unlock_sqls = ["alter user pspapp with LOGIN",
                                    "alter user pspbatch_rw_user with LOGIN",
                                    "alter user pspbatch_ro_user with LOGIN",
                                    "alter user pspread with LOGIN",
                                    "alter user psprjf with LOGIN",
                                    "alter user psp_payroll_dm with LOGIN"]
        mon_app_user_kill_sql = ["SELECT pg_terminate_backend(pid) FROM pg_stat_activity \
                                    WHERE usename in ('pspapp','pspbatch_rw_user','pspbatch_ro_user','pspread','psprjf','psp_payroll_dm')"]
        mon_chk_db_conn_sql = ["select count(*) from pg_stat_activity \
                                WHERE usename in ('pspapp','pspbatch_rw_user','pspbatch_ro_user','pspread','psprjf','psp_payroll_dm')"]
    elif app_env == 'prod' and db_type == 'audit':
        aud_app_user_lock_sqls = ["alter user ibob_prod_pspapp with NOLOGIN"]
        aud_app_user_unlock_sqls = ["alter user ibob_prod_pspapp with LOGIN"]
        aud_app_user_kill_sql = [
            "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('ibob_prod_pspapp')"]
        aud_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('ibob_prod_pspapp')"]
    elif app_env == 'pds' and db_type == 'audit':
        aud_app_user_lock_sqls = ["alter user ibob_pds_pspapp with NOLOGIN"]
        aud_app_user_unlock_sqls = ["alter user ibob_pds_pspapp with LOGIN"]
        aud_app_user_kill_sql = [
            "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('ibob_pds_pspapp')"]
        aud_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('ibob_pds_pspapp')"]
    elif app_env == 'arc' and db_type == 'monolith':
        mon_app_user_lock_sqls = ["alter user psparcapp with NOLOGIN"]
        mon_app_user_unlock_sqls = ["alter user psparcapp with LOGIN"]
        mon_app_user_kill_sql = [
            "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('psparcapp')"]
        mon_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('psparcapp')"]

    else:
        print("Invalid db type.Exiting...")
        exit()

    conn = None
    try:
        if lock_option == 'lock':
            print('Connecting to the Aurora PostgreSQL database...: ' + db_name + " on " + from_cluster)
            conn = pg8000.connect(host=from_clu_endpoint, port=PORT, database=DBNAME, user=USER, password=PWD,
                                  ssl_context=True)
            # conn = psycopg2.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USER, password=PWD,
            #                         sslmode='require')
            cur = conn.cursor()
            print(" ")
            print("##################################################")
            print("Locking/Checking/Killing DB connections  before the switchover ")
            print("##################################################")
            print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                  ": Starting to lock/check/kill DB connections (if any) ...")
            if db_type == 'monolith':
                for statement in mon_app_user_lock_sqls:
                    cur.execute(statement)
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Locked Application Users")
                retry_kill_cnt = 0
                while True:
                    for statement in mon_app_user_kill_sql:
                        cur.execute(statement)
                    cur.execute(mon_chk_db_conn_sql)
                    query_results = cur.fetchall()
                    for r in query_results:
                        mon_db_conn_cnt = r[0]
                    if mon_db_conn_cnt == 0:
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Killed DB Connections")
                        break
                    retry_kill_cnt += 1
                    time.sleep(10)
                    if retry_kill_cnt > 30:
                        print("There is issue while killing db connections. Please kill manually. Exiting....")
                        exit()
            elif db_type == 'audit':
                for statement in aud_app_user_lock_sqls:
                    cur.execute(statement)
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Locked Application Users")
                retry_kill_cnt = 0
                while True:
                    for statement in aud_app_user_kill_sql:
                        cur.execute(statement)
                    cur.execute(aud_chk_db_conn_sql)
                    query_results = cur.fetchall()
                    for r in query_results:
                        aud_db_conn_cnt = r[0]
                    if aud_db_conn_cnt == 0:
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Killed DB Connections")
                        break
                    retry_kill_cnt += 1
                    time.sleep(10)
                    if retry_kill_cnt > 30:
                        print("There is issue while killing db connections. Please kill manually. Exiting....")
                        exit()
            else:
                print("Invalid db type.Exiting...")
                exit()
        elif lock_option == 'unlock':
            print('Connecting to the Aurora PostgreSQL database...: ' + db_name + " on " + to_cluster)
            conn = pg8000.connect(host=to_clu_endpoint, port=PORT, database=DBNAME, user=USER, password=PWD,
                                  ssl_context=True)
            # conn = psycopg2.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USER, password=PWD,
            #                         sslmode='require')
            cur = conn.cursor()
            print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Unlocking Application Users")
            if db_type == 'monolith':
                for statement in mon_app_user_unlock_sqls:
                    cur.execute(statement)
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Unlocked Application Users")
            elif db_type == 'audit':
                for statement in aud_app_user_unlock_sqls:
                    cur.execute(statement)
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Unlocked Application Users")
            else:
                print("Invalid db type.Exiting...")
                exit()
        else:
            print("Invalid lock option. Please check. Exiting...")
            exit()
        # cur.execute(sql)
        conn.commit()
        cur.close()
        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
              ": Completed lock/check/kill DB connections.")
    except Exception as e:
        print("Database connection failed due to {}".format(e))
    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')


def perform_switchover(app_env, global_cluster_name, from_cluster, to_cluster, to_cluster_arn, from_region, to_region,
                       db_type, db_name):
    switchover_prechecks(app_env, db_type, global_cluster_name, from_region, to_region, db_name)
    # commenting temporarily for preprod switchover testing
    # lock_unlock_kill_app_users(app_env,from_cluster,to_cluster,from_region,to_region, db_type , db_name, 'lock')
    resp_switch_action = {}
    resp_switch_action['switch_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": DB Switchover Started..."
    print(" ")
    print("#####################")
    print(
        "Starting Switchover of Aurora Global Cluster " + global_cluster_name + " to target cluster ARN: " + to_cluster_arn)
    print("####################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting DB Switchover ...")
    start_time_utc = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    print("start time in UTC: " + start_time_utc)
    if to_region == 'us-east-2':
        response = rdsclient_ue2.switchover_global_cluster(
            GlobalClusterIdentifier=global_cluster_name,
            TargetDbClusterIdentifier=to_cluster_arn
        )
    else:
        response = rdsclient.switchover_global_cluster(
            GlobalClusterIdentifier=global_cluster_name,
            TargetDbClusterIdentifier=to_cluster_arn
        )
    time.sleep(120)
    # Checking switchover status every 20 secs
    while True:
        print(" ")
        time.sleep(20)
        to_clu_status = getclusterdetails(to_cluster, to_region)
        from_clu_status = getclusterdetails(from_cluster, from_region)
        if to_clu_status['status'] == 'available' and from_clu_status['status'] == 'available':
            print(
                "Both clusters are in available state. Checking switchover events to confirm switchover completion...")
            break
        else:
            print(from_cluster + " cluster is in " + from_clu_status['status'] + " state.")
            print(to_cluster + " cluster is in " + to_clu_status['status'] + " state.")
    if to_region == 'us-east-2':
        while True:
            response_ue2 = rdsclient_ue2.describe_events(
                SourceIdentifier=to_cluster,
                SourceType='db-cluster',
                StartTime=start_time_utc
            )
            if not response_ue2['Events']:
                print("Waiting for describe events output from rds client..")
            else:
                print(response_ue2['Events'])
                exp_resp = 'Global switchover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
                print("Expected output for switching to us-east-2 region : " + exp_resp)
                for dic in response_ue2['Events']:
                    if dic['Message'] == exp_resp:
                        print("New Primary Cluster " + to_cluster + " is " + to_clu_status['status'] + " state.")
                        print("New Secondary Cluster " + from_cluster + " is " + from_clu_status['status'] + " state.")
                        # print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        #       ": Starting Unlocking Application users")
                        # lock_unlock_kill_app_users(app_env, from_cluster, to_cluster, from_region, to_region, db_type,
                        #                            db_name, 'unlock')
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Switchover Completed")
                        resp_switch_action['switch_end'] = "At ", datetime.now().strftime(
                            "%Y-%m-%d %H:%M:%S"), ": DB Switchover Completed..."
                        break
                    else:
                        print("")
                break
    else:
        while True:
            response_uw2 = rdsclient.describe_events(
                SourceIdentifier=to_cluster,
                SourceType='db-cluster',
                StartTime=start_time_utc
            )
            if not response_uw2['Events']:
                print("Waiting for describe events output from rds client..")
            else:
                print(response_uw2['Events'])
                exp_resp = 'Global switchover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
                print("Expected output for switching to us-west-2 region : " + exp_resp)
                # print(response['Events'])
                for dic in response_uw2['Events']:
                    if dic['Message'] == exp_resp:
                        print("New Primary Cluster " + to_cluster + " is " + to_clu_status['status'] + " state.")
                        print("New Secondary Cluster " + from_cluster + " is " + from_clu_status['status'] + " state.")
                        # print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        #       ": Starting Unlocking Application users")
                        # lock_unlock_kill_app_users(app_env, from_cluster, to_cluster, from_region, to_region, db_type,
                        #                            db_name, 'unlock')
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Switchover Completed")
                        resp_switch_action['switch_end'] = "At ", datetime.now().strftime(
                            "%Y-%m-%d %H:%M:%S"), ": DB Switchover Completed..."
                        break
                    else:
                        print("")
            break
    update_route53_record(app_env, to_region)
    return resp_switch_action

def perform_failover(app_env, global_cluster_name, to_cluster, to_cluster_arn, to_region):
    resp_failover_action = {}
    resp_failover_action['failover_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": DB Failover Started..."
    print(" ")
    print("#####################")
    print(
        "Starting Failover of Aurora Global Cluster ", global_cluster_name, " to target cluster ARN: ", to_cluster_arn)
    print("####################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting DB Failover ...")
    start_time_utc = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    print("start time in UTC: " + start_time_utc)
    # Perform failover of the global cluster
    if to_region == 'us-east-2':
        response = rdsclient_ue2.failover_global_cluster(
            GlobalClusterIdentifier=global_cluster_name,
            TargetDbClusterIdentifier=to_cluster_arn,
            AllowDataLoss=True
        )
    else:
        response = rdsclient.failover_global_cluster(
            GlobalClusterIdentifier=global_cluster_name,
            TargetDbClusterIdentifier=to_cluster_arn,
            AllowDataLoss=True
        )
    time.sleep(120)
    # Checking switchover status every 20 secs
    while True:
        print(" ")
        time.sleep(20)
        to_clu_status = getclusterdetails(to_cluster, to_region)
        if to_clu_status['status'] == 'available':
            print(
                "New Primary cluster", to_cluster, "is available in ", to_region, ". Checking switchover events to confirm switchover completion...")
            break
        else:
            print(to_cluster + " cluster is in " + to_clu_status['status'] + " state.")
    if to_region == 'us-east-2':
        while True:
            response_ue2 = rdsclient_ue2.describe_events(
                SourceIdentifier=to_cluster,
                SourceType='db-cluster',
                StartTime=start_time_utc
            )
            if not response_ue2['Events']:
                print("Waiting for describe events output from rds client..")
            else:
                print(response_ue2['Events'])
                exp_resp = 'Global failover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
                print("Expected output for switching to us-east-2 region : " + exp_resp)
                for dic in response_ue2['Events']:
                    if dic['Message'] == exp_resp:
                        print("New Primary Cluster " + to_cluster + " is in " + to_clu_status['status'] + " state.")
                        # print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        #       ": Starting Unlocking Application users")
                        # lock_unlock_kill_app_users(app_env, from_cluster, to_cluster, from_region, to_region, db_type,
                        #                            db_name, 'unlock')
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Failover Completed")
                        resp_failover_action['failover_end'] = "At ", datetime.now().strftime(
                            "%Y-%m-%d %H:%M:%S"), ": DB Failover Completed..."
                        break
                    else:
                        print("")
                break
    else:
        while True:
            response_uw2 = rdsclient.describe_events(
                SourceIdentifier=to_cluster,
                SourceType='db-cluster',
                StartTime=start_time_utc
            )
            if not response_uw2['Events']:
                print("Waiting for describe events output from rds client..")
            else:
                print(response_uw2['Events'])
                exp_resp = 'Global failover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
                print("Expected output for switching to us-west-2 region : " + exp_resp)
                # print(response['Events'])
                for dic in response_uw2['Events']:
                    if dic['Message'] == exp_resp:
                        print("New Primary Cluster " + to_cluster + " is " + to_clu_status['status'] + " state.")
                        # print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                        #       ": Starting Unlocking Application users")
                        # lock_unlock_kill_app_users(app_env, from_cluster, to_cluster, from_region, to_region, db_type,
                        #                            db_name, 'unlock')
                        print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Failover Completed")
                        resp_failover_action['failover_end'] = "At ", datetime.now().strftime(
                            "%Y-%m-%d %H:%M:%S"), ": DB Failover Completed..."
                        break
                    else:
                        print("")
            break
    update_route53_record(app_env, to_region)
    return resp_failover_action

def update_route53_record(app_env, to_region):
    resp_route53_upd = {}
    if app_env == 'pds':
        config = {'HostedZoneId': 'ZPMOD8K08QNEL',
                  'route53urls': [{'Name': 'ppsp-pds-uw02.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-pds-uw02dr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'ppsp-pds-uw02-ro.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-pds-uw02dr.cluster-ro-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-pds-uw02.cluster-ro-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'ppsp-pds-db.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-pds-dbdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-pds-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}}]}
    elif app_env == 'arc':
        config = {'HostedZoneId': 'ZPMOD8K08QNEL',
                  'route53urls': [{'Name': 'ppsp-ppd-arc.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-ppd-arcdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-ppd-arc.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'ppsp-ppd-arc-ro.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-ppd-arcdr.cluster-ro-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-ppd-arc.cluster-ro-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'ppsp-ppd-arc-rjf.sbg-psp-ppd.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'ppsp-ppd-arc-rjf.cluster-custom-cxph5rnzesrt.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'ppsp-ppd-arc-rjf.cluster-custom-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com'}}]}
    elif app_env == 'test':
        config = {'HostedZoneId': 'Z35GYKEF3QQE9L',
                  'route53urls': [{'Name': 'psp-prod-test.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-test.cluster-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-test-uw2.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-prod-test-ro.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-test.cluster-ro-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-test-uw2.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-prod-test-rjf.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-test-rjf.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-test-rjf.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}}]}
    elif app_env == 'prod':
        config = {'HostedZoneId': 'Z35GYKEF3QQE9L',
                  'route53urls': [{'Name': 'psp-apg-prod-02-datalake.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02-datalake.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02-datalake.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-02-dpc.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02-dpc.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02-dpc.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-02-rjf.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02-rjf.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02-rjf.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-02-rpt.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02-rpt.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02-rpt.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-02-vmp.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02-vmp.cluster-custom-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02-vmp.cluster-custom-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-02.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ue02.cluster-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-uw02.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-ibob-ro.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ibobdr.cluster-ro-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-ibob.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}},
                                  {'Name': 'psp-apg-prod-ibob.sbg-psp-prod.a.intuit.com',
                                   'Ttl': 60,
                                   'Type': 'CNAME',
                                   'Values': {
                                       'useast2': 'psp-prod-ibobdr.cluster-cerpnqmbpq9a.us-east-2.rds.amazonaws.com',
                                       'uswest2': 'psp-prod-ibob.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com'}}]}
    else:
        print("Incorrect environment name provided. Unable to proceed with route53 update...")
        exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Route53 update Started")
    resp_route53_upd['update_route53_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Route53 update Started.."
    dnsclient = boto3.client("route53")
    try:
        for record in config['route53urls']:
            gatewayurl = record['Name']
            entryType = record['Type']
            entryTtl = record['Ttl']
            usWestUrl = record['Values']['uswest2']
            usEastUrl = record['Values']['useast2']
            listOfResourceRecordSet = []
            if to_region == 'us-west-2':
                HostedZoneId = config['HostedZoneId']
                ResourceRecordSet = {
                    'Name': gatewayurl,
                    'Type': entryType,
                    'TTL': entryTtl,
                    'ResourceRecords': [
                        {
                            "Value": usWestUrl
                        }
                    ]
                }
                listOfResourceRecordSet.append(ResourceRecordSet)
                changeListForRoute53 = [{'Action': "UPSERT", 'ResourceRecordSet': resourceRecordSet} for
                                        resourceRecordSet in
                                        listOfResourceRecordSet]
                print(HostedZoneId + " , ", changeListForRoute53)
                response = dnsclient.change_resource_record_sets(HostedZoneId=HostedZoneId, ChangeBatch={
                    'Comment': "Creating/Updating route 53 records from config",
                    'Changes': changeListForRoute53
                })
            else:
                HostedZoneId = config['HostedZoneId']
                ResourceRecordSet = {
                    'Name': gatewayurl,
                    'Type': entryType,
                    'TTL': entryTtl,
                    'ResourceRecords': [
                        {
                            "Value": usEastUrl
                        }
                    ]
                }
                listOfResourceRecordSet.append(ResourceRecordSet)
                changeListForRoute53 = [{'Action': "UPSERT", 'ResourceRecordSet': resourceRecordSet} for
                                        resourceRecordSet in
                                        listOfResourceRecordSet]
                print(HostedZoneId + " , ", changeListForRoute53)
                response = dnsclient.change_resource_record_sets(HostedZoneId=HostedZoneId, ChangeBatch={
                    'Comment': "Creating/Updating route 53 records from config",
                    'Changes': changeListForRoute53
                })
    except Exception as e:
        print("Unable to create listOfResourceRecordSet while updating route53 due to ", str(e))
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Route53 update Completed")
    resp_route53_upd['update_route53_start'] = "At ", datetime.now().strftime(
        "%Y-%m-%d %H:%M:%S"), ": Route53 update Completed..."
    return resp_route53_upd

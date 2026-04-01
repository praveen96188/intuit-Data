import time

import boto3, json
import logging
from botocore.exceptions import ClientError
import os, sys, pprint
import psycopg2
from datetime import datetime, timezone

os.environ['AWS_PROFILE'] = "sbg-psp-ppd"
os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

logger = logging.getLogger()
logger.setLevel(logging.INFO)

rdsclient_ue2 = boto3.client('rds',region_name='us-east-2')
rdsclient = boto3.client('rds', region_name=os.environ['AWS_DEFAULT_REGION'])

def getclusterdetails(cluster_name,region):
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

def apg_gl_db_lag_check(to_clu_endpoint,db_name):
    # connect to db
    ENDPOINT = "127.0.0.1"
    # ENDPOINT = to_clu_endpoint
    # PORT = 5432
    PORT = "11532"
    USER = "intuadmin"
    DBNAME = db_name
    PWD = "cJ86WjLGoZheFG5NRo97gg.68lOu.--7"

    conn = None
    try:
        print('Connecting to the Aurora PostgreSQL database...')
        conn = psycopg2.connect(host=ENDPOINT, port=PORT, database=DBNAME, user=USER, password=PWD,
                                sslmode='require')
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
        print("Aurora Global Database " + DBNAME + " replication Lag is ", rep_lag,  "seconds")
        return rep_lag

    except Exception as e:
        print("Database connection failed due to {}".format(e))
    finally:
        if conn is not None:
            conn.close()
            print('Database connection closed.')

def switchover_prechecks(global_cluster_name,from_region,to_region,db_name):
    print(" ")
    print("#####################################")
    print("Switchover Pre-checks: APG Cluster Status Check")
    print("#####################################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),  ": Starting APG Cluster Status Check ...")
    gl_clu_info = getglobalclusterdetails(global_cluster_name)
    # print(gl_clu_info)
    prim_clu_info = getclusterdetails(gl_clu_info['prim_clu_name'],gl_clu_info['prim_region'])
    # print(prim_clu_info)
    sec_clu_info = getclusterdetails(gl_clu_info['sec_clu_name'],gl_clu_info['sec_region'])
    # print(sec_clu_info)
    # Both Primary & DR APG Clusters should be in Available state for Switchover to work
    if prim_clu_info['status'] == 'available':
        print(gl_clu_info['prim_clu_name'] + ' is Primary Cluster in ' + prim_clu_info['status'] + ' state in ' + gl_clu_info['prim_region'])
    else:
        print(gl_clu_info['prim_clu_name'] + "primary cluster is not in available state. Please check. Exiting...")
        exit()
    if sec_clu_info['status'] == 'available':
        print(gl_clu_info['sec_clu_name'] + ' is Secondary Cluster in ' + sec_clu_info['status'] + ' state in ' + gl_clu_info['sec_region'])
    else:
        print(gl_clu_info['sec_clu_name'] + "secondary cluster is not in available state. Please check. Exiting...")
        exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"),": APG Cluster Status Check Completed")
    # Both Primary & DR Cluster databases should be in the correct DB mode before the Switchover
    print(" ")
    print("###############################")
    print("Switchover Pre-checks: Database Mode Check")
    print("###############################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Database Mode Check ...")
    if gl_clu_info['prim_region'] == from_region:
        print("Provided " + from_region + " source region is correct. Proceeding with next steps..")
    else:
        print("Provided source region is not correct as " + gl_clu_info['prim_clu_name'] + " is not primary in " + from_region + ". Please check")
        exit()
    if gl_clu_info['sec_region'] == to_region:
        print("Provided " + to_region + " target region is correct. Proceeding with next steps..")
    else:
        print("Provided target region is not correct as " + gl_clu_info['sec_clu_name'] + "is not secondary in " + to_region + ". Please check")
        exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Database Mode Check Completed")

    # DR lag should be < 5 mins ( or less than the given min_lag_seconds ) before the switchover starts

    print(" ")
    print("#############################")
    print("DBPreCheck: DR Lag Check")
    print("#############################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Aurora Global Replication Lag Check ...")
    retry_count = 0
    while True:
        gl_rep_lag_sec = apg_gl_db_lag_check(sec_clu_info['endpoint'], db_name)
        if gl_rep_lag_sec > 120:
            print(datetime.now().strftime("%Y-%m-%d %H:%M:%S"), " Cluster=",
                  sec_clu_info['endpoint'] + " having lag of " + gl_rep_lag_sec + "seconds")
        else:
            break
        retry_count += 1
        time.sleep(10)
        if retry_count > 30:
            print("Global Replication lag is over " + str(gl_rep_lag_sec) + " seconds for last 5 mins. Switchover has not started. Please check ")
            exit()
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Aurora Global Replication Lag Check Completed.")

def lock_unlock_kill_app_users(app_env,from_cluster,to_cluster,from_region,to_region, db_type , db_name, lock_option):
    # gl_clu_info = getglobalclusterdetails(global_cluster_name)
    # prim_clu_info = getclusterdetails(gl_clu_info['prim_clu_name'], gl_clu_info['prim_region'])
    # sec_clu_info = getclusterdetails(gl_clu_info['sec_clu_name'], gl_clu_info['sec_region'])
    from_clu_info = getclusterdetails(from_cluster, from_region)
    to_clu_info = getclusterdetails(to_cluster, to_region)
    from_clu_endpoint = from_clu_info['endpoint']
    to_clu_endpoint = to_clu_info['endpoint']
    # connect to db
    # ENDPOINT = "127.0.0.1"
    # ENDPOINT = to_clu_endpoint
    # PORT = 5432
    PORT = "11532"
    USER = "intuadmin"
    DBNAME = db_name
    PWD = "cJ86WjLGoZheFG5NRo97gg.68lOu.--7"

    if (app_env == 'pds' and db_type == 'monolith') and (app_env == 'prod' and db_type == 'monolith'):
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
        aud_app_user_kill_sql = ["SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('ibob_prod_pspapp')"]
        aud_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('ibob_prod_pspapp')"]
    elif app_env == 'pds' and db_type == 'audit':
        aud_app_user_lock_sqls = ["alter user ibob_pds_pspapp with NOLOGIN"]
        aud_app_user_unlock_sqls = ["alter user ibob_pds_pspapp with LOGIN"]
        aud_app_user_kill_sql = ["SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('ibob_pds_pspapp')"]
        aud_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('ibob_pds_pspapp')"]
    elif app_env == 'arc' and db_type == 'monolith':
        mon_app_user_lock_sqls = ["alter user psparcapp with NOLOGIN"]
        mon_app_user_unlock_sqls = ["alter user psparcapp with LOGIN"]
        mon_app_user_kill_sql = ["SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename in ('psparcapp')"]
        mon_chk_db_conn_sql = ["select count(*) from pg_stat_activity WHERE usename in ('psparcapp')"]

    else:
        print("Invalid db type.Exiting...")
        exit()

    conn = None
    try:
        if lock_option == 'lock':
            print('Connecting to the Aurora PostgreSQL database...: ' + db_name + " on " + from_cluster)
            conn = psycopg2.connect(host=from_clu_endpoint, port=PORT, database=DBNAME, user=USER, password=PWD,
                                    sslmode='require')
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
            conn = psycopg2.connect(host=to_clu_endpoint, port=PORT, database=DBNAME, user=USER, password=PWD,
                                    sslmode='require')
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


def perform_switchover(app_env, global_cluster_name, from_cluster, to_cluster, to_cluster_arn, from_region, to_region, db_type, db_name):
    switchover_prechecks(global_cluster_name, from_region, to_region, db_name)
    lock_unlock_kill_app_users(app_env,from_cluster,to_cluster,from_region,to_region, db_type , db_name, 'lock')
    print(" ")
    print("#####################")
    print("Starting Switchover")
    print("####################")
    print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting DB Switchover ...")
    start_time_utc = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    print("start time in UTC: " + start_time_utc)
    response = rdsclient.switchover_global_cluster(
        GlobalClusterIdentifier=global_cluster_name,
        TargetDbClusterIdentifier=to_cluster_arn
    )
    # Checking switchover status every 15 secs
    while True:
        print(" ")
        time.sleep(20)
        to_clu_status = getclusterdetails(to_cluster,to_region)
        from_clu_status = getclusterdetails(from_cluster, from_region)
        if to_clu_status['status'] == 'available' and from_clu_status['status'] == 'available':
            print("Both clusters are in available state. Checking switchover events to confirm switchover completion...")
            break
        else:
            print(from_cluster + " cluster is in " + from_clu_status['status'] + " state.")
            print(to_cluster + " cluster is in " + to_clu_status['status'] + " state.")
    if to_region == 'us-east-2':
        response_ue2 = rdsclient_ue2.describe_events(
            SourceIdentifier=to_cluster,
            SourceType='db-cluster',
            StartTime=start_time_utc
        )
        exp_resp = 'Global switchover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
        print("Expected output for switching to us-east-2 region : " + exp_resp)
        for dic in response_ue2['Events']:
            if dic['Message'] == exp_resp:
                print("New Primary Cluster " + to_cluster + " is " + to_clu_status['status'] + " state.")
                print("New Secondary Cluster " + from_cluster + " is " + from_clu_status['status'] + " state.")
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Unlocking Application users")
                lock_unlock_kill_app_users(app_env,from_cluster,to_cluster,from_region,to_region, db_type, db_name, 'unlock')
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Switchover Completed")
                break
            else:
                print("")
    else:
        response_uw2 = rdsclient.describe_events(
            SourceIdentifier=to_cluster,
            SourceType='db-cluster',
            StartTime=start_time_utc
        )
        print(response_uw2)
        exp_resp = 'Global switchover to DB cluster ' + to_cluster + ' in Region ' + to_region + ' completed.'
        print("Expected output for switching to us-west-2 region : " + exp_resp)
        # print(response['Events'])
        for dic in response_uw2['Events']:
            if dic['Message'] == exp_resp:
                print("New Primary Cluster " + to_cluster + " is " + to_clu_status['status'] + " state.")
                print("New Secondary Cluster " + from_cluster + " is " + from_clu_status['status'] + " state.")
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Starting Unlocking Application users")
                lock_unlock_kill_app_users(app_env,from_cluster,to_cluster,from_region,to_region, db_type, db_name, 'unlock')
                print("At ", datetime.now().strftime("%Y-%m-%d %H:%M:%S"), ": Switchover Completed")
                break
            else:
                print("")
            # time.sleep(15)


# apg_gl_db_lag_check('ppsp-ppd-arcdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com',5432,'psppdarc')

if len(sys.argv) != 7:
    print('Usage: python3 apg-global-db-switchover.py <AppEnv> <db_type> <global_cluster_name> <from_region> <to_region> <switch_option>')
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
    app_env = sys.argv[1]
    if app_env == 'sys' or app_env == 'pds' or app_env == 'arc' or app_env == 'prod':
        print(" ")
    else:
        print("Incorrect environment name provided. Please provide from sys, pds, arc, prod")
    db_type = sys.argv[2]
    if db_type == 'monolith' or db_type == 'audit':
        print()
    else:
        print("Not a valid database type. Please provide valid db type either monolith or audit")
        exit()

    global_cluster_name = sys.argv[3]
    g_clu_info = getglobalclusterdetails(global_cluster_name)
    p_clu_info = getclusterdetails(g_clu_info['prim_clu_name'], g_clu_info['prim_region'])
    s_clu_info = getclusterdetails(g_clu_info['sec_clu_name'], g_clu_info['sec_region'])
    from_region = sys.argv[4]
    if app_env == 'pds' and db_type == 'monolith':
            db_name = "ppdspg02"
    elif app_env == 'pds' and db_type == 'audit':
            db_name = "pdsibobdb"
    elif app_env == 'sys' and db_type == 'monolith':
            db_name = "psyspg01"
    elif app_env == 'sys' and db_type == 'audit':
            db_name = "sysibobdb"
    elif app_env == 'arc' and db_type == 'monolith':
            db_name = "psppdarc"
    elif app_env == 'prod' and db_type == 'monolith':
            db_name = "pspapg02"
    elif app_env == 'prod' and db_type == 'audit':
            db_name = "prodapgib"
    else:
        print("Not a valid database name")
    if from_region == 'us-west-2' or from_region =='us-east-2':
        print(" ")
        # print("Source region:" + from_region)
    else:
        print("Please provide valid value for from_region")
        exit()
    to_region = sys.argv[5]
    if to_region == 'us-west-2' or to_region =='us-east-2':
        print(" ")
        # print("Target region:" + to_region)
    else:
        print("Please provide valid value for to_region")
        exit()
    if from_region == to_region:
        print("from_region and to_region cannot be same. Please provide correct values for both")
        exit()
    switch_option = sys.argv[6]
    if switch_option == 'switchover':
        perform_switchover(app_env, global_cluster_name, g_clu_info['prim_clu_name'], g_clu_info['sec_clu_name'], g_clu_info['sec_clu_arn'], from_region, to_region, db_type, db_name)
    elif switch_option == 'failover':
            print("starting failover")
    elif switch_option == 'prechecks':
            print("Performing pre-check for aurora global cluster " + global_cluster_name)
            switchover_prechecks(global_cluster_name, from_region, to_region, db_name)
    elif switch_option == 'lagcheck':
            lag_sec = apg_gl_db_lag_check('ppsp-ppd-arcdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com', 5432, 'psppdarc')
            print("Replication lag is " + str(lag_sec) + " seconds")
    else:
         print("Please enter valid operation name from switchover, failover, prechecks, lagcheck")



# SID and AWS parameters
node.default['oracle']['app_name'] = File.foreach('/etc/intu_metadata/app.ini').grep(/APP_NAME/)[0].split(/=/)[1].chomp
node.default['oracle']['sid'] = File.foreach('/etc/intu_metadata/app.ini').grep(/RDS_INSTANCE_NAME/)[0].split(/=/)[1].chomp
node.default['oracle']['vpc_env'] = File.foreach('/etc/intu_metadata/app.ini').grep(/VPC_ENV/)[0].split(/=/)[1].chomp
node.default['oracle']['account_id'] = File.foreach('/etc/intu_metadata/app.ini').grep(/ACCOUNT_ID/)[0].split(/=/)[1].chomp
node.default['oracle']['region'] = File.foreach('/etc/intu_metadata/app.ini').grep(/REGION/)[0].split(/=/)[1].chomp
node.default['oracle']['idps_endpoint'] = File.foreach('/etc/intu_metadata/app.ini').grep(/IDPS_ENDPOINT/)[0].split(/=/)[1].chomp
node.default['oracle']['idps_policy_id'] = File.foreach('/etc/intu_metadata/app.ini').grep(/IDPS_POLICY_ID/)[0].split(/=/)[1].chomp

# Unix accounts
node.default['oracle']['primary_user_name'] = 'oracle'
node.default['oracle']['primary_user_id'] = '501'
node.default['oracle']['primary_group_name'] = 'oinstall'
node.default['oracle']['primary_group_id'] = '501'
node.default['oracle']['bash_profile'] = File.join('', 'l', 'orcl')
node.default['oracle']['secondary_group_name'] = 'dba'
node.default['oracle']['secondary_group_id'] = '502'

# System conf
node.default['oracle']['artifacts']['local_directory'] = File.join('', 'u01', 'deploy', 'artifacts')
node.default['oracle']['artifacts']['s3_bucket_url'] = "s3://bits-sbg-#{node['oracle']['app_name']}-#{node['oracle']['vpc_env']}-#{node['oracle']['region']}/binaries"
node.default['oracle']['artifacts']['binary'] = File.join('oracle', 'software', '12.1.0.2_client.tar.gz')
node.default['oracle']['artifacts']['goldengate_binary'] = File.join('goldengate', 'software', '123014_fbo_ggs_Linux_x64_shiphome.zip')
node.default['oracle']['artifacts']['iac_host'] = File.join('goldengate', 'iac_host.txt')
node.default['oracle']['artifacts']['iac_aws_host'] = File.join('goldengate', 'iac_aws_host.txt')
node.default['oracle']['artifacts']['monitor'] = File.join('goldengate', 'scripts', 'monitor_aws_latest.tar')

node.default['oracle']['swap']['block_count'] = '1048576'
node.default['oracle']['swap']['block_size'] = '1024'
node.default['oracle']['swap']['filename'] = File.join('', 'var', 'swapfile')

# Install settings
node.default['oracle']['version'] = '12.1.0.2'
node.default['oracle']['goldengate_version'] = '12.3.0.1.4'
node.default['oracle']['goldengate_manager_port'] = '15000'
node.default['oracle']['goldengate_dynamic_port_list'] = '15010-15020'
node.default['oracle']['listener_port'] = '1526'
#node.default['oracle']['disable_sql_commands'] = node['deployment_environment']['disable_sql_commands']
node.default['oracle']['goldengate_installer_wait_time'] = 10 
node.default['oracle']['trail_name'] = ''
node.default['oracle']['include_files'] = ''
node.default['oracle']['disable_fk'] = ''

# Directories
node.default['oracle']['temp_directory'] = File.join(Chef::Config['file_cache_path'], 'oracle')
node.default['oracle']['root_directory'] = File.join('', 'u01')
node.default['oracle']['app_directory'] = File.join(node['oracle']['root_directory'], 'app')
node.default['oracle']['base_directory'] = File.join(node['oracle']['app_directory'], 'oracle')
node.default['oracle']['product_directory'] = File.join(node['oracle']['base_directory'], 'product')
node.default['oracle']['home_directory'] = File.join(node['oracle']['product_directory'], node['oracle']['version'])
node.default['oracle']['scripts_directory'] = File.join(node['oracle']['base_directory'], 'scripts')
node.default['oracle']['ogg_directory'] = File.join(node['oracle']['root_directory'], 'ogg')
node.default['oracle']['goldengate_home'] = File.join('', 'u01', 'ogg', node['oracle']['sid'], node['oracle']['goldengate_version'])
node.default['oracle']['prm_directory'] = File.join(node['oracle']['goldengate_home'], 'dirprm')
node.default['oracle']['dirdat_directory'] = File.join('', 'ogg_dirdat')
node.default['oracle']['java_directory'] = File.join('', 'u01', 'jdk1.7', 'jdk1.7.0_75')
node.default['oracle']['stg_directory'] = File.join('', 'u01', 'app', 'oracle', 'stg_area')
node.default['oracle']['monitor_directory'] = File.join('', 'u01', 'ogg', 'scripts')

# Splunk
node.override['splunk']['forwarder']['inputs']['monitor'] = {
  File.join(node.default['oracle']['ogg_directory'], node['oracle']['sid'], node['oracle']['goldengate_version'], 'ggserr.log') => {
    'idx' => 'ogg',
    'disabled' => 'false',
    'sourcetype' => 'ogg-err',
    'blacklist' => '.*\\.gz'
  },
  File.join(node.default['oracle']['ogg_directory'], node['oracle']['sid'], node['oracle']['goldengate_version'], 'dirrpt', '*.rpt') => {
    'idx' => 'ogg',
    'disabled' => 'false',
    'sourcetype' => 'ogg-rpt',
    'blacklist' => '.*\\.gz'
  },
  File.join(node.default['oracle']['ogg_directory'], 'scripts', 'log', 'monitor_ggserr.log') => {
    'idx' => 'ogg',
    'disabled' => 'false',
    'sourcetype' => 'ogg-err',
    'blacklist' => '.*\\.gz'
  },
  File.join(node.default['oracle']['ogg_directory'], 'scripts', 'log', 'monitor_ogg_exception.log') => {
    'idx' => 'ogg',
    'disabled' => 'false',
    'sourcetype' => 'ogg-err',
    'blacklist' => '.*\\.gz'
  },
  File.join(node.default['oracle']['ogg_directory'], 'scripts', 'log', 'monitor_ogg_process.log') => {
    'idx' => 'ogg',
    'disabled' => 'false',
    'sourcetype' => 'ogg-log',
    'blacklist' => '.*\\.gz'
  }
}

# GG parameters
case node.default['oracle']['app_name']
when 'PSPADM'
  node.default['oracle']['replicate_schema_clause1']="TABLE PSPADM.*;"
  node.default['oracle']['replicate_schema_clause2']="TABLE QBO_DATA.*;"
  node.default['oracle']['replicate_schema_clause3']=""
  node.default['oracle']['replicate_schema_clause4']="SEQUENCE QBO.*;"
  node.default['oracle']['replicate_schema_clause5']="SEQUENCE QBO_DATA.*;"
  node.default['oracle']['replicate_schema_clause6']=""
  node.default['oracle']['replicate_map_clause1']="MAP PSPADM.*, TARGET PSPADM.*;"
  node.default['oracle']['replicate_map_clause2']="MAP QBO_DATA.*, TARGET QBO_DATA.*;"
  node.default['oracle']['replicate_map_clause3']=""

  node.default['oracle']['replicate_map_exception1']="MAP QBO.QBIMPORTDATA_1, TARGET QBO.QBIMPORTDATA_1, KEYCOLS(global_authid, create_date);"
  node.default['oracle']['replicate_map_exception2']="MAP QBO_DATA.OLBACCOUNTSESSIONS_1, TARGET QBO_DATA.OLBACCOUNTSESSIONS_1, KEYCOLS(company_id, olb_session_id, olb_account_id, bank_balance, bank_balance_asof, last_aggregation_time, num_transactions, tek_update_status);"
  node.default['oracle']['replicate_map_exception3']="MAP QBO_DATA.BILLINGORDERDETAILS_1, TARGET QBO_DATA.BILLINGORDERDETAILS_1, KEYCOLS(company_id, order_id, charge_type, amount, string_attrib_1, string_attrib_2, int_attrib_1, create_date, create_user_id, child_co_id, child_company_sku, region_id, last_modify_date);"
  node.default['oracle']['replicate_map_exception4']="MAP QBO_DATA.AUDITINFO_1, TARGET QBO_DATA.AUDITINFO_1 COLCHARSET(PASSTHRU,SUMMARY);"
end


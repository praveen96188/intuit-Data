#
# Cookbook Name:: goldengate_hub
# Recipe:: _setup_local_db
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
dummy_db_startup_log = File.join(node['oracle']['temp_directory'], 'dummy_db_startup.log')

oracle_db_dirs = [
  "/u01/oradata",
  "/u01/app/oracle/admin",
  "/u01/app/oracle/admin/orcl/adump",
  "/u01/app/oracle/fast_recovery_area",
  "/u01/app/oracle/fast_recovery_area/ORCL/controlfile"
]

oracle_db_dirs.each do |dir|
  directory dir do
    owner node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00775'
    recursive true
    action :create
  end
end

oracle_db_dirs.each do |dir|
  execute "chown -R #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{dir}"
end

bash 'copy dummy db init and control file' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  code <<-EOH
    cp /u01/oradata/ORCL/controlfile/o1_mf_bqmf3tpt_.ctl /u01/app/oracle/fast_recovery_area/ORCL/controlfile/o1_mf_bqmf3tr1_.ctl
    cp /u01/oradata/ORCL/initorcl.ora #{File.join(node['oracle']['home_directory'], 'dbs')}
  EOH
  action :nothing
end

execute 'extract dummy db' do
  cwd File.join(node['oracle']['root_directory'], 'oradata')
  command ['tar', 'xvfp', File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['dummy_db'])]
  not_if { File.directory?("/u01/oradata/ORCL/") }
  notifies :run, 'execute[set owner and group on app]', :immediately
  notifies :run, 'bash[copy dummy db init and control file]', :immediately
end

#bash 'Startup dummy db' do
#  user node['oracle']['primary_user_name']
#  group node['oracle']['primary_group_name']
#  code <<-EOH
#    source #{node['oracle']['bash_profile']}
#    sqlplus '/ as sysdba' <<EOF  > #{dummy_db_startup_log} 2>&1
#      startup
#EOF
#  EOH
#end

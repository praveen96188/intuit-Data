#
# Cookbook Name:: goldengate_hub
# Recipe:: _base
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
oraInst_loc = <<-END
inventory_loc=/u01/app/oraInventory
inst_group=oinstall
END

file '/etc/oraInst.loc' do
  content oraInst_loc
  mode '00644'
end

ruby_block 'check if oracle is installed' do
  block do
    node.run_state['oracle_installed'] = Dir.exist?(File.join(node['oracle']['app_directory'], 'oraInventory')) && Dir.exist?(File.join(node['oracle']['dirdat_directory'], node['oracle']['sid']))
    if node.run_state['oracle_installed']
      Chef::Log.warn('Oracle already installed.')
    end
  end
end

packages = [
  'binutils',
  'compat-libcap1',
  'elfutils-libelf-devel',
  'gcc',
  'gcc-c++',
  'glibc',
  'glibc-devel',
  'ksh',
  'libacl',
  'libaio',
  'libaio-devel',
  'libgcc',
  'libstdc++',
  'libstdc++-devel',
  'libxslt',
  'make',
  'sysstat',
]

packages.each do |pkg|
  package pkg
end

oracle_dirs = [
  node['oracle']['app_directory'],
  node['oracle']['dirdat_directory'],
  node['oracle']['product_directory'],
  node['oracle']['scripts_directory'],
]

oracle_dirs.each do |dir|
  directory dir do
    owner node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00775'
    recursive true
    action :create
  end
end

execute 'set owner and group on app' do
  command "chown -R #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{node['oracle']['root_directory']}"
  action :nothing
end

clone_script = File.join(node['oracle']['home_directory'], 'clone', 'bin', 'clone.pl')
perl = File.join(node['oracle']['home_directory'], 'perl', 'bin', 'perl')
bash 'run clone.pl' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  # The ORACLE_XXXX args may look like misplaced environment variables, but they aren't. That's the syntax for clone.pl.
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    #{perl} #{clone_script} ORACLE_BASE=#{node['oracle']['base_directory']} ORACLE_HOME=#{node['oracle']['home_directory']} ORACLE_HOME_NAME=12GR1_HOME
  EOH
  action :nothing
end

execute 'extract binary' do
  cwd node['oracle']['product_directory']
  command ['tar', 'xvfp', File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['binary'])]
  not_if { File.directory?(node['oracle']['home_directory']) }
  not_if { node.run_state['oracle_installed'] }
  notifies :run, 'execute[set owner and group on app]', :immediately
  notifies :run, 'bash[run clone.pl]', :immediately # We can only run clone script once after extraction. It errors out if you run a second time.
end

oracle_root_script = File.join(node['oracle']['home_directory'], 'root.sh')
execute oracle_root_script do
  not_if { node.run_state['oracle_installed'] }
end

ora_inst_root_script = File.join(node['oracle']['app_directory'], 'oraInventory', 'orainstRoot.sh')
execute ora_inst_root_script do
  only_if { ::File.exists?(ora_inst_root_script) } # Not always present. To do: find out if this will ever run. If not, remove.
  not_if { node.run_state['oracle_installed'] }
end

template File.join(node['oracle']['home_directory'], 'network', 'admin', 'tnsnames.ora') do
  source 'tnsnames.ora.erb'
  owner node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00644'
end

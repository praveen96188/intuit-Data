#
# Cookbook Name:: goldengate_hub
# Recipe:: _goldengate
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
ggsci = File.join(node['oracle']['goldengate_home'], 'ggsci')
obey_subdirs_file = File.join(node['oracle']['temp_directory'], 'obey_subdirs.gg')
ogg_installer = File.join(node['oracle']['temp_directory'], 'fbo_ggs_Linux_x64_shiphome', 'Disk1', 'runInstaller')
ogg_installer_log = File.join(node['oracle']['temp_directory'], 'ogg_installer.log')
ogg_installer_response_file = File.join(node['oracle']['temp_directory'], 'oggcore.rsp')
ogg_patch_log = File.join(node['oracle']['temp_directory'], 'ogg_patch.log')

ruby_block 'check if goldengate is installed' do
  block do
    node.run_state['goldengate_installed'] = Dir.exist?(node['oracle']['goldengate_home'])
    if node.run_state['goldengate_installed']
      Chef::Log.warn('Golden Gate already installed.')
    end
  end
end

execute "unzip #{File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['goldengate_binary'])}" do
  cwd node['oracle']['temp_directory']
#Not working  not_if File.file?(ogg_installer)
end

# execute "unzip #{File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['goldengate_patch'])}" do
#   cwd node['oracle']['temp_directory']
# #Not working not_if Dir.exist?(File.join(node['oracle']['temp_directory'], node.default['oracle']['artifacts']['goldengate_patch_number']))
# end

execute "chown -R #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{node['oracle']['temp_directory']}"

obey_subdirs = <<-END
create subdirs
exit
END

file obey_subdirs_file do
  content obey_subdirs
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00644'
end

template ogg_installer_response_file do
  source 'oggcore.rsp.erb'
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00644'
end

ruby_block 'wait for ogg installer' do
  block do
    done = false
    until done do
      sleep(node['oracle']['goldengate_installer_wait_time'])
      if File.file?(ggsci) && File.file?(File.join(node['oracle']['goldengate_home'], 'OPatch', 'opatch'))
        # This file won't exist if the Golden Gate installer hasn't finished.
        # This may not be the strongest possible test.
        # To do: find a test that confirms for sure whether the install is done.
        done = true
        # Give enough time to make sure that installation is completed. 
        sleep(node['oracle']['goldengate_installer_wait_time'])
      end
    end
  end
  action :nothing
end

bash 'run ogg installer' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    #{ogg_installer} -silent -responseFile #{ogg_installer_response_file} > #{ogg_installer_log} 2>&1
  EOH
  not_if { node.run_state['goldengate_installed'] }
  notifies :run, 'ruby_block[wait for ogg installer]', :immediately
end

bash 'run ogg patch' do
#   user node['oracle']['primary_user_name']
#   group node['oracle']['primary_group_name']
#   cwd File.join(node['oracle']['temp_directory'], node.default['oracle']['artifacts']['goldengate_patch_number'])
#   code <<-EOH
#     source #{node['oracle']['bash_profile']}
#     export ORACLE_HOME=#{node['oracle']['goldengate_home']}
#     if [ `$ORACLE_HOME/OPatch/opatch lsinventory |grep 21037711 |wc -l` -eq 0 ]; then  
#       $ORACLE_HOME/OPatch/opatch apply -silent > #{ogg_patch_log} 2>&1
#     else
#       echo "The goldengate patch has already been installed" > #{ogg_patch_log} 
#     fi
#   EOH
  not_if { node.run_state['goldengate_patch_installed'] }
  notifies :run, 'bash[create subdirs using obey file]', :immediately
end

bash 'create subdirs using obey file' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    echo obey #{obey_subdirs_file} | #{ggsci}
  EOH
  action :nothing
end

directory File.join(node['oracle']['goldengate_home'], 'diroby') do
  owner node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  action :create
  recursive true
  mode '00775'
end

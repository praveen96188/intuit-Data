#
# Cookbook Name:: ss_splunk
# Recipe:: default
#
# Copyright 2016, Naohito Takeuchi
#
# All rights reserved - Do Not Redistribute
#

# create symbolic link
if node['splunk']['forwarder']['symlink']['enable']
  unless File.dirname(node['splunk']['forwarder']['symlink']['base']) == '/'
    # create directory (default: '/logs')
    directory File.dirname(node['splunk']['forwarder']['symlink']['base']) do
      owner 'root'
      group 'root'
      mode '0755'
      action :create
      recursive true
    end
    # create symbolic link for IHP (default: '/logs/ihp' -> '/')
    link node['splunk']['forwarder']['symlink']['base'] do
      to "/"
    end
  end
end

# inputs.conf
template '/opt/splunkforwarder/etc/system/local/inputs.conf' do
  source 'inputs.conf.erb'
  owner 'root'
  group 'root'
  mode '0644'
  if node['splunk']['forwarder']['softrestart'] == true
    notifies :run, 'execute[splunk_restart_cmd]', :immediately
  else
    notifies :run, 'execute[splunk_start_cmd]', :immediately
  end
end

##
## in softrestart we shell out instead of using service. This helps in cases where one uses cloudformation and
## does not want to fail a chef run ust because of splunk
if node['splunk']['forwarder']['softrestart'] == true
  execute 'splunk_restart_cmd' do
     command 'service splunk stop; service splunk start; exit 0'
  end
else
  execute 'splunk_start_cmd' do
     command 'rm -f /opt/splunkforwarder/ftr; service splunk enable; service splunk start; exit 0'
  end
end

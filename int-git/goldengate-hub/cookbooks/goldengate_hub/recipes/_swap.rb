#
# Cookbook Name:: goldengate_hub
# Recipe:: _swap
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
file node['oracle']['swap']['filename'] do
  mode '00600'
  action :nothing
end

execute "make #{node['oracle']['swap']['filename']} a swap file" do
  command "mkswap #{node['oracle']['swap']['filename']}"
  action :nothing
end

execute "create #{node['oracle']['swap']['filename']} with zeros" do
  command "dd if=/dev/zero of=#{node['oracle']['swap']['filename']} bs=#{node['oracle']['swap']['block_size']} count=#{node['oracle']['swap']['block_count']}"
  not_if { File.exists?(node['oracle']['swap']['filename']) }
  notifies :create, "file[#{node['oracle']['swap']['filename']}]", :immediately
  notifies :run, "execute[make #{node['oracle']['swap']['filename']} a swap file]", :immediately
end

mount '/dev/null' do  # swap file entry for fstab
  action :enable  # cannot mount; only add to fstab
  device node['oracle']['swap']['filename']
  fstype 'swap'
end

execute 'swapon -a'

# Extend ssh timeout from 60 seconds to 600 seconds
execute "sed -i -e 's/ClientAliveInterval 60/ClientAliveInterval 600/' /etc/ssh/sshd_config"
execute "service sshd restart"

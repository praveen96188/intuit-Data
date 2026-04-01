#
# Cookbook Name:: goldengate_hub
# Recipe:: _users
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
group node['oracle']['primary_group_name'] do
  gid node['oracle']['primary_group_id']
end

user node['oracle']['primary_user_name'] do
  supports :manage_home => true
  comment 'Oracle Runtime User'
#  password '!'
  uid node['oracle']['primary_user_id']
  group node['oracle']['primary_group_name']
  shell '/bin/bash'
end

execute 'chage -M -1 oracle'

group node['oracle']['secondary_group_name'] do
  gid node['oracle']['secondary_group_id']
  members node['oracle']['primary_user_name']
end

directory "/l" do
  owner 'root'
  group 'root'
  mode '00755'
end

template File.join('', 'l', 'orcl') do
  source 'bash_profile.erb'
  owner node['oracle']['primary_user_name']
  group node['oracle']['secondary_group_name']
  mode '00644'
end

file File.join('', 'etc', 'cron.allow') do
  content "#{node['oracle']['primary_user_name']}\n"
  owner 'root'
  group 'root'
  mode '00644'
end


#
# Cookbook Name:: goldengate_hub
# Recipe:: default
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
include_recipe 'ss_splunk'
include_recipe 'goldengate_hub::_swap'
include_recipe 'goldengate_hub::_base'
include_recipe 'goldengate_hub::_users'
include_recipe 'goldengate_hub::_artifacts'
include_recipe 'goldengate_hub::_install'
app_name=node['oracle']['app_name']
if app_name == "qbo" 
then
  include_recipe 'goldengate_hub::qbo_default'
else
  include_recipe 'goldengate_hub::default_install'
end

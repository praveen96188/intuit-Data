#
# Cookbook Name:: goldengate_hub
# Recipe:: default_standby
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
include_recipe 'ss_splunk'
include_recipe 'goldengate_hub::_swap'
include_recipe 'goldengate_hub::_base'
include_recipe 'goldengate_hub::_users'

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
include_recipe 'goldengate_hub::qbo_install_goldengate'
include_recipe 'goldengate_hub::qbo_configure_goldengate'
include_recipe 'goldengate_hub::qbo_monitor'
include_recipe 'goldengate_hub::qbo_start_goldengate'

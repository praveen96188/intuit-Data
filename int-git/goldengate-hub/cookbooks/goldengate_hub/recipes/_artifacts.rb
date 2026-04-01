#
# Cookbook Name:: goldengate_hub
# Recipe:: _artifacts
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
binaries = [
  node['oracle']['artifacts']['binary'],
  node['oracle']['artifacts']['dummy_db'],
  node['oracle']['artifacts']['goldengate_binary'],
#  node['oracle']['artifacts']['goldengate_patch'],
  node['oracle']['artifacts']['monitor'],
]

binaries.each do |binary|
  # The sync command assumes directories, hence the pattern of excluding * and including the one file we want.
  # We do this instead of cp since sync tests to see if the file has already been copied.
  execute "aws s3 sync #{node['oracle']['artifacts']['s3_bucket_url']} #{node['oracle']['artifacts']['local_directory']} --exclude '*' --include '#{binary}'"
end


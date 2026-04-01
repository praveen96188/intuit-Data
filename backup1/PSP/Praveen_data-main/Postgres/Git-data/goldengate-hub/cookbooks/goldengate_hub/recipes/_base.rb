#
# Cookbook Name:: goldengate_hub
# Recipe:: _base
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#
require 'pathname'

execute "rm -rf #{node['oracle']['temp_directory']}"

directory node['oracle']['artifacts']['local_directory'] do
  recursive true
end

# Create temp directory where any user can execute scripts.
# # Later on we have to execute a script that won't allow us to run as root.
Pathname.new(node['oracle']['temp_directory']).descend do |path|
  next if path.root?
  next if path == Pathname.new('/tmp') # /tmp often has mode 777 and if we reduce that it breaks other parts of Linux.
  directory path.to_s do
    mode '00755'
  end
end

bash 'change ClientAliveInterval and download authorized_keys' do
code <<-EOH
  # Change ClientAliveInterval from 5 mins to 30 mins
  sed -i 's/ClientAliveInterval 300/ClientAliveInterval 1800/' /etc/ssh/sshd_config
  service sshd restart

  # Donwload the file authorized_keys if it is on S3
  if [ `/usr/local/bin/secrets list --region #{node['oracle']['region']} --s3-bucket iss-#{node['oracle']['account_id']}-#{node['oracle']['region']} --kms-cmk-id #{node['oracle']['kms_id']} --filter secrets/goldengate/#{node['oracle']['vpc_env']}/authorized_keys |wc -l` -gt 1 ]; then
    echo "The file authorized_keys is on S3. Downloading it"
    /usr/local/bin/secrets get --region #{node['oracle']['region']} --s3-bucket iss-#{node['oracle']['account_id']}-#{node['oracle']['region']} --kms-cmk-id #{node['oracle']['kms_id']} --secret-name secrets/goldengate/#{node['oracle']['vpc_env']}/authorized_keys --output /home/ec2-user/.ssh/authorized_keys
  fi

  # Make oracle passwordless sudo
  echo "oracle ALL=(root) NOPASSWD: ALL" >> /etc/sudoers 
EOH
end

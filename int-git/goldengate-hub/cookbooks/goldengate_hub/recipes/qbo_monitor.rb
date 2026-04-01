#
# Cookbook Name:: goldengate_hub
# Recipe:: _monitor
#
# Copyright (c) 2016 The Authors, All Rights Reserved.
#

directory File.join('', 'u01', 'ogg', 'scripts') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

directory File.join('', 'dev', 'shm', 'goldengate', 'db') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

execute "tar -xvf #{File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['monitor'])}" do
  cwd node['oracle']['ogg_directory']
end

# Download ops_user's encrypted password 
cluster_num=node['oracle']['sid'][-2,2]
char5=node['oracle']['sid'][0,5]
char1=node['oracle']['sid'][0,1]

puts "char5=#{char5}"
puts "char1=#{char1}"
if char5 == "qbopp"
then
  cluster_env="prod"
elsif char5 == "qbosp"
  cluster_env="prod"
elsif char5 == "qbops"
  cluster_env="stg"
elsif char5 == "qboss"
  cluster_env="stg"
elsif char1 == "p" or char1 == "e" or char1 == "q"
  if char1 == "p"
    cluster_env="prf"
  end
  if char1 == "e"
    cluster_env="e2e"
  end
  if char1 == "q"
    cluster_env="qa"
  end
end
puts "cluster_env=#{cluster_env}"

bash 'download encrypted password' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  output_file=File.join('', 'dev', 'shm', 'goldengate', 'db', 'password_file')
  code <<-EOH
    if [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/db/ 2>&1 |grep "password_file" |wc -l` -gt 0 ]; then
      echo "Downloading ops_user's encrypted password"
      rm -r #{output_file}
      /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/db/password_file --output #{output_file}
      sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
    fi
  EOH
end

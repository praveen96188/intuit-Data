cluster_num = node['oracle']['sid']
char45 = node['oracle']['sid'][3,2]
char3 = node['oracle']['sid'][0,3]
char1 = node['oracle']['sid'][0,1]
char5th = node['oracle']['sid'][4,1]

puts "char45 = #{char45}"

if char45 == "pp" or char45 == "sp"
  then 
    cluster_env = "prod"
elsif char45 == "pf"
    cluster_env = "prf"  
else
    puts "unknown environment"
end

puts "cluster_env = #{cluster_env}"


# Save Goldengate wallet and credential files on the share memory
directory File.join('', 'dev', 'shm', 'goldengate') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  action :create
  recursive true  
  mode '00700'
end

directory File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}") do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  action :create
  recursive true  
  mode '00700'
end

directory File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dirwlt') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  action :create
  recursive true  
  mode '00700'
end

link File.join(node['oracle']['goldengate_home'], 'dirwlt', 'cwallet.sso') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
  to File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dirwlt', 'cwallet.sso')
end

directory File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dircrd') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  action :create
  recursive true  
  mode '00700'
end

link File.join(node['oracle']['goldengate_home'], 'dircrd', 'cwallet.sso') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
  to File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dircrd', 'cwallet.sso')
end

bash 'sync wallet file' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  output_file=File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dirwlt', 'cwallet.sso')
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    if [ ! -f #{output_file} ]; then
      # The wallet file is on IDPS (cluster folder)
      if [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/#{cluster_num}/dirwlt/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/#{cluster_num}/dirwlt/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
      # The wallet file is on IDPS (default cluster folder)
      elif [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dirwlt/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dirwlt/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
      else
        echo "Generate the wallet file"
        cd $GG_HOME
        ./ggsci << EOF
          create wallet
          add masterkey
EOF
        echo "\n"
        echo "ACTION: Please upload the wallet file cwallet.sso to IPDS manually"
      fi
    fi
  EOH
end

bash 'sync credential file' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  output_file=File.join('', 'dev', 'shm', 'goldengate', "#{cluster_num}", 'dircrd', 'cwallet.sso')
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    if [ ! -f #{output_file} ]; then
      # The wallet file is on IDPS (cluster folder)
      if [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/#{cluster_num}/dircrd/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/#{cluster_num}/dircrd/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
      # The wallet file is on IDPS (default cluster folder)
      elif [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dircrd/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --vkm #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dircrd/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
      
      else
        echo "ACTION: Please generate and upload the credential file cwallet.sso to IDPS manually"
      fi
    fi
  EOH
end

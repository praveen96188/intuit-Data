#
# Cookbook Name:: goldengate_hub
# Recipe:: _configure_goldengate
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

file File.join(node['oracle']['goldengate_home'], 'GLOBALS') do
  content "CHECKPOINTTABLE GGT.CHECKPOINT\nGGSCHEMA GGS\nTRAIL_SEQLEN_6D\n"
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00644'
end

cluster_num=node['oracle']['sid'][-2,2]
char5=node['oracle']['sid'][0,5]
char3=node['oracle']['sid'][0,3]
char1=node['oracle']['sid'][0,1]
char5th=node['oracle']['sid'][4,1]

puts "char5=#{char5}"
puts "char1=#{char1}"
puts "char5th=#{char5th}"

if cluster_num == "pt"
then
  # for reporting db
  if char3 == "qbo"
  then 
    cluster_env="prod"
  elsif char1 == "p" or char1 == "e" or char1 == "q" or char1 == "s"
    if char1 == "p"
      cluster_env="prf"
    end
    if char1 == "e"
      cluster_env="e2e"
    end
    if char1 == "q"
      cluster_env="qa"
    end
    if char1 == "s"
      cluster_env="stg"
    end
  else
    puts "unknown environment"
  end

else
  # for cluster db
  if char5 == "qbopp"
  then
    # production primary
    site="p"
    cluster_env="prod"
    puts "cluster_env=#{cluster_env}"
    node.default['oracle']['extract'] = "eqbpp0#{cluster_num}"
    node.default['oracle']['extract_to_iac'] = "eiapp0#{cluster_num}"
    node.default['oracle']['pump_to_qdc'] = "pqbqpd#{cluster_num}"
    node.default['oracle']['pump_to_r2'] = "pqbsp0#{cluster_num}"
    node.default['oracle']['pump_to_iac'] = "piapp0#{cluster_num}"
    node.default['oracle']['pump_to_aws_iac'] = "piwpp0#{cluster_num}"
    node.default['oracle']['remote_host']['pump_to_qdc'] = "oraqboqpd#{cluster_num}.qcyf01.ie.intuit.net"
    node.default['oracle']['remote_host']['pump_to_r2'] = "ggqbosp0#{cluster_num}.sbg-qbo-prod.a.intuit.com"
    node.default['oracle']['replicat_from_qdc'] = "rqbqpd#{cluster_num}"
    node.default['oracle']['replicat_from_r2'] = "rqbsp0#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_qdc'] = "qboqpd#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_r2'] = "qbosp0#{cluster_num}"
  elsif char5 == "qbosp"
    # production standby
    site="s"
    cluster_env="prod"
    puts "cluster_env=#{cluster_env}"
    node.default['oracle']['extract'] = "eqbsp0#{cluster_num}"
    node.default['oracle']['extract_to_iac'] = "eiasp0#{cluster_num}"
    node.default['oracle']['pump_to_r1'] = "pqbpp0#{cluster_num}"
    node.default['oracle']['pump_to_iac'] = "piasp0#{cluster_num}"
    node.default['oracle']['pump_to_aws_iac'] = "piwsp0#{cluster_num}"
    node.default['oracle']['remote_host']['pump_to_r1'] = "ggqbopp0#{cluster_num}.sbg-qbo-prod.a.intuit.com"
    node.default['oracle']['replicat_from_r1'] = "rqbpp0#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_r1'] = "qbopp0#{cluster_num}"
  elsif char5 == "qbops"
    # staging primary
    site="p"
    cluster_env="stg"
    puts "cluster_env=#{cluster_env}"
    node.default['oracle']['extract'] = "eqbps0#{cluster_num}"
    node.default['oracle']['extract_to_iac'] = "eiaps0#{cluster_num}"
    node.default['oracle']['pump_to_qdc'] = "pqbstg#{cluster_num}"
    node.default['oracle']['pump_to_r2'] = "pqbss0#{cluster_num}"
    node.default['oracle']['pump_to_iac'] = "piaps0#{cluster_num}"
    node.default['oracle']['pump_to_aws_iac'] = "piwps0#{cluster_num}"
    node.default['oracle']['remote_host']['pump_to_qdc'] = "oraqbostg#{cluster_num}.qcyf01.ie.intuit.net"
    node.default['oracle']['remote_host']['pump_to_r2'] = "ggqboss0#{cluster_num}.sbg-qbo-ppd.a.intuit.com"
    node.default['oracle']['replicat_from_qdc'] = "rqbstg#{cluster_num}"
    node.default['oracle']['replicat_from_r2'] = "rqbss0#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_qdc'] = "qbostg#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_r2'] = "qboss0#{cluster_num}"
  elsif char5 == "qboss"
    # staging standby 
    site="s"
    cluster_env="stg"
    puts "cluster_env=#{cluster_env}"
    node.default['oracle']['extract'] = "eqbss0#{cluster_num}"
    node.default['oracle']['extract_to_iac'] = "eiass0#{cluster_num}"
    node.default['oracle']['pump_to_r1'] = "pqbps0#{cluster_num}"
    node.default['oracle']['pump_to_iac'] = "piass0#{cluster_num}"
    node.default['oracle']['pump_to_aws_iac'] = "piwss0#{cluster_num}"
    node.default['oracle']['remote_host']['pump_to_r1'] = "ggqbops0#{cluster_num}.sbg-qbo-ppd.a.intuit.com"
    node.default['oracle']['replicat_from_r'] = "rqbps0#{cluster_num}"
    node.default['oracle']['remote_db']['replicat_from_r1'] = "qbops0#{cluster_num}"
  elsif char1 == "p" or char1 == "e" or char1 == "q" or char1 == "s" or char1 == "b"
    if char1 == "p"
      cluster_env="prf"
    end
    if char1 == "e"
      cluster_env="e2e"
    end
    if char1 == "q"
      cluster_env="qa"
    end
    if char1 == "s"
      cluster_env="stg"
    end
    if char1 == "b"
      cluster_env="sbx"
    end
    if char5th == "p"
      # non-prod primary 
      site="p"
      puts "cluster_env=#{cluster_env}"
      node.default['oracle']['extract'] = "e#{char1}qbpc#{cluster_num}"
      node.default['oracle']['extract_to_iac'] = "e#{char1}iapc#{cluster_num}"
      node.default['oracle']['pump_to_qdc'] = "p#{char1}qbqc#{cluster_num}"
      node.default['oracle']['pump_to_r2'] = "p#{char1}qbsc#{cluster_num}"
      node.default['oracle']['pump_to_iac'] = "p#{char1}iapc#{cluster_num}"
      node.default['oracle']['pump_to_aws_iac'] = "p#{char1}iwpc#{cluster_num}" 
      node.default['oracle']['remote_host']['pump_to_qdc'] = "ora#{char1}qboqc#{cluster_num}.ie.intuit.net"
      if char1 == "s"
        node.default['oracle']['remote_host']['pump_to_r2'] = "gg#{char1}qbosc#{cluster_num}.sbg-qbo-prod.a.intuit.com"
      elsif char1 == "b"
        node.default['oracle']['remote_host']['pump_to_r2'] = "gg#{char1}qbosc#{cluster_num}.sbg-qbo-sbx.a.intuit.com"
      else
        node.default['oracle']['remote_host']['pump_to_r2'] = "gg#{char1}qbosc#{cluster_num}.sbg-qbo-ppd.a.intuit.com"
      end 
      node.default['oracle']['replicat_from_qdc'] = "r#{char1}qbqc#{cluster_num}"
      node.default['oracle']['replicat_from_r2'] = "r#{char1}qbsc#{cluster_num}"
      node.default['oracle']['remote_db']['replicat_from_qdc'] = "#{char1}qboqc#{cluster_num}"
      node.default['oracle']['remote_db']['replicat_from_r2'] = "#{char1}qbosc#{cluster_num}"
    elsif char5th == "s"
      # non-prod standby 
      site="s"
      puts "cluster_env=#{cluster_env}"
      node.default['oracle']['extract'] = "e#{char1}qbsc#{cluster_num}"
      node.default['oracle']['extract_to_iac'] = "e#{char1}iasc#{cluster_num}"
      node.default['oracle']['pump_to_r1'] = "p#{char1}qbpc#{cluster_num}"
      node.default['oracle']['pump_to_iac'] = "p#{char1}iasc#{cluster_num}"
      node.default['oracle']['pump_to_aws_iac'] = "p#{char1}iwsc#{cluster_num}"
      if char1 == "s"
        node.default['oracle']['remote_host']['pump_to_r1'] = "gg#{char1}qbopc#{cluster_num}.sbg-qbo-prod.a.intuit.com"
      elsif char1 == "b"
        node.default['oracle']['remote_host']['pump_to_r1'] = "gg#{char1}qbopc#{cluster_num}.sbg-qbo-sbx.a.intuit.com"
      else
        node.default['oracle']['remote_host']['pump_to_r1'] = "gg#{char1}qbopc#{cluster_num}.sbg-qbo-ppd.a.intuit.com"
      end 
      node.default['oracle']['replicat_from_r1'] = "r#{char1}qbpc#{cluster_num}"
      node.default['oracle']['remote_db']['replicat_from_r1'] = "#{char1}qbopc#{cluster_num}"
    end
  end

  node.default['oracle']['ggt_replication']['extract'] = "TRANLOGOPTIONS EXCLUDEUSER GGT"
  node.default['oracle']['ggt_replication']['extract_to_iac'] = "--TRANLOGOPTIONS EXCLUDEUSER GGT"

  node.default['oracle']['ddl_replication']['extract'] = ""
  node.default['oracle']['ddl_replication']['extract_to_iac'] = "ddloptions GETREPLICATES, report"

  node.default['oracle']['trail_location']['extract'] = node.default['oracle']['sid']
  node.default['oracle']['trail_location']['extract_to_iac'] = "#{node['oracle']['sid']}".sub('qbo', 'iac')

  node.default['oracle']['trail_format_option']['extract'] = ""
  node.default['oracle']['trail_format_option']['extract_to_iac'] = ", FORMAT RELEASE 12.1"
  node.default['oracle']['trail_format_option']['pump_to_qdc'] = ""
  node.default['oracle']['trail_format_option']['pump_to_r2'] = ""
  node.default['oracle']['trail_format_option']['pump_to_iac'] = ", FORMAT RELEASE 12.1"
  node.default['oracle']['trail_format_option']['pump_to_aws_iac'] = ", FORMAT RELEASE 12.1"

  if site == "p"
    node.default['oracle']['trail_location']['pump_to_qdc'] = node.default['oracle']['sid']
    node.default['oracle']['trail_location']['pump_to_r2'] = node.default['oracle']['sid']
    node.default['oracle']['trail_location']['pump_to_iac'] = "#{node['oracle']['sid']}".sub('qbo', 'iac')
    node.default['oracle']['trail_location']['pump_to_aws_iac'] = "#{node['oracle']['sid']}".sub('qbo', 'iac')

    node.default['oracle']['remote_port']['pump_to_qdc'] = node['oracle']['goldengate_manager_port']
    node.default['oracle']['remote_port']['pump_to_r2'] = node['oracle']['goldengate_manager_port'] 
  elsif site == "s"
    node.default['oracle']['trail_location']['pump_to_r1'] = node.default['oracle']['sid']
    node.default['oracle']['trail_location']['pump_to_iac'] = "#{node['oracle']['sid']}".sub('qbo', 'iac')
    node.default['oracle']['trail_location']['pump_to_aws_iac'] = "#{node['oracle']['sid']}".sub('qbo', 'iac')

    node.default['oracle']['remote_port']['pump_to_r1'] = node['oracle']['goldengate_manager_port'] 
  end

  bash 'download iac_host' do
    code <<-EOH
      aws s3 sync #{node['oracle']['artifacts']['s3_bucket_url']} #{node['oracle']['artifacts']['local_directory']} --exclude '*' --include "#{node['oracle']['artifacts']['iac_host']}"
      while true
      do
        if [ -f #{File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['iac_host'])} ]; then
          break
        else
          sleep 5
        fi
      done
EOH
  end
  bash 'download iac_aws_host' do
    code <<-EOH
      aws s3 sync #{node['oracle']['artifacts']['s3_bucket_url']} #{node['oracle']['artifacts']['local_directory']} --exclude '*' --include "#{node['oracle']['artifacts']['iac_aws_host']}"
      while true
      do
        if [ -f #{File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['iac_aws_host'])} ]; then
          break
        else
          sleep 5
        fi
      done
  EOH
  end

  ruby_block 'set parameters' do
    block do
      iac_host_port = File.foreach(File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['iac_host'])).grep(/#{cluster_env} #{cluster_num}/)[0]

      if iac_host_port == nil
        node.default['oracle']['remote_host']['pump_to_iac'] = "no_iac_host"
        node.default['oracle']['remote_port']['pump_to_iac'] = "no_iac_port"
      else
        node.default['oracle']['remote_host']['pump_to_iac'] = iac_host_port.split(/ /)[2].chomp
        node.default['oracle']['remote_port']['pump_to_iac'] = iac_host_port.split(/ /)[3].chomp
      end
     iac_aws_host_port = File.foreach(File.join(node['oracle']['artifacts']['local_directory'], node['oracle']['artifacts']['iac_aws_host'])).grep(/#{cluster_env} #{cluster_num}/)[0]

      if iac_aws_host_port == nil 
        node.default['oracle']['remote_host']['pump_to_aws_iac'] = "no_iac_host"
        node.default['oracle']['remote_port']['pump_to_aws_iac'] = "no_iac_port"
      else
        node.default['oracle']['remote_host']['pump_to_aws_iac'] = iac_aws_host_port.split(/ /)[2].chomp
        node.default['oracle']['remote_port']['pump_to_aws_iac'] = iac_aws_host_port.split(/ /)[3].chomp
      end
      
      if site == "p"
        puts "extract=#{node['oracle']['extract']}"
        puts "extract=#{node['oracle']['extract_to_iac']}"
        puts "pump_to_qdc=#{node['oracle']['pump_to_qdc']}"
        puts "pump_to_r2=#{node['oracle']['pump_to_r2']}"
        puts "pump_to_iac=#{node['oracle']['pump_to_iac']}"
        puts "pump_to_aws_iac=#{node['oracle']['pump_to_aws_iac']}"
        puts "replicat=#{node['oracle']['replicat_from_qdc']}"
        puts "replicat=#{node['oracle']['replicat_from_r2']}"
        puts "remote_db=#{node['oracle']['remote_db']}"
        puts "remote_host=#{node['oracle']['remote_host']}"
        puts "remote_port=#{node['oracle']['remote_port']}"

        ogg_progress = [
          'extract',
          'extract_to_iac',
          'pump_to_qdc',
          'pump_to_r2',
          'pump_to_iac',
          'pump_to_aws_iac',
          'replicat_from_qdc',
          'replicat_from_r2',
        ]
      elsif site == "s"
        puts "extract=#{node['oracle']['extract']}"
        puts "extract=#{node['oracle']['extract_to_iac']}"
        puts "pump_to_r1=#{node['oracle']['pump_to_r1']}"
        puts "pump_to_iac=#{node['oracle']['pump_to_iac']}"
        puts "pump_to_aws_iac=#{node['oracle']['pump_to_aws_iac']}"
        puts "replicat=#{node['oracle']['replicat_from_r1']}"
        puts "remote_db=#{node['oracle']['remote_db']}"
        puts "remote_host=#{node['oracle']['remote_host']}"
        puts "remote_port=#{node['oracle']['remote_port']}"

        ogg_progress = [
          'extract',
          'extract_to_iac',
          'pump_to_r1',
          'pump_to_iac',
          'pump_to_aws_iac',
          'replicat_from_r1',
        ]
      end

      ogg_progress.each do |process|
        process_type=process.split('_').first
        r = Chef::Resource::Template.new("#{node['oracle'][process]}.prm", run_context)
        r.path       "#{File.join(node['oracle']['goldengate_home'], 'dirprm', "#{node['oracle'][process]}.prm")}"
        r.source     "#{process_type}.prm.erb"
        r.cookbook   "goldengate_hub"
        r.owner      node['oracle']['primary_user_name']
        r.group      node['oracle']['primary_group_name']
        r.mode       00644
        r.variables ({
          :ggt_replication => "#{node['oracle']['ggt_replication'][process]}",
          :ddl_replication => "#{node['oracle']['ddl_replication'][process]}",
          :trail_location => "#{node['oracle']['trail_location'][process]}",
          :trail_format_option => "#{node['oracle']['trail_format_option'][process]}",
          :process => "#{node['oracle'][process]}",
          :remote_host => "#{node['oracle']['remote_host'][process]}",
          :remote_port => "#{node['oracle']['remote_port'][process]}",
          :remote_db => "#{node['oracle']['remote_db'][process]}"
        })
        r.run_action :create

        s = Chef::Resource::Template.new("create_#{node['oracle'][process]}.oby", run_context)
        s.path       "#{File.join(node['oracle']['goldengate_home'], 'diroby', "create_#{node['oracle'][process]}.oby")}"
        s.source     "create_#{process_type}.oby.erb"
        s.cookbook   "goldengate_hub"
        s.owner      node['oracle']['primary_user_name']
        s.group      node['oracle']['primary_group_name']
        s.mode       00644
        s.variables ({
          :trail_location => "#{node['oracle']['trail_location'][process]}",
          :process => "#{node['oracle'][process]}",
          :remote_host => "#{node['oracle']['remote_host'][process]}",
          :remote_port => "#{node['oracle']['remote_port'][process]}",
          :remote_db => "#{node['oracle']['remote_db'][process]}"
        })
        s.run_action :create
      end
    end
  end

  template File.join(node['oracle']['goldengate_home'], 'dirprm', 'mgr.prm') do
    source 'mgr.prm.erb'
    owner node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00644'
  end

  file File.join(node['oracle']['goldengate_home'], 'diroby', 'dblogin_s.oby') do
    content "dblogin USERIDALIAS ggsource\n"
    user node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00644'
  end

  file File.join(node['oracle']['goldengate_home'], 'diroby', 'dblogin_t.oby') do
    content "dblogin USERIDALIAS ggtarget\n"
    user node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00644'
  end

  directory File.join(node['oracle']['goldengate_home'], 'dirdat', node['oracle']['sid']) do
    user node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00700'
  end

  directory File.join(node['oracle']['goldengate_home'], 'dirdat', node['oracle']['trail_location']['extract_to_iac']) do
    user node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00700'
  end

  if site == "p"
    directory File.join(node['oracle']['goldengate_home'], 'dirdat', node['oracle']['remote_db']['replicat_from_qdc']) do
      user node['oracle']['primary_user_name']
      group node['oracle']['primary_group_name']
      mode '00700'
    end

    directory File.join(node['oracle']['goldengate_home'], 'dirdat', node['oracle']['remote_db']['replicat_from_r2']) do
      user node['oracle']['primary_user_name']
      group node['oracle']['primary_group_name']
      mode '00700'
    end
  elsif site == "s"
    directory File.join(node['oracle']['goldengate_home'], 'dirdat', node['oracle']['remote_db']['replicat_from_r1']) do
      user node['oracle']['primary_user_name']
      group node['oracle']['primary_group_name']
      mode '00700'
    end
  end

  directory File.join('', 'u02', 'ogg') do
    user node['oracle']['primary_user_name']
    group node['oracle']['primary_group_name']
    mode '00700'
    only_if 'test -d /u02' 
  end
end

# Save Goldengate wallet and credential files on the share memory
directory File.join('', 'dev', 'shm', 'goldengate') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

directory File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}") do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

directory File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dirwlt') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

link File.join(node['oracle']['goldengate_home'], 'dirwlt', 'cwallet.sso') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
  to File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dirwlt', 'cwallet.sso')
end

directory File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dircrd') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
end

link File.join(node['oracle']['goldengate_home'], 'dircrd', 'cwallet.sso') do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  mode '00700'
  to File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dircrd', 'cwallet.sso')
end

bash 'sync wallet file' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  output_file=File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dirwlt', 'cwallet.sso')
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    if [ ! -f #{output_file} ]; then
      # The wallet file is on IDPS (cluster folder)
      if [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/c#{cluster_num}/dirwlt/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/c#{cluster_num}/dirwlt/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}

      # The wallet file is on IDPS (default cluster folder)
      elif [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dirwlt/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dirwlt/cwallet.sso --output #{output_file}
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
  output_file=File.join('', 'dev', 'shm', 'goldengate', "c#{cluster_num}", 'dircrd', 'cwallet.sso')
  code <<-EOH
    source #{node['oracle']['bash_profile']}
    if [ ! -f #{output_file} ]; then
      # The wallet file is on IDPS (cluster folder)
      if [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/c#{cluster_num}/dircrd/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/c#{cluster_num}/dircrd/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}

      # The wallet file is on IDPS (default cluster folder)
      elif [ `/usr/local/bin/stash list --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --filter secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dircrd/ 2>&1 |grep "cwallet.sso" |wc -l` -gt 0 ]; then
        echo "The wallet file cwallet.sso is on IDPS. Downloading it"
        /usr/local/bin/stash get --api-key-grant-policy-id #{node['oracle']['idps_policy_id']} --api-endpoint #{node['oracle']['idps_endpoint']} --secret-name secrets/goldengate/#{cluster_env}/#{node['oracle']['region']}/default/dircrd/cwallet.sso --output #{output_file}
	sudo chown #{node['oracle']['primary_user_name']}:#{node['oracle']['primary_group_name']} #{output_file}
      
      else
        echo "ACTION: Please generate and upload the credential file cwallet.sso to IDPS manually"
      fi
    fi
  EOH
end

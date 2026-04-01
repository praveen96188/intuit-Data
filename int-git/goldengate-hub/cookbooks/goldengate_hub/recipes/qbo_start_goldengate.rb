#
# Cookbook Name:: goldengate_hub
# Recipe:: _start_goldengate
#
# Copyright (c) 2016 The Authors, All Rights Reserved.

bash 'start Goldengate processes and reinstall crontab' do
  user node['oracle']['primary_user_name']
  group node['oracle']['primary_group_name']
  code <<-EOH
    if [ -f /u01/ogg/scripts/post_reboot_job.sh ]; then
      source #{node['oracle']['bash_profile']}
      chmod +x /u01/ogg/scripts/post_reboot_job.sh
      /u01/ogg/scripts/post_reboot_job.sh 
    fi
    if [ -f /u01/ogg/scripts/auto_start.flag ]; then
      source #{node['oracle']['bash_profile']}
      cd $GG_HOME
      ./ggsci << EOF
        start manager
        sh sleep 5
        start e*
        start p*
        start r*
EOF
      crontab /u01/ogg/scripts/crontab.txt 
    fi
    ln -fs /u01/AWS /home/oracle/AWS
EOH
end


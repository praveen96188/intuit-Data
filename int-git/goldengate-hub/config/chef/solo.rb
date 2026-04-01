### Set chef homedir
Ohai::Config[:disabled_plugins] = [:Passwd]

# Set chef homedir
chef_home_dir File.expand_path('/var/chef/chef-repo')

file_cache_path File.join(chef_home_dir,'cache')
file_backup_path File.join(chef_home_dir,'backup')
cookbook_path [File.join(chef_home_dir, 'cookbooks'),
               File.join(chef_home_dir, 'vendor', 'cookbooks')]
environment_path File.join(chef_home_dir, 'environments')
role_path       File.join(chef_home_dir, 'roles')
data_bag_path   File.join(chef_home_dir, 'data_bags')

# Chef log file
log_location File.expand_path('/var/log/chef/chef.log')

# goldengate_hub

This Chef recipe has been saved to S3 in all Voyager accounts. It will be automatically executed with the Goldengate hub creation AWS Cloudformation as follows:

1. Down load the Cloudformation template and shell scripts under the directory cloudformation
goldengate-hub.json
create_gg_hub_w_cf.sh
ceate_gg_hub_w_cf_prod.sh

2. Run the command as follows:

for preprod:
./create_gg_hub_w_cf.sh <AppEnv> <DBName>
        where <AppEnv> should be qa, e2e, prf or prod

for prod:
./create_gg_hub_w_cf_prod.sh <AppEnv> <DBName>
        where <AppEnv> should be qa, e2e, prf or prod



The Chef recipe can be executed with the following commands after EC2 instance has been created.
  
Login as root

cd /var/chef
chef-solo -l debug -c /var/chef/chef-repo/cookbooks/var/chef/solo.rb -o recipe[goldengate_hub]


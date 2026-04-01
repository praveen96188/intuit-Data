--# create read-only user/role with permission to login and grant needful permissions #--
create user username with login password 'xxxxx';
--grant read only role to users
grant ibobadm_readonly_role to username;
alter user <username> set search_path to ibobadm;


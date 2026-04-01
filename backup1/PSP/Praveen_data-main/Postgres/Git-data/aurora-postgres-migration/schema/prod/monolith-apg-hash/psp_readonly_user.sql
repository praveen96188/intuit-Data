-- switch to psppp001 database
\c psppp001

--# create read-only user for Finance reports permission to login and grant needful permissions #--
create user pspqsfinadmin with password 'xxxxxx';
grant pspadm_readonly_role to pspqsfinadmin;
alter user pspqsfinadmin set search_path to pspadm;
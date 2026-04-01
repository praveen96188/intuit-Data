-- switch to prodapgib database
\c prodapgib

create role ibobadm_readonly_role;
grant connect on database prodapgib to ibobadm_readonly_role;
grant usage on schema ibobadm to ibobadm_readonly_role;
grant select on all tables in schema ibobadm to ibobadm_readonly_role;
alter default privileges in schema ibobadm grant select on tables to ibobadm_readonly_role;

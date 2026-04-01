create schema if not exists mockclientadm;
create user mockclientapp_e2e PASSWORD 'xxxxxxxxxx';

grant usage on schema mockclientadm to mockclientapp_e2e; 
grant all on all tables in schema mockclientadm  to mockclientapp_e2e;
grant all on all sequences in schema mockclientadm to mockclientapp_e2e;
grant all on all functions in schema mockclientadm to mockclientapp_e2e;

alter default privileges in schema mockclientadm grant all on tables to mockclientapp_e2e;
alter default privileges in schema mockclientadm grant all on sequences to mockclientapp_e2e;
alter default privileges in schema mockclientadm grant all on functions to mockclientapp_e2e;

create user mockclientapp_prf PASSWORD 'xxxxxxxxxx';

grant usage on schema mockclientadm to mockclientapp_prf; 
grant all on all tables in schema mockclientadm  to mockclientapp_prf;
grant all on all sequences in schema mockclientadm to mockclientapp_prf;
grant all on all functions in schema mockclientadm to mockclientapp_prf;

alter default privileges in schema mockclientadm grant all on tables to mockclientapp_prf;
alter default privileges in schema mockclientadm grant all on sequences to mockclientapp_prf;
alter default privileges in schema mockclientadm grant all on functions to mockclientapp_prf;
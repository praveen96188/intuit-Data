set search_path to pspadm;

create index psp_quickbooks_i3 on pspadm.psp_quickbooks_info USING BTREE (process_transmissions);


\timing

set session work_mem='64MB';
set session maintenance_work_mem='25098MB';
set search_path to pspadm;
SELECT CURRENT_TIMESTAMP;

create index concurrently psp_property_audit_i1_p0 on pspadm.psp_property_audit_p0 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p1 on pspadm.psp_property_audit_p1 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p2 on pspadm.psp_property_audit_p2 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p3 on pspadm.psp_property_audit_p3 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p4 on pspadm.psp_property_audit_p4 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p5 on pspadm.psp_property_audit_p5 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p6 on pspadm.psp_property_audit_p6 USING BTREE (company_fk, class_name, property_name);
create index concurrently psp_property_audit_i1_p7 on pspadm.psp_property_audit_p7 USING BTREE (company_fk, class_name, property_name);


create index psp_property_audit_i1 ON ONLY pspadm.psp_property_audit USING BTREE (company_fk, class_name, property_name);
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p0 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p1 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p2 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p3 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p4 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p5 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p6 ;
alter index pspadm.psp_property_audit_i1 attach partition pspadm.psp_property_audit_i1_p7 ;


SELECT CURRENT_TIMESTAMP;

-- Ver 1704.3 #### 03/15/2017

CREATE OR REPLACE TRIGGER qbo.qbovpd_ddl_trigger2  AFTER CREATE on database  begin  if ( ora_dict_obj_owner = 'QBO_DATA' ) then  if ( ora_dict_obj_type = 'TABLE' AND ora_dict_obj_name not like 'SYS_JOURNAL%') then  dbms_rls.add_policy('QBO_DATA',  ora_dict_obj_name,  'QBO_VPD_POLICY',  'QBO',  'QBO_VPD_Package.VPD_Security',  'SELECT, INSERT, UPDATE, DELETE',  TRUE,  policy_type => DBMS_RLS.SHARED_CONTEXT_SENSITIVE );  end if;  end if;  end;
/

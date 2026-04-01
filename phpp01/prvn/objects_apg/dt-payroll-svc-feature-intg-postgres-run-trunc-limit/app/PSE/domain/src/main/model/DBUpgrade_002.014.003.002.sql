--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column RETAIL;
DECLARE v_column_exists number := 0; 
BEGIN
    Select count(*) into v_column_exists
    from user_tab_cols
    where column_name = 'RETAIL'
    and table_name = 'PSP_ENTITLEMENT';
 
	if (v_column_exists = 0) then
		execute immediate 'ALTER TABLE PSP_ENTITLEMENT ADD RETAIL NUMBER(1) DEFAULT 0 NOT NULL';
	end if;
end;
/

PROMPT finished DBUpgrade_002.014.003.002.sql
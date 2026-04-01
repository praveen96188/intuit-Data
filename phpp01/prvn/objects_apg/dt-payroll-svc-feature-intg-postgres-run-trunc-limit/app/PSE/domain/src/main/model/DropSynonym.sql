set define on
set echo off
set feedback off
set trimspool on
set sqlcase mixed
set arraysize 1
set verify off
set serveroutput on
--
-- Script to drop all the synonyms from APP user
-- need to switch between v1 and v2 branches in QA
-- only used with destructive DB deployments.
-- 
-- Assumes you are logged in as APP USER

BEGIN

          FOR rec in  ( SELECT 'DROP SYNONYM  '||object_name v_sqlstmt
                            FROM user_objects 
                           WHERE object_type ='SYNONYM'                           
                    ) 
           LOOP
            
                   --dbms_output.put_line('synonym'||rec.v_sqlstmt);
			BEGIN
			
                   EXECUTE IMMEDIATE rec.v_sqlstmt;    
			EXCEPTION 
			WHEN OTHERS THEN
				dbms_output.put_line('Cannot drop synonym '||sqlerrm);
				RETURN;         
		    END;  

	 END LOOP;
    
EXCEPTION 
WHEN OTHERS THEN
     dbms_output.put_line('Cannot create synonym '||sqlerrm);
     RETURN;         
END;  
/
exit
/
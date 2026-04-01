-----------------------------------------------------------------------------------
-- dbinstall.sql - Main installation script

-- Prerequisites before running the scrits
	-- There must be a tablespace created and users (PSPADM,PSPAPP,PSPREAD,PSPREPORTS) alreay created. (create_pspusers.sql)
	-- DB Objects are created prior to running the scripts
	-- Admin user has CREATE ANY SYNONYM grants, part of create user scripts

--  Assumptions
	-- This scripts will run from PSPADM or any admin schema
	-- takes 5 input parameter
		--  p_appuser   application user typically PSPAPP, PSPREPORT, PSPSUPPORT
   		--  p_rolename  role name typically 'PSPAPP_ROLE' appuser_ROLE
   		--  p_privilege'CRUD' or 'SELECT' for readonly schemas
                --  p_readuser 'PSPREAD'
 		--  p_readrole  'PSPREAD_ROLE'
	
-- Used for
	-- Run from the deploy scripts
	-- Creates the db role if does not exists.
        -- creates proper grants for the role
 	-- assigns role to the user
 	-- creates synonyms for the user
  
-- How to run from command line
	-- SQLPLUS username/pwd@connectstring @InstallDB.sql PSPAPP PSPAPP_ROLE CRUD PSPREAD PSPREAD_ROLE

--   p_appuser      varchar2 DEFAULT 'PSPAPP', -- PSPAPP, PSPREPORT, PSPSUPPORT
--   p_rolename     varchar2 DEFAULT 'PSPAPP_ROLE', 
--   p_privilege    varchar2 DEFAULT 'CRUD' -- 'CRUD' or 'SELECT'
--   p_readuser     varchar2 DEFAULT 'PSPREAD'
--   P_readrole     varchar2 DEFAULT 'PSPREAD_ROLE'
-----------------------------------------------------------------------------------
	
set define on
set echo off
set feedback off
set trimspool on
set sqlcase mixed
set arraysize 1
set verify off
set serveroutput on
spool InstallDB.log append

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "START TIME" from dual
/
select user AS "USER Name" from dual
/


PROMPT
PROMPT =============================================
PROMPT Step 1.Decide if it is a new install or an upgrade
PROMPT =============================================

spool off

set termout off 
set heading off
spool CreateOrUpdateDB.sql;
select 
   case when exists(select * from user_objects) then '@DBUpgrade.sql'
   else '@DBCreate.sql'
   end
from dual;
select 
   case when exists(select * from USER_CONSTRAINTS) then '@DB_Update_Constraints.sql'
   else '@DB_Generated_Constraints.sql'
   end
from dual;
spool off
set termout on 
set heading on

spool InstallDB.log append

PROMPT
PROMPT =============================================
PROMPT Step 2.Create or update database objects (TABLES, INDEXES, FKS, PKS)
PROMPT =============================================

@CreateOrUpdateDB.sql

PROMPT
PROMPT ================================================
PROMPT Step 3. Populate tables and create procedures/triggers 
PROMPT =================================================
PROMPT

SET DEFINE OFF

@DBPopulate.sql


PROMPT
PROMPT ================================================
PROMPT Step 4.Create security objects (ROLES, SYNONIMS, GRANTS)
PROMPT =================================================

SET DEFINE ON

DEFINE p_appuser   = '&&1' ;
DEFINE p_rolename  = '&&2' ;
DEFINE p_privilege= '&&3' ;
DEFINE p_readuser = '&&4' ;
DEFINE p_readrole = '&&5' ;

DECLARE
  
    v_sqlstr       varchar2(300);
    v_table_previlege varchar2(300);
    
    ROLE_ALREADY_EXISTS exception;

    DB_OBJECT_NAME_ALREADY_USED exception;

    Pragma exception_init(ROLE_ALREADY_EXISTS ,-01921); 
    	-- ORA-01921: role name 'PSPAPP_ROLE' conflicts with another user or role name
    
    Pragma exception_init(DB_OBJECT_NAME_ALREADY_USED, -00955);
    	-- ORA-00955: name is already used by an existing object
    

BEGIN

-----------------------------------------------------------------------------------
--1.a. Create APP ROLE
-----------------------------------------------------------------------------------    
  IF '&&p_appuser' != 'NONE' THEN
    BEGIN
    
        v_sqlstr := 'CREATE ROLE &&p_rolename';
       
        EXECUTE IMMEDIATE v_sqlstr;
        
    EXCEPTION 
    WHEN ROLE_ALREADY_EXISTS THEN
         null;
         
    WHEN OTHERS THEN
        null; -- handle pragma exception here
        dbms_output.put_line('Cannot create role '||sqlerrm||' '||v_sqlstr);
        RETURN;
    END;
  END IF;
-----------------------------------------------------------------------------------
--1.b. Create READ ROLE
-----------------------------------------------------------------------------------    
  IF '&&p_readuser' != 'NONE' THEN
    BEGIN
    
        v_sqlstr := 'CREATE ROLE &&p_readrole';
       
        EXECUTE IMMEDIATE v_sqlstr;
        
    EXCEPTION 
    WHEN ROLE_ALREADY_EXISTS THEN
         null;
         
    WHEN OTHERS THEN
        null; -- handle pragma exception here
        dbms_output.put_line('Cannot create role '||sqlerrm||' '||v_sqlstr);
        RETURN;
    END;
  END IF;
-----------------------------------------------------------------------------------    
--2.a. GRANT Privileges to the APPROLE
-----------------------------------------------------------------------------------    
    IF '&&p_appuser' != 'NONE' THEN
       IF '&&p_privilege' = 'CRUD' THEN

           BEGIN
    
                   v_table_previlege := ' SELECT, UPDATE, INSERT, DELETE ';
            
               FOR rec in  ( SELECT 'GRANT '||
                                 DECODE(object_type,'TABLE',v_table_previlege,
                                                    'VIEW',' SELECT ',
                                                    'SEQUENCE',' SELECT ',
                                                    'PACKAGE',' EXECUTE ',
                                                    'PROCEDURE',' EXECUTE ',
                                                    'FUNCTION', ' EXECUTE ') ||
                                  ' ON '||object_name ||' TO ' ||'&&p_rolename' v_sqlstmt
                            FROM user_objects 
                           WHERE object_type IN ('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND
                                 object_name not like 'BIN%'
                    ) 
               LOOP
		--dbms_output.put_line(rec.v_sqlstmt);
                  EXECUTE IMMEDIATE rec.v_sqlstmt;
               END LOOP;
          END;        
       ELSE
           BEGIN
    
               v_table_previlege := ' SELECT ';
            
               FOR rec in  ( SELECT 'GRANT '|| v_table_previlege || ' ON '||
                                  object_name ||' TO ' ||'&&p_rolename' v_sqlstmt
                            FROM user_objects 
                           WHERE object_type IN ('TABLE','VIEW') AND
                                 object_name not like 'BIN%'
                    ) 
               LOOP
                   EXECUTE IMMEDIATE rec.v_sqlstmt;    
               END LOOP;
           END;    
       END IF;
    END IF;
-----------------------------------------------------------------------------------    
--2.b. GRANT Privileges to the READ ROLE
-----------------------------------------------------------------------------------    
    IF '&&p_readuser' != 'NONE' THEN
                  BEGIN
    
               v_table_previlege := ' SELECT ';
            
             
               FOR rec in  ( SELECT 'GRANT '|| v_table_previlege || ' ON '||
                                  object_name ||' TO ' ||'&&p_readrole' v_sqlstmt
                            FROM user_objects 
                           WHERE object_type IN ('TABLE','VIEW') AND
                                 object_name not like 'BIN%'
                    ) 
               LOOP
                   EXECUTE IMMEDIATE rec.v_sqlstmt;    
               END LOOP;
           END;    
       END IF;
  
	
-----------------------------------------------------------------------------------
--3.a. Grant role to the app user
-----------------------------------------------------------------------------------
    IF '&&p_appuser' != 'NONE' THEN
       BEGIN
    
           EXECUTE IMMEDIATE 'GRANT '||'&&p_rolename' ||' TO '|| '&&p_appuser';
        
       EXCEPTION WHEN OTHERS THEN
           null; -- handle pragma exception here
           dbms_output.put_line('Cannot GRANT '||sqlerrm);
           return;     
       END;
    END IF;
-----------------------------------------------------------------------------------
--3.b. Grant role to the read user
-----------------------------------------------------------------------------------
    IF '&&p_readuser' != 'NONE' THEN
       BEGIN
    
           EXECUTE IMMEDIATE 'GRANT '||'&&p_readrole' ||' TO '|| '&&p_readuser';
        
       EXCEPTION WHEN OTHERS THEN
           null; -- handle pragma exception here
           dbms_output.put_line('Cannot GRANT '||sqlerrm);
           return;     
       END;
    END IF;
-----------------------------------------------------------------------------------
--4.a. Create Private synonym for the app user
-----------------------------------------------------------------------------------
    IF '&&p_appuser' != 'NONE' THEN
       BEGIN
     
          FOR rec in  ( SELECT 'CREATE SYNONYM  '||'&&p_appuser'||'.'||object_name
                                ||' FOR '||user||'.'||object_name v_sqlstmt
                            FROM user_objects 
                           WHERE object_type IN ('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND
                                 object_name not like 'BIN%'
                    ) 
           LOOP
            
                   --dbms_output.put_line('synonym'||rec.v_sqlstmt);
			BEGIN
			
                   EXECUTE IMMEDIATE rec.v_sqlstmt;    
			EXCEPTION 
			WHEN DB_OBJECT_NAME_ALREADY_USED THEN
				null; -- do nothing
			WHEN OTHERS THEN
				dbms_output.put_line('Cannot create synonym '||sqlerrm);
				RETURN;         
		    END;  

		  END LOOP;
    
       EXCEPTION 
       WHEN DB_OBJECT_NAME_ALREADY_USED THEN
           null; -- do nothing

       WHEN OTHERS THEN

           dbms_output.put_line('Cannot create synonym '||sqlerrm);
           RETURN;         
       END;  
    END IF;


-----------------------------------------------------------------------------------
--4.b. Create Private synonym for the read user
-----------------------------------------------------------------------------------
    IF '&&p_readuser' != 'NONE' THEN
       BEGIN
     
          FOR rec in  ( SELECT 'CREATE SYNONYM  '||'&&p_readuser'||'.'||object_name
                                ||' FOR '||user||'.'||object_name v_sqlstmt
                            FROM user_objects 
                           WHERE object_type IN ('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND
                                 object_name not like 'BIN%'
                    ) 
           LOOP
            
                   --dbms_output.put_line('synonym'||rec.v_sqlstmt);
			BEGIN
			
                   EXECUTE IMMEDIATE rec.v_sqlstmt;    
			EXCEPTION 
			WHEN DB_OBJECT_NAME_ALREADY_USED THEN
				null; -- do nothing
			WHEN OTHERS THEN
				dbms_output.put_line('Cannot create synonym '||sqlerrm);
				RETURN;         
		    END;  

		  END LOOP;
    
       EXCEPTION 
       WHEN DB_OBJECT_NAME_ALREADY_USED THEN
           null; -- do nothing

       WHEN OTHERS THEN

           dbms_output.put_line('Cannot create synonym '||sqlerrm);
           RETURN;         
       END;  
    END IF;


-----------------------------------------------------------------------------------
--4.c. Drop Extra Synonyms owned by PSPAPP user 
-----------------------------------------------------------------------------------

DECLARE
  BEGIN
   FOR rec
      IN (SELECT 'DROP SYNONYM &&p_appuser' || '.' || synonym_name as v_sqlstmt
            FROM all_synonyms
           WHERE owner = upper('&&p_appuser') AND table_owner = USER
                 AND synonym_name NOT IN
                        (SELECT object_name
                           FROM user_objects
                          WHERE object_type IN('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND object_name NOT LIKE 'BIN%'))
   LOOP
      EXECUTE IMMEDIATE rec.v_sqlstmt;
   END LOOP;
END;

-----------------------------------------------------------------------------------
--4.d. Drop Extra Synonyms owned by PSPREAD user 
-----------------------------------------------------------------------------------

DECLARE
  BEGIN
   FOR rec
      IN (SELECT 'DROP SYNONYM &&p_readuser' || '.' || synonym_name as v_sqlstmt
            FROM all_synonyms
           WHERE owner = upper('&&p_readuser') AND table_owner = USER
                 AND synonym_name NOT IN
                        (SELECT object_name
                           FROM user_objects
                          WHERE object_type IN('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND object_name NOT LIKE 'BIN%'))
   LOOP
      EXECUTE IMMEDIATE rec.v_sqlstmt;
   END LOOP;
END;

END;
/




PROMPT 
PROMPT ================================================
PROMPT Step 5. Run Post installation Script
PROMPT =================================================
PROMPT

SET DEFINE ON

@PostInstallDB.sql &&1 &&2 

select to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') AS "END TIME" from dual
/

spool off
/

exit
/

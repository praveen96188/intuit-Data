-- create synonyms
-- SQLPLUS username/pwd@connectstring @InstallDB.sql PSPAPP PSPAPP_ROLE CRUD PSPREAD PSPREAD_ROLE

-- p_appuser varchar2 DEFAULT 'PSPAPP', -- PSPAPP, PSPREPORT, PSPSUPPORT
-- p_rolename varchar2 DEFAULT 'PSPAPP_ROLE',
-- p_privilege varchar2 DEFAULT 'CRUD' -- 'CRUD' or 'SELECT'
-- p_readuser varchar2 DEFAULT 'PSPREAD'
-- P_readrole varchar2 DEFAULT 'PSPREAD_ROLE'


SET DEFINE ON;

DEFINE p_appuser = 'PSPAPP' ;
DEFINE p_rolename = 'PSPAPP_ROLE' ;
DEFINE p_privilege= 'CRUD' ;
DEFINE p_readuser = 'PSPREAD' ;
DEFINE p_readrole = 'PSPREAD_ROLE' ;

DECLARE



v_sqlstr varchar2(300);
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

FOR rec in ( SELECT 'GRANT '||
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

FOR rec in ( SELECT 'GRANT '|| v_table_previlege || ' ON '||
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


FOR rec in ( SELECT 'GRANT '|| v_table_previlege || ' ON '||
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

FOR rec in ( SELECT 'CREATE SYNONYM '||'&&p_appuser'||'.'||object_name
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

FOR rec in ( SELECT 'CREATE SYNONYM '||'&&p_readuser'||'.'||object_name
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

commit;

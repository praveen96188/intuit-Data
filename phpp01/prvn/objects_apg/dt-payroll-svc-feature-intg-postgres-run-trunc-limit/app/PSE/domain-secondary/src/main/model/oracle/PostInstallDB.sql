
-- called from InstallDB.sql
-- same log file as InstallDB
-- call by passing APP user and APP role
-- 

--DEFINE p_appuser   = '&&1' ;
--DEFINE p_rolename  = '&&2' ;


PROMPT USER &&p_appuser
PROMPT ROLE &&p_rolename

PROMPT ===========================================================
PROMPT COMPILE ALL INVALID OBJECTS 
PROMPT ===========================================================


exec DBMS_UTILITY.COMPILE_SCHEMA ( user, FALSE);




PROMPT ===========================================================
PROMPT OBJECTS THAT ARE NOT VALID...
PROMPT ===========================================================

       SELECT 'ERROR ' ||object_name, object_type, status
         FROM user_objects
        WHERE status != 'VALID'
		      AND object_name not like 'BIN%'
        ORDER BY object_name, object_type;

PROMPT ===========================================================
PROMPT CONSTRAINTS THAT CANNOT BE ENABLED...
PROMPT ===========================================================

       SELECT 'WARNING ' ||table_name, constraint_name, status
         FROM user_constraints
        WHERE status <> 'ENABLED';

PROMPT ===========================================================
PROMPT OBJECTS WITHOUT ANY GRANTS
PROMPT ===========================================================

        SELECT 'ERROR ' ||object_name
          FROM   user_objects a          
         WHERE  
            a.object_type in ('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION')
            AND object_name not like 'BIN%'
            AND
         NOT EXISTS (SELECT '1'
                              FROM   USER_TAB_PRIVS b
                              WHERE  GRANTOR = user
                            AND GRANTEE = upper('&&p_rolename')
                            AND a.object_name = b.table_name) AND
         '&&p_appuser' != 'NONE'; 

							
PROMPT ===========================================================
PROMPT OBJECTS WITHOUT SYNONYMS
PROMPT ===========================================================

		SELECT 'ERROR ' ||table_name
		  FROM   user_tables a
         WHERE  NOT EXISTS (SELECT '1'
							  FROM   all_synonyms b
                             WHERE  b.synonym_name = a.table_name 
                               AND  b.table_owner  = user
                               AND  b.owner = upper( '&&p_appuser')) AND
         '&&p_appuser' != 'NONE'; 

PROMPT ===========================================================
PROMPT TOTAL OBJECT COUNTs
PROMPT ===========================================================
		
		SELECT  object_type, status, count(*)
		  FROM user_objects
		 GROUP BY object_type, status;

PROMPT ===========================================================
PROMPT TOTAL SYNONYM COUNTs
PROMPT ===========================================================

	     SELECT 'SYNONYMs count '||decode(sum(col1),sum(col2),'OK','ERROR'),
				sum(col1) "Total", 
				sum(col2) "Should be"
		  FROM 	(
		 SELECT 'SYNONYM', COUNT(*) col1 ,0 col2
           FROM all_synonyms b
          WHERE  b.table_owner  = user
	    AND  b.owner = upper( '&&p_appuser')
		  UNION
		 SELECT 'SYNONYM', 0, COUNT(*)
           FROM user_objects 
          WHERE object_type IN ('TABLE','VIEW','SEQUENCE','PACKAGE','PROCEDURE','FUNCTION') AND
                                 object_name not like 'BIN%' AND
	         '&&p_appuser' != 'NONE'); 
							   
CREATE OR REPLACE PROCEDURE PR_UTIL_RECOMPILE_DIY_PKGS
IS
    tmpVar     NUMBER;
    v_str      VARCHAR2(300);

BEGIN

    -- Design Note
	--   order matters when recompiling packages.

	--
	-- PACKAGES
	--

    -- global constants
    v_str := 'alter package PK_DIYDDTOPSP_CONST compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);


    -- common utilities
    v_str := 'alter package PK_DIYDDTOPSP_UTILS compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    v_str := 'alter package PK_DIYDDTOPSP_UTILS compile body';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);


	-- validation
    v_str := 'alter package PK_DIYDDTOPSP_VALIDATION compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    v_str := 'alter package PK_DIYDDTOPSP_VALIDATION compile body';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);


	-- controller
    v_str := 'alter package PK_DIYDDTOPSP_CONTROLLER compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    v_str := 'alter package PK_DIYDDTOPSP_CONTROLLER compile body';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);


	-- DIY company
    v_str := 'alter package PK_DIYDDTOPSP_CUSTOMER compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    v_str := 'alter package PK_DIYDDTOPSP_CUSTOMER compile body';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);


	-- DIY payrolls
    v_str := 'alter package PK_DIYDDTOPSP_PAYROLLS compile';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    v_str := 'alter package PK_DIYDDTOPSP_PAYROLLS compile body';
    EXECUTE IMMEDIATE (v_str);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

    
    -- this one might not be in production
    BEGIN
    
      SELECT DISTINCT Object_Name
        INTO v_str
        FROM USER_OBJECTS
       WHERE Object_Name = 'PK_TEST_DIY_MIGRATION_APIS';
       
      v_str := 'alter package PK_TEST_DIY_MIGRATION_APIS compile';
      EXECUTE IMMEDIATE (v_str);
      DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);

      v_str := 'alter package PK_TEST_DIY_MIGRATION_APIS compile body';
      EXECUTE IMMEDIATE (v_str);
      DBMS_OUTPUT.PUT_LINE('SUCCESS: ' || v_str);
       
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        NULL;
      WHEN OTHERS THEN
        NULL;
    END;


  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Error with ' || v_str);
	  RAISE;

END PR_UTIL_RECOMPILE_DIY_PKGS; 
/


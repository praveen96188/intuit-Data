CREATE OR REPLACE PACKAGE BODY ${schema}.${name} AS

  PROCEDURE HELLO(ARG DATE) IS
    BEGIN
    DBMS_OUTPUT.PUT_LINE('Hello World ' || ARG);
  EXCEPTION 
    WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error in HELLO, code=' || sqlcode || ', error=' || sqlerrm);
  END;

END;
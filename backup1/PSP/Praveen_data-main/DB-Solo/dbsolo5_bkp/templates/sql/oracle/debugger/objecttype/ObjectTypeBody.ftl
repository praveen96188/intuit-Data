CREATE OR REPLACE TYPE BODY ${schema}.${name} AS

  MEMBER PROCEDURE HELLO(ARG DATE) IS
    BEGIN
    DBMS_OUTPUT.PUT_LINE('Hello World ' || ARG);
  EXCEPTION 
    WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error in HELLO, code=' || sqlcode || ', error=' || sqlerrm);
  END;

END;
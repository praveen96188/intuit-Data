SET SERVEROUTPUT ON

DECLARE
  v_count        NUMBER;
  v_diy_status   VARCHAR2(100);
  
BEGIN

  DBMS_OUTPUT.PUT_LINE ('Start ' || TO_CHAR(SYSDATE, 'HH24:MI'));
  
  FOR i IN (
    SELECT PSP_USERID AS SRCID
      FROM DIY_MIG_QUEUE
  )
  LOOP
  
    -- change the date in timestamp
  
    SELECT COUNT(*)
      INTO v_count 
      FROM diy_iqach 
     WHERE ach_userid    = i.SRCID
       AND ACH_TIMESTAMP > 20081114210000;
    
    IF (v_count > 0) THEN
      
      SELECT TRIM(CLI_STATUS)
        INTO v_diy_status
        FROM DIY_IQCLIENT
       WHERE CLI_USERID = i.SRCID;
      
      DBMS_OUTPUT.PUT_LINE (
        i.SRCID  || ' - ' || 
        v_count  || ' - ' ||
        v_diy_status
      );
        
    END IF;
    
  END LOOP;
  
  DBMS_OUTPUT.PUT_LINE ('End ' || TO_CHAR(SYSDATE, 'HH24:MI'));
     
END;
/
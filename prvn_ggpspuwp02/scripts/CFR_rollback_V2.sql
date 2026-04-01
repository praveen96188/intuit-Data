DECLARE

BEGIN

 FOR emp_rec in   ( select * from pspadm.cfr_backup)
 LOOP
  
   dbms_output.put_line('Emp seq '||emp_rec.employee_seq || 'is being update back to CFR '||emp_rec.consumer_realm_id);
   update pspadm.psp_employee set consumer_realm_id = emp_rec.consumer_realm_id where  employee_seq =emp_rec.employee_seq ;
 END LOOP;
 
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;
/


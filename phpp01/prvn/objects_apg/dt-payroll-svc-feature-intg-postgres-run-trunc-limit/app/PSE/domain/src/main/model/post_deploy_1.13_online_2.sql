SET echo ON;
SET serveroutput ON SIZE 1000000;

DECLARE
      update_counter INTEGER;
      commit_count NUMBER;
      err_num number;
      err_msg varchar2(200);
      CURSOR wuser_cur IS
            select rowid from PSP_QBDT_PAYCHECK_INFO where on_service=0 and modified_date > created_date;                                
      wuser_rec wuser_cur%ROWTYPE;

BEGIN
      update_counter:=0;
      commit_count:=15000;
      FOR wuser_rec IN wuser_cur
      LOOP
            update PSP_QBDT_PAYCHECK_INFO set on_service=1 where rowid=wuser_rec.rowid;
                                                                
            update_counter:=update_counter+1;

            IF update_counter = 0 THEN
                  dbms_application_info.set_action('rows: 0');
            END IF;
            IF MOD(update_counter, commit_count) = 0 THEN
                  COMMIT;
                                                
                  DBMS_LOCK.SLEEP(0.2);
            END IF;
      END LOOP;
      COMMIT;
      DBMS_OUTPUT.PUT_LINE('Total update count->' || update_counter || ' Updated on ->' || sysdate);
      EXCEPTION
      WHEN OTHERS THEN
      err_num := SQLCODE;
      err_msg := substr(SQLERRM, 1, 200);
      dbms_output.put_line('Error: '||err_num||':: '||err_msg);
END;

spool duplicate_cfr_remaining.txt
set serveroutput on;

DECLARE
   CURSOR duplicate_cfr_list IS
	select consumer_realm_id, count(*) as C from pspadm.psp_employee 
    where consumer_realm_id in ('9130346576101316','9130346726931496','9130346898216916','9130346898500096','9130346898591146','9130346898698556','9130346899379496','9130346899819976','9130346903681986','9130346908897276','9130346927804116','9130346944267436','9130346964068796','9130347074275256','9130347394430136','13633946200659554','9130346579729236','9130346606213806','9130346787732516','9130346819889576','9130346898749196','9130346969675056','9130346995912176','9130347001636686','9130347001691366','9130347001880276','9130347002127566','9130347002866376','9130347002985116','9130347008501356','9130347009305986','9130347009498456','9130347009676686','9130347024119806','9130347029039396','9130347030147666','9130347068180036','9130347068212856','9130347069398906','9130347069462256','9130347069462876','9130347069628496','9130347073998756','9130347074398386','9130347074412486','9130347087800376','9130347190702526','9130347208482426','9130347208496556','9130347210484496','9130347214840576','9130347216490166','9130347217001046','9130347232910556','9130347271398466','9130347272336136','9130347272526666','9130347272920896','9130347276176676','9130347285584916','9130347344678446','9130347345096836')
    group by consumer_realm_id having count(*) > 1 order by C desc;
 
    v_pstub_seq varchar2(200);
    v_employee_seq varchar2(200);
    v_count number :=1 ;
 
    v_emp_list_to_update varchar2(500);
    emp_array SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST();
BEGIN
	
    
  
	FOR duplicate_cfr_list_rec IN duplicate_cfr_list
    LOOP
        DBMS_OUTPUT.PUT_LINE('===================');
	    DBMS_OUTPUT.PUT_LINE ('CFR token ' || duplicate_cfr_list_rec.consumer_realm_id);
            SELECT PAYSTUB_SEQ into v_pstub_seq FROM(
                select PAYSTUB_SEQ from pspadm.psp_paystub where PSTUB_EMPLOYEE_INFO_FK in
                    (select PSTUB_EMPLOYEE_INFO_SEQ from pspadm.PSP_PSTUB_EMPLOYEE_INFO where EMPLOYEE_FK in
                        (select EMPLOYEE_SEQ from pspadm.PSP_EMPLOYEE where consumer_realm_id=duplicate_cfr_list_rec.consumer_realm_id))
                order by Paycheck_date desc
            ) WHERE ROWNUM<=1;
        
       DBMS_OUTPUT.PUT_LINE('The latest paystub seq is '|| v_pstub_seq); 
       
       select e.employee_seq into v_employee_seq from pspadm.PSP_PAYSTUB ps ,pspadm.PSP_PSTUB_EMPLOYEE_INFO psei,pspadm.PSP_EMPLOYEE e where --0d0ebb9f-1b25-4aca-8885-4ba6a494aa56
            ps.PSTUB_EMPLOYEE_INFO_FK= psei.PSTUB_EMPLOYEE_INFO_SEQ
            AND psei.employee_fk=e.employee_seq
            AND ps.PAYSTUB_SEQ=v_pstub_seq;
     
       DBMS_OUTPUT.PUT_LINE('The corresponding Employee seq  is  '|| v_employee_seq); 
     
          
        v_emp_list_to_update := '';
         emp_array.delete();
         v_count :=1;
        FOR employee_rec in (select * from pspadm.PSP_EMPLOYEE where consumer_realm_id=duplicate_cfr_list_rec.consumer_realm_id)
        LOOP
           
            
            IF employee_rec.employee_seq = v_employee_seq THEN
                 dbms_output.put_line('Skipping the Employee seq  '||employee_rec.employee_seq);
                 Continue;
            END IF;
            DBMS_OUTPUT.PUT_LINE('The employee seq is ' || employee_rec.employee_seq);
            --v_emp_list_to_update :=v_emp_list_to_update||''',';
            emp_array.extend();
            emp_array(v_count) :=  employee_rec.employee_seq;
            v_count :=v_count+1;
            
        END LOOP;
        
          
         
        --  dbms_output.put_line('v_count is '||v_count);
          select count(*) into v_count from pspadm.psp_employee where employee_seq in
          (select * from table(emp_array));
           
           
         dbms_output.put_line('Count of employees to be updated is '||v_count||' For cfr '||duplicate_cfr_list_rec.consumer_realm_id );
           
         FOR emp_rec in   ( select * from pspadm.psp_employee where employee_seq in
          (select * from table(emp_array)))
         LOOP
              dbms_output.put_line('Emp whose cfr is being update to null  is  '||emp_rec.employee_seq);
         END LOOP;
       
          -- TODO uncomment in PROD ::: update employees cfr to null in v_emp_list_to_update
          update pspadm.psp_employee set consumer_realm_id = NULL where  employee_seq in
           (select * from table(emp_array));
        
    END LOOP;
	 
	
	
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;
/

spool off


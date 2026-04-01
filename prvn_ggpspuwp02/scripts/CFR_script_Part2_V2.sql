spool duplicate_cfr_20.txt
set serveroutput on;

DECLARE
   CURSOR duplicate_cfr_list IS
	select consumer_realm_id, count(*) as C from pspadm.psp_employee 
    where consumer_realm_id in ('1055567450',
'1065951635',
'1065987090',
'1066692360',
'1080535485',
'1086990375',
'1092334945',
'1094455695',
'1097508300',
'1107442160',
'1149641820',
'1149915990',
'1156573465',
'1169788175',
'1171239505',
'1172436405',
'1173069470',
'1182043320',
'1182913630',
'1183381135')
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


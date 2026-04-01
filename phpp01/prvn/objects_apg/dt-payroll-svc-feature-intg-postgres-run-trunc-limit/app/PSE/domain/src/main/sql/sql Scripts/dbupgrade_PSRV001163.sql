-- PSRV001163
-- Team track to update records so that we can add an unique index.
-- Manual script to be run prior to the deploy
-- 
-- RUN AS PSPADM user

SET SERVEROUTPUT ON;

SPOOL dbupgrade_PSRV001163.log

PROMPT DUPLICATE COMPANIES
select fed_Tax_id, round(created_date,'HH') cr_date , count(*) from psp_Company
    group by   fed_Tax_id, round(created_date,'HH')
    having count(*) > 1 ;

PROMPT count number of transactions to update
begin
FOR rec in 
    (select fed_Tax_id, round(created_date,'HH') cr_date , count(*) from psp_Company
    group by   fed_Tax_id, round(created_date,'HH')
    having count(*) > 1 )
LOOP
    UPDATE PSP_COMPANY
    SET created_date = created_date + 1/24,        
        modifier_id ='MANUAL_091409', 
        modified_Date = systimestamp
    WHERE
        FED_TAX_ID = rec.FED_TAX_ID
        and round(created_date,'HH') = rec.cr_date 
        and rownum < 2;
END LOOP;
end;
/
PROMPT DUPLICATE COMPANIES
select fed_Tax_id, round(created_date,'HH') cr_date , count(*) from psp_Company
    group by   fed_Tax_id, round(created_date,'HH')
    having count(*) > 1 ;

-- should be no rows returned


PROMPT Done

SPOOL OFF; 
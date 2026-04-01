
--- THIS IS A POST DEPLOY SCRIPT.

select systimestamp as Start_time_post_deloy_1 from dual;
--ALTER TABLE PSP_EMPLOYER_CONTRIBUTION add (QBDT_PAYLINE_INFO_FK varchar2(255) null);
--select count(*) from psp_employer_contribution where QBDT_PAYLINE_INFO_FK is not null;

merge into psp_qbdt_payline_info
using psp_employer_contribution on (psp_qbdt_payline_info.QBDT_PAYLINE_INFO_SEQ = psp_employer_contribution.QBDT_PAYLINE_INFO_FK)
when matched then update set employer_contribution_fk = employer_contribution_seq;

commit;


--select count(*) from psp_qbdt_payline_info where employer_contribution_fk is not null;

--ALTER TABLE  psp_compensation add (QBDT_PAYLINE_INFO_FK varchar2(255) null);
--select count(*) from psp_compensation where QBDT_PAYLINE_INFO_FK is not null;

merge into psp_qbdt_payline_info
using psp_compensation on (psp_qbdt_payline_info.QBDT_PAYLINE_INFO_SEQ = psp_compensation.QBDT_PAYLINE_INFO_FK)
when matched then update set compensation_fk = compensation_seq;
commit;


--select count(*) from psp_qbdt_payline_info where compensation_fk is not null;

--ALTER TABLE  psp_deduction add (QBDT_PAYLINE_INFO_FK varchar2(255) null);
--select count(*) from psp_deduction where QBDT_PAYLINE_INFO_FK is not null;

merge into psp_qbdt_payline_info
using psp_deduction on (psp_qbdt_payline_info.QBDT_PAYLINE_INFO_SEQ = psp_deduction.QBDT_PAYLINE_INFO_FK)
when matched then update set deduction_fk = deduction_seq;
commit;


update psp_deduction
set deduction_amount = deduction_amount * -1, 
                                deduction_y_t_d_amount = deduction_y_t_d_amount * -1
                                where paycheck_fk in (
                select paycheck_fk
                from psp_qbdt_paycheck_info qbpci);                        
                                commit;



--select count(*) from psp_qbdt_payline_info where deduction_fk is not null;

select systimestamp as End_time_post_deloy_1 from dual;






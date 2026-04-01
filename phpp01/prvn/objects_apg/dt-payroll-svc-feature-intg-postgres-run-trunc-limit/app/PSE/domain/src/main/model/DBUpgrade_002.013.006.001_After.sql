--
-- This script will be executed AFTER the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.006.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
prompt populating new column effective_date added to table psp_achenrollment;
update psp_achenrollment ache 
    set
         effective_date = (select sys_extract_utc(to_timestamp(trunc(created_date, 'Q'))) 
                                            from psp_achenrollment_detail acd 
                                                    where acd.a_c_h_enrollment_fk = ache.achenrollment_seq )
    where 
        ache.status in ( 'EnrollmentRejected', 'Enrolled', 'PendingEnrollmentResponse')
/        

update psp_achenrollment ache 
    set 
        effective_date = sys_extract_utc(to_timestamp(trunc(created_date, 'Q')))
    where 
        ache.status = 'PendingEnrollment' 
                and effective_date is null    
/

update psp_achenrollment ache 
    set 
        effective_date = (select sys_extract_utc(to_timestamp(trunc(created_date, 'Q') - 1)) 
                                        from psp_achenrollment_detail acd 
                                                where acd.a_c_h_enrollment_fk = ache.achenrollment_seq )
where ache.status in ( 'Deleted')
/

update psp_achenrollment ache 
    set 
        effective_date = sys_extract_utc(to_timestamp(trunc(created_date, 'Q') - 1))
    where 
        ache.status = 'PendingDelete' 
            and effective_date is null
/
commit;
PROMPT finished DBUpgrade_002.013.006.001_After.sql
BEGIN

update psp_employee_wage_plan ewp set ewp.invalid_date = null, ewp.modified_date = sys_extract_utc(systimestamp), ewp.modifier_id = 'PSP-2080' where ewp.employee_wage_plan_seq in (
select employee_wage_plan_seq from (
select
first_value(invalid_date) over (partition by employee_fk, state, wage_plan_domain, name order by invalid_date desc) invalids,
rank() over (partition by employee_fk, state, wage_plan_domain, name order by created_date desc, employee_wage_plan_seq) rnk,
wp.* from psp_employee_wage_plan wp
)
where invalids is not null and rnk = 1);

dbms_output.put_line('Number of Rows Updated psp_employee_wage_plan : ' ||  SQL%ROWCOUNT);

commit;

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;

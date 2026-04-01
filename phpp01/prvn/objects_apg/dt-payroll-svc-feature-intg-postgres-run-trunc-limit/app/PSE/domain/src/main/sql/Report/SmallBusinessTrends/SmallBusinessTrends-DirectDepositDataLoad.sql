DECLARE
	dataLoadStartDate DATE;
	dataLoadEndDate DATE;
	boundaryDate DATE;
	currentTime TIMESTAMP;
BEGIN
	select trunc(sysdate, 'MM') 
	into boundaryDate
	from dual;
	
	select coalesce(max(end_date) + 1, date '2010-01-01') 
	into dataLoadStartDate
	from psp_rpt_paid_employees;
	
	WHILE dataLoadStartDate < boundaryDate 
	LOOP	
		dataLoadEndDate := ADD_MONTHS(dataLoadStartDate, 1);
		
		select systimestamp into currentTime from dual;
		dbms_output.put_line('start : ' || to_char(currentTime, 'HH:MI:SS') || ' for period ' || dataLoadStartDate || ' to ' || dataLoadEndDate);
		
		insert into pspadm.psp_rpt_paid_employees
			select 'DD', dataLoadStartDate, dataLoadEndDate - 1, company_fk, count(distinct d_d_employee_fk), sum(coalesce(gross_amount,0)), sum(coalesce(net_amount,0))
			from psp_paycheck
			where created_date >= dataLoadStartDate 									-- use created date versus paycheck date; assume creation of paycheck is for an employee that exists at current time
			and created_date < dataLoadEndDate
			and length(source_paycheck_id) < 10                       -- eliminate QBDTWS paychecks
			and d_d_employee_fk is not null                           -- only DD paychecks (not Tax)
			and status = 'Active'                                     -- do not count voids
			group by company_fk;
			
		COMMIT;

		select systimestamp into currentTime from dual;
		dbms_output.put_line('finish: ' || to_char(currentTime, 'HH:MI:SS'));
		dbms_output.put_line('');
		
		dataLoadStartDate := ADD_MONTHS(dataLoadStartDate, 1);
	END LOOP;
END;

/
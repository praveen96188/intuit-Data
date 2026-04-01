create table PSP_RPT_PAID_EMPLOYEES (
  SERVICE_TYPE varchar(15) not null,
  START_DATE date not null,
	END_DATE date not null,
  COMPANY_SEQ varchar(36) not null,
  EMPLOYEE_COUNT NUMBER(9),
  TOTAL_GROSS_PAY number(19,4),
  TOTAL_NET_PAY number(19,4)
);

spool c11.log
set lines 1000
set pages 1000
select SYS_CONTEXT('USERENV', 'DB_NAME') db_name,
(select /*+parallel(td,8)*/ count(1)
  from qbo_data.txdetails_1 td
 where trunc(create_date) = trunc(sysdate-1)) txd_insert,
(select /*+parallel(td,8)*/ count(1)
  from qbo_data.txdetails_1 td
 where trunc(last_modify_date) = trunc(sysdate-1)) txd_update,
(select /*+parallel(th,8)*/ count(1)
  from qbo_data.txheaders_1 th
 where trunc(create_date) = trunc(sysdate-1)) txh_insert,
(select /*+parallel(th,8)*/ count(1)
  from qbo_data.txheaders_1 th
 where trunc(last_modify_date) = trunc(sysdate-1)) txh_update,
(select /*+parallel(cb,8)*/ count(1)
  from qbo_data.cashbasis_1 cb
 where trunc(CASH_DATE) = trunc(sysdate-1)) cb_insert,
(select count(1)
  from qbo_data.names_1 nm
 where trunc(CREATE_DATE) = trunc(sysdate-1)) nm_insert,
(select count(1)
  from qbo_data.names_1 nm
 where trunc(last_modify_date) = trunc(sysdate-1)) nm_update,
(select count(1)
  from  qbo_data.arapcreditpmntchargelinks_1 arap
 where trunc(CREATE_DATE) = trunc(sysdate-1)) arap_insert,
(select count(1)
  from  qbo_data.arapcreditpmntchargelinks_1 arap
 where trunc(last_modify_date) = trunc(sysdate-1)) arap_update,
(select count(1)
  from  qbo_data.accounts_1 ac
 where trunc(CREATE_DATE) = trunc(sysdate-1)) ac_insert,
(select count(1)
  from  qbo_data.accounts_1 ac
 where trunc(last_modify_date) = trunc(sysdate-1)) ac_update,
(select count(1)
  from  qbo_data.items_1 it
 where trunc(CREATE_DATE) = trunc(sysdate-1)) it_insert,
(select count(1)
  from  qbo_data.items_1 it
 where trunc(last_modify_date) = trunc(sysdate-1)) it_update
 from dual;
spool off
exit

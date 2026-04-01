set lines 300
set pages 0
select * from table(dbms_xplan.display_cursor('&sqlid','&child', 'PEEKED_BINDS'))
/

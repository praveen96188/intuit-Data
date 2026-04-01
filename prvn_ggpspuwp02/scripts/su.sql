select constraint_name from dba_constraints where constraint_name like '%USER';
select count(1) from dba_indexes where VISIBILITY='INVISIBLE';


select count(1) from dba_objects where owner in ('QBO','QBO_DATA');
select default_tablespace from dba_users where username in ('QBO','QBO_DATA');
select count(1) from companies_1;
select owner, tablespace_name, count(1) from dba_segments where owner in ('QBO','QBO_DATA') group by owner, tablespace_name;

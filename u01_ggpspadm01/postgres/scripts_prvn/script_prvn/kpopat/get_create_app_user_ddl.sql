set serverout on
show parameter instance_name
declare
v_cluster number;
v_password varchar2(100) :='salsa';
v_app varchar2(10);
v_prefix varchar2(100) :=''; -- Q for QA, E for E2E and no prefix for prod
begin
v_cluster:=&cn;
-- v_prefix :='Q';
for i in 0..99 loop
v_app := lpad(i,2,'0');
 dbms_output.put_line('exec create_app_admin_db_users('''||v_prefix||'QBOC'||v_cluster||'_UW2APP'||v_app||''','''||v_password||''');');
end loop;
end;
/

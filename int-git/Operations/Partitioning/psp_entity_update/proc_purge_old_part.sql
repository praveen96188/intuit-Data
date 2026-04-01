--procedure to purge old partitions
create procedure pspadm.purge_entityupdate
as
	part_count number;
	part_name varchar2(40);
begin
	-- check number of partitions

	SELECT COUNT(*) INTO part_count
	FROM DBA_TAB_PARTITIONS
	WHERE TABLE_OWNER = 'PSPADM'
	AND TABLE_NAME=upper('PSP_ENTITY_UPDATE');

	if part_count < 4 then
		dbms_output.put_line('Not enough partitions. count='||part_count);
		return;
	end if;

	for p in 1..part_count-3
	loop
		-- partition position gets reset when a partition is dropped, so keep dropping partition 1
		SELECT PARTITION_NAME INTO part_name
		FROM
		    DBA_TAB_PARTITIONS
		  WHERE TABLE_OWNER = 'PSPADM'
		   AND TABLE_NAME=upper('PSP_ENTITY_UPDATE')
		   AND PARTITION_POSITION = 1;

		-- the following lines have the words drop and partition separated to escape DDL replication exclude clause
		dbms_output.put_line('alter table PSPADM.PSP_ENTITY_UPDATE drop '|| 'partition '||part_name||'  UPDATE INDEXES');
		execute immediate 'alter table PSPADM.PSP_ENTITY_UPDATE drop '||'partition '||part_name||'  UPDATE INDEXES';
	end loop;
end;
/

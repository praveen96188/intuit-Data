--old company fk e3c06332-4956-4771-b077-61cb60621006
--reason - mismatch of company fk in EDR and MMT table
update PSPADM.PSP_ENTRY_DETAIL_RECORD
set COMPANY_FK = 'd21a507b-c7f3-4d4b-aeb4-9f3a5a03d142'
where ENTRY_DETAIL_RECORD_SEQ in ('c5db1521-dabe-48b0-9223-0f83955e0527', '5b6e0920-6b42-46e9-ad5f-329c7aa3c027');
commit;
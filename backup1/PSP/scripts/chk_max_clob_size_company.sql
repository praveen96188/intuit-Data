spool chk_max_clob_size_company 
set lines 300 echo on timing on echo on feedback on trimspool on
select /*+ PARALLEL(16) */ SOURCE_SYSTEM_TRANSMISSION_SEQ,VERSION,COMPANY_ID,TO_SOURCE_SYSTEM,
dbms_lob.getlength(RESPONSE_DOCUMENT) / 1024 / 1024       AS RESPONSE_DOCUMENT_Size_MB,
       dbms_lob.getlength(REQUEST_DOCUMENT) / 1024 / 1024        AS REQUEST_DOCUMENT_Size_MB     
from ibobadm_pds.psp_source_system_transmission 
where created_date>sysdate-730
order by dbms_lob.getlength(REQUEST_DOCUMENT) / 1024 / 1024 desc ;

spool off

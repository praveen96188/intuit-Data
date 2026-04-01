spool chk_max_clob_size 
set lines 300 echo on timing on echo on feedback on trimspool on
select max(dbms_lob.getlength(RESPONSE_DOCUMENT)) / 1024 / 1024       AS default_getlength_Size_MB,
       max(mchoubey.cloblengthb(RESPONSE_DOCUMENT)) / 1024 / 1024     AS custom_getlength_Size_MB,
       max(dbms_lob.getlength(REQUEST_DOCUMENT)) / 1024 / 1024        AS default_getlength_Size_MB,
       max(mchoubey.cloblengthb(REQUEST_DOCUMENT)) / 1024 / 1024      AS custom_getlength_Size_MB
from ibobadm.psp_source_system_transmission;
spool off

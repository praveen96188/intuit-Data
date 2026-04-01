set pages 0 lines 900 feedback on
spool cr_synonyms.sql
select 'create or replace synonym PSP_MONOLITH_READ.'|| TABLE_NAME || ' for '|| TABLE_OWNER ||'.'||TABLE_NAME ||';' from DBA_SYNONYMS where owner='PSPAPP' order by SYNONYM_NAME;
spool off

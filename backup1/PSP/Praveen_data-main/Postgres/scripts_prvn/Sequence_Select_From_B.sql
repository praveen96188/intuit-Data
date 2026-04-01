SET LINESIZE 32000;
SET PAGESIZE 40000;
SET LONG 50000;
SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';' as Sequence_reset
FROM all_sequences
WHERE sequence_owner = 'PSPADM';

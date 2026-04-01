SELECT 'ALTER SEQUENCE '||sequence_owner||'.'||sequence_name||' RESTART WITH '||(last_number + 1)||';'
FROM all_sequences
WHERE sequence_owner = 'PSPADM';

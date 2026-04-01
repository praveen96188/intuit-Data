CREATE SEQUENCE IF NOT EXISTS SEQ_TRACE_NUMBER  INCREMENT BY 1 MINVALUE 100000 MAXVALUE 9999999999999999 NO CYCLE
-- all the trace ids would be unique. Order can not be maintained with cache size > 1

;
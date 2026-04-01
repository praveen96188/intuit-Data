-- sequence caching is diff in postgres. cache is maintained per session level. 
-- SEQ_QBDT_SOURCE_COMPANY_ID is invoked once in the session. So need to cache. Also we have limited values.
CREATE SEQUENCE IF NOT EXISTS SEQ_QBDT_SOURCE_COMPANY_ID INCREMENT BY 1 MINVALUE 100000000 MAXVALUE 308999999

;


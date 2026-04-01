-- kill running query
SELECT pg_cancel_backend(<pid>);

-- kill idle query
SELECT pg_terminate_backend(<pid>);

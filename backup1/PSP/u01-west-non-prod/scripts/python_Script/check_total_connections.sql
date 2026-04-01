SELECT
    usename AS username,
    client_hostname AS client_machine,
    application_name,
    COUNT(*) AS total_connections_count
FROM
    pg_stat_activity
GROUP BY
    usename, client_hostname, application_name
ORDER BY
    total_connections_count DESC;

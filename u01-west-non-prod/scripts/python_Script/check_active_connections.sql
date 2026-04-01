SELECT
    usename AS username,
    client_hostname AS client_machine,
    application_name,
    COUNT(*) AS active_connections_count
FROM
    pg_stat_activity
WHERE
    state IN ('active', 'idle in transaction')  -- Filter for active and idle in transaction states
GROUP BY
    usename, client_hostname, application_name
ORDER BY
    active_connections_count DESC;

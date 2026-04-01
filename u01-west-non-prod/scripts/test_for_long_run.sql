select count(*) from generate_series(1,5000000)a CROSS JOIN generate_series(1,2000)b;

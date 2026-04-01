
SELECT 'TRUNCATE TABLE pspadm.' ||  tablename || ' cascade;' FROM pg_tables WHERE schemaname='pspadm';

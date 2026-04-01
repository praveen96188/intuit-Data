select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename like '%psp%');

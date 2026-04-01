select rolname,rolcanlogin
from pg_roles
where rolname in ('ibob_prod_pspapp');

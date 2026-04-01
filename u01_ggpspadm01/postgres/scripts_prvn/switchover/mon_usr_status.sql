select rolname,rolcanlogin
from pg_roles
where rolname in ('pspapp','pspbatch_rw_user','pspbatch_ro_user','pspread','psprjf','psp_payroll_dm');

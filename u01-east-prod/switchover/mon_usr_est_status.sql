select rolname,rolcanlogin
from pg_roles
where rolname in ('pspapp_ue2','pspbatch_rw_user_ue2','pspbatch_ro_user_ue2','pspread_ue2','psprjf_ue2','psp_payroll_dm_ue2');

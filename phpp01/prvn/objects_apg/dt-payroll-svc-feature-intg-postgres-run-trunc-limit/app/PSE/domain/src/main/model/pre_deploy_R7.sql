
--Updating users role to new roles for the roles we are deleting
update PSP_AUTH_USER_AUTH_ROLE__ASSOC set auth_role_fk = '134e8b0d-434d-f331-7345-f463dea1c1da' where  auth_role_fk ='83de8b3e-d35d-f4e1-a461-d565dea1a914'

update PSP_AUTH_USER_AUTH_ROLE__ASSOC set auth_role_fk = '83de8b3e-d35d-f4e1-a461-d565dea1c352' where  auth_role_fk in ('83de8b3e-d35d-f4e1-a461-d565dea1a913')

commit;

\t
\o 'CreateOrUpdateDB.sql'

select
   case when exists(
   select *
    from pg_class cls
      join pg_roles rol on rol.oid = cls.relowner
      join pg_namespace nsp on nsp.oid = cls.relnamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user) then '\i DBUpgrade.sql'
   else '\i DBCreate.sql'
end;

select
   case when exists(
   select *
    from pg_constraint con
      join pg_catalog.pg_class cls on cls.oid = con.conrelid
      join pg_roles rol on rol.oid = cls.relowner
      join pg_catalog.pg_namespace nsp on nsp.oid = connamespace
    where rol.rolname = current_user) then '\i DB_Update_Constraints.sql'
   else '\i DB_Generated_Constraints.sql'
end;

\o

\i 'CreateOrUpdateDB.sql'


\i 'DBPopulate.sql'



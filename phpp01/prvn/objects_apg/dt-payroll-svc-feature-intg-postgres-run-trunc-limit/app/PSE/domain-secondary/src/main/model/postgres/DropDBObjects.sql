DO $$
DECLARE
    tableName RECORD;
    matView RECORD;
    mView RECORD;
    mSequence RECORD;
    mFunction RECORD;
    mAggregate RECORD;
BEGIN
    RAISE NOTICE 'Deleting tables...';

    FOR tableName IN select nsp.nspname as object_schema,
           cls.relname as object_name,
           rol.rolname as owner
    from pg_class cls
      join pg_roles rol on rol.oid = cls.relowner
      join pg_namespace nsp on nsp.oid = cls.relnamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.relkind='r'
    order by nsp.nspname, cls.relname

    LOOP
        RAISE NOTICE 'Deleting table %s ...', quote_ident(tableName.object_name);
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(tableName.object_name) || ' CASCADE';
    END LOOP;

    RAISE NOTICE 'Done deleting tables...';




    RAISE NOTICE 'Deleting materialized views...';

    FOR matView IN select nsp.nspname as object_schema,
           cls.relname as object_name,
           rol.rolname as owner
    from pg_class cls
      join pg_roles rol on rol.oid = cls.relowner
      join pg_namespace nsp on nsp.oid = cls.relnamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.relkind='m'
    order by nsp.nspname, cls.relname

    LOOP
        RAISE NOTICE 'Deleting materialized view %s ...', quote_ident(matView.object_name);
        EXECUTE 'DROP materialized view ' || quote_ident(matView.object_name);
    END LOOP;

    RAISE NOTICE 'Done deleting materialized views...';




    RAISE NOTICE 'Deleting sequences...';

    FOR mSequence IN select nsp.nspname as object_schema,
           cls.relname as object_name,
           rol.rolname as owner
    from pg_class cls
      join pg_roles rol on rol.oid = cls.relowner
      join pg_namespace nsp on nsp.oid = cls.relnamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.relkind='v'
    order by nsp.nspname, cls.relname

    LOOP
        RAISE NOTICE 'Deleting sequence %s ...', quote_ident(mSequence.object_name);
        EXECUTE 'DROP SEQUENCE ' || quote_ident(mSequence.object_name) || ' CASCADE';
    END LOOP;

    RAISE NOTICE 'Done deleting sequences...';



    RAISE NOTICE 'Deleting functions...';

    FOR mFunction IN select nsp.nspname as object_schema,
           cls.proname as object_name,
           rol.rolname as owner,
           cls.prokind as object_type,
           cls.oid as oid
    from pg_catalog.pg_proc cls
      join pg_roles rol on rol.oid = cls.proowner
      join pg_namespace nsp on nsp.oid = cls.pronamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.prokind ='f'
    order by nsp.nspname, cls.proname

    LOOP
        RAISE NOTICE 'Deleting function %s ...', quote_ident(mFunction.object_name);
        EXECUTE 'DROP Function ' || quote_ident(mFunction.object_name) || '(' || pg_catalog.pg_get_function_identity_arguments(mFunction.oid) || ')';
    END LOOP;

    RAISE NOTICE 'Done deleting functions...';



    RAISE NOTICE 'Deleting aggregates...';

    FOR mAggregate IN select nsp.nspname as object_schema,
           cls.proname as object_name,
           rol.rolname as owner,
           cls.prokind as object_type,
           cls.oid as oid
    from pg_catalog.pg_proc cls
      join pg_roles rol on rol.oid = cls.proowner
      join pg_namespace nsp on nsp.oid = cls.pronamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.prokind ='a'
    order by nsp.nspname, cls.proname

    LOOP
        RAISE NOTICE 'Deleting aggregate %s ...', quote_ident(mFunction.object_name);
        EXECUTE 'DROP aggregate ' || quote_ident(mAggregate.object_name) || '(' || pg_catalog.pg_get_function_identity_arguments(mAggregate.oid) || ')';
    END LOOP;

    RAISE NOTICE 'Done deleting aggregates...';



    RAISE NOTICE 'Deleting procedures...';

    FOR mAggregate IN select nsp.nspname as object_schema,
           cls.proname as object_name,
           rol.rolname as owner,
           cls.prokind as object_type,
           cls.oid as oid
    from pg_catalog.pg_proc cls
      join pg_roles rol on rol.oid = cls.proowner
      join pg_namespace nsp on nsp.oid = cls.pronamespace
    where nsp.nspname not in ('information_schema', 'pg_catalog')
      and nsp.nspname not like 'pg_toast%'
      and rol.rolname = current_user
      and cls.prokind ='p'
    order by nsp.nspname, cls.proname

    LOOP
        RAISE NOTICE 'Deleting procedure %s ...', quote_ident(mFunction.object_name);
        EXECUTE 'DROP Procedure ' || quote_ident(mAggregate.object_name) || '(' || pg_catalog.pg_get_function_identity_arguments(mAggregate.oid) || ') cascade' ;
    END LOOP;

    RAISE NOTICE 'Done deleting procedures...';
END;
$$ LANGUAGE plpgsql;

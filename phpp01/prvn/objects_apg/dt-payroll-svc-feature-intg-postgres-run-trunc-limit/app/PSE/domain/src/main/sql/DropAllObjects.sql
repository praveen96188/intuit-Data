BEGIN
    EXECUTE IMMEDIATE 'PURGE RECYCLEBIN'; -- Purge all the objects for the current user

    -- drop all the TABLES, that will drop index, triggers, constraints etc
    FOR rec in (SELECT 'DROP '||object_type||' '|| object_name||  DECODE(OBJECT_TYPE,'TABLE',' CASCADE CONSTRAINTS PURGE') sql_stmt
                 FROM  USER_OBJECTS
                WHERE object_type = 'TABLE')
    LOOP
        EXECUTE IMMEDIATE rec.sql_stmt;
    END LOOP;

    --  drop all the OTHER OBJECTS sysnonyms, dblinks, procedure etc
    FOR rec in (SELECT 'DROP '||object_type||' '|| object_name sql_stmt
                 FROM  USER_OBJECTS
                WHERE object_type NOT IN ('TABLE', 'PACKAGE BODY')) -- dropping package will drop the BODY
    LOOP
        EXECUTE IMMEDIATE rec.sql_stmt;
    END LOOP;

    EXECUTE IMMEDIATE 'PURGE RECYCLEBIN';
END;
/


select distinct  tgname as trigger_name ,tgenabled as status from  pg_trigger where tgname in (  select distinct trigger_name from information_schema.triggers where trigger_schema='pspadm');

select count(1) 
  from dba_users 
 where account_status='OPEN' 
   and username like 'QBO%UW2%';


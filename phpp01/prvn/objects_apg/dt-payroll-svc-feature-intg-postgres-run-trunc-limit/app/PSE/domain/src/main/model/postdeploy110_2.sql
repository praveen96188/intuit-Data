
---this is a post deploy scrit to update rest of the partitions of money_movement_transaction.This can be run online.
select systimestamp as starttime_postdeploy2 from dual;





 begin
 loop
 UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG62008)  SET tax_payment_status = 'None' 
 where tax_payment_status is null and  ROWNUM < 10000;
 if SQL%ROWCOUNT = 0 THEN
  exit;
 else
 commit;
 end if;
 end loop;
 commit;
 end;
 /
 
  begin
  loop
  UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG12009)  SET tax_payment_status = 'None' 
  where tax_payment_status is null and  ROWNUM < 10000;
  if SQL%ROWCOUNT = 0 THEN
   exit;
  else
  commit;
  end if;
  end loop;
  commit;
  end;
 /
 
  begin
  loop
  UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG22009)  SET tax_payment_status = 'None' 
  where tax_payment_status is null and  ROWNUM < 10000;
  if SQL%ROWCOUNT = 0 THEN
   exit;
  else
  commit;
  end if;
  end loop;
  commit;
  end;
 /`	
 
  begin
  loop
  UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG32009)  SET tax_payment_status = 'None' 
  where tax_payment_status is null and  ROWNUM < 10000;
  if SQL%ROWCOUNT = 0 THEN
   exit;
  else
  commit;
  end if;
  end loop;
  commit;
  end;
 /
 
  begin
  loop
  UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG42009)  SET tax_payment_status = 'None' 
  where tax_payment_status is null and  ROWNUM < 10000;
  if SQL%ROWCOUNT = 0 THEN
   exit;
  else
  commit;
  end if;
  end loop;
  commit;
  end;
 /
 
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG52009)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG62009)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG12010)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG22010)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG32010)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
  begin
   loop
   UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG42010)  SET tax_payment_status = 'None' 
   where tax_payment_status is null and  ROWNUM < 10000;
   if SQL%ROWCOUNT = 0 THEN
    exit;
   else
   commit;
   end if;
   end loop;
   commit;
   end;
  /
 
  begin
    loop
    UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG52010)  SET tax_payment_status = 'None' 
    where tax_payment_status is null and  ROWNUM < 10000;
    if SQL%ROWCOUNT = 0 THEN
     exit;
    else
    commit;
    end if;
    end loop;
    commit;
    end;
  /
  
   begin
     loop
     UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG62010)  SET tax_payment_status = 'None' 
     where tax_payment_status is null and  ROWNUM < 10000;
     if SQL%ROWCOUNT = 0 THEN
      exit;
     else
     commit;
     end if;
     end loop;
     commit;
     end;
  /
   begin
     loop
     UPDATE psp_money_movement_transaction partition (MONEY_MOVEMENT_TXN_MG12011)  SET tax_payment_status = 'None' 
     where tax_payment_status is null and  ROWNUM < 10000;
     if SQL%ROWCOUNT = 0 THEN
      exit;
     else
     commit;
     end if;
     end loop;
     commit;
     end;
  /
 	
 	
 	
 	
 	
 select systimestamp as endtime_postdeploy2 from dual;
 	
 	
 	
 	
 	
 	
 	
 		
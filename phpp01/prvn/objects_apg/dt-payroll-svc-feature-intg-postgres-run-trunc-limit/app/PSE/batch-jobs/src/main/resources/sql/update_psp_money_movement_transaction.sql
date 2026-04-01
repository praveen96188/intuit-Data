BEGIN
update psp_money_movement_transaction
set 
    status = 'Created',
    modifier_id = 'mmtcheck',
    modified_date = sys_extract_utc(systimestamp) where 
    status = 'OnHold'
    AND money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
    AND tax_payment_status = 'ReadyToSend';
	commit;
	dbms_output.put_line('Number of Rows Updated for money movement : ' ||  SQL%ROWCOUNT);
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;


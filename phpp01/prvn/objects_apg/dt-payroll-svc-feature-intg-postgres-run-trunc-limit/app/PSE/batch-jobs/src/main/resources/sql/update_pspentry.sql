BEGIN
update psp_entry_detail_record
set n_a_c_h_a_file_fk = null, modified_date = SYS_EXTRACT_UTC(systimestamp), modifier_id = 'edrassocfix'
where (entry_detail_record_seq,company_fk) in (
select entry_detail_record_seq,edr.company_fk
from psp_money_movement_transaction mmt
inner join psp_offload_batch ob on ob.offload_batch_seq = mmt.offload_batch_fk 
inner join psp_entry_detail_record edr on edr.initiation_date = mmt.initiation_date and edr.money_movement_transaction_fk = mmt.money_movement_transaction_seq
and edr.company_fk = mmt.company_fk
left join psp_nachafile nf on nf.nachafile_seq = edr.n_a_c_h_a_file_fk where money_movement_payment_method in ('ACHCredit', 'ACHDirectDeposit') and
nf.offload_batch_fk is not null and
tax_payment_status = 'Ignore' and MMT.MM_TRANSACTION_AMOUNT = 0 and 
mmt.initiation_date between current_timestamp - 1 and current_timestamp + 1
);
commit;
dbms_output.put_line('Number of Rows Updated For psp_entry_detail_record : ' ||  SQL%ROWCOUNT);
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE ('Error : ' || SQLERRM);
END;

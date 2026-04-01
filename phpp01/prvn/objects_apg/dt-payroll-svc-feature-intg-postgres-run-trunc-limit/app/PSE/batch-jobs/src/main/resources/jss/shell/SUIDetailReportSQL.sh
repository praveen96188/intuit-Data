#!/bin/sh

dbadmuser="pspadm"
dbadmuserpassword=`head -1 /etc/intuit/psp/dbadmuserpassword`

myhost=`hostname`
dburl="orapspqpd02.qcyf01.ie.intuit.net:1521/pspprod"  # default datacenter environment

dc=$(echo $myhost | grep -P -o "\d+")
if [[ "${dc}" =~ ^4+ || "${dc}" =~ ^9+ ]]
then
        dburl="orapsplpd02.lasf01.ie.intuit.net:1521/pspprod" 
fi

startDate=$1
endDate=$2
currentDate=`date +%Y%m%d.%H%M`
login_string=$dbadmuser/$dbadmuserpassword@$dburl
output_file=/tmp/sui_detail_report_sql-$currentDate.csv

echo "Running SQL $dbadmuser@dburl from $1 to $2"

sqlplus -s "$login_string" << EOF >> $output_file
set lines 300
set pages 0
select 'Source_company_id, legal_name,  financial_transaction_amount, transaction_type_fk, settlement_date,Credit_Debit' from dual;
select c.Source_company_id||','||c.legal_name||','||financial_transaction_amount||','||transaction_type_fk||','||settlement_date||','||
CASE transaction_type_fk
  WHEN 'EmployerSUITaxReceivable' THEN 'Debit'
  WHEN 'EmployerSUITaxPayable' THEN 'Credit'
  WHEN 'EmployerSUITaxCollection' THEN 'Credit'
  WHEN 'EmployerSUITaxRefund' THEN 'Debit'
END
from pspadm.psp_financial_transaction ft
inner join pspadm.psp_company c on c.company_seq = ft.company_fk
where ft.settlement_date between timestamp '$startDate 07:00:00' and timestamp '$endDate 07:00:00'
and current_transaction_state_fk in ('Created', 'Executed', 'Completed')
and transaction_type_fk in ('EmployerSUITaxReceivable', 'EmployerSUITaxPayable', 'EmployerSUITaxCollection', 'EmployerSUITaxRefund')
--and ft.modifier_id <> 'EoqSUITaxAdjustments'
order by  company_seq, settlement_date;
exit
EOF

echo "Selected Rows"
cat /tmp/sui_detail_report_sql-$currentDate.csv

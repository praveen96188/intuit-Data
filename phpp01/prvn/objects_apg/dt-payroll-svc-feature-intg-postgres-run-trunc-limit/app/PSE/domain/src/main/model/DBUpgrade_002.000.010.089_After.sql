--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\rel-1.10\PSE\Domain\src\main\model\DBUpgrade_002.000.010.089.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE
	psp_eftps_file ef
SET
	file_subtype = 'None'
/
UPDATE
	psp_eftps_file ef
SET
	file_subtype = (
		SELECT
			decode(mmt.money_movement_payment_method, 'EFTPS', 'PaymentNextDay', 'EFTPSDirectDebit', 'Payment100k', 'None')
		FROM
			psp_eftps_payment_detail epd
			join psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
		WHERE
			epd.parent_file_fk = ef.eftps_file_seq
			and rownum = 1		
	)
WHERE
	system_owner = 'PSP'
	and file_code = 813
/

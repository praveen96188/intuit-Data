select 
	*
from 
	pspadm.psp_money_movement_transaction mmt
	inner join pspadm.psp_pmt_template_frequency ptf on MMT.PAYMENT_FREQUENCY_FK = PTF.PAYMENT_TEMPLATE_FREQUENCY_ID
where 
	MMT.PAYMENT_TEMPLATE_FK = 'CA-PITSDI-PAYMENT'
	and PTF.PAYMENT_TEMPLATE_FK = 'IRS-941-PAYMENT'
	and MMT.INITIATION_DATE >= date '2011-09-29'
	and MMT.INITIATION_DATE <= date '2012-02-03';

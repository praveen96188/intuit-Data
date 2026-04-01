package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.payslip.model.MoneyDistributionLineType;
import com.intuit.payroll.api.payslip.model.PrivilegedMoneyDistributionLineCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.BillPayment;
import com.intuit.sbd.payroll.psp.domain.BillPaymentSplit;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BillPaymentToPrivilegedMoneyDistributionLineCDMMapper
		extends BeanMapper<BillPayment, PrivilegedMoneyDistributionLineCDM> {
	public static Logger LOGGER = LoggerFactory.getLogger(BillPaymentToPrivilegedMoneyDistributionLineCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {

	}

	@Override
	public void mapAtoB(BillPayment billPayment, PrivilegedMoneyDistributionLineCDM moneyDistributionLineCDM,
			MappingContext context) {
		// moneyDistributionLineCDM.setDistributionStatus(DirectDepositStatusType.CREATED);
		moneyDistributionLineCDM.setType(MoneyDistributionLineType.DDService);
		PayrollRun payrollRun = billPayment.getPayrollRun();
		if (payrollRun != null) {
			BigDecimal billPaymentDirectDepositAmount = new BigDecimal(
					getBillPaymentDirectDepositAmount(billPayment).toString());
			moneyDistributionLineCDM.setAmount(billPaymentDirectDepositAmount);
		} else {
			LOGGER.error("No PayrollRun exists for the current BillPayment number {}", billPayment.getSourceId());
		}
		PayeeBankAccount payeeBankAccount = PayeeBankAccount
				.findActivePayeeBankAccount(billPayment.getPayee().getCompany(), billPayment.getPayee());
		// set the bank account id
		if (payeeBankAccount != null) {
			moneyDistributionLineCDM.setBankAccountId(payeeBankAccount.getId().toString());
		} else {
			if (billPayment.getBillPaymentSplitCollection().size() > 0) {
				moneyDistributionLineCDM.setBankAccountId(
						billPayment.getBillPaymentSplitCollection().get(0).getPayeeBankAccount().getId().toString());
			}
		}
		moneyDistributionLineCDM
				.setBankAccount(getEntityCDMMapper().mapToTarget(payeeBankAccount, PrivilegedBankAccountCDM.class));
	}

	public SpcfMoney getBillPaymentDirectDepositAmount(BillPayment billpayment) {
		SpcfDecimal accumulateAmount = SpcfMoney.ZERO;
		for (BillPaymentSplit billPaymentSplit : billpayment.getBillPaymentSplitCollection()) {
			accumulateAmount = accumulateAmount.add(billPaymentSplit.getAmount());
		}
		return new SpcfMoney(accumulateAmount);
	}
}

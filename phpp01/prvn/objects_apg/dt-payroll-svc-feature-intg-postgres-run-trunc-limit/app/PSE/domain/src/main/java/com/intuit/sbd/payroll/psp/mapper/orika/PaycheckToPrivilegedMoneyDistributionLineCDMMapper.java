package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.MoneyDistributionLineType;
import com.intuit.payroll.api.payslip.model.PrivilegedMoneyDistributionLineCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaycheckToPrivilegedMoneyDistributionLineCDMMapper
		extends BeanMapper<Paycheck, PrivilegedMoneyDistributionLineCDM> {

	public static Logger LOGGER = LoggerFactory.getLogger(PaycheckToPrivilegedMoneyDistributionLineCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {

	}

	@Override
	public void mapAtoB(Paycheck paycheck, PrivilegedMoneyDistributionLineCDM moneyDistributionLineCDM,
			MappingContext context) {
		// moneyDistributionLineCDM.setDistributionStatus(DirectDepositStatusType.CREATED);
		moneyDistributionLineCDM.setType(MoneyDistributionLineType.DDService);
		PayrollRun payrollRun = paycheck.getPayrollRun();
		if (payrollRun != null) {
			BigDecimal paycheckDirectDepositAmount = new BigDecimal(
					getPaycheckDirectDepositAmount(paycheck).toString());
			moneyDistributionLineCDM.setAmount(paycheckDirectDepositAmount);
		} else {
			LOGGER.error("No PayrollRun exists for the current Paycheck number {}", paycheck.getSourcePaycheckId());
		}
		EmployeeBankAccount employeeBankAccount = EmployeeBankAccount.findLatestActiveEBA(paycheck.getCompany(),
				paycheck.getDDEmployee());
		if (employeeBankAccount != null) {
			moneyDistributionLineCDM.setBankAccountId(employeeBankAccount.getId().toString());
			moneyDistributionLineCDM.setBankAccount(
					getEntityCDMMapper().mapToTarget(employeeBankAccount, PrivilegedBankAccountCDM.class));
		} else {
			if (paycheck.getPaycheckSplitCollection().size() > 0) {
				moneyDistributionLineCDM.setBankAccountId(
						paycheck.getPaycheckSplitCollection().get(0).getEmployeeBankAccount().getId().toString());
				moneyDistributionLineCDM.setBankAccount(getEntityCDMMapper().mapToTarget(
						paycheck.getPaycheckSplitCollection().get(0).getEmployeeBankAccount(),
						PrivilegedBankAccountCDM.class));
			}
		}
	}

	public SpcfMoney getPaycheckDirectDepositAmount(Paycheck paycheck) {
		SpcfDecimal accumulateAmount = SpcfMoney.ZERO;
		for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
			accumulateAmount = accumulateAmount.add(paycheckSplit.getPaycheckSplitAmount());
		}
		return new SpcfMoney(accumulateAmount);
	}

}

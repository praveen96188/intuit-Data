package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.PayslipCDM;
import com.intuit.payroll.api.payslip.model.PayslipType;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Compensation;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class PaycheckToPayslipCDMMapper extends BeanMapper<Paycheck, PayslipCDM> {
    @Override
    public void directFieldToFieldMapping() {
        addBidirectionalFieldMapping("id", "id");
        addBidirectionalFieldMapping("createdDate", "created");
        addBidirectionalFieldMapping("modifiedDate", "updated");
        addBidirectionalFieldMapping("payrollRun.paycheckDate", "checkDate");
        addBidirectionalFieldMapping("createdDate", "approvedDate");
        addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddSettlementDate");
        addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddDebitDate");
        addBidirectionalFieldMapping("company.offloadGroup.cutoffTime", "ddCutoffTime");
        addBidirectionalFieldMapping("payPeriodBeginDate", "payPeriodStartDate");
        addBidirectionalFieldMapping("payPeriodEndDate", "payPeriodEndDate");
        addBidirectionalFieldMapping("netAmount", "netAmount");
        addBidirectionalFieldMapping("payrollRun.payrollDebitInfo.achAmount", "grossAmount");
        addBidirectionalFieldMapping("qbdtPaycheckInfo.checkNumber", "checkNumber");
        addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddInitiationDate");
        addBidirectionalFieldMapping("payrollRun.id", "payrollId");
    }

    @Override
    public void mapAtoB(Paycheck paycheck, PayslipCDM payslipCDM, MappingContext context) {
        Employee employee = (paycheck.getDDEmployee() != null) ? paycheck.getDDEmployee()
                : paycheck.getSourceEmployee();

        if (employee == null) {
            return;
        }

        DomainEntitySet<Compensation> compensations = paycheck.getCompensationCollection();
        for (Compensation compensation : compensations) {
            payslipCDM.setTotalHours(new BigDecimal(compensation.getHoursWorked()));
        }

        setDefaultValues(payslipCDM);

        clearInvalidValues(payslipCDM);

        payslipCDM.setGrossAmount(new BigDecimal(getPaycheckDirectDepositAmount(paycheck).toString()));
        payslipCDM.setNetAmount(new BigDecimal(getPaycheckDirectDepositAmount(paycheck).toString()));

    }

    private void clearInvalidValues(PayslipCDM payslipCDM) {
        // clear out the employer info and MoneyDistributionLines, since
        // privileged version has
        // duplicate data.
        payslipCDM.setEmployerInfo(null);
        payslipCDM.setMoneyDistributionLines(null);
        payslipCDM.setEmployeeInfo(null);
    }

    private void setDefaultValues(PayslipCDM payslipCDM) {
        payslipCDM.setEntityVersion("1");
        payslipCDM.setApprovedBy("NA");
        payslipCDM.setApproved(true);
        payslipCDM.setPayslipType(PayslipType.REGULAR);
        payslipCDM.setHistory(false);
    }

    public SpcfMoney getPaycheckDirectDepositAmount(Paycheck paycheck) {
        SpcfDecimal accumulateAmount = SpcfMoney.ZERO;
        for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
            accumulateAmount = accumulateAmount.add(paycheckSplit.getPaycheckSplitAmount());
        }
        return new SpcfMoney(accumulateAmount);
    }
}

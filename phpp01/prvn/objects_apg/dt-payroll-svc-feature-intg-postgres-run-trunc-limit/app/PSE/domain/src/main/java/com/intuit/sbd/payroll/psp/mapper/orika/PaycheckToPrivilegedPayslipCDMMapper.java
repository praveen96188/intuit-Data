package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.*;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Compensation;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import ma.glasnost.orika.MappingContext;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PaycheckToPrivilegedPayslipCDMMapper extends BeanMapper<Paycheck, PrivilegedPayslipCDM> {

	public static final String DATE_FORMAT = "yyyyMMdd";
	public static final String TIME_FORMAT = "HH:mm";
	public static final String DATE_TIME_FORMAT = "yyyyMMdd HH:mm:ss";

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("createdDate", "created");
		addBidirectionalFieldMapping("modifiedDate", "updated");
		addBidirectionalFieldMapping("payrollRun.paycheckDate", "checkDate");
		addBidirectionalFieldMapping("createdDate", "approvedDate");
		// TODO Get ddSettlementDate, ddDebitDate, ddCutoffTime from Risk Check
		// API
		addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddSettlementDate");
		addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddDebitDate");
		// addBidirectionalFieldMapping("company.offloadGroup.cutoffTime",
		// "ddCutoffTime");
		addBidirectionalFieldMapping("payPeriodBeginDate", "payPeriodStartDate");
		addBidirectionalFieldMapping("payPeriodEndDate", "payPeriodEndDate");
		addBidirectionalFieldMapping("netAmount", "netAmount");
		addBidirectionalFieldMapping("payrollRun.payrollDebitInfo.achAmount", "grossAmount");
		addBidirectionalFieldMapping("qbdtPaycheckInfo.checkNumber", "checkNumber");
		addBidirectionalFieldMapping("payrollRun.paycheckSettlementDate", "ddInitiationDate");
		addBidirectionalFieldMapping("payrollRun.id", "payrollId");
	}

	@Override
	public void mapAtoB(Paycheck paycheck, PrivilegedPayslipCDM privilegedPayslipCDM, MappingContext context) {

		privilegedPayslipCDM.setEntityVersion(Long.toString(paycheck.getVersion()));

		Employee employee = (paycheck.getDDEmployee() != null) ? paycheck.getDDEmployee()
				: paycheck.getSourceEmployee();

		if (employee == null) {
			return;
		}

		DomainEntitySet<Compensation> compensations = paycheck.getCompensationCollection();
		for (Compensation compensation : compensations) {
			privilegedPayslipCDM.setTotalHours(new BigDecimal(compensation.getHoursWorked()));
		}

		setDefaultValues(privilegedPayslipCDM);

		clearInvalidValues(privilegedPayslipCDM);

		BigDecimal paycheckDirectDepositAmount = new BigDecimal(getPaycheckDirectDepositAmount(paycheck).toString());

		privilegedPayslipCDM.setGrossAmount(paycheckDirectDepositAmount);
		privilegedPayslipCDM.setNetAmount(paycheckDirectDepositAmount);
		// TODO Get ddSettlementDate
		SpcfCalendar source = validateOffloadTime(paycheck.getCompany().getOffloadGroup().getCutoffTime(),
				paycheck.getPayrollRun().getPaycheckSettlementDate());
		privilegedPayslipCDM.setDdCutoffTime(new DateTime(source.toUtc().getTimeInMilliseconds()));

		privilegedPayslipCDM
				.setPrivilegedEmployerInfo(getEntityCDMMapper().mapToTarget(paycheck, PrivilegedEmployerInfoCDM.class));
		privilegedPayslipCDM
				.setPrivilegedEmployeeInfo(getEntityCDMMapper().mapToTarget(employee, PrivilegedEmployeeInfoCDM.class));
		List<PrivilegedMoneyDistributionLineCDM> moneyDistributionLineCDMs = new ArrayList<>();
		moneyDistributionLineCDMs
				.add(getEntityCDMMapper().mapToTarget(paycheck, PrivilegedMoneyDistributionLineCDM.class));
		privilegedPayslipCDM.setPrivilegedMoneyDistributionLines(moneyDistributionLineCDMs);

	}

	private void clearInvalidValues(PrivilegedPayslipCDM privilegedPayslipCDM) {
		// clear out the employer info and MoneyDistributionLines, since
		// privileged version has
		// duplicate data.
		privilegedPayslipCDM.setEmployerInfo(null);
		privilegedPayslipCDM.setMoneyDistributionLines(null);
		privilegedPayslipCDM.setEmployeeInfo(null);
	}

	private void setDefaultValues(PrivilegedPayslipCDM privilegedPayslipCDM) {
		privilegedPayslipCDM.setApprovedBy("NA");
		privilegedPayslipCDM.setApproved(true);
		privilegedPayslipCDM.setPayslipType(PayslipType.REGULAR);
		privilegedPayslipCDM.setHistory(false);
	}

	public SpcfMoney getPaycheckDirectDepositAmount(Paycheck paycheck) {
		SpcfDecimal accumulateAmount = SpcfMoney.ZERO;
		for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
			accumulateAmount = accumulateAmount.add(paycheckSplit.getPaycheckSplitAmount());
		}
		return new SpcfMoney(accumulateAmount);
	}

	protected SpcfCalendar validateOffloadTime(String cutoffTimeStr, SpcfCalendar settlementDate) {
		SpcfCalendar cal, offloadTime = settlementDate;
		String currentDateStr = offloadTime.format(DATE_FORMAT);

		Pattern pattern = Pattern.compile("([0-2]?[0-9]:[0-5][0-9]).*");
		Matcher matcher;

		cutoffTimeStr = "01:00";

		// normalize the cutoff time value to ##:##
		matcher = pattern.matcher(cutoffTimeStr);
		if (matcher.matches()) {
			cutoffTimeStr = matcher.group(1);

			if (cutoffTimeStr.matches("[0-9]:[0-5][0-9]")) {
				cutoffTimeStr = "0" + cutoffTimeStr;
			}
		}

		// need date in local time for comparison
		cal = SpcfCalendar.parse(DATE_FORMAT + TIME_FORMAT, currentDateStr + cutoffTimeStr);

		return cal;
	}

}

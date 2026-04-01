package com.intuit.sbd.payroll.psp.processes.billing;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.Bill;
import com.intuit.sbd.payroll.psp.domain.CompanyUsage;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage;
import com.intuit.sbd.payroll.psp.domain.PaycheckUsage;
import com.intuit.sbd.payroll.psp.domain.UsagePeriod;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/16/12
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateBillingUsage extends com.intuit.sbd.payroll.psp.processes.Process {
	private CompanyDTO mCompanyDTO;
	private EmployeeDTO mEmployeeDTO;
	private PaycheckDTO mPaycheckDTO;

	public CreateBillingUsage(CompanyDTO pCompanyDTO, EmployeeDTO pEmployeeDTO, PaycheckDTO pPaycheckDTO) {
		mCompanyDTO = pCompanyDTO;
		mEmployeeDTO = pEmployeeDTO;
		mPaycheckDTO = pPaycheckDTO;
	}

	public ProcessResult validate() {
		ProcessResult validationResult = new ProcessResult();

		// Validate DTOs
		// CompanyDTO
		if (mCompanyDTO == null) {
			validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, "CreateBillingUsage", "Company");
		} else {
			validationResult.merge(mCompanyDTO.validate());
		}

		// EmployeeDTO
		if (mEmployeeDTO == null) {
			validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Employee, "CreateBillingUsage", "Employee");
		} else {
			validationResult.merge(mEmployeeDTO.validate());
		}

		// PaycheckDTO
		if (mPaycheckDTO == null) {
			validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Paycheck, "CreateBillingUsage", "Paycheck");
		} else {
			validationResult.merge(mPaycheckDTO.validate());
		}

		return validationResult;
	}

	public ProcessResult process() {
		// find or create a company usage
		CompanyUsage aCompanyUsage = CompanyUsage.findOrCreateCompanyUsage(mCompanyDTO.getSourceCompanyId(), mCompanyDTO.getSourceSystemCode(), mCompanyDTO.getEntitlementId(), mCompanyDTO.getLicenseId(), mCompanyDTO.getBillingDayOfMonth(), mCompanyDTO.getStartDayOfUsageMonth());

		// figure out the company's billing cycle based on the paycheck date
		SpcfCalendar paycheckDate = mPaycheckDTO.getPaycheckDate();
		SpcfCalendar usagePeriodStartDate = CalendarUtils.dayOfMonthBeforeOrEqualTo(paycheckDate, mCompanyDTO.getStartDayOfUsageMonth());
		SpcfCalendar usagePeriodEndDate = CalendarUtils.endDateOfMonthlyPeriod(usagePeriodStartDate);
		SpcfCalendar billDate = CalendarUtils.dayOfMonthAfterOrEqualTo(PSPDate.getPSPTime(), mCompanyDTO.getBillingDayOfMonth());
		if (usagePeriodEndDate.after(billDate) || usagePeriodEndDate.equals(billDate) ) {
            billDate = CalendarUtils.dayOfMonthAfter(usagePeriodEndDate, mCompanyDTO.getBillingDayOfMonth());
		}
        //In case of backdated paychecks
        //PSPDate is payroll run date as jobs runs every 5min
        //This is calculated only for  payroll run day <=  BillingDayOfMonth as other case will be covered   in CalendarUtils.dayOfMonthAfter()  in above steps.
        if ((PSPDate.getPSPTime().after(usagePeriodEndDate) || PSPDate.getPSPTime().equals(usagePeriodEndDate) )  && PSPDate.getPSPTime().getDay() <= mCompanyDTO.getBillingDayOfMonth()) {
            billDate = CalendarUtils.dayOfMonthAfter(billDate, mCompanyDTO.getBillingDayOfMonth());
        }


		// find or create a bill and an usagePeriod
		// this bill is guaranteed still open
		Bill aBill = Bill.findOpenBillOrCreate(aCompanyUsage, billDate, CalendarUtils.dayOfMonthAfter(billDate, mCompanyDTO.getBillingDayOfMonth()));
		UsagePeriod aUsagePeriod = UsagePeriod.findOrCreateUsagePeriod(aCompanyUsage, usagePeriodStartDate, usagePeriodEndDate);

		// find or create an employeeUsage
		EmployeeUsage anEmployeeUsage = EmployeeUsage.findOrCreateEmployeeUsage(aUsagePeriod, mEmployeeDTO.getEmployeeListId(), mEmployeeDTO.getEmployeeName(), mEmployeeDTO.getEmployeeRecordNumber());

		// find, update or create a paycheckUsage
        AtomicInteger usageContribution = new AtomicInteger(0);
		PaycheckUsage aPaycheckUsage = PaycheckUsage.updateStatusOrCreatePaycheckUsage(anEmployeeUsage, mPaycheckDTO.getPaycheckListID(), mPaycheckDTO.getPaycheckDate(), mPaycheckDTO.getCheckNumber(), mPaycheckDTO.getTransactionID(), mPaycheckDTO.isPaycheckStatusActive(), mPaycheckDTO.getReasonForFreeCharge(), aBill, usageContribution, mPaycheckDTO.isPaycheckCreatedDateLessThanBillingStartDate());

		// update usage count
        int oldEmployeeUsageCount = anEmployeeUsage.getUsageCount();
		if (usageContribution.intValue() == 1) {
			if (anEmployeeUsage.increaseUsageCount() > 0 && oldEmployeeUsageCount == 0) {
				aBill.increaseUsageCount();
			}
		} else if (usageContribution.intValue() == -1) {
			if (anEmployeeUsage.decreaseUsageCount() == 0 && oldEmployeeUsageCount > 0) {
				aBill.decreaseUsageCount();
			}
		}

		ProcessResult processResult = new ProcessResult();
		processResult.setResult(aPaycheckUsage);
		return processResult;
	}
}

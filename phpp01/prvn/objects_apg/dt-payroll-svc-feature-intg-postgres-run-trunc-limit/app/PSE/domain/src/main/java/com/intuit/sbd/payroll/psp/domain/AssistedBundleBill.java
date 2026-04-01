package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;

/**
 * Hand-written business logic
 */
public class AssistedBundleBill extends BaseAssistedBundleBill {
	private static final SpcfLogger logger = SpcfLogManager.getLogger(AssistedBundleBill.class);
	/**
	 * Default constructor.
	 */
	public AssistedBundleBill() {
		super();
	}

	public static Set<SpcfUniqueId> findOpenBillsDuring(SpcfCalendar startDate, SpcfCalendar endDate) {
		Set<SpcfUniqueId> openBillsSet = new HashSet<SpcfUniqueId>();

		ArrayList<Object[]> returnObjects = Application.executeQuery(AssistedBundleBill.class, new com.intuit.sbd.payroll.psp.query.Query<AssistedBundleBill>()
				.Select(AssistedBundleBill.Id())
				.Where(AssistedBundleBill.AsstStatus().in(AssistedBillStatus.Open, AssistedBillStatus.ProcessingFailed)
						.And(AssistedBundleBill.BillDate().greaterOrEqualThan(startDate))
						.And(AssistedBundleBill.BillDate().lessOrEqualThan(endDate))));

		for (Object returnObject : returnObjects) {
			SpcfUniqueId billId = (SpcfUniqueId) returnObject;
			openBillsSet.add(billId);
		}

		return openBillsSet;
	}

	public static AssistedBundleBill findAssistedBundleBill(AsstBundleCompUsage pAssistedBundleCompanyUsage, SpcfCalendar pBillDate) {
		AssistedBundleBill foundBill = null;

		CalendarUtils.clearTime(pBillDate);

		NaturalKey naturalKey = new NaturalKey(AssistedBundleBill.class, pAssistedBundleCompanyUsage.getId(), pBillDate);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundBill = Application.findById(AssistedBundleBill.class, primaryKey);
		} else {
			DomainEntitySet<AssistedBundleBill> bills = Application.find(AssistedBundleBill.class, AssistedBundleBill.BillDate().equalTo(pBillDate).And(AssistedBundleBill.AsstBundleCompUsage().equalTo(pAssistedBundleCompanyUsage)));

			if (bills.size() > 1) {
				throw new RuntimeException("Query for assisted bundle bills by billing date" + pBillDate + " and company " + pAssistedBundleCompanyUsage + " did not return 0 or 1 results as expected");
			}

			if (!bills.isEmpty()) {
				foundBill = bills.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundBill.getId());
			}
		}

		return foundBill;
	}


	public static AssistedBundleBill createAssistedBundleBill(AsstBundleCompUsage pCompanyUsage, SpcfCalendar pBillDate) {
		AssistedBundleBill createdBill = new AssistedBundleBill();
		SpcfMoney zeroAmount = new SpcfMoney("0");
		CalendarUtils.clearTime(pBillDate);

		createdBill.setBillDate(pBillDate);
		createdBill.setTotalAmount(zeroAmount);
		createdBill.setTotalCount(0);
		createdBill.setAsstStatus(AssistedBillStatus.Open);
		createdBill.setAsstBundleCompUsage(pCompanyUsage);

		Application.save(createdBill);
		NaturalKey naturalKey = new NaturalKey(Bill.class, pCompanyUsage.getId(), pBillDate);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdBill.getId());

		return createdBill;
	}



	public static AssistedBundleBill findOpenAssistedBundleBillOrCreate(AsstBundleCompUsage pCompanyUsage, SpcfCalendar pBillDate) {
		AssistedBundleBill assistedBundleBill = findAssistedBundleBill(pCompanyUsage, pBillDate);
		if (assistedBundleBill == null) {
			assistedBundleBill = createAssistedBundleBill(pCompanyUsage, pBillDate);
		}

		return assistedBundleBill;
	}

	public static void createOrUpdateAssistedBundleBill(AsstBundleCompUsage pCompanyUsage, PayrollRun payrollRun) {
		SpcfCalendar billDate = CalendarUtils.getLastDayOfMonth(payrollRun.getPayrollRunDate());
		DomainEntitySet<BillingDetail> billingDetails = BillingDetail.findBillingDetails(payrollRun, OfferingServiceChargeType.EmployeesPaid);
		if(billingDetails.isEmpty()) {
			logger.info("Assisted bundle bill creation, billing detail is empty, payroll run id: " + payrollRun.getId());
			return;
		}
		if (billingDetails.size() > 1) {
			throw new RuntimeException("Query for Billing detail return more than 1 entry Payroll run ID" + payrollRun.getId());
		}
		BillingDetail billingDetail = billingDetails.get(0);
		AssistedBundleBill assistedBundleBill = findOpenAssistedBundleBillOrCreate(pCompanyUsage, billDate);
		assistedBundleBill.setTotalCount(assistedBundleBill.getTotalCount() + billingDetail.getQuantity());
		SpcfDecimal total =  assistedBundleBill.getTotalAmount().add(billingDetail.getItemTotal());
		SpcfMoney money= new SpcfMoney(total);
		assistedBundleBill.setTotalAmount(money);
		Application.save(assistedBundleBill);

		AsstBundleBillDetail.associateAssistedBundleBillWithBillingDetail(assistedBundleBill, billingDetail);
	}
}
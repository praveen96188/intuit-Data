package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.EmployeeUsageFoundCode;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hand-written business logic
 */
public class PaycheckUsage extends BasePaycheckUsage {

	public static SpcfLogger logger = SpcfLogManager.getLogger(PaycheckUsage.class);

	/**
	 * Default constructor.
	 */
	public PaycheckUsage() {
		super();
	}

	public static PaycheckUsage findPaycheckUsage(EmployeeUsage pEmployeeUsage, String pSourcePaycheckId) {
		PaycheckUsage foundPaycheckUsage = null;

		NaturalKey naturalKey = new NaturalKey(PaycheckUsage.class, pEmployeeUsage.getId(), pSourcePaycheckId);
		SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

		if (primaryKey != null) {
			foundPaycheckUsage = Application.findById(PaycheckUsage.class, primaryKey);
		} else {
			DomainEntitySet<PaycheckUsage> paycheckUsages = Application.find(PaycheckUsage.class, PaycheckUsage.EmployeeUsage().equalTo(pEmployeeUsage).And(PaycheckUsage.SourcePaycheckId().equalTo(pSourcePaycheckId)));

			if (paycheckUsages.size() > 1) {
				throw new RuntimeException("Query for paycheck usages by employee" + pEmployeeUsage + " and paycheck id " + pSourcePaycheckId + " did not return 0 or 1 results as expected");
			}

			if (!paycheckUsages.isEmpty()) {
				foundPaycheckUsage = paycheckUsages.get(0);
				Application.getSessionCache().addPrimaryKey(naturalKey, foundPaycheckUsage.getId());
			}
		}

		return foundPaycheckUsage;
	}

	public static PaycheckUsage findPaycheckUsageByQbdtListId(String pSourcePaycheckId) {
		DomainEntitySet<PaycheckUsage> paycheckUsages = Application.find(PaycheckUsage.class, PaycheckUsage.SourcePaycheckId().equalTo(pSourcePaycheckId));
		return paycheckUsages.getFirst();
	}

	private static PaycheckUsage createPaycheckUsage(EmployeeUsage pEmployeeUsage, String pSourcePaycheckId, SpcfCalendar pPaycheckDate, String pCheckNumber, String pTransactionId, boolean pPaycheckStatusActive, ReasonForFreeChargeCode pReasonForFreeChargeCode, Bill pBill) {
		if (pBill.getClosed()) {
			throw new RuntimeException("Can not create a paycheck usage on closed bill");
		}

		PaycheckUsage createdPaycheckUsage = new PaycheckUsage();

		createdPaycheckUsage.setEmployeeUsage(pEmployeeUsage);
		createdPaycheckUsage.setSourcePaycheckId(pSourcePaycheckId);
		createdPaycheckUsage.setPaycheckDate(pPaycheckDate);
		createdPaycheckUsage.setCheckNumber(pCheckNumber);
		createdPaycheckUsage.setTransactionId(pTransactionId);
		BillingPaycheckStatusCode billingPaycheckStatusCode;
		if (pPaycheckStatusActive) {
			billingPaycheckStatusCode = BillingPaycheckStatusCode.Active;
		} else {
			billingPaycheckStatusCode = BillingPaycheckStatusCode.Cancelled;
		}
		createdPaycheckUsage.setPaycheckStatusCode(billingPaycheckStatusCode);
        createdPaycheckUsage.setReasonForFreeCharge(pReasonForFreeChargeCode);
		createdPaycheckUsage.setBill(pBill);

		Application.save(createdPaycheckUsage);

		NaturalKey naturalKey = new NaturalKey(PaycheckUsage.class, pEmployeeUsage.getId(), pSourcePaycheckId);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdPaycheckUsage.getId());

		return createdPaycheckUsage;
	}

    public boolean isFree() {
        return getReasonForFreeCharge() != null && getReasonForFreeCharge() != ReasonForFreeChargeCode.None;
    }

    public boolean isCancelled() {
        return getPaycheckStatusCode() != BillingPaycheckStatusCode.Active;
    }

	public static PaycheckUsage updateStatusOrCreatePaycheckUsage(EmployeeUsage pEmployeeUsage, String pSourcePaycheckId, SpcfCalendar pPaycheckDate, String pCheckNumber, String pTransactionId, boolean pPaycheckStatusActive, ReasonForFreeChargeCode pReasonForFreeChargeCode, Bill pBill, AtomicInteger pUsageContribution,
																  boolean pPaycheckCreatedDateLessThanBillingStartDate) {
        pUsageContribution.set(0);
		PaycheckUsage paycheckUsage = findPaycheckUsage(pEmployeeUsage, pSourcePaycheckId);

		if (paycheckUsage == null) {
			paycheckUsage = createPaycheckUsage(pEmployeeUsage, pSourcePaycheckId, pPaycheckDate, pCheckNumber, pTransactionId, pPaycheckStatusActive, pReasonForFreeChargeCode, pBill);

			boolean paycheckNotPartOfUsageBilling = isPaycheckUsageNotPartOfUsageBilling(pSourcePaycheckId, pPaycheckCreatedDateLessThanBillingStartDate, pEmployeeUsage);

			if(paycheckNotPartOfUsageBilling) {
				paycheckUsage.setReasonForFreeCharge(ReasonForFreeChargeCode.NotPartOfUsageBilling);
				Application.save(paycheckUsage);

				logger.info(String.format("%s %s", "PAYCHECK_NOT_PART_OF_USAGE_BILLING", paycheckUsage));

				// Paycheck Usage not chargeable
				pUsageContribution.set(0);
			} else if(Objects.isNull(pEmployeeUsage.getEmployeeUsageFoundCode())) {

				PaycheckUsageHelper.checkAndAssociatePaycheckUsageWithAlreadyBilledEmployeeUsage(pEmployeeUsage.getAlreadyBilledPaychecks(), pEmployeeUsage, paycheckUsage);

				if (!paycheckUsage.isAlreadyBilled() && !paycheckUsage.isFree() && pPaycheckStatusActive) {
					// No existing employee usage found
					pUsageContribution.set(1);
				}
			} else {
				PaycheckUsageHelper.processExistingEmployeeUsage(paycheckUsage, pEmployeeUsage, pUsageContribution);
			}

		} else {
			SpcfCalendar lookbackDate = PSPDate.getPSPTime();
			lookbackDate.addDays(-SystemParameter.findIntValue(SystemParameter.Code.EMSBS_MAX_NUMBER_LOOKBACK_DAYS));
			if (paycheckUsage.getPaycheckDate().after(lookbackDate) && paycheckUsage.getPaycheckStatusCode() == BillingPaycheckStatusCode.Active && !pPaycheckStatusActive) {
				if (paycheckUsage.getBill().getClosed()) {
					paycheckUsage.setPaycheckStatusCode(BillingPaycheckStatusCode.CancelledAfterBillClose);
				} else {
					paycheckUsage.setPaycheckStatusCode(BillingPaycheckStatusCode.Cancelled);
                    if (!paycheckUsage.isFree()) {
                        pUsageContribution.set(-1);
                    }
				}
				Application.save(paycheckUsage);
			}
		}

		return paycheckUsage;
	}

	private boolean isAlreadyBilled(){
		return getReasonForFreeCharge() == ReasonForFreeChargeCode.AlreadyBilled;
	}

	private static boolean isPaycheckUsageNotPartOfUsageBilling(String pSourcePaycheckId, boolean pPaycheckCreatedDateLessThanBillingStartDate, EmployeeUsage employeeUsage){
		if(!pPaycheckCreatedDateLessThanBillingStartDate){
			return false;
		}

		PaycheckUsage existingPaycheckUsage = PaycheckUsage.findPaycheckUsageByQbdtListId(pSourcePaycheckId);

		if(Objects.nonNull(existingPaycheckUsage)){
			return false;
		}

		Pair<EmployeeUsageFoundCode, DomainEntitySet<EmployeeUsage>> existingEmployeeUsagePair = employeeUsage.findOtherEmployeeUsages();

		if(Objects.nonNull(existingEmployeeUsagePair)){
			return false;
		}

		return true;
	}


	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PaycheckUsage ")
				.append("  PaycheckUsageId=").append(getId())
				.append("  EmployeeUsageId=").append(getEmployeeUsage().getId())
				.append("  SourceCompanyId=").append(getEmployeeUsage().getUsagePeriod().getCompanyUsage().getSourceCompanyId())
				.append("  SourceEmployeeId=").append(getEmployeeUsage().getSourceEmployeeId())
				.append("  UsagePeriodStartDate=").append(getEmployeeUsage().getUsagePeriod().getStartDate())
				.append("  UsagePeriodEndDate=").append(getEmployeeUsage().getUsagePeriod().getEndDate())
				.append("  ReasonForFreeCharge=").append(getReasonForFreeCharge());

		DomainEntitySet<PaycheckUsageHist> paycheckUsageHists = getPaycheckUsageHistCollection();
		for (PaycheckUsageHist paycheckUsageHist: paycheckUsageHists){
			builder.append("  PaycheckUsageHistEmployeeUsageId=").append(paycheckUsageHist.getEmployeeUsage().getId());
			builder.append("  PaycheckUsageHistNotes=").append(paycheckUsageHist.getNotes());
		}

		return builder.toString();
	}
}

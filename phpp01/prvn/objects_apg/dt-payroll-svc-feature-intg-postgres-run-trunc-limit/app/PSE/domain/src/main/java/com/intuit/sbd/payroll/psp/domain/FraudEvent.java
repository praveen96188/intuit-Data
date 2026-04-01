package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;
import java.util.List;

/**
 * Hand-written business logic
 */
public class FraudEvent extends BaseFraudEvent {
    public static String CompanyEinKeyName="FEvent_Company_EIN";

	/**
	 * Default constructor.
	 */
	public FraudEvent()
	{
		super();
	}

    public FraudEvent(Company pCompany, CompanyEvent pCompanyEvent) {
        this(pCompany, pCompanyEvent, null, null);
    }

    public FraudEvent(Company pCompany, CompanyEvent pCompanyEvent, PayrollRun pPayrollRun, Employee pEmployee) {
         // company
        setCompany(pCompany);
        setCompanyEIN(pCompany.getFedTaxId());
        setCompanyPSID(pCompany.getSourceCompanyId());

        // event (must have fraud event details)
        setCompanyEvent(pCompanyEvent);
        setEventTimeStamp(pCompanyEvent.getEventTimeStamp());
        setEventTypeCd(pCompanyEvent.getEventTypeCd());
        setEventStatusCd(pCompanyEvent.getStatusCd());
        setFraudCategory(pCompanyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FraudEventCategory));
        setFraudTriggerDetail(pCompanyEvent.getCompanyEventDetailValue(EventDetailTypeCode.Details));

        // payroll
        if (pPayrollRun != null) {
            setPayrollRun(pPayrollRun);
            setPayrollDirectDepositAmount(pPayrollRun.getPayrollDirectDepositAmount());
        }

        // employee
        if (pEmployee != null) {
            setEmployee(pEmployee);
        }
    }

    public static DomainEntitySet<FraudEvent> findActiveCompanyFraudEvents(Company pCompany) {
        return findActiveCompanyFraudEvents(pCompany, null);
    }

    public static DomainEntitySet<FraudEvent> findActiveCompanyFraudEvents(Company pCompany, FraudEventCategory... pFraudEventCategories) {
        Criterion<FraudEvent> criterion = Company().equalTo(pCompany).And(EventStatusCd().equalTo(CompanyEventStatus.Active));
        if (pFraudEventCategories != null && pFraudEventCategories.length > 0) {
            String[] categoryNames = new String[pFraudEventCategories.length];
            for (int i = 0; i < pFraudEventCategories.length; i++) {
                categoryNames[i] = pFraudEventCategories[i].name();
            }
            criterion = criterion.And(FraudEvent.FraudCategory().in(categoryNames));
        }
        Expression<FraudEvent> fraudEventQuery = new Query<FraudEvent>().Where(criterion).OrderBy(FraudEvent.EventTimeStamp());
        return Application.find(FraudEvent.class, fraudEventQuery);
    }

    public static void deactiveCompanyFraudEvents(Company pCompany, FraudEventCategory... pFraudEventCategories) {
        for (FraudEvent fraudEvent : findActiveCompanyFraudEvents(pCompany, pFraudEventCategories)) {
            fraudEvent.setEventStatusCd(CompanyEventStatus.Inactive);
            Application.save(fraudEvent);
            fraudEvent.getCompanyEvent().setStatusCd(CompanyEventStatus.Inactive);
            Application.save(fraudEvent.getCompanyEvent());
        }
    }

    /**
     * Return fraud company events only if the fraud flag is still set (on payroll fraud) or the company is still on fraud review (sign up)
     * This is basically the unprocessed, "active" fraud events from a queue
     *
     * @param pEinCid             if specified, event must be on a company matching this EIN or source company id
     * @param pFraudEventCategory if specified, event must be in this fraud category (payroll or sign-up)
     * @param pFromDate           if specified, event must have occurred after this date
     * @param pToDate             if specified, event must have occurred before this date
     * @param pPayrollDirectDepositAmount   if specified, payroll fraud events must have this payroll net amount
     * @param report              whether to include report information or just the events
     * @param eventTypeCodes      if specified, event must be of type specified
     * @return either list of < CompanyEvent, fraudEventCategory, fraudEvent"Details", [PayrollRun], [Employee] > or List of CompanyEvent
     */
    static DomainEntitySet<FraudEvent> findActiveCompanyFraudEventsQuery(String pEinCid, Company pCompany,
                                                                         FraudEventCategory pFraudEventCategory, SpcfCalendar pFromDate,
                                                                         SpcfCalendar pToDate, SpcfMoney pPayrollDirectDepositAmount, Collection<EventTypeCode> eventTypeCodes, boolean report) {

        Criterion<FraudEvent> fraudEventCriterion = EventStatusCd().equalTo(CompanyEventStatus.Active);
        if (pFromDate != null) {
            fraudEventCriterion = fraudEventCriterion.And(EventTimeStamp().greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            fraudEventCriterion = fraudEventCriterion.And(EventTimeStamp().lessOrEqualThan(pToDate));
        }

        if (pEinCid != null) {
            List<String> fraudEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(FraudEvent.CompanyEinKeyName,pEinCid);
            pEinCid = pEinCid + "%";
            fraudEventCriterion = fraudEventCriterion.And(CompanyPSID().like(pEinCid).Or(CompanyEinEnc().in(fraudEinEncList)));
        }
        if (pCompany != null) {
            fraudEventCriterion = fraudEventCriterion.And(Company().equalTo(pCompany));
        }
        if (pFraudEventCategory != null) {
            fraudEventCriterion = fraudEventCriterion.And(FraudCategory().equalTo(pFraudEventCategory.name()));
        }
        if (pPayrollDirectDepositAmount != null && pFraudEventCategory != null && pFraudEventCategory == FraudEventCategory.Payroll) {
            fraudEventCriterion = fraudEventCriterion.And(PayrollDirectDepositAmount().greaterOrEqualThan(pPayrollDirectDepositAmount));
        } else if (pPayrollDirectDepositAmount != null && pFraudEventCategory == null) {
            fraudEventCriterion = fraudEventCriterion.And(
                    PayrollDirectDepositAmount().greaterOrEqualThan(pPayrollDirectDepositAmount)
                    .Or(FraudCategory().equalTo(FraudEventCategory.SignUp.name())));
        }
        if (eventTypeCodes != null && eventTypeCodes.size() > 0) {
            fraudEventCriterion = fraudEventCriterion.And(EventTypeCd().in(eventTypeCodes.toArray(new EventTypeCode[]{})));
        }

        Expression<FraudEvent> fraudEventQuery =
                new Query<FraudEvent>()
                        .Where(fraudEventCriterion)
                        .OrderBy(EventTimeStamp(),CreatedDate(), Company())
                        .EagerLoad(Company(), PayrollRun());

        return Application.find(FraudEvent.class, fraudEventQuery);
    }

    public static DomainEntitySet<FraudEvent> findActiveCompanyFraudEventsReport(String pEinCid,
                                                                    FraudEventCategory pFraudEventCategory, SpcfCalendar pFromDate,
                                                                    SpcfCalendar pToDate, SpcfMoney pPayrollNetAmount, Collection<EventTypeCode> eventTypeCodes) {

        return findActiveCompanyFraudEventsQuery(pEinCid, null, pFraudEventCategory, pFromDate, pToDate,
                                                            pPayrollNetAmount, eventTypeCodes, true);

    }

    public void setCompanyEIN(String pCompanyEIN) {
        super.setCompanyEinEnc(EncryptionUtils.deterministicEncrypt(CompanyEinKeyName,pCompanyEIN));
    }


    public String getCompanyEIN() {
        return EncryptionUtils.deterministicDecrypt(CompanyEinKeyName,getCompanyEinEnc());
    }

}
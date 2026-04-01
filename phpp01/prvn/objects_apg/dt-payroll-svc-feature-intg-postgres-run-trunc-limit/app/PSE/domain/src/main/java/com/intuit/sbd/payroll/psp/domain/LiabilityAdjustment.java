package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class LiabilityAdjustment extends BaseLiabilityAdjustment implements IUpdatable {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public LiabilityAdjustment()
	{
		super();
	}
    /**
     * Return the cache key
     * @return
     */
    public NaturalKey getNaturalKey() {
        return getNaturalKey(getCompany(),
                             getPayrollRun(),
                             getEmployee(),
                             getLaw(),
                             getCompanyLaw(),
                             getCompanyAdjustmentSubmission(),
                             getIsReconcilingAdjustment(),
                             getAmount(),
                             getTotalWages(),
                             getTaxableWages(),
                             getEffectiveDate());
    }

    /**
     * Cache Key for this entity
     *
     * @param pCompany                     Company associated with this liability adjustment
     * @param pPayrollRun                  PayrollRun entity
     * @param pEmployee                    Employee
     * @param pLaw                         Law entity
     * @param pCompanyLaw                  Company Law Entity
     * @param pCompanyAdjustmentSubmission Company Adjustment Submission entituy
     * @param pIsReconcilingAdjustment     IsReconcilingAdjustment
     * @param pAmount                      Amount
     * @param pTotalWages                  Total Wages
     * @param pTaxableWages                Taxable Wages
     * @param pEffectiveDate Effective Date
     * @return Cache Key
     */
    public static NaturalKey getNaturalKey(Company pCompany,
                                           PayrollRun pPayrollRun,
                                           Employee pEmployee,
                                           Law pLaw,
                                           CompanyLaw pCompanyLaw,
                                           CompanyAdjustmentSubmission pCompanyAdjustmentSubmission,
                                           boolean pIsReconcilingAdjustment,
                                           SpcfMoney pAmount,
                                           SpcfMoney pTotalWages,
                                           SpcfMoney pTaxableWages,
                                           SpcfCalendar pEffectiveDate) {
        Object[] keys = new Object[11];
        keys[0] = (pCompany == null) ? "NULL_COMPANY" : pCompany.getId();
        keys[1] = (pPayrollRun == null) ? "NULL_PAYROLL_RUN" :  pPayrollRun.getId();
        keys[2] = (pEmployee == null) ? "NULL_EMPLOYEE" : pEmployee.getId();
        keys[3] = (pLaw == null) ? "NULL_LAW" :  pLaw.getLawId();
        keys[4] = (pCompanyLaw == null) ? "NULL_COMPANY_LAW" : pCompanyLaw.getId();
        keys[5] = (pCompanyAdjustmentSubmission == null) ? "NULL_COMP_ADJ_SUBMISSION" : pCompanyAdjustmentSubmission.getId();
        keys[6] =  pIsReconcilingAdjustment;
        keys[7] = (pAmount == null) ? "NULL_AMOUNT" : pAmount;
        keys[8] = (pTotalWages == null) ? "NULL_TOTAL_WAGES" : pTotalWages;
        keys[9] = (pTaxableWages == null) ? "NULL_TAXABLE_WAGES" : pTaxableWages;
        keys[10] = (pEffectiveDate == null) ? "NULL_SUBMISSION_DATE" : pEffectiveDate;
        return new NaturalKey(Company.class, keys);
    }

    public void cache() {
        Application.getSessionCache().addPrimaryKey(getNaturalKey(), getId());
    }


    public boolean isVoided() {
        return getCompanyAdjustmentSubmission() != null && getCompanyAdjustmentSubmission().isVoid();
    }

    public CompanyAdjustmentSubmission getVoidSubmission() {
        return getCompanyAdjustmentSubmission() != null ? getCompanyAdjustmentSubmission().getVoidSubmission() : null;
    }

    public static DomainEntitySet<LiabilityAdjustment> findNonSupersededELAsByEmployee(Company pCompany, Employee pEmployee, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Criterion<LiabilityAdjustment> where = LiabilityAdjustment.PayrollRun().Company().equalTo(pCompany)
                                                    .And(LiabilityAdjustment.Employee().equalTo(pEmployee))
                                                    .And(LiabilityAdjustment.PayrollRun().PayrollRunStatus().notEqualTo(PayrollStatus.Superseded))
                                                                  .And(LiabilityAdjustment.PayrollRun().PaycheckSet().NotExists(Paycheck.SourcePaycheckId().like("-%")));
        if (pFromDate != null) {
            where = where.And(LiabilityAdjustment.PayrollRun().PaycheckDate().greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            where = where.And(LiabilityAdjustment.PayrollRun().PaycheckDate().lessOrEqualThan(pToDate));
        }

        return Application.find(LiabilityAdjustment.class, new Query<LiabilityAdjustment>().Where(where)
                                                                     .OrderBy(LiabilityAdjustment.PayrollRun().PaycheckDate()));
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setAmount(SpcfMoney pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);    
    }

    @Override
    public void setTaxableWages(SpcfMoney pTaxableWages) {
        if(!ObjectUtils.equals(getTaxableWages(), pTaxableWages)) {
            onUpdate();
        }
        super.setTaxableWages(pTaxableWages);    
    }

    @Override
    public void setTotalWages(SpcfMoney pTotalWages) {
        if(!ObjectUtils.equals(getTotalWages(), pTotalWages)) {
            onUpdate();
        }
        super.setTotalWages(pTotalWages);    
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
    }

    @Override
    public void setCompanyAdjustmentSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        if(!ObjectUtils.equals(getCompanyAdjustmentSubmission(), pCompanyAdjustmentSubmission)) {
            onUpdate();
        }
        super.setCompanyAdjustmentSubmission(pCompanyAdjustmentSubmission);    
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if(!ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);    
    }

    @Override
    public void setEmployee(Employee pEmployee) {
        if(!ObjectUtils.equals(getEmployee(), pEmployee)) {
            onUpdate();
        }
        super.setEmployee(pEmployee);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);    
    }

    public void onUpdate() {
        if(getCompanyAdjustmentSubmission() != null) {
            getCompanyAdjustmentSubmission().onUpdate();
        }

        if(getQbdtTransactionInfo() != null) {
            getQbdtTransactionInfo().onUpdate();
        }
    }
    /**
     * Find a LiabilityAdjustment DB/cache
     *
     * @param pCompany                     Company associated with this liability adjustment
     * @param pPayrollRun                  PayrollRun entity
     * @param pEmployee                    Employee
     * @param pLaw                         Law entity
     * @param pCompanyLaw                  Company Law Entity
     * @param pCompanyAdjustmentSubmission Company Adjustment Submission entituy
     * @param pIsReconcilingAdjustment      IsReconcilingAdjustment
     * @param pAmount                      Amount
     * @param pTotalWages                  Total Wages
     * @param pTaxableWages                Taxable Wages
     * @param pEffectiveDate Effective Date
     * @return LiabilityAdjustment entity
     */
    public static LiabilityAdjustment findLiabilityAdjustment(Company pCompany,
                                                              PayrollRun pPayrollRun,
                                                              Employee pEmployee,
                                                              Law pLaw,
                                                              CompanyLaw pCompanyLaw,
                                                              CompanyAdjustmentSubmission pCompanyAdjustmentSubmission,
                                                              boolean pIsReconcilingAdjustment,
                                                              SpcfMoney pAmount,
                                                              SpcfMoney pTotalWages,
                                                              SpcfMoney pTaxableWages,
                                                              SpcfCalendar pEffectiveDate) {
        NaturalKey naturalKey = getNaturalKey(pCompany,
                                              pPayrollRun,
                                              pEmployee,
                                              pLaw,
                                              pCompanyLaw,
                                              pCompanyAdjustmentSubmission,
                                              pIsReconcilingAdjustment,
                                              pAmount,
                                              pTotalWages,
                                              pTaxableWages,
                                              pEffectiveDate);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
        LiabilityAdjustment liabilityAdjustment = null;

        if (primaryKey == null) {
            Expression<LiabilityAdjustment> query = new Query<LiabilityAdjustment>()
                    .Where(LiabilityAdjustment.Company().equalTo(pCompany)
                                              .And(LiabilityAdjustment.PayrollRun().equalTo(pPayrollRun))
                                              .And(LiabilityAdjustment.Employee().equalTo(pEmployee))
                                              .And(LiabilityAdjustment.Law().equalTo(pLaw))
                                              .And(LiabilityAdjustment.CompanyAdjustmentSubmission().equalTo(pCompanyAdjustmentSubmission)
                                              .And(LiabilityAdjustment.CompanyLaw().equalTo(pCompanyLaw))
                                              .And(LiabilityAdjustment.IsReconcilingAdjustment().equalTo(pIsReconcilingAdjustment))
                                              .And(LiabilityAdjustment.Amount().equalTo(pAmount))
                                              .And(LiabilityAdjustment.TotalWages().equalTo(pTotalWages))
                                              .And(LiabilityAdjustment.TaxableWages().equalTo(pTaxableWages))
                                              .And(LiabilityAdjustment.EffectiveDate().equalTo(pEffectiveDate))
                                              ));
            DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = Application.find(LiabilityAdjustment.class, query);
            if (!liabilityAdjustments.isEmpty()) {
                liabilityAdjustment = liabilityAdjustments.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, liabilityAdjustment.getId());
            }
        } else {
            liabilityAdjustment = Application.findById(LiabilityAdjustment.class, primaryKey);
        }

        return liabilityAdjustment;
    }

    public static Map<Law, SpcfDecimal> calculateAdjustmentsAmountForQuarter(Company pCompany, SpcfCalendar pLiabilityDate) {
        SpcfCalendar quarterDate = pLiabilityDate.copy();
        CalendarUtils.clearTime(quarterDate);

        SpcfCalendar quarterStart = CalendarUtils.getFirstDayOfQuarter(quarterDate);
        SpcfCalendar quarterEnd = CalendarUtils.getLastDayOfQuarter(quarterDate);
        CalendarUtils.endOfDay(quarterEnd);

        Criterion<LiabilityAdjustment> searchExpression = LiabilityAdjustment.Company().equalTo(pCompany)
                                                                             .And(LiabilityAdjustment.EffectiveDate().between(quarterStart, quarterEnd));

        Map<Law, SpcfDecimal> calculatedAmounts = new HashMap<>();
        DomainEntitySet<LiabilityAdjustment> adjustmentsInMemory = getAdjustmentsInMemory(pCompany).find(searchExpression);
        // sum adjustments in memory
        for (LiabilityAdjustment liabilityAdjustment : adjustmentsInMemory) {
            calculatedAmounts.merge(liabilityAdjustment.getLaw(), liabilityAdjustment.getAmount(), SpcfDecimal::add);
        }

        //noinspection unchecked
        DomainEntitySet<LiabilityAdjustment> adjustments = Application.find(LiabilityAdjustment.class,
                                                                            new Query<LiabilityAdjustment>().Where(searchExpression)
                                                                                                            .EagerLoad(LiabilityAdjustment.Law()));
        // sum adjustments in the database
        for (LiabilityAdjustment adjustment : adjustments) {
            if(adjustmentsInMemory.contains(adjustment)) {
                // already included above
                continue;
            }
            calculatedAmounts.merge(adjustment.getLaw(), adjustment.getAmount(), SpcfDecimal::add);
        }
        return calculatedAmounts;
    }

    private static final String ADJUSTMENTS_IN_MEMORY_CACHE_KEY = "AdjustmentsInMemoryFromPayrollSubmit";
    public static DomainEntitySet<LiabilityAdjustment> getAdjustmentsInMemory(Company company) {
        DomainEntitySet<LiabilityAdjustment> adjustmentsInMemory = Application.getSessionCache().getNonHibernateObject(ADJUSTMENTS_IN_MEMORY_CACHE_KEY + ":" + company.getId());

        if (adjustmentsInMemory == null) {
            adjustmentsInMemory = new DomainEntitySet<>();
            Application.getSessionCache().addNonHibernateObject(ADJUSTMENTS_IN_MEMORY_CACHE_KEY + ":" + company.getId(), adjustmentsInMemory);
        }

        return adjustmentsInMemory;
    }
}
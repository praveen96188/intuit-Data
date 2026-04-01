package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hand-written business logic
 */
public class CompanyLaw extends BaseCompanyLaw implements IUpdatable {

    public NaturalKey getNaturalSourceIdKey(Company pCompany) {
        return new NaturalKey(CompanyLaw.class, pCompany.getId(), getSourceId());
    }

    public NaturalKey getNaturalLawIdKey(Company pCompany) {
        return new NaturalKey(Law.class, pCompany.getId(), getLaw().getLawId());
    }

    public void cache() {
        cache(getCompanyAgency().getCompany());
    }

    public void cache(Company pCompany) {
        Application.getSessionCache().addPrimaryKey(getNaturalSourceIdKey(pCompany), getId());
        Application.getSessionCache().addPrimaryKey(getNaturalLawIdKey(pCompany), getId());
    }

    public CompanyLaw getLatestCompanyLaw() {
        CompanyLaw companyLaw = this;
        while (companyLaw.getAdditionalCompanyLaw() != null) {
            companyLaw = companyLaw.getAdditionalCompanyLaw();
        }
        return companyLaw;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static CompanyLaw findCompanyLaw(CompanyAgency pCompanyAgency, Law pLaw) {
        return findCompanyLaw(pCompanyAgency.getCompany(), pLaw.getLawId());
    }

    public static DomainEntitySet<CompanyLaw> findAllCompanyLaws(Company pCompany) {
        DomainEntitySet<CompanyLaw> companyLawSet =
                Application.find(CompanyLaw.class,
                                 CompanyLaw.CompanyAgency().Company().equalTo(pCompany))
                           .sort(CompanyLaw.Law().LawId(), CompanyLaw.SourceId());
        return companyLawSet;
    }


    public static CompanyLaw findCompanyLaw(Company pCompany, String pLawId) {
        CompanyLaw companyLaw = null;
        NaturalKey naturalKey = new NaturalKey(Law.class, pCompany.getId(), pLawId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey == null) {
            @SuppressWarnings({"unchecked"})
            DomainEntitySet<CompanyLaw> companyLawSet =
                    Application.find(CompanyLaw.class,
                                     CompanyLaw.CompanyAgency().Company().equalTo(pCompany)
                                               .And(CompanyLaw.Law().LawId().equalTo(pLawId))
                                               .And(CompanyLaw.AdditionalCompanyLaw().isNull()));
            if (companyLawSet != null && companyLawSet.size() > 0) {
                companyLawSet.sort(CompanyLaw.<CompanyLaw>CreatedDate().Descending());
                companyLaw = companyLawSet.get(0);
                companyLaw.cache(pCompany);
            }
        } else {
            companyLaw = Application.findById(CompanyLaw.class, primaryKey);
        }

        return companyLaw;
    }

    public static CompanyLaw findCompanyLawBySourceId(Company pCompany, String pSourceId) {
        return findCompanyLawBySourceId(pCompany, pSourceId, false);
    }

    public static CompanyLaw findCompanyLawBySourceId(Company pCompany, String pSourceId, boolean pFetchAll) {

        //Using CACHE_KEY to identify if company Laws are cached for the company
        //FetchAll is true and Company Laws are not cached for the company, fetch all company laws to cache and add CACHE_KEY session variable

        //If Company law is not found for pSourceId in cache
        //If CACHE_KEY session variable is not found, query and get company law for pSourceId, return the company law for pSourceId.
        //return null if CACHE_KEY session variable is found for pCompany

        //If company law is cached, get the company from cache and return

        final String CACHE_KEY = "Cache:CompanyLaw:" + pCompany.getId();
        boolean isCached = Application.getSessionCache().getNonHibernateObject(CACHE_KEY) != null;

        if (pFetchAll && !isCached) {
            List<CompanyLaw> companyLaws = findCompanyLaws(pCompany, null);
            for (CompanyLaw compLaw : companyLaws) {
                compLaw.cache(pCompany);
            }
            Application.getSessionCache().addNonHibernateObject(CACHE_KEY, true);
        }

        if (pSourceId == null) {
            return null;
        }

        CompanyLaw companyLaw = null;
        NaturalKey naturalKey = new NaturalKey(CompanyLaw.class, pCompany.getId(), pSourceId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey == null) {
            if (!isCached) {
                List<CompanyLaw> companyLaws = findCompanyLaws(pCompany, pSourceId);
                if (companyLaws.size() > 0) {
                    companyLaw = companyLaws.get(0);
                    companyLaw.cache(pCompany);
                }
            }
        } else {
            companyLaw = Application.findById(CompanyLaw.class, primaryKey);
        }

        return companyLaw;
    }

    //Do not call this method to find Company Laws, always use findCompanyLawBySourceId
    private static List<CompanyLaw> findCompanyLaws(Company pCompany, String pSourceId) {

        Criterion<CompanyLaw> where = (CompanyLaw.CompanyAgency().Company().equalTo(pCompany));
        if (pSourceId != null){
            where = where.And(CompanyLaw.SourceId().equalTo(pSourceId));
        }

        Expression<CompanyLaw> query = new Query<CompanyLaw>()
                .Where(where)
                .EagerLoad(CompanyLaw.CompanyAgency()
                        ,CompanyLaw.Law()
                        ,CompanyLaw.QbdtPayrollItemInfo());

        List<CompanyLaw> companyLaws = new ArrayList<CompanyLaw>(Application.find(CompanyLaw.class, query));

        if (pSourceId != null && companyLaws.size() > 1) {
            throw new RuntimeException(
                    "Query for company law by company " + pCompany + " and source id " + pSourceId + " did not return 0 or 1 results as expected");
        }

        return companyLaws;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyLaw() {
        super();
    }

    public String getExpenseAccount() {
        if (getQbdtPayrollItemInfo() != null && getQbdtPayrollItemInfo().getExpenseAccount() != null) {
            return getQbdtPayrollItemInfo().getExpenseAccount();
        } else {
            // employee sui items do not have an expense account, so find the sui er law
            for (CompanyLaw companyLaw : getCompanyAgency().getCompanyLawCollection()) {
                if (companyLaw.getLaw().isSUIER()) {
                    return companyLaw.getExpenseAccount();
                }
            }
        }

        // default if no expense account can be found
        return "Payroll Expenses";
    }

    public static void eagerLoadCompanyLaws(Company pCompany, List<Law> pLawsToEagerLoad) {
        if (pLawsToEagerLoad.size() == 0) {
            return;
        }

        for (Iterator<Law> iterator = pLawsToEagerLoad.iterator(); iterator.hasNext(); ) {
            Law law = iterator.next();
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(new NaturalKey(Law.class, pCompany.getId(), law.getLawId()));
            if (primaryKey != null) {
                iterator.remove();
            }
        }

        if (pLawsToEagerLoad.isEmpty()) {
            return;
        }

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class, new Query<CompanyLaw>()
                .Where(CompanyLaw.CompanyAgency().Company().equalTo(pCompany)
                                 .And(CompanyLaw.Law().in(pLawsToEagerLoad.toArray(new Law[pLawsToEagerLoad.size()])))
                                 .And(CompanyLaw.AdditionalCompanyLaw().isNull()))
                .EagerLoad(CompanyLaw.Law()));
        for (CompanyLaw companyLaw : companyLaws) {
            companyLaw.cache(pCompany);
        }
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setExemptionStatus(LawStatus pExemptionStatus) {
        if (!ObjectUtils.equals(getExemptionStatus(), pExemptionStatus)) {
            onUpdate();
        }
        super.setExemptionStatus(pExemptionStatus);
    }

    @Override
    public void setTaxFormLine(String pTaxFormLine) {
        if (!ObjectUtils.equals(getTaxFormLine(), pTaxFormLine)) {
            onUpdate();
        }
        super.setTaxFormLine(pTaxFormLine);
    }

    @Override
    public void setStatus(PayrollItemStatus pStatus) {
        if (!ObjectUtils.equals(getStatus(), pStatus)) {
            onUpdate();
        }
        super.setStatus(pStatus);
    }

    @Override
    public void setSourceDescription(String pSourceDescription) {
        if (!ObjectUtils.equals(getSourceDescription(), pSourceDescription)) {
            onUpdate();
        }
        super.setSourceDescription(pSourceDescription);
    }

    @Override
    public void setSourceId(String pSourceId) {
        if (!ObjectUtils.equals(getSourceId(), pSourceId)) {
            onUpdate();
        }

        super.setSourceId(pSourceId);

        if (getCompanyAgency() != null && getSourceId() != null) {
            getCompanyAgency().getCompany().usedPayrollItemId(getSourceId());
        }
    }

    @Override
    public void setFilingStatus(PayrollItemStatus pFilingStatus) {
        if (!ObjectUtils.equals(getFilingStatus(), pFilingStatus)) {
            onUpdate();
        }
        super.setFilingStatus(pFilingStatus);
    }

    @Override
    public void setReimbursableStatus(ReimbursableStatus pReimbursableStatus) {
        if (!ObjectUtils.equals(getReimbursableStatus(), pReimbursableStatus)) {
            onUpdate();
        }
        super.setReimbursableStatus(pReimbursableStatus);
    }

    @Override
    public void setQbdtPayrollItemInfo(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        if (!ObjectUtils.equals(getQbdtPayrollItemInfo(), pQbdtPayrollItemInfo)) {
            onUpdate();
        }
        super.setQbdtPayrollItemInfo(pQbdtPayrollItemInfo);
    }

    @Override
    public void setCompanyAgency(CompanyAgency pCompanyAgency) {
        if (!ObjectUtils.equals(getCompanyAgency(), pCompanyAgency)) {
            onUpdate();
        }

        super.setCompanyAgency(pCompanyAgency);

        if (getCompanyAgency() != null && getSourceId() != null) {
            getCompanyAgency().getCompany().usedPayrollItemId(getSourceId());
        }
    }

    @Override
    public void setLaw(Law pLaw) {
        if (!ObjectUtils.equals(getLaw(), pLaw)) {
            onUpdate();
        }
        super.setLaw(pLaw);
    }

    @Override
    public void addCompanyLawRate(CompanyLawRate pCompanyLawRate) {
        super.addCompanyLawRate(pCompanyLawRate);
        onUpdate();
    }

    @Override
    public void removeCompanyLawRate(CompanyLawRate pCompanyLawRate) {
        super.removeCompanyLawRate(pCompanyLawRate);
        onUpdate();
    }

    public void onUpdate() {
        if (getQbdtPayrollItemInfo() != null) {
            getQbdtPayrollItemInfo().onUpdate();
        }
    }
}

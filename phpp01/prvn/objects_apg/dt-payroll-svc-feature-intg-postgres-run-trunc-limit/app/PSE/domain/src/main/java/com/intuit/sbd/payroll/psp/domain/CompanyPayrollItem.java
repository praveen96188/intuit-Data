package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class CompanyPayrollItem extends BaseCompanyPayrollItem implements IUpdatable {
    private static final String NATIONAL_PAID_LEAVE_EE = "TTT22";
    private static final String NATIONAL_PAID_LEAVE_FAMILY = "TTT23";
    private static final String NATIONAL_PAID_LEAVE_FMLA = "TTT24";
    private static final String NATIONAL_PAID_LEAVE_HEALTH = "TTT25";
    private static final String NATIONAL_PAID_LEAVE_MEDICARE = "TTT28";
    private static final String NATIONAL_PAID_LEAVE_ARPA_EE = "TTT30";
    private static final String NATIONAL_PAID_LEAVE_ARPA_FAMILY = "TTT31";
    private static final String NATIONAL_PAID_LEAVE_ARPA_FMLA = "TTT32";
    public static final List<String> FFCRA_TAX_FORM_LINE_CODES = Arrays.asList(NATIONAL_PAID_LEAVE_EE, NATIONAL_PAID_LEAVE_FAMILY,
            NATIONAL_PAID_LEAVE_FMLA, NATIONAL_PAID_LEAVE_HEALTH,
            NATIONAL_PAID_LEAVE_MEDICARE, NATIONAL_PAID_LEAVE_ARPA_EE,
            NATIONAL_PAID_LEAVE_ARPA_FAMILY,NATIONAL_PAID_LEAVE_ARPA_FMLA);


    private static final String CARES_RETENTION_CREDIT_EMP = "TTT26";
    private static final String CARES_RETENTION_CREDIT_HEALTH = "TTT27";
    public static final List<String> CARES_TAX_FORM_LINE_CODES = Arrays.asList(CARES_RETENTION_CREDIT_EMP, CARES_RETENTION_CREDIT_HEALTH);

    private static final String EE_FICA_DEFERRAL = "TTT29";
    public static final List<String> EE_FICA_DEFERRAL_TAX_FORM_LINE_CODES = Collections.singletonList(EE_FICA_DEFERRAL);

	/**
	 * Default constructor.
	 */
	public CompanyPayrollItem()
	{
		super();
	}

    public NaturalKey getNaturalKey() {
        return new NaturalKey(CompanyPayrollItem.class, getCompany().getId(), getSourcePayrollItemId());
    }

    public static CompanyPayrollItem findItemForSourcePayrollItemId(Company pCompany, String pSourcePayrollItemId) {
        CompanyPayrollItem foundCompanyPayrollItem = null;

        NaturalKey naturalKey = new NaturalKey(CompanyPayrollItem.class, pCompany.getId(), pSourcePayrollItemId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundCompanyPayrollItem = Application.findById(CompanyPayrollItem.class, primaryKey);
        } else {
            DomainEntitySet<CompanyPayrollItem> existingCompanyPayrollItems =
                    Application.find(CompanyPayrollItem.class, new Query<CompanyPayrollItem>()
                            .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                                                     .And(CompanyPayrollItem.SourcePayrollItemId().equalTo(pSourcePayrollItemId)))
                            .EagerLoad(CompanyPayrollItem.QbdtPayrollItemInfo()));

            if (existingCompanyPayrollItems.size()>1) {
                throw new RuntimeException("Did not find zero or one CompanyPayrollItem as expected for company " + pCompany.getId() + " and source pay item "+pSourcePayrollItemId);
            }

            if (existingCompanyPayrollItems.size()>0) {
                foundCompanyPayrollItem = existingCompanyPayrollItems.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundCompanyPayrollItem.getId());
            }
        }
        return foundCompanyPayrollItem;
    }

    public static CompanyPayrollItem findItemForPayrollItemIds(Company pCompany, String pSourcePayrollItemId, String pListId) {
        // this is intentionally fetching all items for a company - expected that this method will be used in a sequence
        // (could return excessive items if a company comes on and off service a number of times.  optimize later to
        //  only fetch non-archived items.)
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = pCompany.getCompanyPayrollItemCollection();
        Criterion<CompanyPayrollItem> payrollItemWhere = CompanyPayrollItem.SourcePayrollItemId().equalTo(pSourcePayrollItemId);
        DomainEntitySet<CompanyPayrollItem> existingCompanyPayrollItems = companyPayrollItems.find(payrollItemWhere);

        if (existingCompanyPayrollItems.size() > 1) {
            throw new RuntimeException("Did not find zero or one CompanyPayrollItem as expected for company " + pCompany.getId() + " and source pay item "+pSourcePayrollItemId);
        }

        CompanyPayrollItem payrollItem = null;
        if (existingCompanyPayrollItems.size() > 0) {
            payrollItem = existingCompanyPayrollItems.get(0);
            return payrollItem;
        }

        payrollItem = existingCompanyPayrollItems.findEntity(QbdtPayrollItemInfo().ListId().equalTo(pListId));
        return payrollItem;
    }

    public static DomainEntitySet<CompanyPayrollItem> findItemByPayrollItem(Company pCompany, PayrollItem pPayrollItem) {
        Expression<CompanyPayrollItem> query =
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                                .And(CompanyPayrollItem.PayrollItem().equalTo(pPayrollItem)));

        return Application.find(CompanyPayrollItem.class, query);
    }

    public static DomainEntitySet<CompanyPayrollItem> findAllCompanyPayrollItems(Company pCompany) {
        Expression<CompanyPayrollItem> query =
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany))
                        .OrderBy(CompanyPayrollItem.PayrollItem().PayrollItemCode(), CompanyPayrollItem.SourcePayrollItemId())
                        .EagerLoad(CompanyPayrollItem.PayrollItemTaxableToSet());

        return Application.find(CompanyPayrollItem.class, query);
    }

    public static DomainEntitySet<CompanyPayrollItem> findByPayrollItemType(Company pCompany, PayrollItemType pPayrollItemType) {
        Expression<CompanyPayrollItem> query = new Query<CompanyPayrollItem>()
                .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                        .And(CompanyPayrollItem.PayrollItem().PayrollItemType().equalTo(pPayrollItemType)));
        return Application.find(CompanyPayrollItem.class, query);
    }

    public static CompanyPayrollItem findDirectDepositPayrollItem(Company pCompany) {
        return   findDirectDepositPayrollItem(pCompany, true);
    }
    public static CompanyPayrollItem findDirectDepositPayrollItem(Company pCompany,boolean includeIsArchivedInWhereClause) {
        CompanyPayrollItem ddItem = null;

        NaturalKey naturalKey = new NaturalKey(CompanyPayrollItem.class, pCompany.getId(), PayrollItemCode.DirectDeposit);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            ddItem = Application.findById(CompanyPayrollItem.class, primaryKey);
        } else {
            Expression<CompanyPayrollItem> query = null;
            if(includeIsArchivedInWhereClause){
                query =  new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                                                 .And(CompanyPayrollItem.PayrollItem().PayrollItemCode().equalTo(PayrollItemCode.DirectDeposit))
                                                 .And(CompanyPayrollItem.IsArchived().equalTo(false))
                                                 .And(CompanyPayrollItem.QbdtPayrollItemInfo().IsDeleted().equalTo(false)))
                        .EagerLoad(CompanyPayrollItem.AdditionalPayrollItem());
            } else{
                query =  new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                                                 .And(CompanyPayrollItem.PayrollItem().PayrollItemCode().equalTo(PayrollItemCode.DirectDeposit))
                                                 .And(CompanyPayrollItem.QbdtPayrollItemInfo().IsDeleted().equalTo(false)))
                        .EagerLoad(CompanyPayrollItem.AdditionalPayrollItem());
            }

            List<CompanyPayrollItem> companyPayrollItems = new ArrayList<CompanyPayrollItem>(Application.find(CompanyPayrollItem.class, query));
            if(companyPayrollItems.size() > 0) {
                Collections.sort(companyPayrollItems, new Comparator<CompanyPayrollItem>() {
                    public int compare(CompanyPayrollItem a, CompanyPayrollItem b) {
                        // push all null and non integer ids to the end of the list
                        if(a == null || a.getSourcePayrollItemId() == null) {
                            return 1;
                        } else if (b == null || b.getSourcePayrollItemId() == null) {
                            return -1;
                        }

                        Integer sourceIdA;
                        try {
                            sourceIdA = Integer.parseInt(a.getSourcePayrollItemId());
                        } catch (NumberFormatException e) {
                            return 1;
                        }

                        Integer sourceIdB;
                        try {
                            sourceIdB = Integer.parseInt(b.getSourcePayrollItemId());
                        } catch (NumberFormatException e) {
                            return -1;
                        }

                        // sort descending
                        return sourceIdB.compareTo(sourceIdA);
                    }
                });

                ddItem = companyPayrollItems.get(0).getLatestCompanyPayrollItem();
                Application.getSessionCache().addPrimaryKey(naturalKey, ddItem.getId());
            }
        }

        return ddItem;
    }

    public static ProcessResult validatePayrollItem(PayrollItem pPayrollItem, SpcfMoney pAmount, String paycheckId) {
        ProcessResult validationResult = new ProcessResult();

        boolean doesProviderAcceptPayrollItem = doesProviderAcceptPayrollItem(pPayrollItem);
        boolean canAmountBeNegativeForPayrollItem = canAmountBeNegativeForPayrollItem(pPayrollItem);
        if (!doesProviderAcceptPayrollItem) {
            validationResult.getMessages().ProviderDoesNotAcceptPayrollItem(EntityName.PayrollItem, paycheckId, paycheckId, pPayrollItem.getPayrollItemDescription().toString());
        }

        if (pAmount!=null && pAmount.compareTo(SpcfMoney.ZERO)<0 && !canAmountBeNegativeForPayrollItem) {
            validationResult.getMessages().NegativeAmountNotAllowedForPayrollItem(EntityName.PayrollItem, paycheckId, paycheckId, pPayrollItem.getPayrollItemDescription().toString());
        }

        return validationResult;
    }

    private static boolean canAmountBeNegativeForPayrollItem(PayrollItem pPayrollItem) {
        if (pPayrollItem.getThirdParty401kPayrollItemInfo()!=null && pPayrollItem.getThirdParty401kPayrollItemInfo().getAllowsNegativeAmounts()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean doesProviderAcceptPayrollItem(PayrollItem pPayrollItem) {
        if (pPayrollItem.getThirdParty401kPayrollItemInfo()!=null && pPayrollItem.getThirdParty401kPayrollItemInfo().getIsProviderAccepted()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPreTax() {
        return getPayrollItemTaxableToCollection().isNotEmpty();
    }

    public CompanyPayrollItem getLatestCompanyPayrollItem() {
        CompanyPayrollItem companyPayrollItem = this;
        while (companyPayrollItem.getAdditionalPayrollItem() != null) {
            companyPayrollItem = companyPayrollItem.getAdditionalPayrollItem();
        }
        return companyPayrollItem;
    }

    public static CompanyPayrollItem findCompanyPayrollItemByDescription(Company pCompany, String pSourcePayrollItemDescription) {
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = Application.find(CompanyPayrollItem.class, CompanyPayrollItem.Company().equalTo(pCompany)
                                                                                                                               .And(CompanyPayrollItem.SourceDescription().equalTo(pSourcePayrollItemDescription.trim())));
        if(companyPayrollItems.isNotEmpty()) {
            return companyPayrollItems.get(0).getLatestCompanyPayrollItem();
        }

        return null;
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setStatus(PayrollItemStatus pStatus) {
        if(!ObjectUtils.equals(getStatus(), pStatus)) {
            onUpdate();
        }
        super.setStatus(pStatus);
    }

    @Override
    public void setSourceDescription(String pSourceDescription) {
        if(!ObjectUtils.equals(getSourceDescription(), pSourceDescription)) {
            onUpdate();
        }
        super.setSourceDescription(pSourceDescription);
    }

    @Override
    public void setSourcePayrollItemId(String pSourcePayrollItemId) {
        if(!ObjectUtils.equals(getSourcePayrollItemId(), pSourcePayrollItemId)) {
            onUpdate();
        }

        super.setSourcePayrollItemId(pSourcePayrollItemId);

        if(getCompany() != null && getSourcePayrollItemId() != null) {
            getCompany().usedPayrollItemId(getSourcePayrollItemId());
        }
    }

    @Override
    public void setTaxFormLine(String pTaxFormLine) {
        if(!ObjectUtils.equals(getTaxFormLine(), pTaxFormLine)) {
            onUpdate();
        }
        super.setTaxFormLine(pTaxFormLine);
    }

    @Override
    public void setQbdtPayrollItemInfo(QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        if(!ObjectUtils.equals(getQbdtPayrollItemInfo(), pQbdtPayrollItemInfo)) {
            onUpdate();
        }
        super.setQbdtPayrollItemInfo(pQbdtPayrollItemInfo);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }

        super.setCompany(pCompany);

        if(getCompany() != null && getSourcePayrollItemId() != null) {
            getCompany().usedPayrollItemId(getSourcePayrollItemId());
        }
    }

    @Override
    public void setPayrollItem(PayrollItem pPayrollItem) {
        if(!ObjectUtils.equals(getPayrollItem(), pPayrollItem)) {
            onUpdate();
        }
        super.setPayrollItem(pPayrollItem);
    }

    @Override
    public void addPayrollItemTaxableTo(PayrollItemTaxableTo pPayrollItemTaxableTo) {
        super.addPayrollItemTaxableTo(pPayrollItemTaxableTo);
        onUpdate();
    }

    @Override
    public void removePayrollItemTaxableTo(PayrollItemTaxableTo pPayrollItemTaxableTo) {
        super.removePayrollItemTaxableTo(pPayrollItemTaxableTo);
        onUpdate();
    }

    @Override
    public void addEmployerContribution(EmployerContribution pEmployerContribution) {
        super.addEmployerContribution(pEmployerContribution);
        onUpdate();
    }

    @Override
    public void removeEmployerContribution(EmployerContribution pEmployerContribution) {
        super.removeEmployerContribution(pEmployerContribution);
        onUpdate();
    }

    public void onUpdate() {
        if(getQbdtPayrollItemInfo() != null) {
            getQbdtPayrollItemInfo().onUpdate();
        }
    }
}
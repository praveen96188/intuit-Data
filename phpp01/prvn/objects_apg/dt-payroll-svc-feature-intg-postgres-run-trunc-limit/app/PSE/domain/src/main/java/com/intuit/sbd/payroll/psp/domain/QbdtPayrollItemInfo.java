package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class QbdtPayrollItemInfo extends BaseQbdtPayrollItemInfo implements IUpdatable {
    public static String AgencyIdKeyName="QBDT_PItmInfo_AID";

	/**
	 * Default constructor.
	 */
	public QbdtPayrollItemInfo()
	{
		super();
	}

    public static DomainEntitySet<QbdtPayrollItemInfo> findPayrollItemsWithGreaterToken(Company pCompany, long pSyncToken) {
        return Application.find(QbdtPayrollItemInfo.class,
                                new Query<QbdtPayrollItemInfo>()
                                        .Where(QbdtPayrollItemInfo.Company().equalTo(pCompany)
                                                .And(QbdtPayrollItemInfo.Token().greaterThan(pSyncToken)))
                                        .EagerLoad(QbdtPayrollItemInfo.CompanyLaw(),
                                                   QbdtPayrollItemInfo.CompanyLaw().QbdtPayrollItemInfo(),
                                                   QbdtPayrollItemInfo.CompanyLaw().Law(),
                                                   QbdtPayrollItemInfo.CompanyPayrollItem(),
                                                   QbdtPayrollItemInfo.CompanyPayrollItem().QbdtPayrollItemInfo(),
                                                   QbdtPayrollItemInfo.CompanyPayrollItem().PayrollItem()));
    }

    public static DomainEntitySet<CompanyLaw> findCompanyLawsWithGreaterToken(Company pCompany, long pSyncToken) {
        return Application.find(CompanyLaw.class,
                                new Query<CompanyLaw>()
                                        .Where(CompanyLaw.QbdtPayrollItemInfo().Company().equalTo(pCompany)
                                                .And(CompanyLaw.QbdtPayrollItemInfo().Token().greaterThan(pSyncToken))));
    }

    public static DomainEntitySet<CompanyLawRate> findValidLawRates(CompanyLaw pCompanyLaw) {
        return Application.find(CompanyLawRate.class,
                                new Query<CompanyLawRate>()
                                        .Where(CompanyLawRate.CompanyLaw().equalTo(pCompanyLaw)
                                                .And(CompanyLawRate.InvalidDate().isNull()))
                                        .OrderBy(CompanyLawRate.EffectiveDate().Descending())
                                        .EagerLoad(CompanyLawRate.CompanyLaw(),
                                                   CompanyLawRate.CompanyLaw().QbdtPayrollItemInfo(),
                                                   CompanyLawRate.CompanyLaw().Law(),
                                                   CompanyLawRate.CompanyLaw().Law().PaymentTemplate()));
    }

    public static DomainEntitySet<PayrollItemTaxableTo> findPayrollItemTaxableToWithGreaterToken(Company pCompany, long pSyncToken) {
        return Application.find(PayrollItemTaxableTo.class,
                                new Query<PayrollItemTaxableTo>()
                                        .Where(PayrollItemTaxableTo.CompanyPayrollItem().QbdtPayrollItemInfo().Company().equalTo(pCompany)
                                                .And(PayrollItemTaxableTo.CompanyPayrollItem().QbdtPayrollItemInfo().Token().greaterThan(pSyncToken)))
                                        .EagerLoad(PayrollItemTaxableTo.CompanyLaw(),
                                                   PayrollItemTaxableTo.CompanyLaw().QbdtPayrollItemInfo(),
                                                   PayrollItemTaxableTo.CompanyLaw().Law(),
                                                   PayrollItemTaxableTo.CompanyPayrollItem(),
                                                   PayrollItemTaxableTo.CompanyPayrollItem().QbdtPayrollItemInfo()));
    }

    public static DomainEntitySet<QbdtPayrollItemInfo> findPayrollItemsByCompany(Company pCompany) {
        return Application.find(QbdtPayrollItemInfo.class,
                                new Query<QbdtPayrollItemInfo>()
                                        .Where(QbdtPayrollItemInfo.Company().equalTo(pCompany))
                                        .EagerLoad(QbdtPayrollItemInfo.CompanyPayrollItem(),
                                                   QbdtPayrollItemInfo.CompanyPayrollItem().QbdtPayrollItemInfo(),
                                                   QbdtPayrollItemInfo.CompanyPayrollItem().PayrollItem()));
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setEarningsTable(boolean pEarningsTable) {
        if(getEarningsTable() != pEarningsTable) {
            onUpdate();
        }
        super.setEarningsTable(pEarningsTable);
    }

    @Override
    public void setIsEmployeePaid(boolean pIsEmployeePaid) {
        if(getIsEmployeePaid() != pIsEmployeePaid) {
            onUpdate();
        }
        super.setIsEmployeePaid(pIsEmployeePaid);
    }

    @Override
    public void setLiabilityAccount(String pLiabilityAccount) {
        if(!ObjectUtils.equals(getLiabilityAccount(), pLiabilityAccount)) {
            onUpdate();
        }
        super.setLiabilityAccount(pLiabilityAccount);
    }

    @Override
    public void setLiabilityAgency(String pLiabilityAgency) {
        if(!ObjectUtils.equals(getLiabilityAgency(), pLiabilityAgency)) {
            onUpdate();
        }
        super.setLiabilityAgency(pLiabilityAgency);
    }


    public void setAgencyId(String pAgencyId) {
        if(!ObjectUtils.equals(getAgencyId(), pAgencyId)) {
            onUpdate();
        }
        super.setAgencyIdEnc(EncryptionUtils.deterministicEncrypt(AgencyIdKeyName,pAgencyId));
    }

    @Override
    public void setAdjustsGross(boolean pAdjustsGross) {
        if(getAdjustsGross() != pAdjustsGross) {
            onUpdate();
        }
        super.setAdjustsGross(pAdjustsGross);
    }

    @Override
    public void setBasedOnQuantity(boolean pBasedOnQuantity) {
        if(getBasedOnQuantity() != pBasedOnQuantity) {
            onUpdate();
        }
        super.setBasedOnQuantity(pBasedOnQuantity);
    }

    @Override
    public void setExpenseAccount(String pExpenseAccount) {
        if(!ObjectUtils.equals(getExpenseAccount(), pExpenseAccount)) {
            onUpdate();
        }
        super.setExpenseAccount(pExpenseAccount);
    }

    @Override
    public void setDefaultRate(double pDefaultRate) {
        if(getDefaultRate() != pDefaultRate) {
            onUpdate();
        }
        super.setDefaultRate(pDefaultRate);
    }

    @Override
    public void setDefaultLimit(SpcfMoney pDefaultLimit) {
        if(!ObjectUtils.equals(getDefaultLimit(), pDefaultLimit)) {
            onUpdate();
        }
        super.setDefaultLimit(pDefaultLimit);
    }

    @Override
    public void setExpenseByJob(boolean pExpenseByJob) {
        if(getExpenseByJob() != pExpenseByJob) {
            onUpdate();
        }
        super.setExpenseByJob(pExpenseByJob);
    }

    @Override
    public void setPayType(QbdtPayType pPayType) {
        if(!ObjectUtils.equals(getPayType(), pPayType)) {
            onUpdate();
        }
        super.setPayType(pPayType);
    }

    @Override
    public void setSpecialType(QbdtSpecialType pSpecialType) {
        if(!ObjectUtils.equals(getSpecialType(), pSpecialType)) {
            onUpdate();
        }
        super.setSpecialType(pSpecialType);
    }

    @Override
    public void setIsDeleted(boolean pIsDeleted) {
        if(getIsDeleted() != pIsDeleted) {
            onUpdate();
        }
        super.setIsDeleted(pIsDeleted);
    }

    @Override
    public void setOnService(boolean pOnService) {
        if(getOnService() != pOnService) {
            onUpdate();
        }
        super.setOnService(pOnService);
    }

    @Override
    public void setDefaultRateType(QbdtNumericType pDefaultRateType) {
        if(!ObjectUtils.equals(getDefaultRateType(), pDefaultRateType)) {
            onUpdate();
        }
        super.setDefaultRateType(pDefaultRateType);
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if(!ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);
    }

    @Override
    public void setCompanyPayrollItem(CompanyPayrollItem pCompanyPayrollItem) {
        if(!ObjectUtils.equals(getCompanyPayrollItem(), pCompanyPayrollItem)) {
            onUpdate();
        }
        super.setCompanyPayrollItem(pCompanyPayrollItem);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    public void onUpdate() {
        if(getCompany() != null) {
            setToken(getCompany().getNextToken());
        }
    }

    public String getAgencyId() {
        return EncryptionUtils.deterministicDecrypt(AgencyIdKeyName,getAgencyIdEnc());
    }
}

package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;

/**
 * Hand-written business logic
 */
public class EmployeeTax extends BaseEmployeeTax implements IUpdatable {

    /**
     * Default constructor.
     */

    private static final SpcfLogger logger = SpcfLogManager.getLogger(EmployeeTax.class);

    public EmployeeTax() {
        super();
    }

    /**
     * @param pCompany
     * @return
     * @See Method to find all null records in employee tax for tax type 'Other'
     */
    public static DomainEntitySet<EmployeeTax> findNullOtherTax(Company pCompany) {

        Criterion<EmployeeTax> where = (EmployeeTax.CompanyLaw().isNull()).And(EmployeeTax.Employee().Company().equalTo(pCompany)).And(EmployeeTax.TaxType().equalTo(EmployeeTaxType.Other));
        DomainEntitySet<EmployeeTax> result = Application.find(EmployeeTax.class, new Query<EmployeeTax>()
                .Where(where));
        return result;
    }

    /**
     * @param pEmpTaxId
     * @return
     * @See method to delete null records in employee tax for tax type 'Other'
     */
    public static Boolean deleteEmpTaxRecord(SpcfUniqueId pEmpTaxId) {
        try {
            Application.delete(EmployeeTax.class, pEmpTaxId);

        } catch (HibernateException he) {
            logger.error("HibernateException while deleting the null other tax record for SEQ:  :" + pEmpTaxId);
            logger.error(he.getStackTrace());
            return false;
        } catch (Exception ee) {
            logger.error("Exception while deleting the null other tax record for SEQ:  :" + pEmpTaxId);
            logger.error(ee.getStackTrace());
            return false;
        }
        return true;
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setTaxLawVersion(String pTaxLawVersion) {
        if (!ObjectUtils.equals(getTaxLawVersion(), pTaxLawVersion)) {
            onUpdate();
        }
        super.setTaxLawVersion(pTaxLawVersion);
    }

    @Override
    public void setW2Name(String pW2Name) {
        if (!ObjectUtils.equals(getW2Name(), pW2Name)) {
            onUpdate();
        }
        super.setW2Name(pW2Name);
    }

    @Override
    public void setState(String pState) {
        if (!ObjectUtils.equals(getState(), pState)) {
            onUpdate();
        }
        super.setState(pState);
    }

    @Override
    public void setSubjectTo(boolean pSubjectTo) {
        if (!ObjectUtils.equals(getSubjectTo(), pSubjectTo)) {
            onUpdate();
        }
        super.setSubjectTo(pSubjectTo);
    }

    @Override
    public void setTaxType(EmployeeTaxType pTaxType) {
        if (!ObjectUtils.equals(getTaxType(), pTaxType)) {
            onUpdate();
        }
        super.setTaxType(pTaxType);
    }

    @Override
    public void setFilingStatus(String pFilingStatus) {
        if (!ObjectUtils.equals(getFilingStatus(), pFilingStatus)) {
            onUpdate();
        }
        super.setFilingStatus(pFilingStatus);
    }

    @Override
    public void setAllowances(int pAllowances) {
        if (!ObjectUtils.equals(getAllowances(), pAllowances)) {
            onUpdate();
        }
        super.setAllowances(pAllowances);
    }

    @Override
    public void setExtraWithholding(double pExtraWithholding) {
        if (!ObjectUtils.equals(getExtraWithholding(), pExtraWithholding)) {
            onUpdate();
        }
        super.setExtraWithholding(pExtraWithholding);
    }

    @Override
    public void setExtraWithholdingType(QbdtNumericType pExtraWithholdingType) {
        if (!ObjectUtils.equals(getExtraWithholdingType(), pExtraWithholdingType)) {
            onUpdate();
        }
        super.setExtraWithholdingType(pExtraWithholdingType);
    }

    @Override
    public void setCompanyLaw(CompanyLaw pCompanyLaw) {
        if (!ObjectUtils.equals(getCompanyLaw(), pCompanyLaw)) {
            onUpdate();
        }
        super.setCompanyLaw(pCompanyLaw);
    }

    @Override
    public void setEmployee(Employee pEmployee) {
        if (!ObjectUtils.equals(getEmployee(), pEmployee)) {
            onUpdate();
        }
        super.setEmployee(pEmployee);
    }

    @Override
    public void addTaxTableMiscData(TaxTableMiscData pTaxTableMiscData) {
        super.addTaxTableMiscData(pTaxTableMiscData);
        onUpdate();
    }

    @Override
    public void removeTaxTableMiscData(TaxTableMiscData pTaxTableMiscData) {
        super.removeTaxTableMiscData(pTaxTableMiscData);
        onUpdate();
    }

    @Override
    public void setTaxOrder(int pTaxOrder) {
        if (!ObjectUtils.equals(getTaxOrder(), pTaxOrder)) {
            onUpdate();
        }
        super.setTaxOrder(pTaxOrder);
    }

    public void onUpdate() {
        if (getEmployee() != null) {
            getEmployee().onUpdate();
        }
    }
}
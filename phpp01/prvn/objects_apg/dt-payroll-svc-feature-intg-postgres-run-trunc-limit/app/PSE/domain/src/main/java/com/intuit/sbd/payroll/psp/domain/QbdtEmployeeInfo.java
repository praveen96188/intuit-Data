package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Hand-written business logic
 */
public class QbdtEmployeeInfo extends BaseQbdtEmployeeInfo implements IUpdatable {

    /**
     * Default constructor.
     */
    public QbdtEmployeeInfo()
    {
        super();
    }

    public static DomainEntitySet<Employee> findEmployeesWithGreaterToken(Company pCompany, long pSyncToken) {
        Query<Employee> query = new Query<Employee>();
        query.Where(Employee.QbdtEmployeeInfo().Company().equalTo(pCompany)
                .And(Employee.QbdtEmployeeInfo().Token().greaterThan(pSyncToken)));
        // only pre-loading the small sets
        query.EagerLoad(Employee.Company(),
                // one-to-one relationship
                Employee.Company().QuickbooksInfo(),
                Employee.MailingAddress(),
                // this eager load stops the extra queries
                Employee.MailingAddress().Individual(),
                Employee.QbdtEmployeeInfo(),
                Employee.EmployeeAccrualSet(),
                Employee.EmployeeWagePlanSet());

        return Application.find(Employee.class, query);
    }

    public static DomainEntitySet<EmployeePayrollItem> findEmployeePayrollItemsWithGreaterToken(Company pCompany, long pSyncToken) {
        Expression<EmployeePayrollItem> query = new Query<EmployeePayrollItem>()
                .Where(EmployeePayrollItem.Employee().QbdtEmployeeInfo().Company().equalTo(pCompany)
                        .And(EmployeePayrollItem.Employee().QbdtEmployeeInfo().Token().greaterThan(pSyncToken)))
                .OrderBy(EmployeePayrollItem.ItemOrder())
                .EagerLoad(EmployeePayrollItem.Employee(),
                        EmployeePayrollItem.Employee().QbdtEmployeeInfo(),
                        EmployeePayrollItem.CompanyPayrollItem(),
                        EmployeePayrollItem.CompanyPayrollItem().QbdtPayrollItemInfo())
                .ReadOnly(true);
        return Application.find(EmployeePayrollItem.class, query);
    }

    public static DomainEntitySet<EmployeeCustomField> findEmployeeCustomFieldsWithGreaterToken(Company pCompany, long pSyncToken) {
        Expression<EmployeeCustomField> query = new Query<EmployeeCustomField>()
                .Where(EmployeeCustomField.Employee().QbdtEmployeeInfo().Company().equalTo(pCompany)
                        .And(EmployeeCustomField.Employee().QbdtEmployeeInfo().Token().greaterThan(pSyncToken)))
                .OrderBy(EmployeeCustomField.FieldOrder())
                .EagerLoad(EmployeeCustomField.Employee(), EmployeeCustomField.Employee().QbdtEmployeeInfo())
                .ReadOnly(true);
        return Application.find(EmployeeCustomField.class, query);
    }

    public static DomainEntitySet<EmployeeBankAccount> findEmployeeBankAccountsWithGreaterToken(Company pCompany, long pSyncToken) {
        Expression<EmployeeBankAccount> query = new Query<EmployeeBankAccount>()
                .Where(EmployeeBankAccount.Employee().QbdtEmployeeInfo().Company().equalTo(pCompany)
                        .And(EmployeeBankAccount.Employee().QbdtEmployeeInfo().Token().greaterThan(pSyncToken))
                        .And(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)))
                .OrderBy(EmployeeBankAccount.AccountOrder())
                .EagerLoad(EmployeeBankAccount.Employee(), EmployeeBankAccount.Employee().QbdtEmployeeInfo(), EmployeeBankAccount.BankAccount())
                .ReadOnly(true);
        return Application.find(EmployeeBankAccount.class, query);
    }

    public static DomainEntitySet<EmployeeTax> findEmployeeTaxesWithGreaterToken(Company pCompany, long pSyncToken) {
        Expression<EmployeeTax> query = new Query<EmployeeTax>()
                .Where(EmployeeTax.Employee().QbdtEmployeeInfo().Company().equalTo(pCompany)
                        .And(EmployeeTax.Employee().QbdtEmployeeInfo().Token().greaterThan(pSyncToken)))
                .OrderBy(EmployeeTax.TaxOrder())
                .EagerLoad(EmployeeTax.Employee(),
                        EmployeeTax.Employee().QbdtEmployeeInfo(),
                        EmployeeTax.CompanyLaw(),
                        EmployeeTax.CompanyLaw().AdditionalCompanyLaw(),
                        EmployeeTax.TaxTableMiscDataSet())
                .ReadOnly(true);
        return Application.find(EmployeeTax.class, query);
    }

    // ----- QBDT Token overrides -----
    @Override
    public void setBillPayAccount(String pBillPayAccount) {
        if(!ObjectUtils.equals(getBillPayAccount(), pBillPayAccount)) {
            onUpdate();
        }
        super.setBillPayAccount(pBillPayAccount);
    }

    @Override
    public void setInitials(String pInitials) {
        if(!ObjectUtils.equals(getInitials(), pInitials)) {
            onUpdate();
        }
        super.setInitials(pInitials);
    }

    @Override
    public void setPrintAsName(String pPrintAsName) {
        if(!ObjectUtils.equals(getPrintAsName(), pPrintAsName)) {
            onUpdate();
        }
        super.setPrintAsName(pPrintAsName);
    }

    @Override
    public void setTrackingClass(String pTrackingClass) {
        if(!ObjectUtils.equals(getTrackingClass(), pTrackingClass)) {
            onUpdate();
        }
        super.setTrackingClass(pTrackingClass);
    }

    @Override
    public void setUseDD(boolean pUseDD) {
        if(!ObjectUtils.equals(getUseDD(), pUseDD)) {
            onUpdate();
        }
        super.setUseDD(pUseDD);
    }

    @Override
    public void setUseTime(boolean pUseTime) {
        if(!ObjectUtils.equals(getUseTime(), pUseTime)) {
            onUpdate();
        }
        super.setUseTime(pUseTime);
    }

    @Override
    public void setEnforceSubjectTo(boolean pEnforceSubjectTo) {
        if(!ObjectUtils.equals(getEnforceSubjectTo(), pEnforceSubjectTo)) {
            onUpdate();
        }
        super.setEnforceSubjectTo(pEnforceSubjectTo);
    }

    @Override
    public void setEmployeeType(QbdtEmployeeType pEmployeeType) {
        if(!ObjectUtils.equals(getEmployeeType(), pEmployeeType)) {
            onUpdate();
        }
        super.setEmployeeType(pEmployeeType);
    }


    @Override
    public void setEmployeeSeasonal(QbdtEmployeeSeasonal pEmployeeSeasonal) {
        if(!ObjectUtils.equals(getEmployeeSeasonal(), pEmployeeSeasonal)) {
            onUpdate();
        }
        super.setEmployeeSeasonal(pEmployeeSeasonal);
    }



    @Override
    public void setIsDeleted(boolean pIsDeleted) {
        if(!ObjectUtils.equals(getIsDeleted(), pIsDeleted)) {
            onUpdate();
        }
        super.setIsDeleted(pIsDeleted);
    }

    @Override
    public void setTitle(String pTitle) {
        if(!ObjectUtils.equals(getTitle(), pTitle)) {
            onUpdate();
        }
        super.setTitle(pTitle);
    }

    @Override
    public void setAltPhone(String pAltPhone) {
        if(!ObjectUtils.equals(getAltPhone(), pAltPhone)) {
            onUpdate();
        }
        super.setAltPhone(pAltPhone);
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
        if(!ObjectUtils.equals(getCompany(), pCompany) && pCompany != null) {
            setToken(pCompany.getNextToken());
        }
        super.setCompany(pCompany);
    }

    public void onUpdate() {
        if(getCompany() != null) {
            setToken(getCompany().getNextToken());
        }
    }

    @Override
    public QbdtEmployeeSeasonal getEmployeeSeasonal() {
        return super.getEmployeeSeasonal();
    }
}
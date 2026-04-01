package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * User: rsakhamuri
 * Date: Jan 15, 2008
 * Time: 4:44:15 PM

 */
public class PropertyAuditBETests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    // Test by creating multiple company bank accounts in Audit property
    @Test
    public void testGetPropertyAuditByCompanyAndCBA() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        // Create another bank account
        DataLoader dataLoader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany123123123();
        dataLoader.persistTestCompanyService(company);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        CompanyBankAccountDTO companyBankAccount = dataLoader.getTestCompany123123123BankAccount();
        dataLoader.persistCompanyBankAccount(company, companyBankAccount);
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123123123", "123000", false, false);
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", false, false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // test to get only the specified bank account
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PropertyAudit> propertyAudits = PropertyAudit.findCompanyBankAccountPropertyAudits(company, "123123");
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Audits:", 2, propertyAudits.size());

        // Verify object ids
        assertTrue("Object Id :",
                propertyAudits.get(0).getObjectIdentifier().equals(processResult.getResult().getId().toString()));
        assertTrue("Object Id :",
                propertyAudits.get(0).getObjectIdentifier().equals(processResult.getResult().getId().toString()));

        // verify property values
        assertEquals("Old Value:", BankAccountStatus.PendingVerification.toString(), propertyAudits.get(0).getOldPropertyValue());
        assertEquals("New Value:", BankAccountStatus.Active.toString(), propertyAudits.get(0).getNewPropertyValue());

        assertEquals("Old Value:", BankAccountStatus.Active.toString(), propertyAudits.get(1).getOldPropertyValue());
        assertEquals("New Value:", BankAccountStatus.Inactive.toString(), propertyAudits.get(1).getNewPropertyValue());
    }

    // Test by creating multiple audits for the same company bank a/c
    @Test
    public void testGetPropertyAudit() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        // Create another bank account
        DataLoader dataLoader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany123123123();
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccount = dataLoader.getTestCompany123123123BankAccount();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        dataLoader.persistCompanyBankAccount(company, companyBankAccount);
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123123123", "123000", false, false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba = CompanyBankAccount.findCompanyBankAccount(company, "123123");

        // set company bank a/c status to Pending verification
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 3, SpcfTimeZone.getLocalTimeZone()));
        cba.updateBankAccountStatus(BankAccountStatus.PendingVerification);
        PayrollServices.commitUnitOfWork();

        //Set company bank account  status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult<CompanyBankAccount> processResult =
                PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", false, false);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // test to get only the specified bank account
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PropertyAudit> propertyAudits = PropertyAudit.findCompanyBankAccountPropertyAudits(company, "123123");
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Audits:", 3, propertyAudits.size());

        // Verify object ids
        assertTrue("Object Id :",
                propertyAudits.get(0).getObjectIdentifier().equals(processResult.getResult().getId().toString()));
        assertTrue("Object Id :",
                propertyAudits.get(1).getObjectIdentifier().equals(processResult.getResult().getId().toString()));
        assertTrue("Object Id :",
                propertyAudits.get(2).getObjectIdentifier().equals(processResult.getResult().getId().toString()));
        
        // verify property values
        // workaround the SPCF bug that truncates miliseconds
        // we can't assume order in the collection for the first two values
        assertEquals("Old Value:", BankAccountStatus.PendingVerification.toString(), propertyAudits.get(0).getOldPropertyValue());
        assertEquals("New Value:", BankAccountStatus.Active.toString(), propertyAudits.get(0).getNewPropertyValue());

        assertEquals("Old Value:", BankAccountStatus.Active.toString(), propertyAudits.get(1).getOldPropertyValue());
        assertEquals("New Value:", BankAccountStatus.PendingVerification.toString(), propertyAudits.get(1).getNewPropertyValue());

        assertEquals("Old Value:", BankAccountStatus.PendingVerification.toString(), propertyAudits.get(2).getOldPropertyValue());
        assertEquals("New Value:", BankAccountStatus.Inactive.toString(), propertyAudits.get(2).getNewPropertyValue());

    }

    // test company status field history
    @Test
    public void testCompanyStatusFieldHistory() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        // Create another company and bank accounts
        DataLoader dataLoader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany123123123();
        dataLoader.persistTestCompanyService(company);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        CompanyBankAccountDTO companyBankAccount = dataLoader.getTestCompany123123123BankAccount();
        dataLoader.persistCompanyBankAccount(company, companyBankAccount);
        PayrollServices.commitUnitOfWork();        

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        ProcessResult deactivateResult = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        assertSuccess("deactivateService", deactivateResult);

        //Set company status to "Active"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // test to get only the specified bank account
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PropertyAudit> propertyAudits =
                PropertyAudit.findPropertyAudits(company,
                                                PropertyAudit.TableNames.COMPANY_SERVICE,
                                                PropertyAudit.ColumnNames.DD_COMP_STATUS_CD,
                                                null);
        PayrollServices.commitUnitOfWork();

        verifyPropertyAudits(propertyAudits);
    }

    // test company FundingModel field history
    @Test
    public void testCompanyFundingModelHistory() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Set company status to "Active"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Change company funding model to 2D
        PayrollServices.beginUnitOfWork();
        // Update Funding Model to a Two Day Funding Model  - Default funding model is Five Day
        FundingModel fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);

        assertSuccess(PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, "123272727", fundingModel));
        PayrollServices.commitUnitOfWork();

        //Again change company funding model to 5D
        PayrollServices.beginUnitOfWork();
        fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);

        assertSuccess(PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, "123272727", fundingModel));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // test to get only the specified bank account
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PropertyAudit> propertyAudits =
                PropertyAudit.findPropertyAudits(company,
                                                PropertyAudit.TableNames.COMPANY,
                                                PropertyAudit.ColumnNames.FUNDING_MODEL_CD,
                                                null);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Audits:", propertyAudits.size() == 2);

        String[] oldValues = new String[2];
        String[] newValues = new String[2];
        int i = 0;
        for (PropertyAudit propertyAudit : propertyAudits) {
            if (propertyAudit.getOldPropertyValue() == null) {
                oldValues[i] = "";
            } else {
                oldValues[i] = propertyAudit.getOldPropertyValue();
            }
            newValues[i] = propertyAudit.getNewPropertyValue();
            i++;
        }
        Arrays.sort(oldValues);
        Arrays.sort(newValues);

        // verify property values
        assertTrue("Old Value 2:",
                oldValues[0].equals(FundingModel.Codes.TWO_DAY));
        assertTrue("Old Value 3:",
                oldValues[1].equals(FundingModel.Codes.FIVE_DAY));

        assertTrue("New Value 2:",
                newValues[0].equals(FundingModel.Codes.TWO_DAY));
        assertTrue("New Value 3:",
                newValues[1].equals(FundingModel.Codes.FIVE_DAY));

        assertTrue("Class Name: ", propertyAudits.get(0).getClassName().equals(PropertyAudit.TableNames.COMPANY));
        assertTrue("Class Name: ", propertyAudits.get(1).getClassName().equals(PropertyAudit.TableNames.COMPANY));

        assertTrue("Field Name: ", propertyAudits.get(0).getPropertyName().equals(PropertyAudit.ColumnNames.FUNDING_MODEL_CD));
        assertTrue("Field Name: ", propertyAudits.get(1).getPropertyName().equals(PropertyAudit.ColumnNames.FUNDING_MODEL_CD));
    }

    // test company EmployeeLimit field history
    @Test
    public void testEmployeeLimitHistory() {
        PayrollServices.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        // Update DD Limits
        SpcfMoney newCompanyLimit = new SpcfMoney("27000.27");
        SpcfMoney newEmployeeLimit = new SpcfMoney("7272.72");

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "123272727", newCompanyLimit, newEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        // Change Limits again
        newCompanyLimit = new SpcfMoney("28000.27");
        newEmployeeLimit = new SpcfMoney("9272.72");

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "123272727", newCompanyLimit, newEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<PropertyAudit> propertyAudits =
                PropertyAudit.findPropertyAudits(company,
                                                PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO,
                                                PropertyAudit.ColumnNames.OVERRIDE_EMP_LIMIT_AMT,
                                                null);

        LimitValue defaultDDEmployeeLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Audits:", propertyAudits.size() == 2);

        String[] oldValues = new String[2];
        String[] newValues = new String[2];
        int i = 0;
        for (PropertyAudit propertyAudit : propertyAudits) {
            if (propertyAudit.getOldPropertyValue() == null) {
                oldValues[i] = "";
            } else {
                oldValues[i] = propertyAudit.getOldPropertyValue();
            }
            if (propertyAudit.getNewPropertyValue() == null) {
                newValues[i] = "";
            } else {
                newValues[i] = propertyAudit.getNewPropertyValue();
            }
            i++;
        }
        Arrays.sort(oldValues);
        Arrays.sort(newValues);

        // verify property values
        assertEquals("Old Value 2:",
                Float.parseFloat(oldValues[0]), Float.parseFloat(defaultDDEmployeeLimit.getValue()));
        assertEquals("Old Value 3:",
                Float.parseFloat(oldValues[1]), Float.parseFloat(("7272.72")));

        assertEquals("New Value 2:",
                Float.parseFloat(newValues[0]),Float.parseFloat("7272.72"));
        assertEquals("New Value 3:",
                Float.parseFloat(newValues[1]) , Float.parseFloat("9272.72"));


        assertTrue("Class Name 1: ", propertyAudits.get(0).getClassName().equals(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO));
        assertTrue("Class Name 2: ", propertyAudits.get(1).getClassName().equals(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO));

        assertTrue("Field Name 1: ", propertyAudits.get(0).getPropertyName().equals(PropertyAudit.ColumnNames.OVERRIDE_EMP_LIMIT_AMT));
        assertTrue("Field Name 2: ", propertyAudits.get(1).getPropertyName().equals(PropertyAudit.ColumnNames.OVERRIDE_EMP_LIMIT_AMT));
    }

    // test company CompanyLimit field history
    @Test
    public void testCompanyLimitHistory() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        // Update DD Limits
        SpcfMoney newCompanyLimit = new SpcfMoney("27000.27");
        SpcfMoney newEmployeeLimit = new SpcfMoney("7272.72");

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "123272727", newCompanyLimit, newEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        // Change Limits again
        newCompanyLimit = new SpcfMoney("28000.27");
        newEmployeeLimit = new SpcfMoney("9272.72");

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBOE, "123272727", newCompanyLimit, newEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        DomainEntitySet<PropertyAudit> propertyAudits =
                    PropertyAudit.findPropertyAudits(company,
                                                PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO,
                                                PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT,
                                                null);

        LimitValue defaultDDCompanyLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultCompanyLimit);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Audits:", propertyAudits.size() == 2);

        String[] oldValues = new String[2];
        String[] newValues = new String[2];
        int i = 0;
        for (PropertyAudit propertyAudit : propertyAudits) {
            if (propertyAudit.getOldPropertyValue() == null) {
                oldValues[i] = "";
            } else {
                oldValues[i] = propertyAudit.getOldPropertyValue();
            }
            if (propertyAudit.getNewPropertyValue() == null) {
                newValues[i] = "";
            } else {
                newValues[i] = propertyAudit.getNewPropertyValue();
            }
            i++;
        }
        Arrays.sort(oldValues);
        Arrays.sort(newValues);

        // verify property values
        assertEquals("Old Value 1:",
                Float.parseFloat(oldValues[1]), Float.parseFloat(defaultDDCompanyLimit.getValue()));
        assertEquals("Old Value 2:",
                Float.parseFloat(oldValues[0]), Float.parseFloat("27000.27"));

        assertEquals("New Value 1:",
                Float.parseFloat(newValues[1]), Float.parseFloat("28000.27"));
        assertEquals("New Value 2:",
                Float.parseFloat(newValues[0]), Float.parseFloat("27000.27"));

        // verify property values
        assertTrue("Class Name: ", propertyAudits.get(0).getClassName().equals(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO));
        assertTrue("Class Name: ", propertyAudits.get(1).getClassName().equals(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO));

        assertTrue("Field Name: ", propertyAudits.get(0).getPropertyName().equals(PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT));
        assertTrue("Field Name: ", propertyAudits.get(1).getPropertyName().equals(PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT));
    }

    // test history by class name and field name
    @Test
    public void testHistoryByClassAndProperty() {
        Application.beginUnitOfWork();
        new PayrollSubmitDataLoader().loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        // Create another company and bank accounts
        DataLoader dataLoader = new DataLoader();

        PayrollServices.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany123123123();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        dataLoader.persistTestCompanyService(company);
        CompanyBankAccountDTO companyBankAccount = dataLoader.getTestCompany123123123BankAccount();
        dataLoader.persistCompanyBankAccount(company, companyBankAccount);
        PayrollServices.commitUnitOfWork();

        //Set company status to "Inactive"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        //Set company status to "Active"
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.reactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // test to get only the specified bank account
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PropertyAudit> propertyAudits =
                PropertyAudit.findPropertyAudits(company,
                        PropertyAudit.TableNames.COMPANY_SERVICE,
                        PropertyAudit.ColumnNames.DD_COMP_STATUS_CD,
                        SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        verifyPropertyAudits(propertyAudits);
    }

    private void verifyPropertyAudits(DomainEntitySet<PropertyAudit> pPropertyAudits) {
        assertEquals("Number of Audits:", 3, pPropertyAudits.size());

        // verify property values
        assertTrue("Old Value 1:",
                pPropertyAudits.get(2).getOldPropertyValue().equals(ServiceSubStatusCode.PendingBankVerification.toString()));
        assertTrue("New Value 1:",
                pPropertyAudits.get(2).getNewPropertyValue().equals(ServiceSubStatusCode.PendingFirstPayroll.toString()));

        assertTrue("Old Value 2:",
                pPropertyAudits.get(1).getOldPropertyValue().equals(ServiceSubStatusCode.PendingFirstPayroll.toString()));
        assertTrue("New Value 2:",
                pPropertyAudits.get(1).getNewPropertyValue().equals(ServiceSubStatusCode.Cancelled.toString()));

        assertEquals("Old Value 3:",ServiceSubStatusCode.Cancelled.toString(),pPropertyAudits.get(0).getOldPropertyValue());
        assertEquals("New Value 3:",ServiceSubStatusCode.PendingFirstPayroll.toString(),pPropertyAudits.get(0).getNewPropertyValue());

        assertTrue("Class Name: ", pPropertyAudits.get(0).getClassName().equals(PropertyAudit.TableNames.COMPANY_SERVICE));
        assertTrue("Class Name: ", pPropertyAudits.get(1).getClassName().equals(PropertyAudit.TableNames.COMPANY_SERVICE));
        assertTrue("Class Name: ", pPropertyAudits.get(2).getClassName().equals(PropertyAudit.TableNames.COMPANY_SERVICE));

        assertTrue("Field Name: ", pPropertyAudits.get(0).getPropertyName().equals(PropertyAudit.ColumnNames.DD_COMP_STATUS_CD));
        assertTrue("Field Name: ", pPropertyAudits.get(1).getPropertyName().equals(PropertyAudit.ColumnNames.DD_COMP_STATUS_CD));
        assertTrue("Field Name: ", pPropertyAudits.get(2).getPropertyName().equals(PropertyAudit.ColumnNames.DD_COMP_STATUS_CD));
    }
}

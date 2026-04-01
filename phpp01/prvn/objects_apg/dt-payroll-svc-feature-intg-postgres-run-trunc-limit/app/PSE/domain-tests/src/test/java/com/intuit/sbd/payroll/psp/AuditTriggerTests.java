package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuditTriggerTests {

    private static final String addressLine1 = "Updated Address Line1";
    private static final String notificationEmail = "updatedEmail@abc.com";
    private static final String firstName = "updatedName";
    private static final String lawId = "61";
    private static final String limit = "9001.00";
    private static final String amt = "9999.00";
    private static final String active = "Active";
    private static final String Inactive = "Inactive";
    private static final String offer_fk ="80995ef3-0001-0001-e040-11ac3bda020f";
    private static final String ein = "198765432";
    private static final String psid = "234567891";

    @Before
    public void runBeforeEachTest() {

        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testTrigger_Company(){

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        
        // Update company  Notification email
        company1.setNotificationEmail(notificationEmail);

        Application.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("Company").And(PropertyAudit.PropertyName().equalTo("NotificationEmail"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("Individual", notificationEmail, propertyAudits1.get(0).getNewPropertyValue());

    }
    @Test
    public void testTrigger_Individual(){

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();

        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        // Update the Emp/Individual First Name
        company1.getEmployees().get(0).setFirstName(firstName);
        Application.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("Individual").And(PropertyAudit.PropertyName().equalTo("FirstName"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("Individual", firstName, propertyAudits1.get(0).getNewPropertyValue());

    }
    @Test
    public void testTrigger_CompanyLaw(){

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        DataLoadServices.addCompanyLaws(company1, lawId);
        DataLoadServices.updateCompanyLawFilingFlag(company1, lawId);

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("CompanyLaw").And(PropertyAudit.PropertyName().equalTo("FilingStatus"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("CompanyLaw", Inactive, propertyAudits1.get(0).getNewPropertyValue());

    }
    @Test
    public void testTrigger_CompanyOffer(){

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company comp = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);
        DataLoadServices.claimOffer(comp, "1099426");

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);
        // Set company offer
        company1.getCompanyOffers().getFirst().setOffer(Offer.findOfferByOfferCode("Waive all major fees"));
        Application.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("CompanyOffer").And(PropertyAudit.PropertyName().equalTo("Offer"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("CompanyOffer", offer_fk, propertyAudits1.get(0).getNewPropertyValue());

    }

    @Test
    public void testTrigger_Address(){

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);

        Address address = company1.getMailingAddress();
        address.setAddressLine1(addressLine1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("Address").And(PropertyAudit.PropertyName().equalTo("AddressLine1"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("AddressLine",addressLine1, propertyAudits1.get(0).getNewPropertyValue());

    }

    @Test
    public void testTrigger_CompBPLimit(){

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);

        PayrollServices.commitUnitOfWork();

        // update BP limit
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateBPLimits(SourceSystemCode.QBDT, psid, new SpcfMoney(limit), null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo(PropertyAudit.TableNames.BP_COMPANY_SERVICE_INFO).And(PropertyAudit.PropertyName().equalTo(PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals(Float.parseFloat(limit), Float.parseFloat(propertyAudits1.get(0).getNewPropertyValue()), 0.0f);

    }

    @Test
    public void testTrigger_PayeeBPLimit(){

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);

        PayrollServices.commitUnitOfWork();

        // update BP limit
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateBPLimits(SourceSystemCode.QBDT, psid,  null, new SpcfMoney(amt));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo(PropertyAudit.TableNames.BP_COMPANY_SERVICE_INFO).And(PropertyAudit.PropertyName().equalTo(PropertyAudit.ColumnNames.OVERRIDE_PAYEE_LIMIT_AMT));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals(Float.parseFloat(amt), Float.parseFloat(propertyAudits1.get(0).getNewPropertyValue()), 0.0f);

    }

    @Test
    public void testTrigger_CompanyDDLimit(){

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);

        LimitValue defaultDDCompLimit = LimitRule.findLimitRule(company1, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultCompanyLimit);
        PayrollServices.commitUnitOfWork();

        // update BP limit
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT, psid, new SpcfMoney(limit), null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO).And(PropertyAudit.PropertyName().equalTo(PropertyAudit.ColumnNames.OVERRIDE_COMP_LIMIT_AMT));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals(Float.parseFloat(defaultDDCompLimit.getValue()), Float.parseFloat(propertyAudits1.get(0).getOldPropertyValue()), 0.0f);
        assertEquals(Float.parseFloat(limit), Float.parseFloat(propertyAudits1.get(0).getNewPropertyValue()), 0.0f);

    }

    @Test
    public void testTrigger_EmployeeDDLimit(){

        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.BillPayment, ServiceCode.DirectDeposit, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(company1, company1);

        LimitValue defaultEmpLimit = LimitRule.findLimitRule(company1, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
        PayrollServices.commitUnitOfWork();

        // update BP limit
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT, psid, null, new SpcfMoney(amt));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo(PropertyAudit.TableNames.DD_COMPANY_SERVICE_INFO).And(PropertyAudit.PropertyName().equalTo(PropertyAudit.ColumnNames.OVERRIDE_EMP_LIMIT_AMT));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals(Float.parseFloat(defaultEmpLimit.getValue()), Float.parseFloat(propertyAudits1.get(0).getOldPropertyValue()), 0.0f);
        assertEquals(Float.parseFloat(amt), Float.parseFloat(propertyAudits1.get(0).getNewPropertyValue()), 0.0f);

    }

    @Test
    public void testTrigger_CompanyBankAccount(){

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        Criterion<PropertyAudit> where1 =
                PropertyAudit.ClassName().equalTo("CompanyBankAccount").And(PropertyAudit.PropertyName().equalTo("StatusCd"));
        DomainEntitySet<PropertyAudit> propertyAudits1 = Application.find(PropertyAudit.class, new Query<PropertyAudit>().Where(where1));
        PayrollServices.commitUnitOfWork();

        assertEquals(1,propertyAudits1.size());

        assertEquals("CompanyBankAccount", active, propertyAudits1.get(0).getNewPropertyValue());

    }

}

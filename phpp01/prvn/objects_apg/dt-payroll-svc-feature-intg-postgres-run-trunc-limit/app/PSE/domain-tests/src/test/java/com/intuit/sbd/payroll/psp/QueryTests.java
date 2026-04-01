package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.App;

import java.util.Collections;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

/**
 * User: dweinberg
 * Date: 1/8/13
 * Time: 3:03 PM
 * Tests for various features in the Criterion API
 */
public class QueryTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void test1001InClause() {
        Application.beginUnitOfWork();
        Application.find(Company.class, Company.SourceCompanyId().in(Collections.nCopies(1001, "Is it here yet?")));
        Application.rollbackUnitOfWork();
    }

    //Test sub-queries and joins
    @Test
    //X.Y, exists y in Y s.t. y.a = :a
    public void testSubqueryAndJoinScalar() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Tax, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateServices(company, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

    }

    @Test
    public void testSubqueryOnLoadedCollection() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Tax, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet()));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        companies = companies.find(Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        Assert.assertEquals(0, companies.size());

        DataLoadServices.activateServices(company, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet()));
        Assert.assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        companies = companies.find(Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        Assert.assertEquals(1, companies.size());
    }

    @Test
    //X.Y, exists y in Y s.t. y.Z.a = :a
    public void testSubqueryAndJoinOneToOneScalar() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Filter().Service().ServiceCd().equalTo(ServiceCode.Tax));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        DataLoadServices.addTaxService(company);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyServiceSet().Filter().Service().ServiceCd().equalTo(ServiceCode.Tax));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

    }

    @Test
    //X.Y.Z, exists y in Y s.t. exists z in Z s.t. z.W.a :a
    public void testDeepSubqueryAndJoin() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.setPSPDate(2013, 1, 1);

        Company company = DataLoadPalette.setupTaxCompany();

        Application.beginUnitOfWork();
        String blah = EncryptionUtils.deterministicEncrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName, "blah");
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.CompanyAgencySet().Exists(CompanyAgency.CompanyAgencyPaymentTemplateSet().Exists(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().equalTo(blah))));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyAgencySet().Filter().CompanyAgencyPaymentTemplateSet().Filter().AgencyTaxpayerIdEnc().equalTo(blah));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        DataLoadServices.updateAgencyTaxpayerId(company, "CA-PITSDI-PAYMENT", "blah");

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyAgencySet().Exists(CompanyAgency.CompanyAgencyPaymentTemplateSet().Exists(CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().equalTo(blah))));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyAgencySet().Filter().CompanyAgencyPaymentTemplateSet().Filter().AgencyTaxpayerIdEnc().equalTo(blah));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

    }

    @Test
    //X.Y, exists y in Y s.t. y.X.Z.a = :a; X.Z.a = a
    //weird scenario, but mainly want to verify no conflicts in the generated aliases
    public void testSubqueryAndJoinOneToOneOnBoth() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.QuickbooksInfo().CoaFeeAccountName().equalTo("Coa").And(Company.CompanyBankAccountSet().Exists(CompanyBankAccount.Company().QuickbooksInfo().CoaFeeAccountName().equalTo("CoA"))));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.QuickbooksInfo().CoaFeeAccountName().equalTo("CoA").And(Company.CompanyBankAccountSet().Filter().Company().QuickbooksInfo().CoaFeeAccountName().equalTo("CoA")));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        company.getQuickbooksInfo().setCoaFeeAccountName("CoA");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.QuickbooksInfo().CoaFeeAccountName().equalTo("CoA").And(Company.CompanyBankAccountSet().Exists(CompanyBankAccount.Company().QuickbooksInfo().CoaFeeAccountName().equalTo("CoA"))));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.QuickbooksInfo().CoaFeeAccountName().equalTo("CoA").And(Company.CompanyBankAccountSet().Filter().Company().QuickbooksInfo().CoaFeeAccountName().equalTo("CoA")));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();
    }

    @Test
    //X.Y, X.Z exists y in Y s.t. y.a = :a and exists z in Z s.t. z.b = :b
    public void testSubqueryAndJoinMultiple() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.CompanyBankAccountSet().Exists(CompanyBankAccount.BankAccount().AccountTypeCd().equalTo(BankAccountType.Savings))
                                                                                    .And(Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();


        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyBankAccountSet().Filter().BankAccount().AccountTypeCd().equalTo(BankAccountType.Savings)
                                                           .And(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateServices(company, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyBankAccountSet().Exists(CompanyBankAccount.BankAccount().AccountTypeCd().equalTo(BankAccountType.Savings))
                                                                                    .And(Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))));
        assertEquals(0, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        assertOne(Application.refresh(company).getCompanyBankAccountCollection()).getBankAccount().setAccountTypeCd(BankAccountType.Savings);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyBankAccountSet().Exists(CompanyBankAccount.BankAccount().AccountTypeCd().equalTo(BankAccountType.Savings))
                                                           .And(Company.CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, Company.CompanyBankAccountSet().Filter().BankAccount().AccountTypeCd().equalTo(BankAccountType.Savings)
                                                           .And(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFilteredEagerLoadSimple() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companies.size());
        assertFalse(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size()); //outer join!
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(0, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateCloudService(company);

        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size()); //outer join!
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(1, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNoUpdateIfNothingChangesOnFilteredCollection() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        DataLoadServices.activateCloudService(company);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        Company loadedCompany = assertOne(companies);
        assertEquals(-1, loadedCompany.getVersion());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        loadedCompany = Application.findById(Company.class, loadedCompany.getId());
        assertEquals(-1, loadedCompany.getVersion());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFilteredEagerLoadSimpleAnd() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent).And(Company.CompanyServiceSet().Filter().ServiceStartDate().isNull())));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(0, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateCloudService(company);

        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent).And(Company.CompanyServiceSet().Filter().ServiceStartDate().isNull())));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(1, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();
    }


    @Test
    //Not supported.  A query can't be created that would achieve the desired effect.
    //To get the result implied by this query, one could load the Service object outside and then use simple equality.
    public void testFilteredEagerLoadDeep() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        DataLoadServices.activateCloudService(company);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet().Filter().Service().ServiceCd().equalTo(ServiceCode.DirectDeposit)).EagerLoad(Company.CompanyServiceSet()));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().getFirst().getService()));
        Assert.assertEquals(2, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();

    }


    @Test
    public void testFilteredEagerLoadMultiple() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companies.size());
        assertFalse(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        assertFalse(Hibernate.isInitialized(companies.getFirst().getContactCollection().toNative()));
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                                                        .EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))
                                                                        .EagerLoad(Company.ContactSet().Filter().JobTitle().equalTo("Bridge Director")));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        assertTrue(Hibernate.isInitialized(companies.getFirst().getContactCollection().toNative()));
        Assert.assertEquals(0, companies.getFirst().getCompanyServiceCollection().size());
        Assert.assertEquals(0, companies.getFirst().getContactCollection().size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateCloudService(company);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.getContactByRoleCode(ContactRole.PayrollAdmin).setJobTitle("Bridge Director");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                                                        .EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent))
                                                                        .EagerLoad(Company.ContactSet().Filter().JobTitle().equalTo("Bridge Director")));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        assertTrue(Hibernate.isInitialized(companies.getFirst().getContactCollection().toNative()));
        Assert.assertEquals(1, companies.getFirst().getCompanyServiceCollection().size());
        Assert.assertEquals(1, companies.getFirst().getContactCollection().size());
        Application.rollbackUnitOfWork();
    }

    @Test
    /*
        this is valid, but the behavior is not as might be expected.
        if the collection is referenced both in the where filter and also in the eager load filter, it will not be joined a second time.
        As such, the outer join will really function as an inner join.
        To achieve the "normal" behavior, use an exist in conjunction with the eager filter
    */
    public void testFilteredEagerLoadAlreadyJoined() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT).And(Company.CompanyServiceSet().Filter().StatusEffectiveDate().isNotNull())).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(0, companies.size()); //outer join turned into inner join because of where clause
        Application.rollbackUnitOfWork();

        DataLoadServices.activateCloudService(company);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT).And(Company.CompanyServiceSet().Filter().StatusEffectiveDate().isNotNull())).EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(1, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testFilteredEagerLoadPlusSubquery() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT).And(Company.CompanyServiceSet().NotExists(CompanyService.StatusEffectiveDate().isNull())))
                                                                                                 .EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(0, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();

        DataLoadServices.activateCloudService(company);

        Application.beginUnitOfWork();
        companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT).And(Company.CompanyServiceSet().NotExists(CompanyService.StatusEffectiveDate().isNull())))
                                                                        .EagerLoad(Company.CompanyServiceSet().Filter().StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));
        assertEquals(1, companies.size());
        assertTrue(Hibernate.isInitialized(companies.getFirst().getCompanyServiceCollection().toNative()));
        Assert.assertEquals(1, companies.getFirst().getCompanyServiceCollection().size());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testPropertyComparison() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        Application.refresh(company);
        assertEquals("Created and Modified date should be same for the company",
                company.getCreatedDate().getTimeInMilliseconds(),
                company.getModifiedDate().getTimeInMilliseconds());
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setModifierId("MisterRogers");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        assertNotEquals("Created and Modified date should be same for the company",
                company.getCreatedDate().getTimeInMilliseconds(),
                company.getModifiedDate().getTimeInMilliseconds());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testReadOnlyQuery() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setLegalName("Stella's Second Place Pie Contest Ribbon");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).ReadOnly(true));
        company = assertOne(companies);
        company.setLegalName("Stella's Mistrust of Local News");
        Application.commitUnitOfWork(); //company entity is read-only so nothing will be flushed!

        Application.beginUnitOfWork();
        Application.refresh(company);
        Assert.assertEquals("Stella's Second Place Pie Contest Ribbon", company.getLegalName());
    }

    @Test
    public void testReadOnlySession() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setLegalName("Stella's Second Place Pie Contest Ribbon");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork(FlushMode.MANUAL, true);
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        company = assertOne(companies);
        company.setLegalName("Stella's Mistrust of Local News");
        Application.commitUnitOfWork(); //company entity is read-only so nothing will be flushed!

        Application.beginUnitOfWork();
        Application.refresh(company);
        Assert.assertEquals("Stella's Second Place Pie Contest Ribbon", company.getLegalName());
    }

    @Test
    public void testReadWriteQueryInReadOnlySession() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Cloud);
        Application.beginUnitOfWork();
        Application.refresh(company);
        company.setLegalName("Stella's Second Place Pie Contest Ribbon");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork(FlushMode.MANUAL, true);
        DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).ReadOnly(false));
        company = assertOne(companies);
        company.setLegalName("Stella's Mistrust of Local News");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(company);
        Assert.assertEquals("Stella's Mistrust of Local News", company.getLegalName());
    }

    @Test
    public void testErrorEagerLoadingWithLimitResults() {

        Application.beginUnitOfWork();

        //property
        try {
            Application.find(Company.class, new Query<Company>().EagerLoad(Company.CompanyBankAccountSet()).LimitResults(0, 10));
            fail("Expected exception");
        } catch (RuntimeException e){
            assertEquals("Limit Results cannot be used when eagerly loading a collection", e.getMessage());
        }

        //filter
        try {
            Application.find(Company.class, new Query<Company>().EagerLoad(Company.CompanyBankAccountSet().Filter().StatusCd().equalTo(BankAccountStatus.Active)).LimitResults(0, 10));
            fail("Expected exception");
        } catch (RuntimeException e){
            assertEquals("Limit Results cannot be used when eagerly loading a collection", e.getMessage());
        }

        Application.rollbackUnitOfWork();
    }

}

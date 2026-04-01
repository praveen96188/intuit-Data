package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: rnorian
 * Date: Feb 10, 2011
 * Time: 4:53:33 PM
 */
public class AddOrUpdateEntityChangeTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath_AddEntityChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        // change occurs in AS400 and flows to PSP via data sync
        PayrollServices.beginUnitOfWork();
        String oldEIN = company.getFedTaxId();
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("991234567");
        ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // rep updates ACI info on AS400 - change flows to PSP via AS400Gateway PSPEVENT processing
        EntityChangeDTO changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN(oldEIN);
        changeDTO.setUserId("AS400USER");
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        assertEqual(changeDTO, Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId()));
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("change count", 1, company.getEntityChangeCollection().size());
        // EntityCHange with null isError should include the entitychange in the PayrollFormBuilder
        junit.framework.Assert.assertNotNull(EntityChange.findMostRecentEntityChangeForCompanyWithoutError(company));
        PayrollServices.commitUnitOfWork();

        // add a 2nd change for good measure
        // change occurs in AS400 and flows to PSP via data sync
        PayrollServices.beginUnitOfWork();
        oldEIN = company.getFedTaxId();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("881234567");
        processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // rep updates ACI info on AS400 - change flows to PSP via AS400Gateway PSPEVENT processing
        changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("881234567");
        changeDTO.setOldEIN(oldEIN);
        changeDTO.setUserId("AS400USER");
        changeDTO.setIsError(false);
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        assertEqual(changeDTO, Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId()));
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        // EntityChange with false isError should include the entitychange in the PayrollFormBuilder
        assertEquals("change count", 2, company.getEntityChangeCollection().size());
        junit.framework.Assert.assertNotNull(EntityChange.findMostRecentEntityChangeForCompanyWithoutError(company));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHappyPath_UpdateEntityChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        // change occurs in AS400 and flows to PSP via data sync
        PayrollServices.beginUnitOfWork();
        String oldEIN = company.getFedTaxId();
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("991234567");
        ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // rep updates ACI info on AS400 - change flows to PSP via AS400Gateway PSPEVENT processing
        EntityChangeDTO changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN(oldEIN);
        changeDTO.setUserId("AS400USER");
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEqual(changeDTO, Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId()));
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("change count", 1, company.getEntityChangeCollection().size());
        PayrollServices.commitUnitOfWork();

        // rep made a mistake on old EIN and corrects mistake, a 2nd txaci record w/same ACI_EIN value inserted
        changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN("441234567");
        changeDTO.setUserId("AS400USER2");
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        EntityChange actual = Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId());
        assertEqual(changeDTO, actual);
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("change count", 1, company.getEntityChangeCollection().size());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        PayrollServices.commitUnitOfWork();
        assertEquals("Entitlement Unit Status",EntitlementUnitStatusCode.PendingActivation,entitlementUnit.getEntitlementUnitStatus());
    }

    @Test
    public void testHappyPath_DeleteEntityChange() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        // change occurs in AS400 and flows to PSP via data sync
        PayrollServices.beginUnitOfWork();
        String oldEIN = company.getFedTaxId();
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("991234567");
        ProcessResult processResult = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // rep updates ACI info on AS400 - change flows to PSP via AS400Gateway PSPEVENT processing
        EntityChangeDTO changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN(oldEIN);
        changeDTO.setUserId("AS400USER");
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEqual(changeDTO, Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId()));
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("change count", 1, company.getEntityChangeCollection().size());
        PayrollServices.commitUnitOfWork();

        // rep made a mistake - no entity change occurred.  CLI_ORIGINAL_FEIN is blanked out on AS400
        changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN("0");
        changeDTO.setUserId("AS400USER2");
        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("change count", 0, company.getEntityChangeCollection().size());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testHappyPath_AddEntityChangeWithError() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        EntityChangeDTO changeDTO = new EntityChangeDTO();
        changeDTO = new EntityChangeDTO();
        changeDTO.setNewEIN("991234567");
        changeDTO.setOldEIN("991234567");
        changeDTO.setUserId("PSP-2317");
        changeDTO.setIsError(true);

        changeDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateEntityChange(company.getSourceSystemCd(), company.getSourceCompanyId(), changeDTO);
        assertSuccess(processResult);
        assertEqual(changeDTO, Application.<EntityChange>findById(EntityChange.class, ((EntityChange)processResult.getResult()).getId()));
        // EntityChange with true isError should not include the entitychange in the PayrollFormBuilder
        junit.framework.Assert.assertNull(EntityChange.findMostRecentEntityChangeForCompanyWithoutError(company));
        PayrollServices.commitUnitOfWork();
    }

    private void assertEqual(EntityChangeDTO pExpected, EntityChange pActual) {
        assertEquals("new EIN", pExpected.getNewEIN(), pActual.getNewEIN());
        assertEquals("old EIN", pExpected.getOldEIN(), pActual.getOldEIN());
        assertEquals("effective date", pExpected.getEffectiveDate(), new DateDTO(pActual.getEffectiveDate()));
        assertEquals("agentid", pExpected.getUserId(), pActual.getAgentId());
        Assert.assertEquals("isError", pExpected.getIsError(), pActual.getIsError());
    }
}

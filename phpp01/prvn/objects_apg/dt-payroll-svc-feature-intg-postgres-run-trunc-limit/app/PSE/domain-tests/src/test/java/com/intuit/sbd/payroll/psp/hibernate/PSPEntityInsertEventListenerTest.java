package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.BillPayment;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class PSPEntityInsertEventListenerTest {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void verifyEntityUpdateEmployeeBankAccount(){
        // Create 1 EmployeeBankAccount
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 1, "Active", null);
        Employee employee = company.getDirectDepositEmployees().get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Check if Event is EntityCreate event in the PSP_ENTITY_UPDATE on create of EmployeeBankAccount
        DomainEntitySet<EntityUpdate> entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(employee.getEmployeeBankAccountCollection().get(0).getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("EmployeeBankAccount"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityCreate))));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();


        // Update the EmployeeBankAccount
        PayrollServices.beginUnitOfWork();
        EmployeeBankAccount employeeBankAccount = employee.getEmployeeBankAccountCollection().get(0);
        EmployeeBankAccountDTO employeeBankAccountDTO = GenerateData.getEmployeeBankAccountDTO(employeeBankAccount.getSourceBankAccountId());
        employeeBankAccountDTO.getBankAccount().setBankName("ICICI");
        employeeBankAccountDTO.getBankAccount().setRoutingNumber("124000012");
        ProcessResult<EmployeeBankAccount> processResult1 = PayrollServices.employeeManager.updateEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
        assertSuccess("updateEmployeeBankAccount", processResult1);
        PayrollServices.commitUnitOfWork();

        // Check if Event is EntityUpdate event in the PSP_ENTITY_UPDATE on update of EmployeeBankAccount
        PayrollServices.beginUnitOfWork();
        entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(employee.getEmployeeBankAccountCollection().get(0).getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("EmployeeBankAccount"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityUpdate)))
                .OrderBy(EntityUpdate.CreatedDate().Descending()));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void verifyEntityUpdatePaycheck() {

        // submit paychecks
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        System.out.println("Payroll Submit Starts Here");

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);


        // validate
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = processResult.getResult().getPaycheckCollection();

        // All the paychecks should have EventType=EntityCreate and Status=Created
        for(Paycheck paycheck : paychecks){
            DomainEntitySet<EntityUpdate> entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                    .Where(EntityUpdate.EntityId().equalTo(paycheck.getId().toString())
                            .And(EntityUpdate.EntityName().equalTo("Paycheck"))
                            .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityCreate))
                            .And(EntityUpdate.Status().equalTo(Status.Created))));
            assertEquals(1, entityUpdates.size());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void verifyEntityUpdateCompanyBankAccount(){

        // Create Company BankAccount Data
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(c1dl.getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c1dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();
        PayrollServices.commitUnitOfWork();

        // TS01
        // Check if Event is EntityCreate event in the PSP_ENTITY_UPDATE on create of CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntityUpdate> entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(companyBankAccount.getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("CompanyBankAccount"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityCreate))));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();


        // TS02
        // Update Company BankAccount
        PayrollServices.beginUnitOfWork();
        companyBankAccount = PayrollServices.entityFinder.findById(CompanyBankAccount.class, companyBankAccount.getId());
        companyBankAccount.setSourceBankAccountName("XYZ");
        Application.save(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        // Check if Event is EntityUpdate event in the PSP_ENTITY_UPDATE
        PayrollServices.beginUnitOfWork();
        entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(companyBankAccount.getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("CompanyBankAccount"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityUpdate))));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void verifyEntityUpdatePayeeBankAccount(){

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        Company company = dataloader.persistCompany(c1dl.getCompany1());

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c1dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> addCBAProcResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), dataloader.getTestCompanyBankAccount(), true, true);
        assertSuccess("addCompanyBankAccount", addCBAProcResult);
        CompanyBankAccount companyBankAccount = addCBAProcResult.getResult();

        PayeeDTO payeeDTO = getTestPayee();
        ProcessResult<Payee> pr = PayrollServices.billPaymentManager.addOrUpdatePayee(SourceSystemCode.QBOE, company.getSourceCompanyId(), payeeDTO);
        assertTrue("Add Payee:", pr.isSuccess());
        Payee payee = pr.getResult();
        PayeeBankAccountDTO payeeBankAccountDTO = GenerateData.getPayeeBankAccountDTO("NewPBATest");
        ProcessResult<PayeeBankAccount> processResult = PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), payee.getSourcePayeeId(), payeeBankAccountDTO);
        assertSuccess("addOrUpdatePayeeBankAccount", processResult);
        PayeeBankAccount payeeBankAccount = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        // TS01
        // Check if Event is EntityCreate event in the PSP_ENTITY_UPDATE on create of PayeeBankAccount
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntityUpdate> entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(payeeBankAccount.getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("PayeeBankAccount"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityCreate))));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void verifyEntityUpdateBillPayment(){

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess("submitBillPayment", submitResult);
        PayrollServices.commitUnitOfWork();

        BillPayment billPayment = null;
        for (PayrollRun pr: submitResult.getResult()
             ) {
            billPayment = pr.getBillPaymentCollection().get(0);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EntityUpdate> entityUpdates =  PayrollServices.entityFinder.find(EntityUpdate.class, new Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(billPayment.getId().toString())
                        .And(EntityUpdate.EntityName().equalTo("BillPayment"))
                        .And(EntityUpdate.EventType().equalTo(EventEnumType.EntityCreate))));
        assertEquals(1, entityUpdates.size());
        PayrollServices.rollbackUnitOfWork();



    }

    private PayeeDTO getTestPayee() {
        PayeeDTO payee = new PayeeDTO();
        payee.setSourcePayeeId("TESTADDPAYEE");
        payee.setName("Add Payee Core Test");
        payee.setPhone("775-227-7227");
        payee.setTaxId("123456789");

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("123 High Country Rd");
        mailingAddress.setCity("Reno");
        mailingAddress.setState("NV");
        mailingAddress.setZipCode("89502");
        mailingAddress.setCountry("USA");
        payee.setMailingAddress(mailingAddress);

        return payee;
    }
}

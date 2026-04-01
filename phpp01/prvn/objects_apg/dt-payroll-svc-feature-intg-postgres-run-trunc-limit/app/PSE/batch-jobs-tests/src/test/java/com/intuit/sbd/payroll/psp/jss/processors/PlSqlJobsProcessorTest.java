package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.EntitlementStateCode;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SyncStatus;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.domain.WagePlanDomainCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.UUID;

public class PlSqlJobsProcessorTest {


    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void executePlsqlJobs_PayrollFraudBatchPurgePlSqlJobsProcessor(){

        Application.beginUnitOfWork();
        BatchJobAuditLog batchJobAuditLog = new BatchJobAuditLog();
        batchJobAuditLog.setCreatedDate(SpcfCalendar.createInstance(2018, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        batchJobAuditLog.setJobAction("SendEmailTest");
        batchJobAuditLog.setJobNamespace("/PSP/UnitTest/PayrollFraudBatchPurgePlSqlJobsProcessor/test");
        batchJobAuditLog.setMessage("Started");
        Application.save(batchJobAuditLog);
        PayrollFraudBatch payrollFraudBatch = new PayrollFraudBatch();
        payrollFraudBatch.setCreatedDate(SpcfCalendar.createInstance(2018, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        payrollFraudBatch.setNumberOfPayrollsProcessed(1);
        Application.save(payrollFraudBatch);
        Application.commitUnitOfWork();
        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.PayrollFraudBatchPurgePlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<BatchJobAuditLog> batchJobLogExpr =
                new Query<BatchJobAuditLog>().Where(BatchJobAuditLog.JobNamespace().like("/PSP/UnitTest/PayrollFraudBatchPurgePlSqlJobsProcessor/test"));
        DomainEntitySet<BatchJobAuditLog> dbJobLogEntries = PayrollServices.entityFinder.find(BatchJobAuditLog.class, batchJobLogExpr);
        assertEquals(PayrollServices.entityFinder.find(PayrollFraudBatch.class).size(), 0);
        assertEquals(dbJobLogEntries.size(), 0);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void executePlsqlJobs_NCDPlSqlJobsProcessorAndRetryEntitlementActivationProcessor() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        Entitlement entitlement = new Entitlement();
        entitlement.setNextChargeDate(SpcfCalendar.createInstance(2022, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        entitlement.setEntitlementState(EntitlementStateCode.Enabled);
        entitlement.setEntitlementOfferingCode("037893");
        entitlement.setLicenseNumber("1234");
        Expression<EntitlementCode> expression =
                new Query<EntitlementCode>().Where(EntitlementCode.AssetItemNumber().equalTo("1101349"));
        entitlement.setEntitlementCode((EntitlementCode) PayrollServices.entityFinder.find(EntitlementCode.class, expression).get(0));
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        currentDate.addDays(70);
        entitlement.setSubscriptionStartDate(currentDate);
        Application.save(entitlement);
        EntitlementUnit entitlementUnit = new EntitlementUnit();
        entitlementUnit.setEntitlement(entitlement);
        entitlementUnit.setCompany(company);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ErrorActivating);
        Application.save(entitlementUnit);
        Application.commitUnitOfWork();


        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.NCDFixALLPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        assertEquals(Entitlement.findEntitlement("1234", null).getModifierId(), "EMSOPS-9096");
        Application.rollbackUnitOfWork();

        String[] params1 = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.NCDFixPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor1 = new PlSqlJobsProcessor(params1);
        plSqlJobsProcessor1.execute();
        Application.beginUnitOfWork();
        assertEquals(Entitlement.findEntitlement("1234", null).getModifierId(), "PSP-2531");
        Application.rollbackUnitOfWork();

        String[] params2 = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.RetryEntitlementActivationPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor2 = new PlSqlJobsProcessor(params2);
        plSqlJobsProcessor2.execute();
        Application.beginUnitOfWork();
        Expression<EntitlementUnit> entitlementUnitExpression =
                new Query<EntitlementUnit>().Where(EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.PendingActivation)
                .And(EntitlementUnit.ModifierId().equalTo("JIRA-14162")));
        assertEquals(PayrollServices.entityFinder.find(EntitlementUnit.class, entitlementUnitExpression).size(),1);
        Application.rollbackUnitOfWork();

    }
    @Test
    public void executePlsqlJobs_CostCoPlSqlJobsProcessorAndFailedPayrollPlSqlJobsProcessor(){
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        company = Application.refresh(company);
        company.setSignUpDate(SpcfCalendar.createInstance(2022, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        Entitlement entitlement = new Entitlement();
        entitlement.setNextChargeDate(PSPDate.getPSPTime());
        entitlement.setEntitlementState(EntitlementStateCode.Enabled);
        entitlement.setEntitlementOfferingCode("615772");
        entitlement.setLicenseNumber("1234");
        entitlement.setEntitlementCode((EntitlementCode) PayrollServices.entityFinder.find(EntitlementCode.class).get(0));
        Application.save(entitlement);
        EntitlementUnit entitlementUnit =  new EntitlementUnit();
        entitlementUnit.setCompany(company);
        entitlementUnit.setEntitlement(entitlement);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementUnit);
        PayrollRun  payrollRun = new PayrollRun();
        payrollRun.setCompany(company);
        payrollRun.setPayrollRunDate(SpcfCalendar.createInstance(2021, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        Application.save(payrollRun);
        CompanyOffering companyOffering = new CompanyOffering();
        companyOffering.setCompany(company);
        companyOffering.setOffering((Offering) PayrollServices.entityFinder.find(Offering.class,
                new Query<Offering>().Where(Offering.OfferingCode().equalTo(OfferingCode.COSTCO672).And(Offering.ServiceCode().equalTo(ServiceCode.DirectDeposit)))).get(0));
        Application.save(companyOffering);
        FailedPayrollRun failedPayrollRun = new FailedPayrollRun();
        failedPayrollRun.setStatusToken(SyncStatus.Error);
        failedPayrollRun.setPayrollRun(payrollRun);
        Application.save(failedPayrollRun);
        Application.commitUnitOfWork();

        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.CostCoPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<CompanyEvent> event =
                new Query<CompanyEvent>().Where((CompanyEvent.ModifierId().equalTo("PSP-5052")).And(CompanyEvent.CreatorId().equalTo("PSP-5052")));
        assertEquals(PayrollServices.entityFinder.find(CompanyEvent.class, event).size(),1);
        Expression<CompanyEventDetail> eventDetail =
                new Query<CompanyEventDetail>().Where((CompanyEventDetail.ModifierId().equalTo("PSP-5052")).And(CompanyEventDetail.CreatorId().equalTo("PSP-5052")));
        assertEquals(PayrollServices.entityFinder.find(CompanyEventDetail.class, eventDetail).size(),2);
        Application.rollbackUnitOfWork();

        String[] params1 = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.FailedPayrollPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor1 = new PlSqlJobsProcessor(params1);
        plSqlJobsProcessor1.execute();
        Application.beginUnitOfWork();
        Expression<FailedPayrollRun> expression =
                new Query<FailedPayrollRun>().Where(FailedPayrollRun.StatusToken().equalTo(com.intuit.sbd.payroll.psp.domain.SyncStatus.Pending));
        assertEquals(PayrollServices.entityFinder.find(FailedPayrollRun.class, expression).size(),1);
        Application.rollbackUnitOfWork();

    }
    @Test
    public void executePlsqlJobs_EFTPSOnHoldPaymentPlSqlJobsProcessor() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction =  new MoneyMovementTransaction();
        moneyMovementTransaction.setCompany(company);
        moneyMovementTransaction.setStatus(PaymentStatus.OnHold);
        moneyMovementTransaction.setMoneyMovementPaymentMethod(PaymentMethod.EFTPS);
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
        Application.save(moneyMovementTransaction);
        Application.commitUnitOfWork();


        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.EFTPSOnHoldPaymentPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<MoneyMovementTransaction> expression =
                new Query<MoneyMovementTransaction>().Where(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created).And(MoneyMovementTransaction.ModifierId().equalTo("mmtcheck")));
        assertEquals(PayrollServices.entityFinder.find(MoneyMovementTransaction.class, expression).size(),1);
        Application.rollbackUnitOfWork();

    }

    @Test
    public void executePlsqlJobs_EDRAssociationFixPlSqlJobsProcessor() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        OffloadBatch  offloadBatch = new OffloadBatch();
        OffloadGroup offloadGroup = new OffloadGroup();
        offloadGroup.setOffloadGroupCd("ABC");
        offloadGroup.setCutoffTime("15:00:00");
        offloadGroup.setName("Test OffloadGroup ");
        offloadGroup.setDescription("Test 15:00 Offload Group");
        Application.save(offloadGroup);
        offloadBatch.setOffloadGroup(offloadGroup);
        offloadBatch.setOffloadDate(SpcfCalendar.createInstance(2018, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        Application.save(offloadBatch);
        MoneyMovementTransaction moneyMovementTransaction =  new MoneyMovementTransaction();
        moneyMovementTransaction.setCompany(company);
        moneyMovementTransaction.setStatus(PaymentStatus.OnHold);
        moneyMovementTransaction.setMoneyMovementPaymentMethod(PaymentMethod.ACHCredit);
        SpcfCalendar initiationDate = PSPDate.getPSPTime();
        initiationDate.setValues(initiationDate.getYear(), initiationDate.getMonth(), initiationDate.getDay());
        moneyMovementTransaction.setInitiationDate(initiationDate);
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.Ignore);
        moneyMovementTransaction.setMoneyMovementTransactionAmount(SpcfMoney.ZERO);
        moneyMovementTransaction.setOffloadBatch(offloadBatch);
        Application.save(moneyMovementTransaction);
        NACHAFile nachaFile = new NACHAFile();
        nachaFile.setOffloadBatch(offloadBatch);
        Application.save(nachaFile);
        EntryDetailRecord entryDetailRecord =  new EntryDetailRecord();
        entryDetailRecord.setCompany(company);
        entryDetailRecord.setMoneyMovementTransaction(moneyMovementTransaction);
        entryDetailRecord.setInitiationDate(moneyMovementTransaction.getInitiationDate());
        entryDetailRecord.setNACHAFile(nachaFile);
        Application.save(entryDetailRecord);
        Application.commitUnitOfWork();


        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.EDRAssociationFixPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<EntryDetailRecord> expression =
                new Query<EntryDetailRecord>().Where(EntryDetailRecord.ModifierId().equalTo("edrassocfix"));
        assertEquals(PayrollServices.entityFinder.find(EntryDetailRecord.class, expression).size(),1);
        Application.rollbackUnitOfWork();

    }

    @Test
    public void executePlsqlJobs_ValidateEmployeeWagePlansPlSqlJobsProcessor() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        Employee employeee = new Employee();
        employeee.setCompany(company);
        Application.save(employeee);
        EmployeeWagePlan employeeWagePlan = new EmployeeWagePlan();
        employeeWagePlan.setInvalidDate(PSPDate.getPSPTime());
        employeeWagePlan.setState("test");
        employeeWagePlan.setWagePlanDomain(WagePlanDomainCode.WorkState);
        employeeWagePlan.setEmployee(employeee);
        Application.save(employeeWagePlan);
        Application.commitUnitOfWork();

        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.ValidateEmployeeWagePlansPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<EmployeeWagePlan> expression =
                new Query<EmployeeWagePlan>().Where(EmployeeWagePlan.ModifierId().equalTo("PSP-2080"));
        assertEquals(PayrollServices.entityFinder.find(EmployeeWagePlan.class, expression).size(),1);
        Application.rollbackUnitOfWork();

    }

    @Test
    public void executePlsqlJobs_OfferingUpdateUsageBillingPlSqlJobsProcessor(){

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "999091339", true, ServiceCode.DirectDeposit);
        Application.beginUnitOfWork();
        Entitlement entitlement = new Entitlement();
        entitlement.setNextChargeDate(SpcfCalendar.createInstance(2022, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        entitlement.setEntitlementState(EntitlementStateCode.Enabled);
        entitlement.setEntitlementOfferingCode("037893");
        entitlement.setLicenseNumber("1234");
        entitlement.setEntitlementCode((EntitlementCode) PayrollServices.entityFinder.find(EntitlementCode.class,
                new Query<EntitlementCode>().Where(EntitlementCode.Id().equalTo(SpcfUniqueId.createInstance("60000000-0000-0000-0000-000000000005")))).get(0));
        Application.save(entitlement);
        EntitlementUnit entitlementUnit = new EntitlementUnit();
        entitlementUnit.setEntitlement(entitlement);
        entitlementUnit.setCompany(company);
        Application.save(entitlementUnit);
        CompanyService companyService = new CompanyService();
        companyService.setCompany(company);
        companyService.setService(Application.findById(Service.class, ServiceCode.Tax));
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        CompanyOffering companyOffering = new CompanyOffering();
        companyOffering.setCompany(company);
        companyOffering.setOffering((Offering) PayrollServices.entityFinder.find(Offering.class, new Query<Offering>().Where(Offering.Name().equalTo("USAGE-BILLING"))).get(0));
        Application.save(companyOffering);
        Application.commitUnitOfWork();


        String[] params = {BatchJobProcessor.RunMode.NotUsingFlux.toString(), BatchJobType.OfferingUpdateUsageBillingPlSqlJobsProcessor.toString(), UUID.randomUUID().toString()};
        PlSqlJobsProcessor plSqlJobsProcessor = new PlSqlJobsProcessor(params);
        plSqlJobsProcessor.execute();
        Application.beginUnitOfWork();
        Expression<CompanyOffering> expression1 =
                new Query<CompanyOffering>().Where(CompanyOffering.Offering().Id().equalTo(SpcfUniqueId.createInstance("e46965dc-410e-4628-8d2a-9159bff92e65")));
        assertEquals(PayrollServices.entityFinder.find(CompanyOffering.class, expression1).size(),1);
        Application.rollbackUnitOfWork();
    }

}

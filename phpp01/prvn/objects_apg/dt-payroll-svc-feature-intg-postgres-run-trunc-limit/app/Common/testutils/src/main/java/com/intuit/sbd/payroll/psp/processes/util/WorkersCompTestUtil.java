package com.intuit.sbd.payroll.psp.processes.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import org.junit.Assert;

import java.util.Collection;
import java.util.Set;

/**
 * User: michaelp696
 */
public class WorkersCompTestUtil {

    public static ProcessResult<CompanyService> addWorkersCompServiceToCompany(Company company, DateDTO serviceStartDate) {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
        serviceInfoDTO.setServiceStartDate(serviceStartDate.toSpcfCalendar());
        serviceInfoDTO.setServiceCode(ServiceCode.WorkersComp);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.addService(SourceSystemCode.QBDT, company.getSourceCompanyId(), serviceInfoDTO);
        PayrollServices.commitUnitOfWork();
        return processResult;
    }

    public static ProcessResult<CompanyService> removeWorkersCompService(Company pCompany) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.deactivateService(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), ServiceCode.WorkersComp);
        PayrollServices.commitUnitOfWork();

        return processResult;
    }

    /*
     * This method is intended for use with one payroll run at a time
     */
    public static void markWorkersCompPaychecksSent(Company company, PayrollRunDTO payrollRunDto) {
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDto.getPayrollTXBatchId());
        //Simulate all workers comp paychecks being sent successfully
        for(Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            paycheck.getWorkersCompPaycheck().markAsSent();
        }
        PayrollServices.commitUnitOfWork();
        WorkersCompTestUtil.assertWorkersCompPaycheck(payrollRunDto.getPaychecks(), WorkersCompPaycheckStateCode.Sent);
        //Will not necessarily be true if used with multiple workers comp payroll runs
        WorkersCompTestUtil.assertWorkersCompPaycheckPendingStateEmpty();
    }

    public static Company createAssistedCompanyWithWorkersCompService(String psid) {
        Company assistedCompany = DataLoadServices.setupAssistedCompanyForCA(psid, 2, true);
        Assert.assertNotNull(assistedCompany);
        DateDTO serviceStartDate = new DateDTO("2012-12-01");
        CompanyService workersCompService = WorkersCompTestUtil.addWorkersCompServiceToCompany(assistedCompany, serviceStartDate).getResult();
        Assert.assertNotNull(workersCompService);
        return assistedCompany;
    }

    public static void assertWorkersCompPaycheckEmpty() {
        Expression<WorkersCompPaycheck> query = new Query<WorkersCompPaycheck>().EagerLoad(WorkersCompPaycheck.Paycheck());
        Set<WorkersCompPaycheck> workersCompPaycheckSet = Application.find(WorkersCompPaycheck.class, query);
        Assert.assertTrue("Expected workers comp paycheck table to be empty", workersCompPaycheckSet.size() == 0);
    }

    public static void assertWorkersCompPaycheck(Collection<PaycheckDTO> expectedPaychecks, WorkersCompPaycheckStateCode expectedStateCode) {
        Expression<WorkersCompPaycheck> query = new Query<WorkersCompPaycheck>().EagerLoad(WorkersCompPaycheck.Paycheck());
        Set<WorkersCompPaycheck> workersCompPaycheckSet = Application.find(WorkersCompPaycheck.class, query);

        boolean matchingPaycheck;
        if(workersCompPaycheckSet.size() != expectedPaychecks.size()) {
            Assert.fail("WorkersCompPaycheck size: " + workersCompPaycheckSet.size() +
                                " does not match expected size of: " + expectedPaychecks.size());
        }
        for(PaycheckDTO paycheckDTO : expectedPaychecks) {
            matchingPaycheck = false;
            for(WorkersCompPaycheck workersCompPaycheck : workersCompPaycheckSet) {
                if(paycheckDTO.getPaycheckId().equals(workersCompPaycheck.getPaycheck().getSourcePaycheckId())) {
                    if(workersCompPaycheck.getCurrentStateCd() == expectedStateCode) {
                        matchingPaycheck = true;
                    }
                    break;
                }
            }
            if(!matchingPaycheck) {
                Assert.fail("Unable to find workers comp paycheck for paycheck id: " + paycheckDTO.getPaycheckId());
            }
        }
    }

    public static void assertWorkersCompPaycheckPendingStateEmpty() {
        Expression<WorkersCompPaycheckPendingState> query = new Query<WorkersCompPaycheckPendingState>().EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck(),
                                                                                                                   WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck());
        Set<WorkersCompPaycheckPendingState> workersCompPaycheckPendingStateSet = Application.find(WorkersCompPaycheckPendingState.class, query);
        Assert.assertTrue("Expected workers comp paycheck pending state table to be empty", workersCompPaycheckPendingStateSet.size() == 0);
    }

    public static void assertWorkersCompPaycheckPendingState(Collection<PaycheckDTO> expectedPaychecks, WorkersCompPaycheckStateCode expectedStateCode) {
        Expression<WorkersCompPaycheckPendingState> query = new Query<WorkersCompPaycheckPendingState>().EagerLoad(WorkersCompPaycheckPendingState.WorkersCompPaycheck(),
                                                                                                                   WorkersCompPaycheckPendingState.WorkersCompPaycheck().Paycheck());
        Set<WorkersCompPaycheckPendingState> workersCompPaycheckPendingStateSet = Application.find(WorkersCompPaycheckPendingState.class, query);

        boolean matchingPaycheck;
        if(workersCompPaycheckPendingStateSet.size() != expectedPaychecks.size()) {
            Assert.fail("WorkersCompPaycheckPendingState size: " + workersCompPaycheckPendingStateSet.size() +
                                " does not match expected size of: " + expectedPaychecks.size());
        }
        for(PaycheckDTO paycheckDTO : expectedPaychecks) {
            matchingPaycheck = false;
            for(WorkersCompPaycheckPendingState workersCompPaycheckPendingState : workersCompPaycheckPendingStateSet) {
                if(paycheckDTO.getPaycheckId().equals(workersCompPaycheckPendingState.getWorkersCompPaycheck().getPaycheck().getSourcePaycheckId())) {
                    if(workersCompPaycheckPendingState.getStateCd() == expectedStateCode) {
                        matchingPaycheck = true;
                    }
                    break;
                }
            }
            if(!matchingPaycheck) {
                Assert.fail("Unable to find workers comp paycheck for paycheck id: " + paycheckDTO.getPaycheckId());
            }
        }
    }

    public static void cancelEmployeeTransactionFirstPayroll(Company company) {
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePayrollRunId(PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId());
        PayrollServices.beginUnitOfWork();
        PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), transactionCancelEEDTO);
        PayrollServices.commitUnitOfWork();
    }

    public static void voidFirstPayroll(Company company) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId());
        PayrollServices.beginUnitOfWork();
        PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
    }

}

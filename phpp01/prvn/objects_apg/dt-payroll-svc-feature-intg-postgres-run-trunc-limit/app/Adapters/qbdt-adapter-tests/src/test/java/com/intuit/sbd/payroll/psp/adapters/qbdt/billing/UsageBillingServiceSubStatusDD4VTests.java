package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: YifengS302
 * Date: 9/14/12
 * Time: 3:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsageBillingServiceSubStatusDD4VTests extends UsageBillingTestsBase {

    @Test
    public void fraudReviewForDD4VTest() throws Exception {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String sourceCompanyId=company.getSourceCompanyId();
        DataLoadServices.activateCloudService(company);
        DataLoadServices.activateDDService(company);

        DataLoadServices.addBillPaymentService(company);
        DataLoadServices.updateCompanyPIN(company, "1234567a");

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.BillPayment);
        companyService.setStatusCd(ServiceSubStatusCode.FraudReview);
        Application.save(companyService);
        PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, sourceCompanyId, ServiceSubStatusCode.FraudReview);

        PayrollServices.commitUnitOfWork();

        // submit bp payment
        List<Payee> payees = DataLoadServices.addPayees(company, 2);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertFalse("submit BP Payroll", submitBPPayroll.isSuccess());
        PayrollServices.rollbackUnitOfWork();
    }
}

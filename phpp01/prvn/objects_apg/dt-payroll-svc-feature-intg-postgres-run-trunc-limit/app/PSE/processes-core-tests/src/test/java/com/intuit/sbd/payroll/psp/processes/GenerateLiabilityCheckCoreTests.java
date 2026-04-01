package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAgencyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 9/2/12
 * Time: 11:01 AM
 */
public class GenerateLiabilityCheckCoreTests {
    private static final String PSID = "123456789";


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testReturnedCheckPayment() {
        DataLoadServices.setPSPDate(2012, 8, 1);
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT",SpcfCalendar.createInstance(2012, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, PSID, true, ServiceCode.Tax);

        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        List<Employee> employees = new ArrayList<Employee>();
        employees.addAll(Employee.findEmployees(company));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2012-08-15"), employees, new String[]{"40","1"}, new String[]{"5", "12"});
        PayrollRun payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("PA-501-PAYMENT").And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment))));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, 2012, 8, 13);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("PA-501-PAYMENT"), company);

        // reject payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), "cancel check payment"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertOne(Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)
                                                                                   .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Returned))
                                                                                   .And(FinancialTransaction.Law().LawId().equalTo("40"))));
        PayrollServices.rollbackUnitOfWork();

        // recalc liability check, an exception will be thrown if the total doesn't match the sum of the lines
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertSuccess(PayrollServices.companyManager.generateLiabilityChecks(company, payrollRun));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.initiateTaxRepayment(moneyMovementTransaction.getId().toString(), moneyMovementTransaction.getInitiationDate().toLocal(), true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertOne(Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)
                                                                                   .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Returned))
                                                                                   .And(FinancialTransaction.Law().LawId().equalTo("40"))));
        assertOne(Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit)
                                                                                   .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))
                                                                                   .And(FinancialTransaction.Law().LawId().equalTo("40"))));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertSuccess(PayrollServices.companyManager.generateLiabilityChecks(company, payrollRun));
        PayrollServices.commitUnitOfWork();
    }
}

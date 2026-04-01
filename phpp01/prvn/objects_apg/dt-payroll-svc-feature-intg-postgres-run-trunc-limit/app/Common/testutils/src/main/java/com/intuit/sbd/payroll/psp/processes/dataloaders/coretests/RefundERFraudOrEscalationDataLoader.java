package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 24, 2008
 * Time: 11:59:07 AM
 */
public class RefundERFraudOrEscalationDataLoader {

    public static final SourceSystemCode SRC_SYS_CODE = SourceSystemCode.QBDT;
    public static final String SRC_COMPANY_ID = "123272727";

    public static void before(){
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    public static PayrollRun loadData(String pInitialDateTime) {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SRC_SYS_CODE);

        // create everything and submit a payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pInitialDateTime);
        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> prSubmit = PayrollServices.payrollManager.submitPayroll(SRC_SYS_CODE, SRC_COMPANY_ID, dtoPayroll);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submitPayroll()", prSubmit);

        // offload the ER DD debit
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> found = FinancialTransaction.findFinancialTransactions(SRC_SYS_CODE, SRC_COMPANY_ID, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        FinancialTransaction erDebit = found.get(0);
        MoneyMovementTransaction mmt = erDebit.getMoneyMovementTransaction();
        PSPDate.setPSPTime(mmt.getInitiationDate());
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);        

        // post-offload processing
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions post = new ProcessACHTransactions();
        post.process(PSPDate.getPSPTime());
        erDebit = Application.refresh(erDebit);
        TransactionStateCode erDebitState = erDebit.getCurrentTransactionState().getTransactionStateCd();
        PayrollServices.commitUnitOfWork();
        assertEquals("ER DD Debit state after post-offload processing", TransactionStateCode.Completed, erDebitState);

        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = Application.refresh(prSubmit.getResult());
        PayrollServices.commitUnitOfWork();

        return payroll;
    }

    public static void loadRefundData(){
        before();
        loadData("20070831000000");
    }

}

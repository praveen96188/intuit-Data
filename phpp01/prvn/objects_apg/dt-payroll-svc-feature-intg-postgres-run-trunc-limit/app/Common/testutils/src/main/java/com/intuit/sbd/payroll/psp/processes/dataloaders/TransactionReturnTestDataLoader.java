package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 *
 * User: rkrishna
 * Date: Dec 10, 2007
 * Time: 3:03:46 PM

 */
public class TransactionReturnTestDataLoader {

    public static TransactionReturn loadDataForTransactionReturn(){
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        PayrollServicesTest.assertSuccess("submitPayroll", processResult);

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        Employee employee = Employee.findEmployee(company, "Emp1");

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company,payrollRunDTO.getPayrollTXBatchId(),employee
                        ,null,null,null,null,null,null);

        TransactionReturnBatch transactionReturnBatch = persistTransactionReturnBatch();
        
        TransactionReturn transactionReturn =  null;
        
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an NSF description");
            transactionReturn.setBankReturnTraceNumber(new Long(112));
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());                
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());

            transactionReturn = Application.save(transactionReturn);
        }

        return transactionReturn;        
    }


    public static void createLotsaBankReturns() {

        for(int i=0; i < 20; i++)
        {
            PayrollServices.beginUnitOfWork();
            PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
            String newCompanyId = String.valueOf(Long.parseLong(psdl.getCompanyId()) - i);
            String newFein = String.valueOf(Long.parseLong(psdl.getFein()) - i);

            psdl.setCompanyId(newCompanyId);
            psdl.setFein(newFein);

            //set PSP Date
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
            PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                    newCompanyId, payrollRunDTO);

            PayrollServicesTest.assertSuccess("submitPayroll", processResult);

            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                    newCompanyId, SourceSystemCode.QBOE);

            DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                    findFinancialTransactions(company,null,null
                            ,null,null,null,null,null,null);

            TransactionReturnBatch transactionReturnBatch = persistTransactionReturnBatch();
            TransactionReturn transactionReturn = null;

            for (FinancialTransaction financialTransaction : financialTransactions) {
                transactionReturn = new TransactionReturn();
                transactionReturn.setBankReturnCd("R01");
                transactionReturn.setBankReturnDescription("This is an NSF description");
                transactionReturn.setBankReturnTraceNumber(new Long(112));
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                        SpcfTimeZone.getLocalTimeZone()));
                transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
                transactionReturn.setReturnBatch(transactionReturnBatch);
                transactionReturn.setCompany(financialTransaction.getCompany());

                transactionReturn = Application.save(transactionReturn);
            }
            PayrollServices.commitUnitOfWork();
        }


    }


    public static void loadDataForMultipleTransactionReturns() {
        PayrollServices.beginUnitOfWork();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        PayrollServicesTest.assertSuccess("submitPayroll", processResult);

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company,null,null
                        ,null,null,null,null,null,null).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        TransactionReturnBatch transactionReturnBatch = persistTransactionReturnBatch();
        TransactionReturn transactionReturn = null;

        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an NSF description");
            transactionReturn.setBankReturnTraceNumber(new Long(112));
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());

            transactionReturn = Application.save(transactionReturn);
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Function to add the TransactionReturns to the associated Financial Transactions
     *
     * @param pFinTxnList     DomainEntitySet<FinancialTransaction>
     * @param pBankReturnCd   String
     * @param pBankReturnDesc String
     * @return DomainEntitySet<TransactionReturn>
     */
    public static DomainEntitySet<TransactionReturn> persistTransactionReturns(DomainEntitySet<FinancialTransaction> pFinTxnList,
                                                                   String pBankReturnCd, String pBankReturnDesc) {
        TransactionReturnBatch transactionReturnBatch = persistTransactionReturnBatch();
        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : pFinTxnList) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd(pBankReturnCd);
            transactionReturn.setBankReturnDescription(pBankReturnDesc);
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());
            
            returnList.add(Application.save(transactionReturn));
        }

        return returnList;
    }

    public static TransactionReturnBatch persistTransactionReturnBatch(){
        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        //long mmResponseFiled = 124;
        //transactionReturnBatch.setACHReturnFileId(mmResponseFiled);
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        return transactionReturnBatch; 
    }
}

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 *
 * User: wnichols
 * Date: Jan 3, 2008
 * Time: 11:09:22 AM

 */
public class AddEscalationCore extends Process implements IProcess
{
    // process inputs
    SourceSystemCode mSrcSystemCd;
    String mCompanyId;
    String mPayrollRunId;
    boolean mIsEmployee;
    SettlementType mSettlementType;
    BigDecimal mAmount;
    SpcfCalendar mSettlementDate;

    // retrieved during validate() for use during execute()
    Company mCompany;
    PayrollRun mPayrollRun;

    FinancialTransaction mEscalationTxn;
    
    public AddEscalationCore(SourceSystemCode pSrcSystemCd, String pCompanyId, String pPayrollRunId,
                             boolean pIsEmployee, SettlementType pSettlementType, BigDecimal pAmount,
                             DateDTO pSettlementDate)
    {
        mSrcSystemCd = pSrcSystemCd;
        mCompanyId = pCompanyId;
        mPayrollRunId = pPayrollRunId;
        mIsEmployee = pIsEmployee;
        mSettlementType = pSettlementType;
        mAmount = pAmount;
        mSettlementDate = DateDTO.convertToSpcfCalendar(pSettlementDate);
    }

    public ProcessResult validate()
    {
        ProcessResult result = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSrcSystemCd, mCompanyId);

        if (result.isSuccess()) // if we have enough info to look for a company...
        {
            // make sure the company exists
            mCompany = Company.findCompany(mCompanyId, mSrcSystemCd);
            if (mCompany == null)
            {
                result.getMessages().CompanyDoesNotExist(EntityName.Escalation, mCompanyId,
                        mSrcSystemCd.toString(), mCompanyId);
            }
            else // the company exists -- we need it for these validations
            {
                // make sure the payroll run exists
                mPayrollRun = PayrollRun.findPayrollRun(mCompany, mPayrollRunId);
                if (mPayrollRun == null)
                {
                    result.getMessages().PayrollRunDoesNotExist(EntityName.Escalation, mPayrollRunId,
                            mPayrollRunId, mSrcSystemCd.toString(), mCompanyId);
                }
            }

            // make sure the amount is > 0
            if (mAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                result.getMessages().AmountNotPositive(EntityName.Escalation, mPayrollRunId);
            }

            CalendarUtils.clearTime(mSettlementDate);
            // make sure txn date not in the future (Cannot record a transaction of Settlement Type {0} with the future date {1})
            SpcfCalendar eodToday = PSPDate.getPSPTime();
            eodToday.setValues(eodToday.getYear(), eodToday.getMonth(), eodToday.getDay(), 23, 59, 59, 999);

            if (mSettlementDate.after(eodToday))
            {
                SimpleDateFormat formatter = new SimpleDateFormat("M/d/yyyy");
                String formattedDate = formatter.format(CalendarUtils.convertToDate(mSettlementDate));
                result.getMessages().SettlementDateTooFarInFuture(EntityName.Escalation, mPayrollRunId, formattedDate, mSettlementType.toString());
            }
            else
            {
                // make sure txn date not older than 45 days (Cannot record a transaction of Settlement Type {0} and date {1}, which is more than 45 days in the past.)
                SpcfCalendar back45 = PSPDate.getPSPTime();
                back45.addDays(-45);

                if (mSettlementDate.before(back45))
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("M/d/yyyy");
                    String formattedDate = formatter.format(CalendarUtils.convertToDate(mSettlementDate));
                    result.getMessages().SettlementDateTooFarInPast(EntityName.Escalation, mPayrollRunId, formattedDate, mSettlementType.toString());
                }
            }

            if (mIsEmployee && ! mCompany.isCompanyOnService(ServiceCode.DirectDeposit)) {
                result.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                        mCompany.getSourceCompanyId(), mCompany.getSourceSystemCd().toString(),
                        mCompany.getSourceCompanyId(), ServiceCode.DirectDeposit.toString());
            }
        }

        return result;
    }


    public ProcessResult process()
    {
        ProcessResult<FinancialTransaction> result = new ProcessResult();

        // create escalation txn (initial state is CR)
        FinancialTransaction txn = FinancialTransaction.createFinancialTransaction(mCompany,
                mPayrollRun, //PayrollRun pPayrollRun,
                null, //PaycheckSplit pPaycheckSplit,
                null, //BankAccount pCreditBankAccount,
                null, //BankAccount pDebitBankAccount,
                null, //BankAccountOwnerType pCreditBankAccountOwnerType,
                null, //BankAccountOwnerType pDebitBankAccountOwnerType,
                (mIsEmployee ? TransactionTypeCode.EmployeeEscalationCredit : TransactionTypeCode.EmployerEscalationCredit), //String pTransactionTypeCode,
                new SpcfMoney(mAmount.toPlainString()), //SpcfMoney pFinancialTransactionAmount,
                mSettlementType, //SettlementType pSettlementType,
                mSettlementDate
                );

        // advance the txn state to EX, then to CP
        txn = txn.updateFinancialTransactionState(TransactionStateCode.Executed);
        txn = txn.updateFinancialTransactionState(TransactionStateCode.Completed);

        mEscalationTxn = txn;

        return result;
    }

    public FinancialTransaction getEscalationTxn() {
        return mEscalationTxn;
    }
}

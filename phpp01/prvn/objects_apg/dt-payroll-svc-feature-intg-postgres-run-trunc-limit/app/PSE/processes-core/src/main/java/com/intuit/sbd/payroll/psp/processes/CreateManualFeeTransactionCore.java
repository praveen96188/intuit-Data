package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: ihannur
 * Date: 6/29/12
 * Time: 2:34 PM
 */
public class CreateManualFeeTransactionCore extends Process implements IProcess {

    private ERFeeAddDTO[] mFeeInputDTOs;
    private Company mCompany;

    public CreateManualFeeTransactionCore(ERFeeAddDTO... pFeeAddDTOs) {
        mFeeInputDTOs = pFeeAddDTOs;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        for (ERFeeAddDTO erFeeAddDTO : mFeeInputDTOs) {
            // Check if Company parameters are valid
            validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(erFeeAddDTO.getSourceSystemCd(), erFeeAddDTO.getSourceCompanyId()));
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            // Check if Company Exists
            mCompany = Company.findCompany(erFeeAddDTO.getSourceCompanyId(), erFeeAddDTO.getSourceSystemCd());

            if (mCompany == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                        erFeeAddDTO.getSourceCompanyId(),
                        erFeeAddDTO.getSourceSystemCd().toString(),
                        erFeeAddDTO.getSourceCompanyId());
                return validationResult;
            }

            // validate the fee type
            ProcessResult validateDTOResult = erFeeAddDTO.validateFeeAddDTO();
            if (!validateDTOResult.isSuccess()) {
                validationResult.merge(validateDTOResult);
                return validationResult;
            }

            // only "additional fees" may be billed manually... no "payroll usage fees"
            if (OfferingServiceChargeGroup.isPayrollChargeType(erFeeAddDTO.getFeeTypeCode()) && !OfferingServiceChargeGroup.isW2ChargeType(erFeeAddDTO.getFeeTypeCode())) {
                validationResult.getMessages().InvalidValue(EntityName.Fee, null, "FeeType");
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        PayrollRun payrollRun;
        if(mFeeInputDTOs[0].getSourcePayrollRunId() == null) {
            //Create FeeOnly Payroll Run
            SpcfCalendar today = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(today);
            payrollRun = PayrollRun.createFeePayrollRun(mCompany, today);

            for (ERFeeAddDTO erFeeAddDTO : mFeeInputDTOs) {
                erFeeAddDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            }

            Application.save(payrollRun);
            Application.getHibernateSession().flush();
        } else {
            payrollRun = PayrollRun.findPayrollRun(mCompany,mFeeInputDTOs[0].getSourcePayrollRunId());
        }

        AddFeeTransactionCore addFeeTransactionCore = new AddFeeTransactionCore(mFeeInputDTOs);

        // If all transactions are complete set the payroll run status to complete
       ProcessResult<DomainEntitySet<FinancialTransaction>>   addFeeResult =    addFeeTransactionCore.execute();
       DomainEntitySet<FinancialTransaction> feeTransactions =  addFeeResult.getResult();
       boolean completePayroll = true;
       for (FinancialTransaction feeTransaction:feeTransactions) {
           if (!feeTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Completed)) {
               completePayroll = false;
               continue;
           }
       }
        if (payrollRun != null && completePayroll) {
            payrollRun.setPayrollRunStatus(PayrollStatus.Complete);
        }
        Application.save(payrollRun);
        return addFeeResult;
    }
}

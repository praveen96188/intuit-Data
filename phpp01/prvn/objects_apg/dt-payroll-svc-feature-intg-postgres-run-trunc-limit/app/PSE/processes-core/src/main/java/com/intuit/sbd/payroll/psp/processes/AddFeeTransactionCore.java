package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * User: rsakhamuri
 * Date: Dec 14, 2007
 * Time: 9:32:59 AM
 * THIS WON'T WORK FOR SERVICE-SPECIFIC FEES, SUCH AS PERPAYROLL & PERTRANSMISSION
 */
public class AddFeeTransactionCore extends Process implements IProcess {

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private CompanyBankAccount mCBA;
    private DDCompanyServiceInfo mCompanyService;
    private TransactionResponse mTransactionResponse;
    private ERFeeAddDTO[] mERFeeAddDTOs;
    private SpcfCalendar mSettlementDate;

    public AddFeeTransactionCore() {
        super();
    }

    public AddFeeTransactionCore(ERFeeAddDTO... pFeeAddDTOs) {
        mERFeeAddDTOs = pFeeAddDTOs;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        for (ERFeeAddDTO erFeeAddDTO : mERFeeAddDTOs) {
            // Check if Company parameters are valid
            validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(erFeeAddDTO.getSourceSystemCd(),
                    erFeeAddDTO.getSourceCompanyId()));
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            if (mCompany == null) {
                // Check if Company Exists
                mCompany = Company.findCompany(erFeeAddDTO.getSourceCompanyId(), erFeeAddDTO.getSourceSystemCd());

                if (mCompany == null) {
                    validationResult.getMessages().CompanyDoesNotExist(EntityName.Company,
                            erFeeAddDTO.getSourceCompanyId(),
                            erFeeAddDTO.getSourceSystemCd().toString(),
                            erFeeAddDTO.getSourceCompanyId());
                    return validationResult;
                }
            } else {
                if (!mCompany.getSourceSystemCd().equals(erFeeAddDTO.getSourceSystemCd()) ||
                        !mCompany.getSourceCompanyId().equals(erFeeAddDTO.getSourceCompanyId())) {
                    validationResult.getMessages().CompanyDoesNotMatchPreviousCompany(EntityName.Company,
                            erFeeAddDTO.getSourceCompanyId(),
                            erFeeAddDTO.getSourceSystemCd().toString(),
                            erFeeAddDTO.getSourceCompanyId(),
                            mCompany.getSourceCompanyId(),
                            mCompany.getSourceSystemCd().toString());
                    return validationResult;
                }
            }

            // validate the fee type
            ProcessResult validateDTOResult = erFeeAddDTO.validateFeeAddDTO();
            if (!validateDTOResult.isSuccess()) {
                validationResult.merge(validateDTOResult);
                return validationResult;
            }

            // only "additional fees" may be billed manually... no "payroll usage fees"
            if (OfferingServiceChargeGroup.isPayrollChargeType(erFeeAddDTO.getFeeTypeCode()) &&
                    !OfferingServiceChargeGroup.isW2ChargeType(erFeeAddDTO.getFeeTypeCode())) {
                validationResult.getMessages().InvalidValue(EntityName.Fee, null, "FeeType");
                return validationResult;
            }

            // make sure the payroll run exists
            if (erFeeAddDTO.getSourcePayrollRunId() == null) {
                validationResult.getMessages().SourcePayrollRunIdNotSpecified(EntityName.PayrollRun, "DDTxBatchId");
                return validationResult;
            }

            mPayrollRun = PayrollRun.findPayrollRun(mCompany, erFeeAddDTO.getSourcePayrollRunId());
            if (mPayrollRun == null) {
                validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, erFeeAddDTO.getSourcePayrollRunId(),
                                                                      erFeeAddDTO.getSourcePayrollRunId(),
                                                                      erFeeAddDTO.getSourceSystemCd().toString(),
                                                                      erFeeAddDTO.getSourceCompanyId());
                return validationResult;
            } else {

                // Check whether this is a valid action
                PayrollStatus statusCode = mPayrollRun.getPayrollRunStatus();
                ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.ERFeeAdd);
                if (!mPayrollRun.validateAction(actionEvent)) {
                    validationResult.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun,
                                                                               mPayrollRun.getSourcePayRunId(),
                                                                               actionEvent.getCode().toString(),
                                                                               mPayrollRun.getSourcePayRunId(),
                                                                               mPayrollRun.getPayrollRunStatus().toString());
                    return validationResult;
                }

            }

            // further validations depend on settlement type
            if (SettlementTypeDTO.ACH.equals(erFeeAddDTO.getSettlementTypeCode())) {

                mCBA = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

                if (mCBA == null) {
                    validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                            erFeeAddDTO.getSourceCompanyId(), erFeeAddDTO.getSourceSystemCd().toString(), erFeeAddDTO.getSourceCompanyId());
                    return validationResult;
                }

                if (!CompanyService.isCompanyOnDirectDepositOrTaxService(mCompany)) {
                    validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                            erFeeAddDTO.getSourceCompanyId(), erFeeAddDTO.getSourceSystemCd().toString(),
                            erFeeAddDTO.getSourceCompanyId(), ServiceCode.DirectDeposit.toString() + " or " + ServiceCode.Tax.toString());
                    return validationResult;
                }

            } else {  // non ACH settlement

                // validate amount
                if (erFeeAddDTO.getAmount() == null || erFeeAddDTO.getAmount().compareTo(new SpcfMoney("0.00")) <= 0) {
                    validationResult.getMessages().AmountPositiveForNonACHTransactions(EntityName.DDTransaction,
                                                                                       erFeeAddDTO.getSourcePayrollRunId());
                    return validationResult;
                }
                validateNonACHSettlementDate(erFeeAddDTO, validationResult);
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();


        boolean createEvent = false;
        DomainEntitySet<FinancialTransaction> feeTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> eventTransactions = new DomainEntitySet<FinancialTransaction>();

        for (ERFeeAddDTO erFeeAddDTO : mERFeeAddDTOs) {
            FinancialTransaction feeTransaction = null;
            FinancialTransaction taxTxn = null;
            if (SettlementTypeDTO.ACH.equals((erFeeAddDTO.getSettlementTypeCode()))) {

                // charge the fee
                DomainEntitySet<BillingDetail> details = null;
                if (erFeeAddDTO.getAmount() != null) {
                    details = BillingDetail.createBillingDetailWithPriceOverride(mPayrollRun, mCBA, erFeeAddDTO.getFeeTypeCode(), 1, SpcfUtils.convertToBigDecimal(erFeeAddDTO.getAmount()), null, erFeeAddDTO.getMemo());
                } else {
                    details = BillingDetail.createBillingDetail(mPayrollRun, mCBA, erFeeAddDTO.getFeeTypeCode(), 1, null);
                }

                // create a transaction response
                Collection<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();
                // question: no null check needed?
                for (BillingDetail detail : details) {
                    feeTransaction = detail.getFeeTransaction();
                    transactions.add(feeTransaction);

                    taxTxn = detail.getTaxTransaction();
                    if (taxTxn != null) {
                        transactions.add(taxTxn);
                    }
                }
                mTransactionResponse = TransactionResponse.createTransactionResponse(mCompany, transactions, null);

            } else { // non-ACH

                // charge the fee
                BigDecimal feeAmount = SpcfUtils.convertToBigDecimal(erFeeAddDTO.getAmount());
                SettlementType settlementType = ProcessesToDTO.getDomainSettlementType(erFeeAddDTO.getSettlementTypeCode());
                DomainEntitySet<BillingDetail> details = BillingDetail.createNonACHFee(mPayrollRun, mCBA, erFeeAddDTO.getFeeTypeCode(), 1, feeAmount,
                                                                   settlementType, mSettlementDate);

                // advance the fee [and tax] transaction[s] thru Executed to Completed
                TransactionState executedState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
                TransactionState completedState = Application.findById(TransactionState.class, TransactionStateCode.Completed);

                for (BillingDetail detail : details) {
                    // fee FT...
                    feeTransaction = detail.getFeeTransaction();
                    feeTransaction.addTransactionState(executedState);
                    feeTransaction.addTransactionState(completedState);

                    // tax FT...
                    taxTxn = detail.getTaxTransaction();
                    if (taxTxn != null) {
                        taxTxn.addTransactionState(executedState);
                        taxTxn.addTransactionState(completedState);
                    }
                }
            }

            if (feeTransaction.getTransactionType().getFeeInd() && feeTransaction.getSku() != null) {
                OfferingServiceCharge ofc = OfferingServiceCharge.findBySKU(feeTransaction.getSku());
                if (ofc != null && (SkuType.NonPayroll.equals(ofc.getSkuType()) || OfferingServiceChargeGroup.isW2ChargeType(erFeeAddDTO.getFeeTypeCode()))) {
                    eventTransactions.add(feeTransaction);
                    if (taxTxn != null) {
                        eventTransactions.add(taxTxn);
                    }
                }
            }

            feeTransactions.add(feeTransaction);
        }

        if (!eventTransactions.isEmpty()) {
            CompanyEvent.createFeeCreatedEvent(mCompany, eventTransactions);
        }

        // If this is a non payroll fee transaction, add a "Fee Created" event


        processResult.setResult(feeTransactions);

        return processResult;
    }

 
    private ProcessResult validateNonACHSettlementDate(ERFeeAddDTO pERFeeAddDTO, ProcessResult validationResult) {
        Date txDate = pERFeeAddDTO.getTxDate();
        if (txDate == null) {
            validationResult.getMessages().SettlementDateNotSpecified(EntityName.Date, pERFeeAddDTO.getSourcePayrollRunId());
            return validationResult;
        }

        Calendar settlementDateCalendar = Calendar.getInstance();
        settlementDateCalendar.setTime(txDate);
        mSettlementDate = CalendarUtils.convertToSpcfCalendar(settlementDateCalendar);
        CalendarUtils.clearTime(mSettlementDate);

        // Validate that the settlement date is valid
        SpcfCalendar earliestDate = PSPDate.getPSPTime();
        earliestDate.addDays(-45);
        CalendarUtils.clearTime(earliestDate);

        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);

        if (mSettlementDate.before(earliestDate)) {
            validationResult.getMessages().SettlementDateTooFarInPast(EntityName.Date, mSettlementDate.toString(),
                                                                      mSettlementDate.toString(),
                                                                      pERFeeAddDTO.getSettlementTypeCode().toString());
        } else if (mSettlementDate.after(today)) {
            validationResult.getMessages().SettlementDateTooFarInFuture(EntityName.Date, mSettlementDate.toString(),
                                                                        mSettlementDate.toString(),
                                                                        pERFeeAddDTO.getSettlementTypeCode().toString());
        }

        return validationResult;
    }
}

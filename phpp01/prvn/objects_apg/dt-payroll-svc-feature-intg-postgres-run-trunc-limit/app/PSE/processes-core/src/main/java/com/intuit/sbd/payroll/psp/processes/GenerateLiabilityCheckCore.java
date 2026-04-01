package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityCheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityCheckLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 2/16/12
 * Time: 4:16 PM
 */
public class GenerateLiabilityCheckCore extends Process implements IProcess {
    private static final String EXCESS_FUNDS_TRANSACTION_ID = "ExcessFunds";
    private static final String EXCESS_FUNDS_APPLIED_TRANSACTION_ID = "ExcessFundsApplied";
    private static final String DD_TRANSACTION_ID = "DirectDeposit";
    private static final String NULL_EMP_NAME_STR = "<EMPTY>";

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private FinancialTransaction mFinancialTransaction;
    private String mFeeCOA;
    private String mSalesTaxCOA;
    private String mLegalAddressState;
    private String mSourceBankAccountName;

    public GenerateLiabilityCheckCore(Company pCompany, PayrollRun pPayrollRun) {
        mCompany = pCompany;
        mPayrollRun = pPayrollRun;
    }

    public GenerateLiabilityCheckCore(Company pCompany, FinancialTransaction pFinancialTransaction) {
        mCompany = pCompany;
        mFinancialTransaction = pFinancialTransaction;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mPayrollRun == null && mFinancialTransaction == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, "PayrollRun", "PayrollRun");
            return validationResult;
        }

        // financial transaction option is only valid for verification debits because they don't have a payroll run
        if (mFinancialTransaction != null && mFinancialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerVerificationDebit) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, "FinancialTransaction", "FinancialTransaction");
            return validationResult;
        }

        if (mCompany == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, "Company", "Company");
            return validationResult;
        }

        // using current account so that we always use the most current bank account COA
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
        if(companyBankAccount == null && mFinancialTransaction != null) {
            DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                    mCompany.getCompanyBankAccountCollection()
                            .find(CompanyBankAccount.BankAccount().equalTo(mFinancialTransaction.getDebitBankAccount())
                                                    .And(CompanyBankAccount.ExpirationDate().isNull()));

            if (companyBankAccounts.isNotEmpty()) {
                companyBankAccount = companyBankAccounts.get(0);
            }
        }

        if(companyBankAccount == null) {
            mSourceBankAccountName = "Active Bank Account Not Found";
            validationResult.getMessages().ActiveBankAccountWarning(EntityName.Company, mCompany.getSourceCompanyId(), "LiabilityCheck");
        } else {
            mSourceBankAccountName = companyBankAccount.getSourceBankAccountName();
        }



        return validationResult;
    }

    @Override
    public ProcessResult process() {
        mFeeCOA = mCompany.getQuickbooksInfo().getCoaFeeAccountName();
        mSalesTaxCOA = mCompany.getQuickbooksInfo().getCoaSalesTaxAccountName();
        mLegalAddressState = mCompany.getLegalAddress().getState();

        ProcessResult processResult = null;
        if(mPayrollRun != null) {
            processResult = processLiabilityChecks(mPayrollRun);
        } else if(mFinancialTransaction != null) {
            if(mFinancialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerVerificationDebit) {
                processResult = processVerificationLiabilityCheck(mFinancialTransaction);
            }
        }

        //saving company because we may have changed some of the next ids
        mCompany = Application.save(mCompany);
        return processResult;
    }

    private ProcessResult processLiabilityChecks(PayrollRun pPayrollRun) {
        ProcessResult processResult = new ProcessResult();

        // if there is an employer tax credit on the payroll run, we do not need to create a liability check for it - deposit return of liabilities
        FinancialTransaction employerCredit = pPayrollRun.getFinancialTransactionCollection()
                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit));
        if (employerCredit != null) {
            return processResult;
        }

        // don't create a liability check for payroll runs that include non-QBDT laws
        // this could possibly cause a liability check not to be created for a valid debit if an agent over credits an advance, but better to not create the liability check then have an unbalanced check
        DomainEntitySet<FinancialTransaction> nonQBDTLaws = pPayrollRun.getFinancialTransactionCollection()
                                                                       .find(FinancialTransaction.Law().LawId().in(Law.NON_QBDT_LAWS));
        if (nonQBDTLaws.isNotEmpty()) {
            return processResult;
        }

        // non 100k debit
        DomainEntitySet<FinancialTransaction> employerDebits = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerDdDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))
                        .And(FinancialTransaction.QbdtTransactionInfo().isNull()));
        for (FinancialTransaction financialTransaction : employerDebits) {
            if(financialTransaction.getMoneyMovementTransaction() != null) {
                processResult.merge(processLiabilityChecks(financialTransaction.getMoneyMovementTransaction()));
                break;
            }
        }

        // if all of the employer tax debits have been cancelled check for and update any existing liability check
        if (employerDebits.size() == 0) {
            LiabilityCheck existingLiabilityCheck = pPayrollRun.getLiabilityCheckCollection()
                    .findEntity(LiabilityCheck.Type().equalTo(LiabilityCheckType.EmployerDebit));
            if(existingLiabilityCheck != null) {
                processResult.merge(updateLiabilityCheck(existingLiabilityCheck, pPayrollRun, null));
            }
        }

        // 100k debit
        DomainEntitySet<FinancialTransaction> employerDirectDebits = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))
                        .And(FinancialTransaction.QbdtTransactionInfo().isNull()));
        for (FinancialTransaction financialTransaction : employerDirectDebits) {
            if(financialTransaction.getMoneyMovementTransaction() != null) {
                processResult.merge(processLiabilityChecks(financialTransaction.getMoneyMovementTransaction()));
            }
        }

        // if all of the employer tax direct debits have been cancelled check for and update any existing liability check
        if (employerDirectDebits.size() == 0) {
            LiabilityCheck existingLiabilityCheck = pPayrollRun.getLiabilityCheckCollection()
                    .findEntity(LiabilityCheck.Type().equalTo(LiabilityCheckType.EFTPSDirectDebit));
            if(existingLiabilityCheck != null) {
                processResult.merge(updateLiabilityCheck(existingLiabilityCheck, pPayrollRun, null));
            }
        }

        DomainEntitySet<MoneyMovementTransaction> processedMMTs = new DomainEntitySet<MoneyMovementTransaction>();
        for (BillingDetail billingDetail : pPayrollRun.getBillingDetailCollection()) {

            // track the fee mmts that we have processed
            FinancialTransaction feeTransaction = billingDetail.getFeeTransaction();
            if(feeTransaction == null) {
                continue;
            }

            // add all of the related billing details so we only create one LC for each set
            DomainEntitySet<BillingDetail> relatedBillingDetails = new DomainEntitySet<BillingDetail>();
            if(feeTransaction.getMoneyMovementTransaction() != null) {
                if(feeTransaction.getMoneyMovementTransaction().getFinancialTransactionCollection()
                                 .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerFeeRefundCredit)).isNotEmpty()) {
                    // fee refund credits and fees can be combined, we cannot handle the credits
                    continue;
                } else if(!processedMMTs.contains(feeTransaction.getMoneyMovementTransaction())) {
                    // if we haven't processed the mmt add it to the collection, otherwise continue on to the other fees
                    processedMMTs.add(feeTransaction.getMoneyMovementTransaction());
                    relatedBillingDetails.addAll(billingDetail.findBillingDetailsAssociatedWithTheSameMMT());
                } else {
                    continue;
                }
            }

            if(relatedBillingDetails.size() == 0) {
                relatedBillingDetails.add(billingDetail);
            }


            boolean foundExistingLiabilityCheck = false;
            for (LiabilityCheck liabilityCheck : pPayrollRun.getLiabilityCheckCollection()) {
                for (LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc : liabilityCheck.getLiabilityCheckBillingDetailAssocCollection()) {
                    if (relatedBillingDetails.contains(liabilityCheckBillingDetailAssoc.getBillingDetail())) {
                        foundExistingLiabilityCheck = true;
                        // we only want to update employer fee types, the other types were handled above
                        if(liabilityCheck.getType() == LiabilityCheckType.EmployerFee) {
                            processResult.merge(updateLiabilityCheck(liabilityCheck, pPayrollRun, billingDetail));
                        }
                        // stop looping through associations
                        break;
                    }
                }
                if(foundExistingLiabilityCheck) {
                    // stop looping through LCs
                    break;
                }
            }

            // must be a new fee
            if(!foundExistingLiabilityCheck) {
                processResult.merge(createLiabilityCheck(pPayrollRun, LiabilityCheckType.EmployerFee, billingDetail));
            }
        }

        return processResult;
    }

    private ProcessResult processVerificationLiabilityCheck(FinancialTransaction pFinancialTransaction) {
        LiabilityCheckDTO liabilityCheckDTO = initializeLiabilityCheckDTO(null, LiabilityCheckType.EmployerFee);
        liabilityCheckDTO.setPeriodEndDate(pFinancialTransaction.getSettlementDate());
        liabilityCheckDTO.setTransactionDate(pFinancialTransaction.getSettlementDate());

        TransactionLine transactionLine = createFeeTransaction();
        transactionLine.setMemo(BillingDetail.MEMOS.ENROLLMENT_FEE);
        if(pFinancialTransaction.getMoneyMovementTransaction() != null) {
            liabilityCheckDTO.setAmount((SpcfMoney)pFinancialTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount().negate());
            transactionLine.setAmount(pFinancialTransaction.getFinancialTransactionAmount());
        } else {
            liabilityCheckDTO.setAmount(SpcfMoney.ZERO);
            transactionLine.setAmount(SpcfMoney.ZERO);
        }
        liabilityCheckDTO.getLiabilityCheckLineDTOs().add(transactionLine.getLiabilityCheckLineDTO());
        return PayrollServices.companyManager.addOrUpdateLiabilityCheck(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), liabilityCheckDTO);
    }

    private ProcessResult processLiabilityChecks(MoneyMovementTransaction pMoneyMovementTransaction) {
        ProcessResult processResult = new ProcessResult();

        FinancialTransaction employerDebit = pMoneyMovementTransaction.getFinancialTransactionCollection()
                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit,
                                                                                          TransactionTypeCode.EmployerTaxDirectDebit));
        // check for DD
        if(employerDebit == null) {
            employerDebit = pMoneyMovementTransaction.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerDdDebit));
        }

        LiabilityCheckType liabilityCheckType;
        switch (employerDebit.getTransactionType().getTransactionTypeCd()) {
            case EmployerTaxDirectDebit:
                liabilityCheckType = LiabilityCheckType.EFTPSDirectDebit;
                break;
            default:
                liabilityCheckType = LiabilityCheckType.EmployerDebit;
        }

        LiabilityCheck existingLiabilityCheck = null;
        PayrollRun payrollRun = employerDebit.getPayrollRun();
        if(payrollRun != null) {
            existingLiabilityCheck =
                    payrollRun.getLiabilityCheckCollection()
                            .findEntity(LiabilityCheck.Type().equalTo(liabilityCheckType));
        }

        if(existingLiabilityCheck == null) {
            processResult.merge(createLiabilityCheck(employerDebit.getPayrollRun(), liabilityCheckType, null));
        } else {
            processResult.merge(updateLiabilityCheck(existingLiabilityCheck, payrollRun, null));
        }

        return processResult;
    }

    private ProcessResult<LiabilityCheck> createLiabilityCheck(PayrollRun pPayrollRun, LiabilityCheckType pLiabilityCheckType, BillingDetail pBillingDetail) {
        LiabilityCheckDTO liabilityCheckDTO = initializeLiabilityCheckDTO(pPayrollRun, pLiabilityCheckType);

        if(pBillingDetail == null) {
            Collection<TransactionLine> transactionLines = getLiabilityCheckLiabilityAmounts(pPayrollRun, liabilityCheckDTO.getLiabilityCheckType());
            for (TransactionLine transactionLine : transactionLines) {
                liabilityCheckDTO.getLiabilityCheckLineDTOs().add(transactionLine.getLiabilityCheckLineDTO());
            }
        }

        updateLiabilityCheckTotalAndTransactionDate(pPayrollRun, liabilityCheckDTO, pBillingDetail, null);

        if(liabilityCheckDTO.hasNonZeroLines()) {
            return PayrollServices.companyManager.addOrUpdateLiabilityCheck(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), liabilityCheckDTO);
        }

        return new ProcessResult<LiabilityCheck>();
    }

    private LiabilityCheckDTO initializeLiabilityCheckDTO(PayrollRun pPayrollRun, LiabilityCheckType pLiabilityCheckType) {
        LiabilityCheckDTO liabilityCheckDTO = new LiabilityCheckDTO();
        liabilityCheckDTO.setSystemModifiedToken(mCompany.getNextToken());
        liabilityCheckDTO.setLiabilityCheckType(pLiabilityCheckType);
        if(pPayrollRun != null) {
            liabilityCheckDTO.setSourcePayrollRunId(pPayrollRun.getSourcePayRunId());
            liabilityCheckDTO.setPeriodEndDate(pPayrollRun.getPaycheckDate());
            liabilityCheckDTO.setTransactionDate(pPayrollRun.getPaycheckDate());
        }

        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setCleared("0");
        qbdtTransactionInfoDTO.setOnService(true);
        qbdtTransactionInfoDTO.setMemo(QBOFX.MEMOS.getCreatedByPayrollServiceMemo(SpcfUtils.convertSpcfCalendarToDate(PSPDate.getPSPTime())));
        qbdtTransactionInfoDTO.setAccountName(mSourceBankAccountName);
        qbdtTransactionInfoDTO.setAgencyName(pLiabilityCheckType == LiabilityCheckType.EFTPSDirectDebit ?  QBOFX.AGENCIES.INTERNAL_REVENUE_SERVICE : QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE);
        liabilityCheckDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        return liabilityCheckDTO;
    }

    private ProcessResult<LiabilityCheck> updateLiabilityCheck(LiabilityCheck pLiabilityCheck, PayrollRun pPayrollRun, BillingDetail pBillingDetail) {
        LiabilityCheckDTO liabilityCheckDTO = PayrollServices.dtoFactory.create(pLiabilityCheck);
        liabilityCheckDTO.setSystemModifiedToken(mCompany.getNextToken());

        if(liabilityCheckDTO.getQBDTTransactionInfoDTO() != null && pBillingDetail == null){
            if(pPayrollRun.hasVoidedOrRecalledPaycheck()) {
                liabilityCheckDTO.getQBDTTransactionInfoDTO().setMemo(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK);
            }
        }

        Collection<TransactionLine> transactionLines = getLiabilityCheckLiabilityAmounts(pPayrollRun, liabilityCheckDTO.getLiabilityCheckType());
        for (TransactionLine transactionLine : transactionLines) {
            liabilityCheckDTO.getLiabilityCheckLineDTOs().add(transactionLine.getLiabilityCheckLineDTO());
        }

        updateLiabilityCheckTotalAndTransactionDate(pPayrollRun, liabilityCheckDTO, pBillingDetail, pLiabilityCheck);

        // add (subtract since the total is negative) in customer items
        DomainEntitySet<LiabilityCheckLine> clientAddedLines =
                pLiabilityCheck.getLiabilityCheckLineCollection().find(LiabilityCheckLine.QbdtTransactionInfo().SystemGenerated().equalTo(false));
        for (LiabilityCheckLine clientAddedLine : clientAddedLines) {
            LiabilityCheckLineDTO liabilityCheckLineDTO = PayrollServices.dtoFactory.create(clientAddedLine);
            liabilityCheckDTO.getLiabilityCheckLineDTOs().add(liabilityCheckLineDTO);
            if(liabilityCheckLineDTO.getAmount() != null) {
                liabilityCheckDTO.setAmount(new SpcfMoney(liabilityCheckDTO.getAmount().subtract(liabilityCheckLineDTO.getAmount())));
            }
        }

        return PayrollServices.companyManager.addOrUpdateLiabilityCheck(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), liabilityCheckDTO);
    }

    private void updateLiabilityCheckTotalAndTransactionDate(PayrollRun pPayrollRun, LiabilityCheckDTO pLiabilityCheckDTO, BillingDetail pBillingDetail, LiabilityCheck pLiabilityCheck) {

        MoneyMovementTransaction moneyMovementTransaction = null;
        if(pBillingDetail == null) {
            Criterion<FinancialTransaction> debitCriterion = FinancialTransaction.DebitBankAccountType().equalTo(BankAccountOwnerType.Company)
                                                                                 .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided))
                                                                                 .And(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Impound))
                                                                                 .And(FinancialTransaction.MoneyMovementTransaction().isNotNull());
            if(pLiabilityCheckDTO.getLiabilityCheckType() == LiabilityCheckType.EmployerDebit) {
                debitCriterion = debitCriterion.And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH));
            } else {
                debitCriterion = debitCriterion.And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.EFTPSDirectDebit));
            }

            DomainEntitySet<FinancialTransaction> debitsTransactions =
                    pPayrollRun.getFinancialTransactionCollection().find(debitCriterion);
            for (FinancialTransaction financialTransaction : debitsTransactions) {
	            	if(moneyMovementTransaction == null) {
	            		moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
	            		pLiabilityCheckDTO.setTransactionDate(financialTransaction.getSettlementDate());
	            	} else {
	            		break;
	            	}
            }
        } else {
            if(pLiabilityCheck != null) {
                for (LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc : pLiabilityCheck.getLiabilityCheckBillingDetailAssocCollection()) {
                    if(moneyMovementTransaction == null &&
                            liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction() != null &&
                            liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getMoneyMovementTransaction() != null) {
                        moneyMovementTransaction = liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getMoneyMovementTransaction();
                        pLiabilityCheckDTO.setTransactionDate(liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getSettlementDate());
                    } else if(liabilityCheckBillingDetailAssoc.getBillingDetail().getTaxTransaction() != null &&
                            liabilityCheckBillingDetailAssoc.getBillingDetail().getTaxTransaction().getMoneyMovementTransaction() != null) {
                        moneyMovementTransaction = liabilityCheckBillingDetailAssoc.getBillingDetail().getTaxTransaction().getMoneyMovementTransaction();
                        pLiabilityCheckDTO.setTransactionDate(liabilityCheckBillingDetailAssoc.getBillingDetail().getTaxTransaction().getSettlementDate());
                        break;
                    }
                }
            } else {
                if(pBillingDetail.getFeeTransaction() != null && pBillingDetail.getFeeTransaction().getMoneyMovementTransaction() != null) {
                    moneyMovementTransaction = pBillingDetail.getFeeTransaction().getMoneyMovementTransaction();
                    pLiabilityCheckDTO.setTransactionDate(pBillingDetail.getFeeTransaction().getSettlementDate());
                }
            }
        }

        // if all of the paycheck are recalled we still charge the transmission fee, find the mmt
        if(pLiabilityCheck != null && pBillingDetail == null && moneyMovementTransaction == null) {
            for (LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc : pLiabilityCheck.getLiabilityCheckBillingDetailAssocCollection()) {
                if(liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction() != null &&
                        liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getMoneyMovementTransaction() != null) {
                    moneyMovementTransaction = liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getMoneyMovementTransaction();
                    pLiabilityCheckDTO.setTransactionDate(liabilityCheckBillingDetailAssoc.getBillingDetail().getFeeTransaction().getSettlementDate());
                    break;
                }
            }
        }

        addFeeTransactionLines(pLiabilityCheckDTO, pPayrollRun, moneyMovementTransaction);

        if(moneyMovementTransaction != null) {
        		pLiabilityCheckDTO.setAmount(new SpcfMoney(moneyMovementTransaction.getMoneyMovementTransactionAmount().negate()));
            if(moneyMovementTransaction.isOffloadPending() && pLiabilityCheckDTO.getLiabilityCheckType() == LiabilityCheckType.EmployerDebit) {
                pLiabilityCheckDTO.getQBDTTransactionInfoDTO().setAccountName(mSourceBankAccountName);
            }
        } else {
            pLiabilityCheckDTO.setAmount(SpcfMoney.ZERO);
        }

        SpcfDecimal totalAmount = SpcfMoney.ZERO;
        for (LiabilityCheckLineDTO liabilityCheckLineDTO : pLiabilityCheckDTO.getLiabilityCheckLineDTOs()) {
            if(liabilityCheckLineDTO.getAmount() != null) {
                totalAmount = totalAmount.add(liabilityCheckLineDTO.getAmount());
            }
        }

        if(!totalAmount.equals(pLiabilityCheckDTO.getAmount().negate())) {
            String errorMessage = "Liability check total does not match the sum of the lines for company:" + pPayrollRun.getCompany().getSourceCompanyId() + " txid: " + pLiabilityCheckDTO.getSourceId();
            if(Application.isProdEnvironment()) {
                Application.getLogger(GenerateLiabilityCheckCore.class).error(errorMessage);
            } else {
                throw new RuntimeException(errorMessage);
            }
        }

        // if liability check ends up negative, zero out all of the lines so that QB does not get an inconsistent data error
        if(moneyMovementTransaction != null && moneyMovementTransaction.getMoneyMovementTransactionAmount().isLessThan(SpcfMoney.ZERO)) {
            for (LiabilityCheckLineDTO liabilityCheckLineDTO : pLiabilityCheckDTO.getLiabilityCheckLineDTOs()) {
                liabilityCheckLineDTO.setAmount(SpcfMoney.ZERO);
            }
            pLiabilityCheckDTO.setAmount(SpcfMoney.ZERO);
        }
    }

    public Collection<TransactionLine> getLiabilityCheckLiabilityAmounts(PayrollRun pPayrollRun, LiabilityCheckType pLiabilityCheckType) {
        switch (pLiabilityCheckType) {
            case EmployerDebit:
                return findEmployerDebitTransactionLines(pPayrollRun);
            case EFTPSDirectDebit:
                return findEFTPSDirectDebitTransactionLines(pPayrollRun);
        }

        return new ArrayList<TransactionLine>();
    }

    private Collection<TransactionLine> findEmployerDebitTransactionLines(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                                                                                    TransactionTypeCode.AgencyTaxDebit,
                                                                                    TransactionTypeCode.AgencyTaxOverpayment)
                              .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled)));

        Map<String, TransactionLine> transactionLineMap = new HashMap<String, TransactionLine>();
        TransactionLine transactionLine;
        for (FinancialTransaction financialTransaction : financialTransactions) {
            if((financialTransaction.getCurrentTransactionState().getTransactionStateCd().in(TransactionStateCode.Returned, TransactionStateCode.Voided) && financialTransaction.getAssociatedTransactionsCollection().size() > 0)) {
                // skip returned and voided transactions that have been recreated
                continue;
            }
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()) {
                case AgencyTaxCredit:
                    transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().add(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case AgencyTaxDebit:
                    if(financialTransaction.getRelatedTransactionsCollection().size() == 0) {
                        // adds to excess funds line
                        transactionLine = transactionLineMap.get(EXCESS_FUNDS_TRANSACTION_ID);
                        if(transactionLine == null) {
                            transactionLine = createExcessFundsTransaction();
                            transactionLineMap.put(EXCESS_FUNDS_TRANSACTION_ID, transactionLine);
                        }
                        transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().add(financialTransaction.getFinancialTransactionAmount()));

                        // subtracts from payroll item line
                        transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                        if(transactionLine == null) {
                            transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                            transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                        }
                        transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    }
                    break;
                case AgencyTaxOverpayment:
                    transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    transactionLine.setMemo(QBOFX.MEMOS.VOID.OVERPAYMENT_TO_TAXING_AGENCY);
                    transactionLine.setOverPaymentToAgency(true);
                    break;
            }
        }

        // employer transactions do not get recreated if returned
        financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCreditApplied,
                                                                                    TransactionTypeCode.EmployerTaxOverpaymentApplied,
                                                                                    TransactionTypeCode.EmployerSUITaxCollection)
                              .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()) {
                case EmployerTaxCreditApplied:
                    transactionLine = transactionLineMap.get(EXCESS_FUNDS_APPLIED_TRANSACTION_ID);
                    if(transactionLine == null) {
                        transactionLine = createExcessFundsAppliedTransaction();
                        transactionLineMap.put(EXCESS_FUNDS_APPLIED_TRANSACTION_ID, transactionLine);
                    }
                    // line should be negative
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case EmployerTaxOverpaymentApplied:
                    transactionLine = transactionLineMap.get("-" + financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLine.setMemo(QBOFX.MEMOS.DEBIT_REDUCED.APPLIED_OVERPAID_TAX_FUNDS);
                        transactionLineMap.put("-" + financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    // line should be negative
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case EmployerSUITaxCollection:
                    for (FinancialTransaction associatedTransaction : financialTransaction.getAssociatedTransactionsCollection()) {
                        if(TransactionTypeCode.EmployerSUITaxReceivable == associatedTransaction.getTransactionType().getTransactionTypeCd()) {
                            transactionLine = transactionLineMap.get(associatedTransaction.getLaw().getLawId());
                            if(transactionLine == null) {
                                transactionLine = createPayrollItemTransaction(associatedTransaction.getLaw());
                                transactionLine.setRequiresAccountName(true);
                                transactionLineMap.put(associatedTransaction.getLaw().getLawId(), transactionLine);
                            }
                            transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().add(associatedTransaction.getFinancialTransactionAmount()));
                        } else if(TransactionTypeCode.EmployerSUITaxPayable == associatedTransaction.getTransactionType().getTransactionTypeCd()) {
                            // subtracts from payroll item line
                            transactionLine = transactionLineMap.get(associatedTransaction.getLaw().getLawId());
                            if(transactionLine == null) {
                                transactionLine = createPayrollItemTransaction(associatedTransaction.getLaw());
                                transactionLine.setRequiresAccountName(true);
                                transactionLineMap.put(associatedTransaction.getLaw().getLawId(), transactionLine);
                            }
                            transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(associatedTransaction.getFinancialTransactionAmount()));
                        }
                    }
                    break;
            }
        }

        // add transaction lines for cancelled transactions
        financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                        TransactionTypeCode.EmployerTaxOverpaymentApplied)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Cancelled)));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
            if(transactionLine == null) {
                transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
            }
        }

        // dd transaction line
        financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                                           .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                                                                     .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionLine = createDDTransaction();
            transactionLine.setAmount(financialTransaction.getFinancialTransactionAmount());
            transactionLineMap.put(DD_TRANSACTION_ID, transactionLine);
        }

        // check for canceled dd debit
        if(financialTransactions.isEmpty()) {
            financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                                               .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                                                                         .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Cancelled)));
            if(!financialTransactions.isEmpty()) {
                transactionLine = createDDTransaction();
                transactionLine.setAmount(SpcfMoney.ZERO);
                transactionLineMap.put(DD_TRANSACTION_ID, transactionLine);
            }
        }

        return mapPayrollItems(pPayrollRun, transactionLineMap);
    }

    private void addFeeTransactionLines(LiabilityCheckDTO pLiabilityCheckDTO, PayrollRun pPayrollRun, MoneyMovementTransaction pMoneyMovementTransaction) {
        // All sales tax jurisdictions will be grouped into jurisdictions
        // and a single tax amount returned for each.
        DomainEntitySet<FinancialTransaction> salesTaxTransactionSet = new DomainEntitySet<FinancialTransaction>();
        for (BillingDetail billingDetailItem : pPayrollRun.getBillingDetailCollection()) {
            if((billingDetailItem.getFeeTransaction() == null && pLiabilityCheckDTO.getLiabilityCheckType() == LiabilityCheckType.EmployerDebit) ||
                    pLiabilityCheckDTO.getAssociatedBillingDetails().contains(billingDetailItem) ||
                    (billingDetailItem.getFeeTransaction() != null &&
                            billingDetailItem.getFeeTransaction().getMoneyMovementTransaction() != null &&
                            billingDetailItem.getFeeTransaction().getMoneyMovementTransaction().equals(pMoneyMovementTransaction)) ||
                    (billingDetailItem.getTaxTransaction() != null &&
                            billingDetailItem.getTaxTransaction().getMoneyMovementTransaction() != null &&
                            billingDetailItem.getTaxTransaction().getMoneyMovementTransaction().equals(pMoneyMovementTransaction))) {

                if(billingDetailItem.getTaxTransaction() != null) {
                    salesTaxTransactionSet.add(billingDetailItem.getTaxTransaction());
                }
                SpcfMoney feeAmount = SpcfMoney.ZERO;
                if(billingDetailItem.getFeeTransaction() != null &&
                        billingDetailItem.getFeeTransaction().getCurrentTransactionState().getTransactionStateCd().notIn(TransactionStateCode.Voided,
                                                                                                                         TransactionStateCode.Cancelled)) {
                    feeAmount = billingDetailItem.getFeeTransaction().getFinancialTransactionAmount();
                }

                TransactionLine transactionLine = createFeeTransaction();
                transactionLine.setAmount(feeAmount);
                transactionLine.setMemo(billingDetailItem.getMemo());
                transactionLine.setIsFee(true);
                pLiabilityCheckDTO.getLiabilityCheckLineDTOs().add(transactionLine.getLiabilityCheckLineDTO());
                if(!pLiabilityCheckDTO.getAssociatedBillingDetails().contains(billingDetailItem)) {
                    pLiabilityCheckDTO.getAssociatedBillingDetails().add(billingDetailItem);
                }
            }

        }

        TransactionLine salesTaxTransactionLine = null;
        for (FinancialTransaction financialTransaction : salesTaxTransactionSet) {
            if(salesTaxTransactionLine == null) {
                salesTaxTransactionLine = createSalesTaxTransaction();
                // the jurisdictions on the billing detail are numbers, so we will always use the legal address state...
                salesTaxTransactionLine.setMemo(QBOFX.MEMOS.getSalesTaxMemo(mLegalAddressState));
                salesTaxTransactionLine.setAmount(SpcfMoney.ZERO);
            }

            if(financialTransaction.getCurrentTransactionState().getTransactionStateCd().notIn(TransactionStateCode.Voided,
                                                                                               TransactionStateCode.Cancelled)) {
                salesTaxTransactionLine.setAmount((SpcfMoney)salesTaxTransactionLine.getAmount().add(financialTransaction.getFinancialTransactionAmount()));
            }

        }

        if(salesTaxTransactionLine != null) {
            pLiabilityCheckDTO.getLiabilityCheckLineDTOs().add(salesTaxTransactionLine.getLiabilityCheckLineDTO());
        }
    }

    private Collection<TransactionLine> findEFTPSDirectDebitTransactionLines(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyDirectCredit,
                                                                                    TransactionTypeCode.AgencyDirectOverpayment,
                                                                                    TransactionTypeCode.AgencyDirectDebit,
                                                                                    TransactionTypeCode.EmployerTaxDirectOverpaymentApplied)
                                          .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Returned, TransactionStateCode.Voided)));

        Map<String, TransactionLine> transactionLineMap = new HashMap<String, TransactionLine>();
        TransactionLine transactionLine;
        for (FinancialTransaction financialTransaction : financialTransactions) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()) {
                case AgencyDirectCredit:
                    transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().add(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case AgencyDirectOverpayment:
                    transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    transactionLine.setMemo(QBOFX.MEMOS.VOID.OVERPAYMENT_TO_TAXING_AGENCY);
                    transactionLine.setOverPaymentToAgency(true);
                    break;
                case EmployerTaxCreditApplied:
                    transactionLine = transactionLineMap.get(EXCESS_FUNDS_APPLIED_TRANSACTION_ID);
                    if(transactionLine == null) {
                        transactionLine = createExcessFundsAppliedTransaction();
                        transactionLineMap.put(EXCESS_FUNDS_APPLIED_TRANSACTION_ID, transactionLine);
                    }
                    // line should be negative
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case EmployerTaxDirectOverpaymentApplied:
                    transactionLine = transactionLineMap.get("-" + financialTransaction.getLaw().getLawId());
                    if(transactionLine == null) {
                        transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                        transactionLine.setMemo(QBOFX.MEMOS.DEBIT_REDUCED.APPLIED_OVERPAID_TAX_FUNDS);
                        transactionLineMap.put("-" + financialTransaction.getLaw().getLawId(), transactionLine);
                    }
                    // line should be negative
                    transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    break;
                case AgencyDirectDebit:
                    if(financialTransaction.getRelatedTransactionsCollection().size() == 0) {
                        transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
                        if(transactionLine == null) {
                            transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                            transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
                        }
                        transactionLine.setAmount((SpcfMoney) transactionLine.getAmount().subtract(financialTransaction.getFinancialTransactionAmount()));
                    }
                    break;
            }
        }

        // check for overpayments created after payment submitted
        financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxOverpayment)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Returned, TransactionStateCode.Voided)));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
            if(transactionLine != null) {
                transactionLine.setMemo(QBOFX.MEMOS.VOID.OVERPAYMENT_TO_TAXING_AGENCY);
                transactionLine.setOverPaymentToAgency(true);
            }
        }

        // add transaction lines for cancelled transactions
        financialTransactions = pPayrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyDirectCredit,
                        TransactionTypeCode.EmployerTaxDirectOverpaymentApplied)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Cancelled)));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transactionLine = transactionLineMap.get(financialTransaction.getLaw().getLawId());
            if(transactionLine == null) {
                transactionLine = createPayrollItemTransaction(financialTransaction.getLaw());
                transactionLineMap.put(financialTransaction.getLaw().getLawId(), transactionLine);
            }
        }

        return mapPayrollItems(pPayrollRun, transactionLineMap);
    }

    private TransactionLine createPayrollItemTransaction(Law pLaw) {
        TransactionLine transactionLine = new TransactionLine();
        transactionLine.setAmount(SpcfMoney.ZERO);
        transactionLine.setLawId(pLaw.getLawId());
        transactionLine.setSystemGenerated(true);
        return transactionLine;
    }

    private TransactionLine createDDTransaction() {
        TransactionLine transactionLine = new TransactionLine();
        transactionLine.setAmount(SpcfMoney.ZERO);
        transactionLine.setIsDirectDeposit(true);
        transactionLine.setSystemGenerated(true);
        return transactionLine;
    }

    private TransactionLine createFeeTransaction() {
        TransactionLine transactionLine = new TransactionLine();
        transactionLine.setAccountName(mFeeCOA);
        transactionLine.setAmount(SpcfMoney.ZERO);
        transactionLine.setIsFee(true);
        transactionLine.setSystemGenerated(true);
        return transactionLine;
    }

    private TransactionLine createSalesTaxTransaction() {
        TransactionLine transactionLine = new TransactionLine();
        transactionLine.setAccountName(mSalesTaxCOA);
        transactionLine.setAmount(SpcfMoney.ZERO);
        transactionLine.setIsSalesTax(true);
        transactionLine.setSystemGenerated(true);
        return transactionLine;
    }

    private TransactionLine createExcessFundsTransaction() {
        TransactionLine assetAccountExcessTransaction = new TransactionLine();
        assetAccountExcessTransaction.setAccountName(QBOFX.ACCOUNTS.ASSET_ACCOUNT_NAME);
        assetAccountExcessTransaction.setMemo(QBOFX.MEMOS.VOID.EXCESS_TAX_RESULT_OF_VOID);
        assetAccountExcessTransaction.setSystemGenerated(true);
        assetAccountExcessTransaction.setAmount(SpcfMoney.ZERO);
        return assetAccountExcessTransaction;
    }

    public TransactionLine createExcessFundsAppliedTransaction() {
        TransactionLine assetAccountAppliedTransaction = new TransactionLine();
        assetAccountAppliedTransaction.setAmount(SpcfMoney.ZERO);
        assetAccountAppliedTransaction.setAccountName(QBOFX.ACCOUNTS.ASSET_ACCOUNT_NAME);
        assetAccountAppliedTransaction.setMemo(QBOFX.MEMOS.DEBIT_REDUCED.APPLIED_EXCESS_TAX_FUNDS);
        assetAccountAppliedTransaction.setSystemGenerated(true);
        return assetAccountAppliedTransaction;
    }

    private Collection<TransactionLine> mapPayrollItems(PayrollRun pPayrollRun, Map<String, TransactionLine> pTransactionLineMap) {
        Set<TransactionLine> returnList = new HashSet<TransactionLine>();
        boolean isManualAdjustment = isPayrollRunManualAdjustment(pPayrollRun);

        String quarterYear = "Q" + CalendarUtils.getQuarterAsInt(pPayrollRun.getPaycheckDate()) + " " + pPayrollRun.getPaycheckDate().getYear();

        // combine excess funds transactions
        // we only want one or the other on the liability check
        TransactionLine assetAccountExcessTransaction = pTransactionLineMap.get(EXCESS_FUNDS_TRANSACTION_ID);
        TransactionLine assetAccountAppliedTransaction = pTransactionLineMap.get(EXCESS_FUNDS_APPLIED_TRANSACTION_ID);
        if (assetAccountExcessTransaction != null && assetAccountAppliedTransaction == null) {
            returnList.add(assetAccountExcessTransaction);
        } else if (assetAccountExcessTransaction == null && assetAccountAppliedTransaction != null) {
            returnList.add(assetAccountAppliedTransaction);
        } else if (assetAccountExcessTransaction != null && assetAccountAppliedTransaction != null) {
            int compare = SpcfUtils.compareSpcfDecimalTo(assetAccountExcessTransaction.getAmount(), assetAccountAppliedTransaction.getAmount().negate());
            switch(compare) {
                // equal
                case 0:
                    // do not add the transactions to the return list
                    break;
                // excess is greater
                case 1:
                    assetAccountExcessTransaction.setAmount(new SpcfMoney(assetAccountExcessTransaction.getAmount().add(assetAccountAppliedTransaction.getAmount())));
                    returnList.add(assetAccountExcessTransaction);
                    break;
                // applied is greater
                case -1:
                    assetAccountAppliedTransaction.setAmount(new SpcfMoney(assetAccountAppliedTransaction.getAmount().add(assetAccountExcessTransaction.getAmount())));
                    returnList.add(assetAccountAppliedTransaction);
                    break;
            }
        }        

        List<String> lawIds = new ArrayList<String>();
        for (String key : pTransactionLineMap.keySet()) {
            Integer lawId;
            try {
                lawId = Math.abs(Integer.parseInt(key));
            } catch (NumberFormatException e) {
                // if the key is not an int skip it
                continue;
            }
            if(!lawIds.contains(lawId.toString())) {
                lawIds.add(lawId.toString());
            }            
        }

        // update memos for voided paychecks and add payroll item id
        // also add transaction lines for voided checks with DD
        TransactionLine transactionLine;
        for (Paycheck paycheck : pPayrollRun.getPaycheckCollection()) {
            for (Tax tax : paycheck.getTaxCollection()) {
                if(tax.getCompanyLaw() != null) {
                    transactionLine = pTransactionLineMap.get(tax.getCompanyLaw().getLaw().getLawId());
                    if(transactionLine != null) {
                        if(paycheck.isVoidedOrRecalled() && !transactionLine.isOverPaymentToAgency()) {
                            transactionLine.setMemo(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK);
                        }
                        if(transactionLine.getLawId() == null) {
                            transactionLine.setLawId(tax.getCompanyLaw().getLaw().getLawId());
                            returnList.add(transactionLine);
                        }
                    }

                    // check for overpayment applied
                    transactionLine = pTransactionLineMap.get("-" + tax.getCompanyLaw().getLaw().getLawId());
                    if(transactionLine != null && transactionLine.getLawId() == null) {
                        transactionLine.setLawId(tax.getCompanyLaw().getLaw().getLawId());
                        returnList.add(transactionLine);
                    }
                }
            }

            // DD voids
            if(paycheck.isVoided()) {
                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplits()) {
                    if(pTransactionLineMap.containsKey(DD_TRANSACTION_ID)) {
                        if(pTransactionLineMap.containsKey(paycheck.getDDEmployee().getId().toString())) {
                            transactionLine = pTransactionLineMap.get(paycheck.getDDEmployee().getId().toString());
                            transactionLine.setAmount((SpcfMoney)transactionLine.getAmount().add(paycheckSplit.getPaycheckSplitAmount()));
                        } else {
                            transactionLine = createDDTransaction();
                            transactionLine.setAmount(paycheckSplit.getPaycheckSplitAmount());
                            transactionLine.setMemo(QBOFX.MEMOS.VOID.DD_OVERPAYMENT + paycheck.getDDEmployee().getFirstMiddleLastName().replaceAll(NULL_EMP_NAME_STR, "").trim());
                            pTransactionLineMap.put(paycheck.getDDEmployee().getId().toString(), transactionLine);
                        }

                        // subtract overpayment from dd total
                        transactionLine = pTransactionLineMap.get(DD_TRANSACTION_ID);
                        if(transactionLine != null) {
                            transactionLine.setAmount((SpcfMoney)transactionLine.getAmount().subtract(paycheckSplit.getPaycheckSplitAmount()));
                            transactionLine.setMemo(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK);
                        }
                    }
                }                
            } else if(paycheck.isRecalled()) {
                if (!paycheck.getPaycheckSplits().isEmpty() && pTransactionLineMap.containsKey(DD_TRANSACTION_ID)) {
                    transactionLine = pTransactionLineMap.get(DD_TRANSACTION_ID);
                    if(transactionLine != null && transactionLine.getMemo() == null) {
                        transactionLine.setMemo(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK);
                    }
                }
            }
        }

        for (LiabilityAdjustment liabilityAdjustment : pPayrollRun.getLiabilityAdjustmentCollection()) {
            if(liabilityAdjustment.getCompanyLaw() != null) {
                transactionLine = pTransactionLineMap.get(liabilityAdjustment.getCompanyLaw().getLaw().getLawId());
                if(transactionLine != null) {
                    if(transactionLine.getLawId() == null) {
                        transactionLine.setLawId(liabilityAdjustment.getCompanyLaw().getLaw().getLawId());
                        returnList.add(transactionLine);
                    }
                }

                // check for overpayment applied
                transactionLine = pTransactionLineMap.get("-" + liabilityAdjustment.getCompanyLaw().getLaw().getLawId());
                if(transactionLine != null && transactionLine.getLawId() == null) {
                    transactionLine.setLawId(liabilityAdjustment.getCompanyLaw().getLaw().getLawId());
                    returnList.add(transactionLine);
                }
            }
        }

        // update any ids that did not have liability associated with them        
        for (TransactionLine line : pTransactionLineMap.values()) {
            if(line.isDirectDeposit()) {
                if(pPayrollRun.getCompany().isCompanyOnService(ServiceCode.Tax)) {
                    CompanyPayrollItem ddCompanyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(pPayrollRun.getCompany());
                    if(ddCompanyPayrollItem != null) {
                        line.setPayrollItemId(ddCompanyPayrollItem.getSourcePayrollItemId());
                    }
                }
                returnList.add(line);
            } else if(line.isFee()) {
                returnList.add(line);
            } else if(line.isSalesTax()) {
                returnList.add(line);
            } else if(line.getPayrollItemId() == null && line.getLawId() != null) {
                CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(pPayrollRun.getCompany(), line.getLawId());
                if(companyLaw == null) {
                    // find the company law in the cache
                    Law law = Application.find(Law.class, Law.LawId().equalTo(line.getLawId())).get(0);
                    CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pPayrollRun.getCompany(), law.getPaymentTemplate().getAgency().getAgencyId());
                    if(companyAgency != null) {
                        companyLaw = CompanyLaw.findCompanyLaw(companyAgency, law);
                    }
                }

                if(companyLaw != null) {
                    if (isManualAdjustment || line.isRequiresAccountName()) {
                        line.setAccountName(companyLaw.getExpenseAccount());
                        line.setMemo(quarterYear + QBOFX.MEMOS.getExpenseAccountForMemo(companyLaw.getSourceDescription()));
                    } else {
                        line.setPayrollItemId(companyLaw.getSourceId());
                    }
                    returnList.add(line);
                }
            }
        }

        return returnList;
    }

    private boolean isPayrollRunManualAdjustment(PayrollRun pPayrollRun) {
        if(pPayrollRun == null) {
            return false;
        }

        boolean isManualAdjustment = pPayrollRun.getLiabilityAdjustmentCollection().size() > 0;

        for (LiabilityAdjustment liabilityAdjustment : pPayrollRun.getLiabilityAdjustmentCollection()) {
            isManualAdjustment = isManualAdjustment && liabilityAdjustment.getQbdtTransactionInfo() == null;
        }

        return isManualAdjustment;
    }

    private static class TransactionLine {
        private SpcfMoney mAmount;
        private String mPayrollItemId;
        private String mLawId;
        private String mAccountName;
        private String mMemo;
        private boolean mIsDirectDeposit;
        private boolean mIsFee = false;
        private boolean mIsSalesTax = false;
        private SpcfMoney mTaxableWages;
        private SpcfMoney mTotalWages;
        private boolean mOverPaymentToAgency = false;
        private boolean mSystemGenerated = false;
        private boolean mRequiresAccountName = false;        

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public void setAmount(SpcfMoney pAmount) {
            mAmount = pAmount;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public void setPayrollItemId(String pPayrollItemId) {
            mPayrollItemId = pPayrollItemId;
        }

        public String getLawId() {
            return mLawId;
        }

        public void setLawId(String pLawId) {
            mLawId = pLawId;
        }

        public void setAccountName(String pAccountName) {
            mAccountName = pAccountName;
        }

        public String getMemo() {
            return mMemo;
        }

        public void setMemo(String pMemo) {
            mMemo = pMemo;
        }

        public boolean isDirectDeposit() {
            return mIsDirectDeposit;
        }

        public void setIsDirectDeposit(boolean pIsDirectDeposit) {
            mIsDirectDeposit = pIsDirectDeposit;
        }

        public boolean isFee() {
            return mIsFee;
        }

        public void setIsFee(boolean pIsFee) {
            mIsFee = pIsFee;
        }

        public boolean isSalesTax() {
            return mIsSalesTax;
        }

        public void setIsSalesTax(boolean pIsSalesTax) {
            mIsSalesTax = pIsSalesTax;
        }

        public SpcfMoney getTaxableWages() {
            return mTaxableWages;
        }

        public void setTaxableWages(SpcfMoney pTaxableWages) {
            mTaxableWages = pTaxableWages;
        }

        public SpcfMoney getTotalWages() {
            return mTotalWages;
        }

        public void setTotalWages(SpcfMoney pTotalWages) {
            mTotalWages = pTotalWages;
        }

        public boolean isOverPaymentToAgency() {
            return mOverPaymentToAgency;
        }

        public void setOverPaymentToAgency(boolean pOverPaymentToAgency) {
            mOverPaymentToAgency = pOverPaymentToAgency;
        }

        public void setSystemGenerated(boolean pSystemGenerated) {
            mSystemGenerated = pSystemGenerated;
        }

        public boolean isRequiresAccountName() {
            return mRequiresAccountName;
        }

        public void setRequiresAccountName(boolean pRequiresAccountName) {
            mRequiresAccountName = pRequiresAccountName;
        }

        public LiabilityCheckLineDTO getLiabilityCheckLineDTO() {
            LiabilityCheckLineDTO liabilityCheckLineDTO = new LiabilityCheckLineDTO();
            liabilityCheckLineDTO.setAmount(mAmount);
            liabilityCheckLineDTO.setCompanyPayrollItemId(mPayrollItemId);
            liabilityCheckLineDTO.setFeeLine(mIsFee);

            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            qbdtTransactionInfoDTO.setAccountName(mAccountName);
            qbdtTransactionInfoDTO.setMemo(mMemo);
            qbdtTransactionInfoDTO.setIsDirectDeposit(mIsDirectDeposit);
            qbdtTransactionInfoDTO.setSystemGenerated(mSystemGenerated);
            liabilityCheckLineDTO.setQBDTTransactionInfo(qbdtTransactionInfoDTO);

            return liabilityCheckLineDTO;
        }
    }
}
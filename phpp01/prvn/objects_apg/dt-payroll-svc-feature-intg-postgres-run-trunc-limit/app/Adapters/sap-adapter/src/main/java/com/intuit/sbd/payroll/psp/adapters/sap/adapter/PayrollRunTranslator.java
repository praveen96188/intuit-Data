/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/PayrollRunTranslator.java#4 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.payroll.agency.api.IAgency;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.dao.LawData;
import com.intuit.payroll.agency.impl.RulesInfo;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * PayrollRunTranslator
 *
 * @author Joe Warmelink
 */
public class PayrollRunTranslator {
    private static final String LS_EMPLOYER = "Employer - ";
    private static final SpcfLogger logger = PayrollServices.getLogger(PayrollRunTranslator.class);

    public static SAPPayrollRun getSAPPayrollRunFromDomainEntity(PayrollRun pPayrollRunDE,
                                                                 BankAccount pBankAccount,
                                                                 Collection<ActionEvent> pActionEvents,
                                                                 Date expectedResolutionDate,
                                                                 boolean hasVoidedPaycheck,
                                                                 double payrollAchAmount,
                                                                 CompanyEventDetail manualNoteDetail,
                                                                 boolean isSuperseded) {
        SAPPayrollRun sapPayrollRun = new SAPPayrollRun();
        sapPayrollRun.setPayrollType(pPayrollRunDE.getPayrollRunType().toString());
        sapPayrollRun.setCompanyId(pPayrollRunDE.getCompany().getSourceCompanyId());
        sapPayrollRun.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(pPayrollRunDE.getPaycheckDate()));
        sapPayrollRun.setPaycheckSettlementDate(
                SAPTranslator.getDateFromSpcfCalendar(pPayrollRunDE.getPaycheckSettlementDate()));
        sapPayrollRun.setPayrollNetAmount(payrollAchAmount);
        sapPayrollRun.setPayrollRunDate(
                SAPTranslator.getDateFromSpcfCalendar(pPayrollRunDE.getPayrollRunDate()));
        sapPayrollRun.setPayrollRunStatus(pPayrollRunDE.getPayrollRunStatus());
        sapPayrollRun.setCollectionStage(pPayrollRunDE.getCollectionStageCd());

        sapPayrollRun.setWireExpectedDate(SAPTranslator.getDateFromSpcfCalendar(pPayrollRunDE.getWireExpectedDate()));
        sapPayrollRun.setSourcePayRunId(pPayrollRunDE.getSourcePayRunId());
        sapPayrollRun.setSourceSystemId(pPayrollRunDE.getCompany().getSourceSystemCd().toString());
        sapPayrollRun.setStatusEffectiveDate(
                SAPTranslator.getDateFromSpcfCalendar(pPayrollRunDE.getStatusEffectiveDate()));
        if (pBankAccount != null) {
            sapPayrollRun.setBankAccount(CompanyTranslator.getSAPCompanyBankAccountFromDomainEntity(pBankAccount, PIIMask.authenticatedUserCanViewFullBankAccountNumbers()));
        }
        sapPayrollRun.setId(pPayrollRunDE.getId().toString());

        sapPayrollRun.setActionCollection(
                getSAPActionEventsFromDomainEntities(pActionEvents));

        sapPayrollRun.setExpectedResolutionDate(expectedResolutionDate);
        sapPayrollRun.setHasVoidedPaycheck(hasVoidedPaycheck);

        sapPayrollRun.setHPDE(pPayrollRunDE.isHistoricalPayroll());

        if (manualNoteDetail != null) {
            sapPayrollRun.setManualCreator(SAPTranslator.getUserNameFromUserID(manualNoteDetail.getCompanyEvent().getCreatorId()));
            sapPayrollRun.setManualNote(manualNoteDetail.getValue());
        }

        sapPayrollRun.setIsSuperseded(isSuperseded);
        sapPayrollRun.setIsBackdated(pPayrollRunDE.isBackDated());

        sapPayrollRun.setHasDDTransactions(pPayrollRunDE.getDdDebit() != null);
        sapPayrollRun.setHasTaxTransactions(pPayrollRunDE.getTaxDebit() != null);

        if (pPayrollRunDE.getPayrollRunType() == PayrollType.FeeOnly) {
            DomainEntitySet<FinancialTransaction> financialTransactions = pPayrollRunDE.getFinancialTransactions(TransactionTypeCode.EmployerFeeDebit)
                                                                                       .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd()
                                                                                                                 .in(TransactionStateCode.Created, TransactionStateCode.Executed, TransactionStateCode.Completed));
            SpcfMoney feeAmount = SpcfMoney.ZERO;
            for (FinancialTransaction financialTransaction : financialTransactions) {
                feeAmount = new SpcfMoney(feeAmount.add(financialTransaction.getFinancialTransactionAmount()));
            }
            sapPayrollRun.setFeeOnlyAmount(SAPTranslator.getDoubleFromSpcfMoney(feeAmount));
        }
        // adding txn number in this method to reduce the impact of the changes.
        String employerDDDebitTxnNumber = null;
        // added to make sure the value in popup is displayed only for Employee Transaction
        if (pPayrollRunDE.getPayrollRunType() == PayrollType.Regular) {
            DomainEntitySet<FinancialTransaction> employerDDDebitTxns =
                    pPayrollRunDE.getFinancialTransactions(TransactionTypeCode.EmployerDdDebit)
                            .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd()
                                    .notIn(TransactionStateCode.Cancelled));
            if(employerDDDebitTxns.isNotEmpty()) {
                FinancialTransaction ft = employerDDDebitTxns.get(0);
                try{

                    if(null != ft.getMoneyMovementTransaction()) {
                        employerDDDebitTxnNumber = ft.getMoneyMovementTransaction().getTransactionNumber();
                    }

                }catch (ObjectNotFoundException mmtEx){
                    //Added this code since there are many mmt's (between 2009-07-01 and 2009-07-02)  where mmt id is present in FT table
                    // but the actual data is missing in the mmt table which is resulting in the Hibernate object not found exception.
                    // To fix this we are logging the error and moving forward.

                    logger.info("Money movement transaction object is missing for the id: "+mmtEx.getMessage());
                }

            }
        }
        sapPayrollRun.setEmployerDDDebitTxnNumber(employerDDDebitTxnNumber);

        return sapPayrollRun;
    }

    public static SAPPItem getSAPPItemFromDomainEntity(CompanyPayrollItem pCompanyPayrollItem, CompanyPayrollItem pDirectDepositPayrollItem) {
        if (pCompanyPayrollItem != null && pCompanyPayrollItem.getPayrollItem() != null) {
            SAPPItem sapPItem = new SAPPItem();
            PayrollItem payrollItem = pCompanyPayrollItem.getPayrollItem();
            sapPItem.setPitemNumber(pCompanyPayrollItem.getSourcePayrollItemId());
            sapPItem.setPitemName(pCompanyPayrollItem.getSourceDescription());
            if (payrollItem != null) {
                sapPItem.setPitemDescription(payrollItem.getPayrollItemDescription());
                sapPItem.setPitemType(payrollItem.getPayrollItemType().toString());
                PayrollItemCode pitemCode = payrollItem.getPayrollItemCode();
                if (pitemCode.in(PayrollItemCode.OtherAdditionPreTax, PayrollItemCode.OtherTaxableEmployerContribution)) {
                    sapPItem.setTaxabilityHeader("Taxable To");
                } else if (pitemCode.in(PayrollItemCode.OtherPreTaxDeduction)) {
                    sapPItem.setTaxabilityHeader("Exempt From");
                }
            }
            sapPItem.setStatus(pCompanyPayrollItem.getStatus().toString());
            sapPItem.setTaxFormLine(pCompanyPayrollItem.getTaxFormLine());
            DomainEntitySet<PayrollItemTaxableTo> taxableTos = pCompanyPayrollItem.getPayrollItemTaxableToCollection();
            ArrayList<String> taxableToLawIds = new ArrayList<String>();
            for (PayrollItemTaxableTo payrollItemTaxableTo : taxableTos) {
                if (payrollItemTaxableTo != null) {
                    CompanyLaw companyLaw = payrollItemTaxableTo.getCompanyLaw();
                    if (companyLaw.getLaw() != null) {
                        taxableToLawIds.add(companyLaw.getLaw().getLawId());
                    }
                }
            }
            sapPItem.setTaxableToLawIds(taxableToLawIds);
            if (taxableToLawIds.size() > 0) {
                sapPItem.setTaxability("Tax Affects");
            } else {
                PayrollItemCode payrollItemCode = pCompanyPayrollItem.getPayrollItem().getPayrollItemCode();
                if (payrollItemCode != null &&
                        payrollItemCode.in(PayrollItemCode.Salary, PayrollItemCode.Commission, PayrollItemCode.Bonus, PayrollItemCode.Hourly, PayrollItemCode.DirectDeposit)) {
                    sapPItem.setTaxability("");
                } else {
                    sapPItem.setTaxability("No Tax Affects");
                }
            }
            sapPItem.setLatestId(pCompanyPayrollItem.getLatestCompanyPayrollItem().getSourcePayrollItemId());
            QbdtPayrollItemInfo qbdtPayrollInfo = pCompanyPayrollItem.getQbdtPayrollItemInfo();
            if (qbdtPayrollInfo != null) {
                sapPItem.setCoaExpense(qbdtPayrollInfo.getExpenseAccount());
                sapPItem.setCoaLiability(qbdtPayrollInfo.getLiabilityAccount());
                sapPItem.setDeleteStatus(qbdtPayrollInfo.getIsDeleted() ? "DELETE" : "");
                sapPItem.setToken(Long.toString(qbdtPayrollInfo.getToken()));
            }
            //Replicating the logic used in  employee paycheck screens for setting the group title
            if (pCompanyPayrollItem.getPayrollItem().getPayrollItemType().equals(PayrollItemType.Compensation)) {
                if (pCompanyPayrollItem.getPayrollItem().isTaxableAddition()) {
                    sapPItem.setGroupTitle(SAPPItem.TAXABLE_ADDITION_TITLE);
                } else if (pCompanyPayrollItem.getPayrollItem().isAdditionNoTaxAffect()) {
                    sapPItem.setGroupTitle(SAPPItem.NO_TAX_AFFECT_ADDITION_TITLE);
                } else {
                    sapPItem.setGroupTitle(sapPItem.getPitemDescription());
                }
            } else if (pCompanyPayrollItem.getPayrollItem().getPayrollItemType().equals(PayrollItemType.EmployerContribution)) {
                if (pCompanyPayrollItem.isPreTax()) {
                    sapPItem.setGroupTitle(SAPPItem.TAXABLE_ER_CONTRIBUTION_TITLE);
                } else {
                    sapPItem.setGroupTitle(SAPPItem.NO_TAX_AFFECT_ER_CONTRIBUTION_TITLE);
                }
            } else if (pCompanyPayrollItem.equals(pDirectDepositPayrollItem)) {
                sapPItem.setGroupTitle(sapPItem.getPitemDescription());
            } else if (pCompanyPayrollItem.getPayrollItem().getPayrollItemType().equals(PayrollItemType.Deduction)) {
                if (pCompanyPayrollItem.getPayrollItem().isTaxableAddition()) {
                    sapPItem.setGroupTitle(SAPPItem.TAXABLE_ADDITION_TITLE);
                } else if (pCompanyPayrollItem.getPayrollItem().isAdditionNoTaxAffect()) {
                    sapPItem.setGroupTitle(SAPPItem.NO_TAX_AFFECT_ADDITION_TITLE);
                } else if (pCompanyPayrollItem.getPayrollItem().isDirectDeposit()) {
                    sapPItem.setGroupTitle(SAPPItem.DIRECT_DEPOSIT_TITLE);
                } else if (pCompanyPayrollItem.isPreTax()) {
                    sapPItem.setGroupTitle(SAPPItem.PRE_TAX_DEDUCTION_TITLE);
                } else {
                    sapPItem.setGroupTitle(SAPPItem.POST_TAX_DEDUCTION_TITLE);
                }
            } else {
                sapPItem.setGroupTitle(sapPItem.getPitemDescription());
            }
            return sapPItem;
        } else {
            return null;
        }
    }

    public static SAPCompanyLaw getSAPCompanyLawFromDomainEntity(CompanyLaw pCompanyLaw) {
        if (pCompanyLaw != null && pCompanyLaw.getLaw() != null) {
            SAPCompanyLaw companyLaw = new SAPCompanyLaw();
            companyLaw.setSourceId(pCompanyLaw.getSourceId());
            companyLaw.setDescription(pCompanyLaw.getSourceDescription());
            companyLaw.setLawType(pCompanyLaw.getLaw().getLawTypeCd());
            companyLaw.setStatus(pCompanyLaw.getStatus().toString());
            companyLaw.setTaxFormLine(pCompanyLaw.getTaxFormLine());
            companyLaw.setLawId(pCompanyLaw.getLaw().getLawId());
            QbdtPayrollItemInfo qbdtPayrollInfo = pCompanyLaw.getQbdtPayrollItemInfo();
            if (qbdtPayrollInfo != null) {
                companyLaw.setAgencyId(qbdtPayrollInfo.getAgencyId());
                companyLaw.setCoaExpense(qbdtPayrollInfo.getExpenseAccount());
                companyLaw.setCoaLiability(qbdtPayrollInfo.getLiabilityAccount());
                companyLaw.setDeleteStatus(qbdtPayrollInfo.getIsDeleted() ? "DELETE" : "");
                companyLaw.setToken(Long.toString(qbdtPayrollInfo.getToken()));
                companyLaw.setPendingPush(qbdtPayrollInfo.getRatePushToken() != -1 ? "YES" : "");
                companyLaw.setIisemp(qbdtPayrollInfo.getIsEmployeePaid());
            }
            //duplicates doesn't apply for Law 177 (Law Id = 177 is exception)
            if(!pCompanyLaw.getLaw().getLawId().equals(Law.LAW_177)) {
                companyLaw.setLatestId(pCompanyLaw.getLatestCompanyLaw().getSourceId());
            }
            return companyLaw;
        } else {
            return null;
        }
    }

    public static SAPPaycheck getSAPPaycheckFromDomainEntity(Paycheck pPaycheck, boolean is401kPaycheck) {
        SAPPaycheck sapPaycheck = new SAPPaycheck();
        sapPaycheck.setPaycheckGseq(pPaycheck.getId().toString());
        sapPaycheck.setSourcePaycheckId(pPaycheck.getSourcePaycheckId());

        sapPaycheck.setEmployeeName(SAPTranslator.getEmployeeFullName(pPaycheck.getDDEmployee()));
        sapPaycheck.setSourceEmployeeName(SAPTranslator.getEmployeeFullName(pPaycheck.getSourceEmployee()));

        sapPaycheck.setPaycheckDate(SAPTranslator.getDateFromSpcfCalendar(pPaycheck.getPayrollRun().getPaycheckDate()));
        sapPaycheck.setPayPeriodBeginDate(SAPTranslator.getDateFromSpcfCalendar(pPaycheck.getPayPeriodBeginDate()));
        sapPaycheck.setPayPeriodEndDate(SAPTranslator.getDateFromSpcfCalendar(pPaycheck.getPayPeriodEndDate()));

        sapPaycheck.setNetPaycheckAmount(SAPTranslator.getDoubleFromSpcfMoney(pPaycheck.getNetAmount()));

        //todo does Inactive exist for assisted?
        sapPaycheck.setStatus((pPaycheck.getStatus() == PaycheckStatusCode.Inactive && !pPaycheck.isVoided()) ? "Cancelled" : "Completed");

        if (pPaycheck.isVoided()) {
            sapPaycheck.setVoidedAfterOffload(true);
            sapPaycheck.setVoidedDate(SAPTranslator.getDateFromSpcfCalendar(pPaycheck.getCompanyAdjustmentSubmission().getSubmissionDate()));
        }

        if (is401kPaycheck) {
            sapPaycheck.setPaycheck401k(getSAPPaycheck401kFromDomainEntity(pPaycheck));
        }

        return sapPaycheck;
    }

    private static SAPPaycheck401k getSAPPaycheck401kFromDomainEntity(Paycheck pPaycheck) {
        SAPPaycheck401k sapPaycheck401k = new SAPPaycheck401k();

        sapPaycheck401k.setDateSentToTOK(SAPTranslator.getDateFromSpcfCalendar(pPaycheck.getTOKSendDate()));
        sapPaycheck401k.setVoidedAfterTOKOffload(pPaycheck.isVoided());
        sapPaycheck401k.setDeletedAfterTOKOffload(pPaycheck.getStatus() == PaycheckStatusCode.Deleted);

        String status;

        //set TOK status
        switch (pPaycheck.getThirdParty401kPaycheck().getCurrentStateCd()) {
            case Sent:
                status = "Sent";
                break;
            case Pending:
                status = "Pending";
                break;
            case InvalidPaycheckData:
                status = "Invalid Paycheck Data";
                break;
            case InvalidEmployeeData:
                status = "Invalid Employee Data";
                break;
            case Ineligible:
                status = "Missed Cutoff";
                break;
            case Cancelled:
                if (pPaycheck.isVoided()) {
                    status = "Voided";
                } else if (pPaycheck.getStatus() == PaycheckStatusCode.Deleted) {
                    status = "Deleted";
                } else {
                    status = "Cancelled";
                }

                break;
            case None:
                // Should never have a None status code
                status = "n/a";
                break;
            default:
                status = "Unknown";
                break;
        }

        sapPaycheck401k.setTokStatus(status);

        return sapPaycheck401k;
    }

    public static SAPTransactionType getSAPTransactionTypeFromDomainEntity(TransactionType pTransactionType) {
        SAPTransactionType sapTransactionType = new SAPTransactionType();
        sapTransactionType.setAssociationType(pTransactionType.getAssociationType());
        sapTransactionType.setDescription(pTransactionType.getDescription());
        sapTransactionType.setFeeInd(pTransactionType.getFeeInd());
        sapTransactionType.setNACHABatchType(pTransactionType.getNACHABatchType());
        sapTransactionType.setName(pTransactionType.getName());
        sapTransactionType.setTransactionCategory(pTransactionType.getTransactionCategory());
        sapTransactionType.setTransactionTypeCd(pTransactionType.getTransactionTypeCd());
        return sapTransactionType;
    }

    public static TransactionCancelEEDTO getTransactionCancelDTOFromParameters(ArrayList<String> pPaycheckIds,
                                                                               String pPayrollRunId) {
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePaycheckIdList(pPaycheckIds);
        transactionCancelDTO.setSourcePayrollRunId(pPayrollRunId);
        transactionCancelDTO.setAgentCancel(true);
        return transactionCancelDTO;
    }

    public static TransactionReverseDTO getTransactionReverseDTOFromParameters(ArrayList<String> transactionIds,
                                                                               String pPayrollRunId,
                                                                               boolean pChargeFee,
                                                                               Date pFeeTxnDate,
                                                                               String pFeeSettlementType,
                                                                               boolean pInitiateForCollection) {
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();
        transactionReverseDTO.setChargeFee(pChargeFee);
        transactionReverseDTO.setDdTransactionIdList(transactionIds);
        transactionReverseDTO.setSourcePayrollRunId(pPayrollRunId);
        transactionReverseDTO.setIntuitInitiatedReversals(pInitiateForCollection);
        Calendar calDate = null;
        if (pFeeTxnDate != null) {
            calDate = Calendar.getInstance();
            calDate.setTime(pFeeTxnDate);
        }
        transactionReverseDTO.setTxDate(calDate);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.valueOf(pFeeSettlementType));
        return transactionReverseDTO;
    }

    private static void buildSAPLedgerTransactionFromDomainEntity(
            FinancialTransaction pFinancialTransaction,
            boolean isCredit,
            SAPPayrollTransaction sapPayrollTransaction, FinancialTransactionState transactionState) {

        sapPayrollTransaction.setAmount(SAPTranslator.
                                                             getDoubleFromSpcfMoney(new SpcfMoney(pFinancialTransaction.getFinancialTransactionAmount().abs())));

        sapPayrollTransaction.setId(pFinancialTransaction.getId().toString());

        if (pFinancialTransaction.getPaycheckSplit() != null) {
            sapPayrollTransaction.setTransactionId(pFinancialTransaction.getPaycheckSplit().getSourceDdTxnId());
        }

        sapPayrollTransaction.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(transactionState.getCreatedDate().toLocal()));

        sapPayrollTransaction.setTxnDate(SAPTranslator.getDateFromSpcfCalendar(transactionState.getTransactionStateEffectiveDate().toLocal()));

        sapPayrollTransaction.setSettlementType(pFinancialTransaction.getSettlementTypeCd());

        sapPayrollTransaction.setStatus(transactionState.getTransactionState().getTransactionStateCd());

        sapPayrollTransaction.setTxnType(pFinancialTransaction.getTransactionType().getTransactionTypeCd());

        sapPayrollTransaction.setCredit(isCredit);
    }

    private static void buildSAPPayrollTransactionFromDomainEntity(
            FinancialTransaction pFinancialTransaction,
            Collection<ActionEvent> actionEvents,
            SAPPayrollTransaction sapPayrollTransaction,
            String transactionReturns) {

        PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();

        if (payrollRun != null) {
            sapPayrollTransaction.setSourcePayRunId(payrollRun.getSourcePayRunId());
        }

        sapPayrollTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoney(getDebitAmount(pFinancialTransaction)));

        sapPayrollTransaction.setId(pFinancialTransaction.getId().toString());

        if (pFinancialTransaction.getPaycheckSplit() != null) {
            sapPayrollTransaction.setTransactionId(pFinancialTransaction.getPaycheckSplit().getSourceDdTxnId());
        } else if (pFinancialTransaction.getBillPaymentSplit() != null) {
            sapPayrollTransaction.setTransactionId(pFinancialTransaction.getBillPaymentSplit().getSourceId());
        }

        sapPayrollTransaction.setCreatedDate(
                SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getCreatedDate().toLocal()));

        if (pFinancialTransaction.getSettlementDate() != null) {
            sapPayrollTransaction.setTxnDate(SAPTranslator.getDateFromSpcfCalendar(
                    pFinancialTransaction.getSettlementDate().toLocal()));
        }

        sapPayrollTransaction.setSettlementType(pFinancialTransaction.getSettlementTypeCd());

        sapPayrollTransaction.setStatus(pFinancialTransaction.getCurrentTransactionState().getTransactionStateCd());

        sapPayrollTransaction.setTxnType(pFinancialTransaction.getTransactionType().getTransactionTypeCd());
        if (pFinancialTransaction.getTransactionType().getFeeInd() && pFinancialTransaction.getBillingDetail() != null && StringUtils.isNotEmpty(pFinancialTransaction.getBillingDetail().getItemName())) {
            if (pFinancialTransaction.getBillingDetail().getItemName().equals("Other Fee")) {
                sapPayrollTransaction.setDescription("Other Fee (" + pFinancialTransaction.getBillingDetail().getMemo() + ")");
            } else {
                sapPayrollTransaction.setDescription(pFinancialTransaction.getBillingDetail().getItemName());
            }
        } else {
            sapPayrollTransaction.setDescription(pFinancialTransaction.getTransactionType().getDescription());
        }

        if (actionEvents != null)
            sapPayrollTransaction.setActionCollection(
                    getSAPActionEventsFromDomainEntities(actionEvents));

        if (transactionReturns != null) {
            sapPayrollTransaction.setReturnCd(transactionReturns);
        }
    }

    public static SAPPayrollTransaction getSAPPayrollTransactionFromDomainEntity(FinancialTransaction pFinancialTransaction,
                                                                                 Collection<ActionEvent> actionEvents,
                                                                                 String transactionReturns) {
        SAPPayrollTransaction sapPayrollTransaction = new SAPPayrollTransaction();
        buildSAPPayrollTransactionFromDomainEntity(pFinancialTransaction, actionEvents, sapPayrollTransaction, transactionReturns);
        return sapPayrollTransaction;
    }

    public static SAPPayrollTransaction getSAPLedgerTransactionFromDomainEntity(FinancialTransaction pFinancialTransaction,
                                                                                boolean isCredit, FinancialTransactionState transactionState) {
        SAPPayrollTransaction sapPayrollTransaction = new SAPPayrollTransaction();
        buildSAPLedgerTransactionFromDomainEntity(pFinancialTransaction, isCredit, sapPayrollTransaction, transactionState);
        return sapPayrollTransaction;
    }

    public static SAPPayrollEmployeeTransaction getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
            BillPaymentSplit billPaymentSplit, boolean canViewFullBankAccountNumbers) {
        SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction = new SAPPayrollEmployeeTransaction();

        PayrollRun payrollRun = billPaymentSplit.getBillPayment().getPayrollRun();

        if (payrollRun != null) {
            sapPayrollEmployeeTransaction.setSourcePayRunId(payrollRun.getSourcePayRunId());
        }

        sapPayrollEmployeeTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoney(billPaymentSplit.getAmount()));

        sapPayrollEmployeeTransaction.setId(billPaymentSplit.getId().toString());
        sapPayrollEmployeeTransaction.setTransactionId(billPaymentSplit.getSourceId());

        sapPayrollEmployeeTransaction.setCreatedDate(
                SAPTranslator.getDateFromSpcfCalendar(billPaymentSplit.getCreatedDate().toLocal()));

        sapPayrollEmployeeTransaction.setTxnDate(SAPTranslator.getDateFromSpcfCalendar(
                billPaymentSplit.getBillPayment().getPayrollRun().getPaycheckSettlementDate().toLocal()));

        sapPayrollEmployeeTransaction.setSettlementType(SettlementType.ACH);
        sapPayrollEmployeeTransaction.setStatus(TransactionStateCode.Completed);
        sapPayrollEmployeeTransaction.setTxnType(TransactionTypeCode.EmployeeDdCredit);
        sapPayrollEmployeeTransaction.setVoidedAfterOffload(false);

        PayeeBankAccount payeeBankAccount = billPaymentSplit.getPayeeBankAccount();
        if (payeeBankAccount != null) {
            sapPayrollEmployeeTransaction.setEmployeeName(payeeBankAccount.getPayee().getName());
            sapPayrollEmployeeTransaction.setEmployeeBankAccountNumber(PIIMask.maskText(payeeBankAccount.getBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapPayrollEmployeeTransaction.setEmployeeBankRoutingNumber(payeeBankAccount.getBankAccount().getRoutingNumber());
            sapPayrollEmployeeTransaction.setEmailId(payeeBankAccount.getPayee().getEmail());
            sapPayrollEmployeeTransaction.setHasInvalidEmail(payeeBankAccount.getPayee().getHasInvalidEmail());
        }

        return sapPayrollEmployeeTransaction;
    }

    public static SAPPayrollEmployeeTransaction getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
            FinancialTransaction pFinancialTransaction, Collection<ActionEvent> actionEvents, String transactionReturns, Paycheck paycheck, boolean canViewFullBankAccountNumbers) {
        SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction = new SAPPayrollEmployeeTransaction();
        buildSAPPayrollTransactionFromDomainEntity(pFinancialTransaction, actionEvents, sapPayrollEmployeeTransaction, transactionReturns);

        if (paycheck != null) {
            sapPayrollEmployeeTransaction.setVoidedAfterOffload(paycheck.isVoided());
            CompanyAdjustmentSubmission companyVoidForPaycheck = paycheck.getCompanyAdjustmentSubmission();
            if (companyVoidForPaycheck != null) {
                sapPayrollEmployeeTransaction.setVoidedDate(SAPTranslator.getDateFromSpcfCalendar(companyVoidForPaycheck.getSubmissionDate()));
            }
        } else {
            sapPayrollEmployeeTransaction.setVoidedAfterOffload(false);
        }

        // these two bank accounts are mutual exclusive so there is no chance of the name getting over written
        EmployeeBankAccount employeeBankAccount = pFinancialTransaction.getEmployeeBankAccount();
        if (employeeBankAccount != null) {
            sapPayrollEmployeeTransaction.setEmployeeName(SAPTranslator.getEmployeeFullName(employeeBankAccount.getEmployee()));
            sapPayrollEmployeeTransaction.setEmployeeBankAccountNumber(PIIMask.maskText(employeeBankAccount.getBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapPayrollEmployeeTransaction.setEmployeeBankRoutingNumber(CompanyTranslator.getRoutingNumberPayCardDisplayText(employeeBankAccount.getBankAccount()));
            sapPayrollEmployeeTransaction.setEmailId(employeeBankAccount.getEmployee().getEmail());
            sapPayrollEmployeeTransaction.setHasInvalidEmail(employeeBankAccount.getEmployee().getHasInvalidEmail());
        }

        PayeeBankAccount payeeBankAccount = pFinancialTransaction.getPayeeBankAccount();
        if (payeeBankAccount != null) {
            sapPayrollEmployeeTransaction.setEmployeeName(payeeBankAccount.getPayee().getName());
            sapPayrollEmployeeTransaction.setEmployeeBankAccountNumber(PIIMask.maskText(payeeBankAccount.getBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapPayrollEmployeeTransaction.setEmployeeBankRoutingNumber(payeeBankAccount.getBankAccount().getRoutingNumber());
            sapPayrollEmployeeTransaction.setEmailId(payeeBankAccount.getPayee().getEmail());
            sapPayrollEmployeeTransaction.setHasInvalidEmail(payeeBankAccount.getPayee().getHasInvalidEmail());
        }

        return sapPayrollEmployeeTransaction;
    }

    public static SAPPayrollEmployeeTransaction getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(PaycheckSplit pPaycheckSplit, boolean canViewFullBankAccountNumbers) {
        SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction = new SAPPayrollEmployeeTransaction();

        Paycheck paycheck = pPaycheckSplit.getPaycheck();
        PayrollRun payrollRun = paycheck.getPayrollRun();

        sapPayrollEmployeeTransaction.setVoidedAfterOffload(false);
        sapPayrollEmployeeTransaction.setSettlementType(SettlementType.ACH);
        //sapPayrollEmployeeTransaction.setId(pFinancialTransaction.getId().toString());
        sapPayrollEmployeeTransaction.setSourcePayRunId(payrollRun.getSourcePayRunId());
        sapPayrollEmployeeTransaction.setTransactionId(pPaycheckSplit.getSourceDdTxnId());
        sapPayrollEmployeeTransaction.setTxnType(TransactionTypeCode.EmployeeDdCredit);
        sapPayrollEmployeeTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoney(pPaycheckSplit.getPaycheckSplitAmount()));
        sapPayrollEmployeeTransaction.setTxnDate(SAPTranslator.getDateFromSpcfCalendar(payrollRun.getPaycheckDate().toLocal()));
        sapPayrollEmployeeTransaction.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pPaycheckSplit.getCreatedDate().toLocal()));

        if (!paycheck.getStatus().equals(PaycheckStatusCode.Active)) {
            sapPayrollEmployeeTransaction.setStatus(TransactionStateCode.Cancelled);
        }

        EmployeeBankAccount employeeBankAccount = pPaycheckSplit.getEmployeeBankAccount();
        if (employeeBankAccount != null) {
            sapPayrollEmployeeTransaction.setEmployeeName(SAPTranslator.getEmployeeFullName(employeeBankAccount.getEmployee()));
            sapPayrollEmployeeTransaction.setEmployeeBankAccountNumber(PIIMask.maskText(employeeBankAccount.getBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapPayrollEmployeeTransaction.setEmployeeBankRoutingNumber(CompanyTranslator.getRoutingNumberPayCardDisplayText(employeeBankAccount.getBankAccount()));
            sapPayrollEmployeeTransaction.setEmailId(employeeBankAccount.getEmployee().getEmail());
            sapPayrollEmployeeTransaction.setHasInvalidEmail(employeeBankAccount.getEmployee().getHasInvalidEmail());
        }

        return sapPayrollEmployeeTransaction;
    }

    public static SAPAgencyTransaction getSAPAgencyTransactionFromDomainEntity(FinancialTransaction pFinancialTransaction,
                                                                               Collection<ActionEvent> actionEvents,
                                                                               String transactionReturns,
                                                                               RulesInfo rulesInfo) {
        SAPAgencyTransaction sapAgencyTransaction = new SAPAgencyTransaction();
        buildSAPPayrollTransactionFromDomainEntity(pFinancialTransaction, actionEvents, sapAgencyTransaction, transactionReturns);
        setSAPAgencyTransactionNamesAndAbreviations(sapAgencyTransaction, pFinancialTransaction.getLaw(), null, rulesInfo);

        return sapAgencyTransaction;
    }

    private static void setSAPAgencyTransactionNamesAndAbreviations(SAPAgencyTransaction sapAgencyTransaction, Law law, String lawId, RulesInfo rulesInfo) {
        if(law == null && StringUtils.isNotEmpty(lawId)) {
            law = Application.findById(Law.class, lawId);
        }

        // try to use the static data first, otherwise fallback to the agency rules
        if(law != null) {
            sapAgencyTransaction.setAgencyName(law.getPaymentTemplate().getAgency().getName());
            sapAgencyTransaction.setAgencyAbbreviation(law.getPaymentTemplate().getAgency().getAgencyAbbrev());
            sapAgencyTransaction.setTaxDescription(law.getDescription());
            sapAgencyTransaction.setTaxAbbreviation(law.getLawAbbrev());
        } else {
            IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(rulesInfo.getPaymentTemplateID(Integer.parseInt(lawId)));
            IAgency agency = rulesInfo.getAgency(paymentTemplate.getAgencyID());
            LawData lawData = rulesInfo.getLawByLawId(lawId);

            sapAgencyTransaction.setAgencyName(agency.getName());
            sapAgencyTransaction.setAgencyAbbreviation(agency.getAgencyAbbrev());
            sapAgencyTransaction.setTaxDescription(lawData.getDescription());
            sapAgencyTransaction.setTaxAbbreviation(lawData.getLawAbbrev());
        }
    }

    public static SAPCompanyLedgerAccount getSAPCompanyLedgerAccountFromParameters(
            LedgerAccount pLedgerAccount,
            SpcfMoney pBalance,
            boolean isCredit,
            Collection<ActionEvent> pActionEventList) {
        SAPCompanyLedgerAccount sapCompanyLedgerAccount = new SAPCompanyLedgerAccount();
        sapCompanyLedgerAccount.setActionCollection(
                getSAPActionEventsFromDomainEntities(pActionEventList));
        sapCompanyLedgerAccount.setBalance(SAPTranslator.getDoubleFromSpcfMoney(new SpcfMoney(pBalance.abs())));
        sapCompanyLedgerAccount.setCredit(isCredit);
        sapCompanyLedgerAccount.setName(pLedgerAccount.getName());
        sapCompanyLedgerAccount.setDescription(pLedgerAccount.getDescription());
        sapCompanyLedgerAccount.setLedgerAccountCode(pLedgerAccount.getLedgerAccountCd());
        sapCompanyLedgerAccount.setRequiresQuarterLaw(pLedgerAccount.getRequiresQuarterLaw());
        boolean creditAddsTobalance = pLedgerAccount.getBalanceCalculationRule().equals
                (LedgerBalanceCalculationRuleEnum.CreditAddsToBalance);
        sapCompanyLedgerAccount.setCreditAddsToBalance(creditAddsTobalance);
        return sapCompanyLedgerAccount;
    }

    public static ModifyWireExpectedDTO getWireExpectedDTOFromParameters(String pFinancialTxId,
                                                                         String pCollectionStageCd,
                                                                         String pActionEventCd,
                                                                         Date pTxnDate,
                                                                         Boolean pSendLastEmail) {

        CollectionStage collectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.valueOf(pCollectionStageCd));
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(pFinancialTxId, new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pTxnDate)),
                                                                          collectionStage, ActionEventCode.valueOf(pActionEventCd), false);

        wireExpectedDTO.setLastChanceEmail(pSendLastEmail);
        return wireExpectedDTO;
    }

    public static ERRefundDTO getERRefundDTOFromParameters(String pFinancialTxId,
                                                           double pFinancialTxAmt,
                                                           Date initiationDate,
                                                           Date settlementDate,
                                                           String pSettlementType) {
        ERRefundDTO erRefundDTO = new ERRefundDTO();
        erRefundDTO.setFinancialTxAmt(SAPTranslator.getSpcfMoneyFromDouble(pFinancialTxAmt));
        erRefundDTO.setFinancialTxId(pFinancialTxId);
        erRefundDTO.setSettlementType(SettlementTypeDTO.valueOf(pSettlementType));
        if (erRefundDTO.getSettlementType() == SettlementTypeDTO.ACH) {
            erRefundDTO.setTxDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(initiationDate)));
        } else {
            erRefundDTO.setTxDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(settlementDate)));
        }
        return erRefundDTO;
    }

    public static ArrayList<ERRefundDTO> getERRefundDTOList(SAPBillingTransaction sapBillingTransaction,
                                                            String settlementTypeCd,
                                                            Date settlementDate,
                                                            Date initiationDate) {
        ArrayList<ERRefundDTO> refunds = new ArrayList<ERRefundDTO>();

        if (sapBillingTransaction == null)
            return refunds;

        if (sapBillingTransaction.getFinancialTxnId() != null && sapBillingTransaction.getFinancialReturnAmount() > 0) {
            refunds.add(
                    getERRefundDTOFromParameters(
                            sapBillingTransaction.getFinancialTxnId(),
                            sapBillingTransaction.getFinancialReturnAmount(),
                            initiationDate,
                            settlementDate,
                            settlementTypeCd));
        }

        if (sapBillingTransaction.getSalesTaxTxnId() != null && sapBillingTransaction.getSalesTaxReturnAmount() > 0) {
            refunds.add(
                    getERRefundDTOFromParameters(
                            sapBillingTransaction.getSalesTaxTxnId(),
                            sapBillingTransaction.getSalesTaxReturnAmount(),
                            initiationDate,
                            settlementDate,
                            settlementTypeCd));
        }

        return refunds;
    }

    public static BadDebtRecoverDTO getBadDebtRecoverDTOFromParameters(String pPayRunId,
                                                                       String pSettlementTypeCd,
                                                                       Date pSettlementDate,
                                                                       Date pInitiationDate,
                                                                       double pAmount,
                                                                       String pTxnId,
                                                                       boolean isCustomer) {
        BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
        badDebtRecoverDTO.setFinancialTxAmt(SAPTranslator.getSpcfMoneyFromDouble(pAmount));
        badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.valueOf(pSettlementTypeCd));
        badDebtRecoverDTO.setSourcePayrollRunId(pPayRunId);
        if (badDebtRecoverDTO.getSettlementType() == SettlementTypeDTO.ACH) {
            badDebtRecoverDTO.setTxDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pInitiationDate)));
        } else {
            badDebtRecoverDTO.setTxDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pSettlementDate)));
        }
        badDebtRecoverDTO.setOriginalTransactionId(pTxnId);
        badDebtRecoverDTO.setCustomer(isCustomer);
        return badDebtRecoverDTO;
    }

    public static FeeTransferDTO getFeeTransferDTOFromParameters(String pPayRunId, double pAmount, String pOfferingServiceChargeTypeCd) {
        FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
        feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.valueOf(pOfferingServiceChargeTypeCd));
        feeTransferDTO.setFinancialTxAmt(SAPTranslator.getSpcfMoneyFromDouble(pAmount));
        feeTransferDTO.setSourcePayrollRunId(pPayRunId);
        return feeTransferDTO;
    }

    public static RefundDTO getRefundDTOFromParameters(String pPayRunId,
                                                       String pSettlementTypeCd,
                                                       Date pTxnDate,
                                                       double pAmount) {
        RefundDTO refundDTO = new RefundDTO();
        refundDTO.setFinancialTxAmt(SAPTranslator.getSpcfMoneyFromDouble(pAmount));
        refundDTO.setSettlementType(SettlementTypeDTO.valueOf(pSettlementTypeCd));
        refundDTO.setSourcePayrollRunId(pPayRunId);
        refundDTO.setTxDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pTxnDate)));
        return refundDTO;
    }

    public static ArrayList<SAPPropertyAudit> getSAPPropertAuditsFromFinancialTransactionStates(
            DomainEntitySet<FinancialTransactionState> financialTxStateCollection) {
        ArrayList<SAPPropertyAudit> sapPropertyAuditList = new ArrayList<SAPPropertyAudit>();
        for (FinancialTransactionState financialTransactionState : financialTxStateCollection) {
            sapPropertyAuditList.add(getSAPPropertyAuditFromFinancialTransactionState(financialTransactionState));
        }
        return sapPropertyAuditList;
    }

    private static SAPPropertyAudit getSAPPropertyAuditFromFinancialTransactionState(
            FinancialTransactionState financialTransactionState) {
        SAPPropertyAudit sapPropertyAudit = new SAPPropertyAudit();
        sapPropertyAudit.setAuditDate(
                SAPTranslator.getDateFromSpcfCalendar(financialTransactionState.getCreatedDate()));
        sapPropertyAudit.setNewPropertyValue(financialTransactionState.getTransactionState().getTransactionStateCd().toString());
        sapPropertyAudit.setUserId(SAPTranslator.getUserNameFromUserID(financialTransactionState.getCreatorId()));
        return sapPropertyAudit;
    }


    public static ArrayList<RedebitImpoundDTO> getRedebitImpoundDTOList(SAPBillingTransaction sapUncollectedTransaction,
                                                                        String settlementTypeCd,
                                                                        Date settlementDate,
                                                                        Date initionDate) {
        ArrayList<RedebitImpoundDTO> redebits = new ArrayList<RedebitImpoundDTO>();

        if (sapUncollectedTransaction == null)
            return redebits;

        if (sapUncollectedTransaction.getFinancialTxnId() != null && sapUncollectedTransaction.getFinancialReturnAmount() > 0) {
            RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementTypeCd));
            redebitDTO.setOriginalFinancialTxId(sapUncollectedTransaction.getFinancialTxnId());
            if (redebitDTO.getSettlementType() == SettlementTypeDTO.ACH) {
                redebitDTO.setInitiationDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(initionDate)));
            } else {
                redebitDTO.setInitiationDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(settlementDate)));
            }
            redebitDTO.setAmount(SAPTranslator.getSpcfMoneyFromDouble(sapUncollectedTransaction.getFinancialReturnAmount()));
            redebits.add(redebitDTO);
        }

        if (sapUncollectedTransaction.getSalesTaxTxnId() != null && sapUncollectedTransaction.getSalesTaxReturnAmount() > 0) {
            RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementTypeCd));
            redebitDTO.setOriginalFinancialTxId(sapUncollectedTransaction.getSalesTaxTxnId());
            redebitDTO.setInitiationDate(new DateDTO(SAPTranslator.getSpcfCalendarFromDate(settlementDate)));
            redebitDTO.setAmount(SAPTranslator.getSpcfMoneyFromDouble(sapUncollectedTransaction.getSalesTaxReturnAmount()));
            redebits.add(redebitDTO);
        }

        return redebits;
    }

    public static SAPChaseReport getSAPChaseReportFromDomainEntitys(PayrollRun pPayrollRun, DomainEntitySet<FinancialTransaction> pFinancialTransactions, boolean canViewFullBankAccountNumbers) {
        SAPChaseReport sapChaseReport = new SAPChaseReport();
        // not all of the data loaders set the transmission
        if (pPayrollRun.getTransmissionPayrollRunCollection().size() > 0) {
            sapChaseReport.setConnectionDate(SAPTranslator.getDateFromSpcfCalendar(SourceSystemTransmission.getInitialTransmission(pPayrollRun).getInitializeDateTime()));
        }
        else {
            sapChaseReport.setConnectionDate(SAPTranslator.getDateFromSpcfCalendar(pPayrollRun.getPayrollRunDate()));
        }

        sapChaseReport.setCompanyName(pPayrollRun.getCompany().getLegalName());
        sapChaseReport.setSourceSystem(pPayrollRun.getCompany().getSourceSystemCd().toString());
        sapChaseReport.setPostingDate(SAPTranslator.getDateFromSpcfCalendar(pPayrollRun.getPaycheckSettlementDate()));

        ArrayList<SAPChaseReportTransaction> chaseReportTransactions = new ArrayList<SAPChaseReportTransaction>(pFinancialTransactions.size());
        for (FinancialTransaction financialTransaction : pFinancialTransactions) {
            chaseReportTransactions.add(getSAPChaseReportTransactionFromDomainEntity(financialTransaction, canViewFullBankAccountNumbers));
        }
        // this will display as a blank row. flex does not calculate the row height of the last row
        // if it is blank we don't care
        chaseReportTransactions.add(new SAPChaseReportTransaction());
        sapChaseReport.setTransactions(chaseReportTransactions);

        return sapChaseReport;
    }

    public static SAPChaseReportTransaction getSAPChaseReportTransactionFromDomainEntity(FinancialTransaction pFinancialTransaction, boolean canViewFullBankAccountNumbers) {
        SAPChaseReportTransaction sapChaseReportTransaction = new SAPChaseReportTransaction();

        if (pFinancialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Employee) {
            if (pFinancialTransaction.getPaycheckSplit() != null) {
                Employee employee = pFinancialTransaction.getPaycheckSplit().getEmployeeBankAccount().getEmployee();
                sapChaseReportTransaction.setCreditAccountName(SAPTranslator.getEmployeeFullName(employee));
            } else if (pFinancialTransaction.getBillPaymentSplit() != null) {
                Payee payee = pFinancialTransaction.getBillPaymentSplit().getPayeeBankAccount().getPayee();
                sapChaseReportTransaction.setCreditAccountName(payee.getName());
            } else {
                sapChaseReportTransaction.setCreditAccountName(LS_EMPLOYER + pFinancialTransaction.getCreditBankAccount().getBankName());
            }
        } else if (pFinancialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company) {
            String companyAccount = pFinancialTransaction.getCreditBankAccount() != null ? pFinancialTransaction.getCreditBankAccount().getBankName() : "Credit Account is null!";
            sapChaseReportTransaction.setCreditAccountName(LS_EMPLOYER + companyAccount);
        } else {
            sapChaseReportTransaction.setCreditAccountName(BankAccountOwnerType.Intuit.toString());
        }

        if (pFinancialTransaction.getCreditBankAccount() != null) {
            sapChaseReportTransaction.setCreditAccountNumber(PIIMask.maskText(pFinancialTransaction.getCreditBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapChaseReportTransaction.setCreditAccountRoutingNumber(pFinancialTransaction.getCreditBankAccount().getRoutingNumber());
        } else {
            sapChaseReportTransaction.setCreditAccountNumber("Credit account is null!");
            sapChaseReportTransaction.setCreditAccountRoutingNumber("Credit account is null!");
        }

        if (pFinancialTransaction.getDebitBankAccountType() != BankAccountOwnerType.Intuit) {
            sapChaseReportTransaction.setDebitAmount(SAPTranslator.getDoubleFromSpcfMoney(pFinancialTransaction.getFinancialTransactionAmount()));
        } else {
            sapChaseReportTransaction.setCreditAmount(SAPTranslator.getDoubleFromSpcfMoney(pFinancialTransaction.getFinancialTransactionAmount()));
        }

        // debit account should never be null, but just in case
        if (pFinancialTransaction.getDebitBankAccount() != null) {
            if (pFinancialTransaction.getDebitBankAccountType() == BankAccountOwnerType.Company) {
                sapChaseReportTransaction.setDebitAccountName(LS_EMPLOYER + pFinancialTransaction.getDebitBankAccount().getBankName());
            } else {
                sapChaseReportTransaction.setDebitAccountName(BankAccountOwnerType.Intuit.toString());
            }

            sapChaseReportTransaction.setDebitAccountNumber(PIIMask.maskText(pFinancialTransaction.getDebitBankAccount().getAccountNumber(), !canViewFullBankAccountNumbers));
            sapChaseReportTransaction.setDebitAccountRoutingNumber(pFinancialTransaction.getDebitBankAccount().getRoutingNumber());
        } else {
            sapChaseReportTransaction.setDebitAccountName("Debit account is null!");
            sapChaseReportTransaction.setDebitAccountNumber("Debit account is null!");
            sapChaseReportTransaction.setDebitAccountRoutingNumber("Debit account is null!");
        }

        sapChaseReportTransaction.setSettlementDate(SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getSettlementDate()));

        return sapChaseReportTransaction;
    }

    public static SAPBillingTransaction getSAPBillingFinancialTransaction(FinancialTransaction financialTransaction, SpcfMoney amount, boolean populateReturnAmount) {
        SAPBillingTransaction sapBillingTransaction = new SAPBillingTransaction();
        if (financialTransaction != null && amount != null) {
            sapBillingTransaction.setFinancialTxnId(financialTransaction.getId().toString());
            sapBillingTransaction.setFinancialTxnType(financialTransaction.getTransactionType().getName());
            sapBillingTransaction.setFinancialAmount(SAPTranslator.getDoubleFromSpcfMoney(amount));
            if (populateReturnAmount) {
                sapBillingTransaction.setFinancialReturnAmount(sapBillingTransaction.getFinancialAmount());
            } else {
                sapBillingTransaction.setFinancialReturnAmount(0.00);
            }
        }

        return sapBillingTransaction;
    }

    public static SAPBillingTransaction getSAPBillingFinancialTransactionForHandlingCharge(SpcfDecimal amount, SpcfUniqueId txnIdToUse) {
        SAPBillingTransaction sapBillingTransaction = new SAPBillingTransaction();
        sapBillingTransaction.setFinancialTxnId(txnIdToUse.toString());
        sapBillingTransaction.setFinancialTxnType(TransactionTypeCode.EmployerFeeDebit.toString());
        sapBillingTransaction.setFinancialAmount(SAPTranslator.getDoubleFromSpcfMoneyNullZero(amount));
        sapBillingTransaction.setFinancialReturnAmount(0.00);
        return sapBillingTransaction;
    }

    public static SAPBillingTransaction getSAPBillingFinancialTransactionSalesTax(FinancialTransaction taxTransaction, SpcfMoney amount) {
        SAPBillingTransaction sapBillingTransaction = new SAPBillingTransaction();
        if (taxTransaction != null && amount != null) {
            sapBillingTransaction.setSalesTaxTxnId(taxTransaction.getId().toString());
            sapBillingTransaction.setSalesTaxAmount(SAPTranslator.getDoubleFromSpcfMoney(amount));
            sapBillingTransaction.setSalesTaxReturnAmount(sapBillingTransaction.getSalesTaxAmount());
        }

        return sapBillingTransaction;
    }

    public static void updateSAPBillingFinancialTransactionSalesTax(SAPBillingTransaction txn, FinancialTransaction taxTransaction, SpcfMoney amount, boolean populateReturnAmount) {
        if (txn != null && taxTransaction != null && amount != null) {
            txn.setSalesTaxTxnId(taxTransaction.getId().toString());
            txn.setSalesTaxAmount(SAPTranslator.getDoubleFromSpcfMoney(amount));
            if (populateReturnAmount) {
                txn.setSalesTaxReturnAmount(txn.getSalesTaxAmount());
            } else {
                txn.setSalesTaxReturnAmount(0.00);
            }
        }
    }

    public static double getAccountBalanceFromDomainEntity(SpcfMoney balance, LedgerAccount ledgerAccount) {
        if (balance == null) {
            return -1;
        }

        return SAPTranslator.getDoubleFromSpcfMoney(balance);
    }

    public static SAPCompanyBalance getCompanyBalance(SpcfMoney companyBalanceDue) {
        SAPCompanyBalance sapCompanyBalance = new SAPCompanyBalance();
        sapCompanyBalance.setBalanceDue(SAPTranslator.getDoubleFromSpcfMoney(companyBalanceDue));
        return sapCompanyBalance;
    }

    public static SAPMoneyMovementTransaction getSAPMoneyMovementTransactionFromDomainEntity(MoneyMovementTransaction moneyMovementTransaction,
                                                                                             BankAccount bankAccount,
                                                                                             Date settlementDate,
                                                                                             Date checkDate,
                                                                                             double debitAmount,
                                                                                             String achReason,
                                                                                             PayrollRun pPayrollRun) {
        SAPMoneyMovementTransaction sapMoneyMovementTransaction = new SAPMoneyMovementTransaction();
        sapMoneyMovementTransaction.setAchAmount(debitAmount);
        sapMoneyMovementTransaction.setAchReason(achReason);
        sapMoneyMovementTransaction.setBankAccount(CompanyTranslator.getSAPCompanyBankAccountFromDomainEntity(bankAccount, PIIMask.authenticatedUserCanViewFullBankAccountNumbers()));
        sapMoneyMovementTransaction.setCheckDate(checkDate);
        sapMoneyMovementTransaction.setCreationDate(SAPTranslator.getDateFromSpcfCalendar(moneyMovementTransaction.getCreatedDate()));
        sapMoneyMovementTransaction.setSpcfId(moneyMovementTransaction.getId().toString());
        sapMoneyMovementTransaction.setSettlementDate(settlementDate);
        sapMoneyMovementTransaction.setShowDetail(moneyMovementTransaction.getFinancialTransactionCollection().size() > 1 || moneyMovementTransaction.getFinancialTransactionCollection().get(0).getPayrollRun() != null);
        if (pPayrollRun != null) {
            sapMoneyMovementTransaction.setHPDE(pPayrollRun.isHistoricalPayroll());
        } else {
            sapMoneyMovementTransaction.setHPDE(false);
        }

        return sapMoneyMovementTransaction;
    }

    public static String getFeeName(FinancialTransaction financialTransaction) {
        if (financialTransaction != null && financialTransaction.getSku() != null) {
            OfferingServiceCharge offeringServiceCharge = OfferingServiceCharge.findBySKU(financialTransaction.getSku());
            if (offeringServiceCharge.getOfferingServiceChargeGroup() != null) {
                return offeringServiceCharge.getOfferingServiceChargeGroup().getName();
            }
        }
        return null;
    }

    public static SAPPayrollTransaction getFeeTransactionFromDomainEntity(FinancialTransaction financialTransaction) {
        String transactionDescription = getFeeName(financialTransaction);
        if (transactionDescription == null) {
            transactionDescription = EnumUtils.getReadableName(financialTransaction.getTransactionType().getTransactionTypeCd());
        } else if (financialTransaction.getSkuQuantity() > 1) {
            transactionDescription += " (" + financialTransaction.getSkuQuantity() + ")";
        }

        SAPPayrollTransaction sapPayrollTransaction = new SAPPayrollTransaction();
        sapPayrollTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoney(getDebitAmount(financialTransaction)));
        sapPayrollTransaction.setDescription(transactionDescription);

        return sapPayrollTransaction;
    }

    public static SAPAgencyTransaction getACHSAPAgencyTransaction(String lawId,
                                                                  double amount,
                                                                  RulesInfo rulesInfo) {
        SAPAgencyTransaction sapAgencyTransaction = new SAPAgencyTransaction();
        sapAgencyTransaction.setAmount(amount);

        setSAPAgencyTransactionNamesAndAbreviations(sapAgencyTransaction, null, lawId, rulesInfo);
        return sapAgencyTransaction;
    }

    public static SpcfMoney getDebitAmount(FinancialTransaction financialTransaction) {
        SpcfMoney debitAmount = financialTransaction.getFinancialTransactionAmount();
        if ((financialTransaction.getDebitBankAccountType() == BankAccountOwnerType.Intuit &&
                (financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company ||
                        financialTransaction.getCreditBankAccountType() == null)) ||
                (financialTransaction.getDebitBankAccountType() == BankAccountOwnerType.Employee &&
                        financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Intuit) ||
                (financialTransaction.getDebitBankAccountType() == BankAccountOwnerType.TaxAgency &&
                        financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Intuit)) {
            debitAmount = (SpcfMoney) debitAmount.negate();
        }

        return debitAmount;
    }

    public static SAPSuspectPaycheck getSapSuspectPaycheckFromDomainEntity(Paycheck paycheck, String trigger) {
        SAPSuspectPaycheck sapSuspectPaycheck = new SAPSuspectPaycheck();
        sapSuspectPaycheck.setEmployeeName(SAPTranslator.getEmployeeFullName(paycheck.getDDEmployee()));
        sapSuspectPaycheck.setAmount(SAPTranslator.getDoubleFromSpcfMoney(paycheck.getNetAmount()));
        sapSuspectPaycheck.setPaycheckId(paycheck.getSourcePaycheckId());
        sapSuspectPaycheck.setTrigger(trigger);
        return sapSuspectPaycheck;
    }

    public static PrefundPayrollTransactionDTO getPrefundPayrollTransactionDTO(SAPBillingTransaction sapBillingTransaction) {
        PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = new PrefundPayrollTransactionDTO();

        if (sapBillingTransaction.getFinancialTxnId() != null && sapBillingTransaction.getFinancialReturnAmount() > 0) {
            prefundPayrollTransactionDTO.setOriginalTransactionId(sapBillingTransaction.getFinancialTxnId());
            prefundPayrollTransactionDTO.setTransactionAmount(SAPTranslator.getSpcfMoneyFromDouble(sapBillingTransaction.getFinancialReturnAmount()));
        }

        if (sapBillingTransaction.getSalesTaxTxnId() != null && sapBillingTransaction.getSalesTaxAmount() > 0) {
            prefundPayrollTransactionDTO.setOriginalTaxTransactionId(sapBillingTransaction.getSalesTaxTxnId());
            prefundPayrollTransactionDTO.setTaxTransactionAmount(SAPTranslator.getSpcfMoneyFromDouble(sapBillingTransaction.getSalesTaxAmount()));
        }

        return prefundPayrollTransactionDTO;
    }

    public static SAPPayrollRunAction getSAPPayrollRunActionFromDomainEntity(PayrollStatus pPayrollStatusDE, ArrayList<SAPActionEvent> pActionEventList) {
        SAPPayrollRunAction sapPayrollRunAction = new SAPPayrollRunAction();
        sapPayrollRunAction.setStatus(pPayrollStatusDE);
        sapPayrollRunAction.setActionEvents(pActionEventList);
        return sapPayrollRunAction;
    }

    public static ArrayList<SAPActionEvent> getSAPActionEventsFromDomainEntities(Collection<ActionEvent> pActionEvents) {
        ArrayList<SAPActionEvent> sapActionEvents = new ArrayList<SAPActionEvent>();
        for (ActionEvent actionEvent : pActionEvents) {
            sapActionEvents.add(getSAPActionEventFromDomainEntity(actionEvent));
        }
        return sapActionEvents;
    }

    public static SAPActionEvent getSAPActionEventFromDomainEntity(ActionEvent pActionEvent) {
        SAPActionEvent sapActionEvent = new SAPActionEvent();
        sapActionEvent.setActionEventCd(pActionEvent.getCode());
        if (pActionEvent.getDescription() != null) {
            sapActionEvent.setDescription(pActionEvent.getDescription());
        }
        return sapActionEvent;
    }

    public static SAPPaycheckLineItem getSAPLineItemsFromCompensation(Compensation pCompensation) {
        SAPPaycheckLineItem sapLineItem = new SAPPaycheckLineItem();

        sapLineItem.setLineItemGseq(pCompensation.getId().toString());
        sapLineItem.setAmount(SAPTranslator.getDoubleFromSpcfMoney(pCompensation.getCompensationAmount()));
        populateGenericLineItemInfo(sapLineItem, pCompensation.getCompanyPayrollItem());

        return sapLineItem;
    }

    public static SAPPaycheckLineItem getSAPLineItemsFromDeduction(Deduction pDeduction) {
        SAPPaycheckLineItem sapLineItem = new SAPPaycheckLineItem();

        sapLineItem.setLineItemGseq(pDeduction.getId().toString());
        sapLineItem.setAmount(SAPTranslator.getDoubleFromSpcfMoney(pDeduction.getDeductionAmount()));
        populateGenericLineItemInfo(sapLineItem, pDeduction.getCompanyPayrollItem());

        return sapLineItem;
    }

    public static SAPPaycheckLineItem getSAPLineItemsFromEmployerContribution(EmployerContribution pEmployerContribution) {
        SAPPaycheckLineItem sapLineItem = new SAPPaycheckLineItem();

        sapLineItem.setLineItemGseq(pEmployerContribution.getId().toString());
        sapLineItem.setAmount(SAPTranslator.getDoubleFromSpcfMoney(pEmployerContribution.getContributionAmount()));
        populateGenericLineItemInfo(sapLineItem, pEmployerContribution.getCompanyPayrollItem());

        return sapLineItem;
    }

    private static void populateGenericLineItemInfo(SAPPaycheckLineItem paycheckLineItem, CompanyPayrollItem pCompanyPayrollItem) {
        paycheckLineItem.setPayrollItemCategory(pCompanyPayrollItem.getPayrollItem().getPayrollItemType().toString());
        paycheckLineItem.setPayrollItemType(pCompanyPayrollItem.getPayrollItem().getPayrollItemDescription());
        paycheckLineItem.setSourcePayrollItemName(pCompanyPayrollItem.getSourceDescription());
    }
}

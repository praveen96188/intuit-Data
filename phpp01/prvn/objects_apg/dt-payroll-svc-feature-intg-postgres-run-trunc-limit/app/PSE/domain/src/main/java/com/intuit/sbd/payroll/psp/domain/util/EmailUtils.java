package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 4, 2009
 * Time: 10:57:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailUtils {
    private static String newLine = System.getProperty("line.separator");

    public static String formatErrorMsg(CompanyEventEmail pCompanyEventEmail, String pErrorMsg) {
        StringBuffer err = new StringBuffer();

        err.append(pErrorMsg)
                .append(newLine)
                .append("  Source company id: ")
                .append(pCompanyEventEmail.getCompanyEvent().getCompany().getSourceCompanyId())
                .append(newLine)
                .append("         Event type: ")
                .append(pCompanyEventEmail.getCompanyEvent().getEventTypeCd().toString())
                .append(newLine)
                .append("         Company id: ")
                .append(pCompanyEventEmail.getCompanyEvent().getCompany().getId().toString())
                .append(newLine)
                .append("           Event id: ")
                .append(pCompanyEventEmail.getCompanyEvent().getId().toString())
                .append(newLine)
                .append("     Event email id: ")
                .append(pCompanyEventEmail.getId().toString());

        return err.toString();
    }

    /**
     * Finds an entity by its unique id
     *
     * @param pClass    The domain class representing the entity
     * @param pUniqueId The unique entity id
     * @return The entity matching the unique id, null otherwise
     */
    public static <T extends DomainEntity> T getById(Class<T> pClass, String pUniqueId) {
        return Application.findById(pClass, SpcfUniqueId.createInstance(pUniqueId));
    }

    public static String getDetailString(CompanyEvent pCompanyEvent, EventDetailTypeCode pDetailType) {
        return pCompanyEvent.getCompanyEventDetailValue(pDetailType);
    }

    public static Collection<String> getDetailStrings(CompanyEvent pCompanyEvent, EventDetailTypeCode pDetailType) {
        return pCompanyEvent.getCompanyEventDetailValues(pDetailType);
    }

    public static PayrollRun getPayrollRun(CompanyEvent pCompanyEvent) {
        String prId = getDetailString(pCompanyEvent, EventDetailTypeCode.PayrollRunId);
        return (prId != null) ? (PayrollRun) getById(PayrollRun.class, prId) : null;
    }

    public static BillPaymentSplit getBillPaymentSplit(CompanyEvent pCompanyEvent) {
        String bpId = getDetailString(pCompanyEvent, EventDetailTypeCode.BillPaymentId);
        return (bpId != null) ? (BillPaymentSplit) getById(BillPaymentSplit.class, bpId) : null;
    }

    public static MoneyMovementTransaction getMoneyMovementTransaction(CompanyEvent pCompanyEvent) {
        String mmtId = getDetailString(pCompanyEvent, EventDetailTypeCode.UniqueIdentifier);
        return (mmtId != null) ? (MoneyMovementTransaction) getById(MoneyMovementTransaction.class, mmtId) : null;
    }

    public static Entitlement getEntitlement(CompanyEvent pCompanyEvent) {
        String entitlementId = getDetailString(pCompanyEvent, EventDetailTypeCode.UniqueIdentifier);
        return (entitlementId != null) ? (Entitlement) getById(Entitlement.class, entitlementId) : null;
    }

    public static EventEmailTemplateTypeCode getEventEmailTemplateTypeCode(CompanyEvent pCompanyEvent) {
        String eventEmailTemplateTypeCodeString = getDetailString(pCompanyEvent, EventDetailTypeCode.EmailTemplateType);
        return (eventEmailTemplateTypeCodeString !=null)
                ? EventEmailTemplateTypeCode.valueOf(eventEmailTemplateTypeCodeString)
                : null;
    }

    public static EntitlementUnit getEntitlementUnit(CompanyEvent pCompanyEvent) {
        String entitlementUnitId = getDetailString(pCompanyEvent, EventDetailTypeCode.EntitlementUnitId);
        return (entitlementUnitId != null) ? (EntitlementUnit) getById(EntitlementUnit.class, entitlementUnitId) : null;
    }

    public static BillingDetail getRefundBillingDetail(CompanyEvent pCompanyEvent) {
        String bdId = getDetailString(pCompanyEvent, EventDetailTypeCode.RefundedFeeBillingDetailId);
        return (bdId != null) ? (BillingDetail) getById(BillingDetail.class, bdId) : null;
    }

    public static BillingDetail getFeeBillingDetail(CompanyEvent pCompanyEvent) {
        String bdId = getDetailString(pCompanyEvent, EventDetailTypeCode.FeeBillingDetailId);
        return (bdId != null) ? (BillingDetail) getById(BillingDetail.class, bdId) : null;
    }

    public static Contact getContact(CompanyEvent pCompanyEvent) {
        String contactId = getDetailString(pCompanyEvent, EventDetailTypeCode.ContactId);
        return (contactId != null) ? (Contact) getById(Contact.class, contactId) : null;
    }

    public static CompanyService getCompanyService(CompanyEvent pCompanyEvent) {
        String serviceId = getDetailString(pCompanyEvent, EventDetailTypeCode.CompanyServiceId);
        return (serviceId != null) ? (CompanyService) getById(CompanyService.class, serviceId) : null;
    }

    public static int getCompanyStrikeCount(Company pCompany) {
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);

        return CompanyEvent.getCompanyStrikeCount(pCompany, fromDate, null);
    }

    public static SpcfDecimal getCompanyBalanceDue(Company pCompany) {
        // is the company is in debt to Intuit for any prior Payroll
        return LedgerAccount.getLedgerAccountBalance(pCompany, LedgerAccountCode.ERReturnReceivable);
    }

    public static FinancialTransaction getFinancialTransaction(CompanyEvent pCompanyEvent) {
        String ftId = getDetailString(pCompanyEvent, EventDetailTypeCode.FinancialTransactionId);
        return (ftId != null) ? (FinancialTransaction) getById(FinancialTransaction.class, ftId) : null;
    }

    public static Paycheck getPaycheck(CompanyEvent pCompanyEvent) {
        String paycheckId = getDetailString(pCompanyEvent, EventDetailTypeCode.PaycheckId);
        return (paycheckId != null) ? (Paycheck) getById(Paycheck.class, paycheckId) : null;
    }

    public static String getOverrideEmailAddress(CompanyEvent pCompanyEvent) {
        String overrideRecipientEmailAddress = getDetailString(pCompanyEvent, EventDetailTypeCode.OverrideRecipientEmailAddress);
        return overrideRecipientEmailAddress;
    }

    public static DomainEntitySet<FinancialTransaction> getFinancialTransactions(CompanyEvent pCompanyEvent) {
        DomainEntitySet<FinancialTransaction> ftSet = null;
        ArrayList<SpcfUniqueId> financialTxnIds = new ArrayList<SpcfUniqueId>();
        for (String txn : getDetailStrings(pCompanyEvent, EventDetailTypeCode.FinancialTransactionId)) {
            financialTxnIds.add(SpcfUniqueId.createInstance(txn));
        }
        if(financialTxnIds.size() > 0) {
            ftSet = Application.find(FinancialTransaction.class,FinancialTransaction.Id().in(financialTxnIds)).sort(FinancialTransaction.TransactionType());
        }
        return ftSet;
    }

    public static SpcfDecimal getIntuitHandlingFee(PayrollRun pPayrollRun) {

        DomainEntitySet<FinancialTransaction> nsfFeesList =
                FinancialTransaction.findFinancialTransactions(pPayrollRun.getCompany(),
                                                               pPayrollRun,
                                                               TransactionTypeCode.EmployerFeeDebit,
                                                               OfferingServiceChargeType.DebitReturnFee);

        // Add up all the uncollected amounts for NSF fees and their associated tax for the payroll run
        SpcfDecimal nsfFeeAmount = SpcfDecimal.createInstance("0.00");

        if (!nsfFeesList.isEmpty()) {
            for (FinancialTransaction nsfFeeTxn : nsfFeesList) {
                // Get the fee and tax transactions for this fee transaction via the billing detail
                // If the fee was returned, the amount will be accounted for in the ER Returns Receivable account,
                // so no need to include it here as well

                if (nsfFeeTxn.calculateCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Returned) {
                    DomainEntitySet<FinancialTransaction> feeAndTax =
                            nsfFeeTxn.getBillingDetail().getFinancialTransactionCollection();

                    for (FinancialTransaction currFeeOrTaxTxn : feeAndTax) {
                        // If the DebitReturnFee was cancelled by the system add the to the nsfFeeAmount
                        boolean wasCancelledBySystem = false;
                        BillingDetail billingDetail = currFeeOrTaxTxn.getBillingDetail();
                        if (currFeeOrTaxTxn.isCancelled() && billingDetail.getOfferingServiceChargeType().equals(OfferingServiceChargeType.DebitReturnFee)) {
                            FinancialTransactionState ftState = currFeeOrTaxTxn.getFinancialTransactionStateByTransactionState(currFeeOrTaxTxn.getCurrentTransactionState());
                            wasCancelledBySystem = AuthUser.findUser(ftState.getCreatorId()) == null;
                        }

                        if (wasCancelledBySystem) {
                            nsfFeeAmount = nsfFeeAmount.add(currFeeOrTaxTxn.getFinancialTransactionAmount());
                        } else {
                            TransactionSummary txnSummary =
                                    currFeeOrTaxTxn.summarizeRelatedTransactions();

                            nsfFeeAmount = nsfFeeAmount.add(txnSummary.amtUncollected);
                            nsfFeeAmount = nsfFeeAmount.add(txnSummary.amtPending);
                        }
                    }
                }
            }
        } else {
            boolean bPayrollHasRejectReturn = false;

            DomainEntitySet<TransactionReturn> txnReturn =
                    TransactionReturn.findTransactionReturns(pPayrollRun.getSourcePayRunId(),
                            pPayrollRun.getCompany());

            for (TransactionReturn currTxnReturn : txnReturn) {
                 if (currTxnReturn.isRejectReturn()) {
                    bPayrollHasRejectReturn = true;
                    break;
                }
            }

            if (bPayrollHasRejectReturn) {

                // Fee Only return of System Generated Fees use the smaller FeeOnlyNSFFee.
                DomainEntitySet<Fee> feeList;
                if (pPayrollRun.getPayrollRunType().equals(PayrollType.FeeOnly) &&
                        (nsfFeesList.isEmpty() || FinancialTransaction.isSystemGeneratedFeesOnly(nsfFeesList))) {
                    feeList = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.FeeOnlyNSFFee));
                } else {
                    feeList = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.NSFFee));
                }
                if (feeList.size() != 1) {
                    throw new RuntimeException("Did not find exactly 1 NSF Fee Type as expected.  Instead found " + feeList.size());
                }
                nsfFeeAmount = feeList.get(0).getAmount();
            }
        }

        for (FinancialTransaction financialTransaction : pPayrollRun.getFinancialTransactions(TransactionTypeCode.IntuitFeeTransfer).find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled))) {
            nsfFeeAmount = nsfFeeAmount.subtract(financialTransaction.getFinancialTransactionAmount());
        }

        if (nsfFeeAmount.isLessThan(SpcfMoney.ZERO)) {
            return SpcfMoney.ZERO;
        } else {
            return nsfFeeAmount;
        }
    }

    /**
     * @param pPayrollRun payroll run to examine
     * @return returns an array of 2:
     *         element 0 = Uncollected payroll debit amount
     *         element 1 = Uncollected NSF fee amounts associated with the payroll or the static NSF fee amount if no NSF fees are associated with the payroll
     */
    public static SpcfDecimal[] getPayrollDebitsAndFees(PayrollRun pPayrollRun) {
        SpcfDecimal uncollectedPayrollFunds = pPayrollRun.getUncollectedAmountForPayroll();

        SpcfDecimal intuitHandlingFee = getIntuitHandlingFee(pPayrollRun);

        return new SpcfDecimal[]{uncollectedPayrollFunds, intuitHandlingFee};
    }

    public static SpcfDecimal getTotalRedebitAmount(DomainEntitySet<FinancialTransaction> pReferenceTxns) {
        SpcfDecimal totalRedebitAmount = SpcfDecimal.createInstance("0.00");

        for (FinancialTransaction currFinTxn : pReferenceTxns) {
            totalRedebitAmount = totalRedebitAmount.add(currFinTxn.getFinancialTransactionAmount());
        }

        return totalRedebitAmount;
    }

    public static String getReadableReturnFailureReason(CompanyEvent pCompanyEvent) {
        String description = null;
        String detail = getDetailString(pCompanyEvent, EventDetailTypeCode.ACHReturnReasonCode);

        if (detail != null) {
            try {
                ACHReturnReason reasonCode = ACHReturnReason.valueOf(detail);
                ReturnReasonDesc desc = Application.findById(ReturnReasonDesc.class, reasonCode);

                if (desc != null) {
                    description = desc.getDescription();
                }
            }
            catch (Exception e) {
                // do nothing (error reported by caller)
            }
        }

        return description;
    }

    public static String getFeeDescription(FinancialTransaction pTxn) {
        String description = null;
        BillingDetail billingDetail = pTxn.getBillingDetail();

        if (billingDetail != null) {
            OfferingServiceChargeType oscType = billingDetail.getOfferingServiceChargeType();
            //todo:Offerings find first only
            DomainEntitySet<OfferingServiceChargeGroup> oscGroups = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(pTxn.getCompany(), oscType);

            if (oscGroups != null && oscGroups.size() > 0) {
                //Descriptions must be the same for the same offering service charge type for a single company, so just use the first one
                description = oscGroups.get(0).getDescription();
            }
        }

        if(pTxn.isSalesTaxTransaction()) {
            description += " (Sales Tax)";
        }

        return description;
    }

    public static SpcfDecimal getPaycheckNetAmount(FinancialTransaction pFinTxn) {
        SpcfDecimal paycheckNetAmount = SpcfMoney.createInstance("0.00");
        Paycheck paycheck = pFinTxn.getPaycheckSplit().getPaycheck();

        for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
            paycheckNetAmount = paycheckNetAmount.add(paycheckSplit.getPaycheckSplitAmount());
        }

        return paycheckNetAmount;
    }

    public static SpcfDecimal getBillPaymentAmount(FinancialTransaction pFinTxn) {
        SpcfDecimal billPaymentAmount = SpcfMoney.createInstance("0.00");
        BillPayment billPayment = pFinTxn.getBillPaymentSplit().getBillPayment();

        for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
            billPaymentAmount = billPaymentAmount.add(billPaymentSplit.getAmount());
        }

        return billPaymentAmount;
    }

    public static FinancialTransaction retrieveCreditForReversal(FinancialTransaction pReversalTransaction) {
        DomainEntitySet<FinancialTransaction> finTxns =
                FinancialTransaction.findFinancialTransactions(
                        pReversalTransaction.getCompany(),
                        pReversalTransaction,
                        TransactionTypeCode.EmployerDdReversalRefundCredit);

        return finTxns.isEmpty() ? null : finTxns.get(0);
    }

    public static FinancialTransaction getDebitTransactionForPayrollRun(PayrollRun pPayrollRun,
                                                                        TransactionTypeCode pTxnType,
                                                                        TransactionStateCode pTxnState) {
        DomainEntitySet<FinancialTransaction> txnList =
                pPayrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{pTxnType},
                        new TransactionStateCode[]{pTxnState});

        return txnList.isEmpty() ? null : txnList.get(0);
    }

    public static String formatMoney(SpcfDecimal pAmount) {
        // Dollar Amounts: $###,###.## - no zero padding. ($500,000.00)

        return String.format("$%,.2f", new BigDecimal(pAmount.toString()));
    }

    public static String formatTime(SpcfCalendar pCal) {
        // Time Date and Stamps:
        //  a. MONTH DD (exclude leading zeroes), YYYY (i.e., January 4, 2010)
        //  b. HH:MM (exclude leading zeroes), (AM/PM) Pacific Time (i.e., 3:03 AM Pacific Time)

        return pCal.format("h:mm a 'Pacific Time'");
    }

    public static String formatDate(SpcfCalendar pCal) {
        // Time Date and Stamps:
        //  a. MONTH DD (exclude leading zeroes), YYYY (i.e., January 4, 2010)
        //  b. HH:MM (exclude leading zeroes), (AM/PM) Pacific Time (i.e., 3:03 AM Pacific Time)

        return pCal.format("MMMM d, yyyy");
    }

    public static SpcfCalendar parseAsLocalDateTime(String pPattern, String pDateTime) {
        SpcfCalendar cal = SpcfCalendar.parse(pPattern, pDateTime);
        SpcfCalendar local = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        local.setValues(cal.getYear(), cal.getMonth(), cal.getDay(), cal.getHour(),
                cal.getMinute(), cal.getSecond(), cal.getMillisecond());

        return local;
    }

    public static String formatDateAddBusinessDays(SpcfCalendar pCal, int pAddDays) {
        SpcfCalendar date = pCal.copy();

        if (pAddDays != 0) {
            CalendarUtils.addBusinessDays(date, pAddDays);
        }

        return formatDate(date);
    }

    public static String formatDateAddCalendarDays(SpcfCalendar pCal, int pAddDays) {
        SpcfCalendar date = pCal.copy();

        if (pAddDays != 0) {
            date.addDays(pAddDays);
        }

        return formatDate(date);
    }

    public static String getCbaLastFour(CompanyEvent pCompanyEvent) {
        String cbaLastFour = null;
        String accountId = getDetailString(pCompanyEvent, EventDetailTypeCode.CompanyBankAccountId);

        if (accountId != null) {
            CompanyBankAccount account = getById(CompanyBankAccount.class, accountId);

            if (account != null) {
                String accountNumber = account.getBankAccount().getAccountNumber();

                // if account number is > 4 characters, get last four, else get last digit only
                if (accountNumber.length() > 4) {
                    cbaLastFour = accountNumber.substring(accountNumber.length() - 4);
                } else if (accountNumber.length() > 0) {
                    cbaLastFour = accountNumber.substring(accountNumber.length() - 1);
                }
            }
        }

        return cbaLastFour;
    }

    public static String getCbaLastFour(Company pCompany) {
        String cbaLastFour = "";
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(pCompany);

        if (cba != null)
            cbaLastFour = getBALastFourDigit(cba.getBankAccount());

        return cbaLastFour;
    }

    public static String getCbaLastFour(FinancialTransaction pTxn) {
        String cbaLastFour = null;
        CompanyBankAccount cba = pTxn.getCompanyBankAccountIncludingExpired();

        if (cba != null)
            cbaLastFour = getBALastFourDigit(cba.getBankAccount());

        return cbaLastFour;
    }

    /** @return Last 4 digits of BA */
    public static String getBALastFourDigit(BankAccount ba) {
        String baLastFour = "";

        if (ba != null)
            baLastFour = getBALastFourDigit(ba.getAccountNumber());

        return baLastFour;
    }

    /** @return Last four digits if account number is > 4 characters, else get last digit only */
    public static String getBALastFourDigit(String accountNumber) {
        String baLastFour = "";
        if (accountNumber != null) {
            if (accountNumber.length() > 4) {
                baLastFour = accountNumber.substring(accountNumber.length() - 4);
            } else if (accountNumber.length() > 0) {
                baLastFour = accountNumber.substring(accountNumber.length() - 1);
            }
        }

        return baLastFour;
    }

    public static String getServiceTypeDescription(CompanyService pCompanyService) {
        switch (pCompanyService.getService().getServiceCd()) {
            case BillPayment:
                return "Direct Deposit for Vendors";
            case DirectDeposit:
                return "Direct Deposit";
            case Tax:
                return "Assisted";
            case CheckDistribution:
                return "Check Distribution";
            default:
                return "Payroll";
        }
    }

    public static String getSubTypeDescription(EntitlementCode pEntitlementCode) {
        if(pEntitlementCode==null || pEntitlementCode.getSubtypeDescription()==null) {
            return null;
        } else if(pEntitlementCode.getIsUsageBilling()) {
            if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Dummy")) {
                return "QuickBooks Payroll";
            } else if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Enhanced")) {
                return "QuickBooks Enhanced Payroll";
            } else if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Basic")) {
                return "QuickBooks Basic Payroll";
            } else if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Annual Enhanced")) {
                return "QuickBooks Enhanced Payroll";
            } else if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Annual Basic")) {
                return "QuickBooks Basic Payroll";
            } else if (pEntitlementCode.getSubtypeDescription().equals("DIY Usage Billing Annual Dummy")) {
                return "QuickBooks Payroll";
            } else {
                return pEntitlementCode.getSubtypeDescription();
            }
        } else {
            return pEntitlementCode.getSubtypeDescription();
        }
    }


    public static String getServiceSubStatus(CompanyEvent pCompanyEvent) {
        String serviceStatus = getDetailString(pCompanyEvent, EventDetailTypeCode.ServiceStatus);
        return serviceStatus;
    }
}

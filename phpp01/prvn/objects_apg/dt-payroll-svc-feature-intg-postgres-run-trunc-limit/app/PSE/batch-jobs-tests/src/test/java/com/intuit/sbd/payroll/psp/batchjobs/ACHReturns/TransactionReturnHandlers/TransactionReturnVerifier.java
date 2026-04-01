package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author kmuthurangam
 * <p>
 * Utility methods to validate Transactions Returns
 */
public class TransactionReturnVerifier {

    public void verifyEmployerVerificationDebitFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions) {
        verifyDebitFinancialTransactionReturn(returnedFinancialTransactions, false);
    }

    public void verifyDebitFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions) {
        verifyDebitFinancialTransactionReturn(returnedFinancialTransactions, true);
    }

    public void verifyDebitFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions, boolean verifyTransactionResponse) {
        // find the returned transactions
        assertEquals("number of returned transactions", 1, returnedFinancialTransactions.size());

        verifyFinancialTransactionReturn(returnedFinancialTransactions, verifyTransactionResponse);
    }

    public void verifyFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions, boolean verifyTransactionResponse) {
        for (FinancialTransaction currTxn : returnedFinancialTransactions) {
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());

            if (verifyTransactionResponse) {
                DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
                assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
            }
        }
    }

    public void verifyRedebitFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions, int expectedReturnCount) {
        assertEquals("number returned transactions", expectedReturnCount, returnedFinancialTransactions.size());
        for (FinancialTransaction currTxn : returnedFinancialTransactions) {
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (TransactionType.isRedebitTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                assertEquals("Transaction response for Returned REDEBIT transaction", expectedReturnCount, responses.size());
            } else {
                assertEquals("Transaction response for Returned transaction", 1, responses.size());
            }
        }
    }

    public void verifyRefundFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions) {
        assertEquals("4 returned transactions", 4, returnedFinancialTransactions.size());
        for (FinancialTransaction currTxn : returnedFinancialTransactions) {
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeRefundCredit
                    || currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit) {
                assertEquals("Transaction response for Returned " + currTxn.getTransactionType().getTransactionTypeCd() + " transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned " + currTxn.getTransactionType().getTransactionTypeCd() + " transaction", 1, responses.size());
            }
        }
    }

    public void verifyDebitReturnedFinancialTransactionReturn(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions) {
        assertEquals("Number of fee redebits returned", 1, returnedFinancialTransactions.size());
    }

    public void verifyVariableBankDebitAmount(DomainEntitySet<FinancialTransaction> employerVerificationReturnTransferSet, FinancialTransaction financialTransaction) {
        boolean matchedAmount = false;
        financialTransaction = Application.refresh(financialTransaction);
        String sku = financialTransaction.getSku();
        for (FinancialTransaction employerVerificationReturnTransfer : employerVerificationReturnTransferSet) {
            if (employerVerificationReturnTransfer.getFinancialTransactionAmount().equals(financialTransaction.getFinancialTransactionAmount())) {
                matchedAmount = true;
                assertEquals("SKU", sku, employerVerificationReturnTransfer.getSku());
            }
        }
        assertTrue("Found transfer with correct amount", matchedAmount);
    }

    public void verifyIntuitEmployerVerificationReturnTransfer(DomainEntitySet<FinancialTransaction> employerVerificationReturnTransfers, DomainEntitySet<FinancialTransaction> financialTransactionReturn) {
        assertEquals("Number of returned transactions", 1, financialTransactionReturn.size());
        assertEquals("Transfer financial transactions ", 2, employerVerificationReturnTransfers.size());
    }

    public void verifyPayrollStatus(PayrollRun payrollRun, PayrollStatus expectedPayrollStatus) {
        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status", expectedPayrollStatus, payrollRun.getPayrollRunStatus());
    }

    public void verifyDebitReturnFee(Company company, PayrollRun payrollRun) {
        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must be a new fee FT and it must be related to this payroll run
        assertEquals("Fee Transactions", 1, feeFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                feeFTs.get(0).getPayrollRun().getSourcePayRunId());

        // that fee must be the DebitReturnFee
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());

        // find the tax transactions that MUST have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        //there must be a new tax FT
        assertEquals("Tax Transactions", 1, taxFTs.size());
        FinancialTransaction taxFT = taxFTs.get(0);
        OfferingServiceChargeType ofct = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(taxFT.getSku());
        assertEquals("Sku is return fee sku", OfferingServiceChargeType.DebitReturnFee, ofct);
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                taxFT.getPayrollRun().getSourcePayRunId());
    }

    public void verifyNoDebitReturnFee(Company company) {
        // find the fee transactions that should NOT have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must NOT be a new fee FT
        assertEquals("Fee Transactions", 0, feeFTs.size());

        // find the tax transactions that must NOT have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                company.getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should NOT have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must NOT be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 0, redebitFTs.size());

        //there must NOT be a new tax FT
        assertEquals("Tax Transactions", 0, taxFTs.size());
    }

    public void verifyRedebitFinancialTransactions(Company company, PayrollRun payrollRun) {
        // find the ER DD Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 1, redebitFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());
    }

    public void verifyBankAccountStatus(Company company, BankAccount debitBankAccount, BankAccountStatus expectedStatus) {

        CompanyBankAccount cba = CompanyBankAccount
                .findCompanyBankAccountIncludingExpired(company, debitBankAccount);
        BankAccountStatus cbaStatus = cba.getStatusCd();

        //Ensure the company bank account was NOT deactivated
        assertEquals("Company bank account status", expectedStatus, cbaStatus);
    }

    public void verifyEmployeeDdReversalDebitFinancialTransaction(Company company) {
        SpcfMoney amount = new SpcfMoney("30.00");
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployeeDdReversalDebit);
        assertEquals("EmployeeDdReversalDebit FTs", 1, financialTransactions.size());
        assertEquals("EmployeeDdReversalDebit FT Amount", amount, financialTransactions.get(0).getFinancialTransactionAmount());
    }

    public void verifyEmployeeDdReversalDebitCompanyEmailEvent(SpcfMoney reversalAmount) {
        DomainEntitySet<CompanyEventEmail> companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1);
        assertEquals("CompanyEventEmail records", 1, companyEventEmails.size());
        DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = companyEventEmails.get(0).getEmailParamForEmailEvent(EventEmailParamTypeCode.ReversalPendingList);
        assertEquals("CompanyEventEmailParam with ReversalPendingLists", 1, companyEventEmailParams.size());

        String emailString = "&#8217;s direct deposit in the amount " + EmailUtils.formatMoney(reversalAmount) + " will be reversed<br>";
        assertTrue("Amount in Email param", companyEventEmailParams.get(0).getValue().indexOf(emailString) > 0);
    }

    public void verifyNSFCompanyEvent(Company company, DomainEntitySet<FinancialTransaction> financialTransactionSet, NSFSubTypeType expectedNSFSubType) {
        verifyNSFCompanyEvent(company, financialTransactionSet, null, expectedNSFSubType, true);
    }

    public void verifyNSFCompanyEvent(Company company, DomainEntitySet<FinancialTransaction> financialTransactionSet, SpcfCalendar fromDate, NSFSubTypeType expectedNSFSubType, boolean verifyCompanyEventDetails) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NSF, CompanyEventStatus.Active, fromDate, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent transactionReturnEvent = companyEventsList.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(expectedNSFSubType),
                transactionReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                transactionReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        if (verifyCompanyEventDetails) {
            verifyCompanyEventDetails(financialTransactionSet, transactionReturnEvent);
        }

    }

    public void verifyDDDebitReturnCompanyEvent(Company company, DomainEntitySet<FinancialTransaction> financialTransactionSet) {
        verifyDDDebitReturnCompanyEvent(company, financialTransactionSet, null, true);
    }

    public void verifyDDDebitReturnCompanyEvent(Company company, DomainEntitySet<FinancialTransaction> financialTransactionSet, SpcfCalendar fromDate, boolean verifyCompanyEventDetails) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn, CompanyEventStatus.Active, fromDate, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent transactionReturnEvent = companyEventsList.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                transactionReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        if (verifyCompanyEventDetails) {
            verifyCompanyEventDetails(financialTransactionSet, transactionReturnEvent);
        }
    }

    public void verifyDDRejectReturnCompanyEvent(Company company) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.Issued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", null,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
    }

    public void verifyDDRejectReturnOnHoldCompanyEvent(Company company) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.NotIssued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", EnumUtils.getReadableName(RefundStatusReasonType.CompanyOnHold),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
    }

    public void verifyDDRejectReturnCBADeactivatedCompanyEvent(Company company, FinancialTransaction finTxn) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.NotIssued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", EnumUtils.getReadableName(RefundStatusReasonType.BankAccountInactive),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));

            assertEquals("EE Txn Id", finTxn.getId().toString(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        }
    }

    public void verifyReversalReturnCompanyEvent(Company company, int expectedCompanyEvent) {
        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ReversalReturn,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", expectedCompanyEvent, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ReversalReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
    }

    public void verifyFeeReturnCompanyEvent(Company company, int expectedCompanyEvent) {
        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", expectedCompanyEvent, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
    }

    public void verifySalexTaxReturnCompanyEvent(Company company, int expectedCompanyEvent) {
        // make sure the right events were created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company, EventTypeCode.SalesTaxReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", expectedCompanyEvent, events.size());
        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.SalesTaxReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
    }

    public void verifyERRefundReturnCompanyEvent(Company company) {
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ERRefundReturn,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ERRefundReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
    }

    public void verifyFeeCreated(DomainEntitySet<FinancialTransaction> financialTransactionSet, OfferingServiceChargeType offeringServiceChargeType) {

        FinancialTransaction returnedFee = null;

        for (FinancialTransaction currTxn : financialTransactionSet) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (offeringServiceChargeType == osc) {
                    returnedFee = currTxn;
                }
            }
        }

        assertNotNull("Fee not found", returnedFee);
    }

    public void verifyNoDebitReturnFeeForFeeOnlyNSF(Company company) {
        // Verify the fee was not charged for the NSF.
        verifyFeeOnlyNsfFee(company, TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created, false);
    }

    public void verifyFeeAndSalesTaxReturnForReversal(Company company) {

        verifyFeeTransactionReturnForOfferingServiceCharge(company, TransactionTypeCode.EmployerFeeDebit, OfferingServiceChargeType.ReversalFee);

        verifyFeeTransactionReturnForOfferingServiceCharge(company, TransactionTypeCode.ServiceSalesAndUseTax, OfferingServiceChargeType.ReversalFee);
    }

    public void verifyCBAVerifyReturnCompanyEvent(Company company, int expectedEventCount) {
        // make sure the right company event got created
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", expectedEventCount, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("Verification Status", EnumUtils.getReadableName(VerificationStatusType.PendingVerification),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.VerificationStatus));
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
    }

    public void verifyCompanyEventDetails(DomainEntitySet<FinancialTransaction> financialTransactionSet, CompanyEvent nsfEvent) {
        Collection<String> eventDetailValue = nsfEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", financialTransactionSet.size(), eventDetailValue.size());

        for (String currValue : eventDetailValue) {
            assertTrue("Financial transaction id associated with event", financialTransactionSet.stream().anyMatch(financialTransaction -> Objects.equals(financialTransaction.getId().toString(), currValue)));
        }
    }

    public void verifyTransactionReturn(Company company, PayrollRun payrollRun, TransactionReturnStatusCode expectedTransactionReturnStatusCode) {
        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);

        assertEquals("There is one txn return", 1, txnReturn.size());

        verifyTransactionReturnStatus(txnReturn.getFirst(), expectedTransactionReturnStatusCode);
    }

    public void verifyTransactionReturnStatus(TransactionReturn transactionReturn, TransactionReturnStatusCode expectedTransactionReturnStatusCode) {
        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", expectedTransactionReturnStatusCode, transactionReturn.getReturnStatusCd());
    }

    public void verifyStrikeEvent(Company company, ServiceSubStatusCode serviceSubStatusCode, StrikeReason strikeReason) {
        verifyCompanyOnHold(company, serviceSubStatusCode);

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(strikeReason),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
    }

    public void verifyCompanyOnHold(Company company, ServiceSubStatusCode serviceSubStatusCode) {
        assertTrue("Company is on hold", company.isCompanyOnHold());
        verifyCompanyOnHoldReason(company.getCurrentOnHoldReasonsDomainEntitySet(), serviceSubStatusCode);
    }

    public void verifyCompanyNotOnHold(Company company, DomainEntitySet<OnHoldReason> onHoldReasonList) {
        // verify onhold is created with code AchRejectR1R9
        Assert.assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());
    }

    public void verifyCompanyOnHoldReason(DomainEntitySet<OnHoldReason> onHoldReasonList, ServiceSubStatusCode serviceSubStatusCode) {
        // verify onhold is created with code AchRejectR1R9
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", serviceSubStatusCode, onHoldReasonList.get(0).getOnHoldReasonCd());
    }

    public void verifyEmployerDdRejectRefundCreditUsingChangedBankAccount(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions, CompanyBankAccount companyBankAccount) {
        FinancialTransaction finTxn = returnedFinancialTransactions.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());
        // verify the created FeeDebit transaction is associated with the new bank account
        assertTrue("Refund Transaction bank account",
                companyBankAccount.getBankAccount().equals(ddRejectRefundFinTxns.get(0).getCreditBankAccount()));
    }

    public void verifyEmployerDdRejectRefundCreditUsingSameBankAccount(DomainEntitySet<FinancialTransaction> returnedFinancialTransactions) {
        FinancialTransaction finTxn = returnedFinancialTransactions.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());

        CompanyBankAccount companyBankAccount = employerDdDebitFinTxns.get(0).getCompanyBankAccount(
        );

        assertEquals("Company Bank Account ", companyBankAccount.getBankAccount(),
                ddRejectRefundFinTxns.get(0).getCreditBankAccount());
    }

    public void verifyNoEmployerDdRejectRefundCredit(Company company) {
        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 0, ddRejectRefundFinTxns.size());
    }

    /**
     * Verify Email
     */

    public void verifyCompanyEventEmail(Company company, EventTypeCode eventTypeCode, EventEmailTemplateTypeCode eventEmailTemplateTypeCode, Map<EventEmailParamTypeCode, String> emailParamTypeCodeStringMap) {
        CompanyEventEmail companyEventEmail = verifyCompanyEventEmailTemplate(company, eventTypeCode, eventEmailTemplateTypeCode);

        verifyCompanyEventEmailParams(companyEventEmail, emailParamTypeCodeStringMap);
    }

    public CompanyEventEmail verifyCompanyEventEmailTemplate(Company company, EventTypeCode eventTypeCode, EventEmailTemplateTypeCode eventEmailTemplateTypeCode) {
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                eventTypeCode, CompanyEventStatus.Active, null, null);

        CompanyEventEmail companyEventEmail = Application.find(CompanyEventEmail.class, CompanyEventEmail.CompanyEvent().equalTo(companyEventsList.getFirst())).get(0);
        assertEquals("Email Template = " + companyEventEmail.getEmailTemplateTypeCd().toString(), companyEventEmail.getEmailTemplateTypeCd().toString(), eventEmailTemplateTypeCode.toString());

        return companyEventEmail;
    }

    public void verifyCompanyEventEmailParams(CompanyEventEmail companyEventEmail, Map<EventEmailParamTypeCode, String> emailParamTypeCodeStringMap) {
        emailParamTypeCodeStringMap.forEach((eventEmailParamTypeCode, value) -> {
            assertTrue("Email Event Param Value not matches", Objects.equals(companyEventEmail.getEmailParamValue(eventEmailParamTypeCode), value));
        });
    }

    private void verifyFeeTransactionReturnForOfferingServiceCharge(Company company, TransactionTypeCode transactionTypeCode, OfferingServiceChargeType offeringServiceChargeType) {
        DomainEntitySet<FinancialTransaction> c2FinTxns = FinancialTransaction
                .findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(),
                        transactionTypeCode, TransactionStateCode.Returned);
        FinancialTransaction returnTransaction = null;
        for (FinancialTransaction currTxn : c2FinTxns) {
            if (transactionTypeCode == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (offeringServiceChargeType == osc) {
                    returnTransaction = currTxn;
                }
            }
        }

        assertNotNull(returnTransaction);
    }

    private void verifyFeeOnlyNsfFee(Company company, TransactionTypeCode txType, TransactionStateCode txState, boolean charged) {

        // Query to find the qualifying transactions.
        DomainEntitySet<FinancialTransaction> nsfFeeTx = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(), txType, txState);
        // Get the fee so we can compare amounts.
        Fee fee = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.FeeOnlyNSFFee)).getFirst();

        boolean foundFee = false;
        for (FinancialTransaction ft : nsfFeeTx) {
            if (ft.getFinancialTransactionAmount().equals(fee.getAmount())) {
                foundFee = true;
                break;
            }
        }
        assertTrue("Fee Only NSF Fee", foundFee == charged);
    }
}

package com.intuit.sbd.payroll.psp;

import org.junit.runner.RunWith;
import com.intuit.sbd.payroll.psp.junit.MethodSuite;

/**
 * This is the test suite with all tests to be executed as part of pre-check-in procedure.
 * 
 * @author Wiktor Kozlik
 */

@RunWith(MethodSuite.class)
@MethodSuite.SuiteMethods({
        // SAP Adapter
        "com.intuit.sbd.payroll.psp.adapters.sap.SAPAdapterTests.testPayrollRunAction",        

        // Sales Tax Gateway
        "com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayTests.testHappyPath",

        // Batch Jobs
        "com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.DirectDepositReversalReturnTests.testDDReversalReturnWrittenOff",
        "com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.GenericDebitReturnTests.testGenericDebitReturnsForOffloadedDebit",
        "com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.TestProcessMissedACHTransactions.testProcessMissedACHTxnsForMultipleCompanies_HappyPath",
        "com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.TestGemsUploadBatchProcess.testGemsUploadProcess",
        "com.intuit.sbd.payroll.psp.batchjobs.offload.TestOffloadACHTransactions.testOffloadThenReversalsPending",
        "com.intuit.sbd.payroll.psp.batchjobs.salestax.TestSalesTaxExceptionProcess.testMainProcess_WithMultipleOffloads",

        // Business Entities
        "com.intuit.sbd.payroll.psp.domain.BillingManagerTests.testHappyPathUpdateBillingDetails_WithSalesTax",
        "com.intuit.sbd.payroll.psp.domain.FinancialTransactionBETests.testFinancialTransactionStatesByCompanyAndTxId",

        // PayrollServicesAPI
        "com.intuit.sbd.payroll.psp.api.finders.CompanyBankAccountFinderTests.findActiveCompanyBankAccountTest",
        "com.intuit.sbd.payroll.psp.api.finders.PayrollRunFinderTests.testFindPayrollRuns",

        // Processes-Core
        "com.intuit.sbd.payroll.psp.processes.AddCompanyBankAccountCoreTests.addCompanyBankAccountSuccessful",
        "com.intuit.sbd.payroll.psp.processes.AddCompanyBankAccountCoreTests.addCompanyBankAccountFailsFraudControlsEEBA",
        "com.intuit.sbd.payroll.psp.processes.AddCompanyCoreTests.addCompanyCoreSuccess",
        "com.intuit.sbd.payroll.psp.processes.AddCompanyCoreTests.addQBDTCompanyCoreSuccess",
        "com.intuit.sbd.payroll.psp.processes.AddEmployeeBankAccountCoreTests.employeeBankAccountExistsAndNotInactive",
        "com.intuit.sbd.payroll.psp.processes.AddEmployeeCoreTests.addEmployeeCoreExistingInactiveEE",
        "com.intuit.sbd.payroll.psp.processes.AddEscalationProcessTests.happy_ee",
        "com.intuit.sbd.payroll.psp.processes.AddEscalationProcessTests.happy_er",
        "com.intuit.sbd.payroll.psp.processes.AddOnHoldStatusCoreTests.addOnHoldStatusCore_Success",
        "com.intuit.sbd.payroll.psp.processes.AddServiceCoreTests.addServiceCoreSuccess",
        //"com.intuit.sbd.payroll.psp.processes.CancelCompanyCoreTests.testCancelHasPendingFinTxns",
        "com.intuit.sbd.payroll.psp.processes.CancelERFinancialTxCoreTests.testERCancel",
        "com.intuit.sbd.payroll.psp.processes.CancelERFinancialTxCoreTests.testERCancelNSF",
        "com.intuit.sbd.payroll.psp.processes.CancelServiceCoreTests.testERCancelNSF",
        "com.intuit.sbd.payroll.psp.processes.CancelServiceCoreTests.cancelServiceCoreSuccess",
        "com.intuit.sbd.payroll.psp.processes.ChangeCompanyBankAccountCoreTests.changeCompanyBankAccount_NotActive",
        "com.intuit.sbd.payroll.psp.processes.ChangeCompanyBankAccountCoreTests.testChangeCBAWithPendingTransactions",
        "com.intuit.sbd.payroll.psp.processes.ChangeCompanyBankAccountCoreTests.testVerificationTransactionsNotMoved",
        "com.intuit.sbd.payroll.psp.processes.ClaimOfferCoreTests.addCompanyOfferSuccess",
        "com.intuit.sbd.payroll.psp.processes.CreateCompanyPINCoreTests.createRandomCompanyPIN_Success",
        "com.intuit.sbd.payroll.psp.processes.DeactivateCompanyBankAccountCoreTests.deactivateCompanyBankAccountSuccessful",
        "com.intuit.sbd.payroll.psp.processes.DeactivateCompanyBankAccountCoreTests.deactivateCBASucess_HasUnResolvedTransactionReturns",
        "com.intuit.sbd.payroll.psp.processes.DeactivateCompanyBankAccountCoreTests.deactivateCBASuccess_HasPendingTransactions",
        "com.intuit.sbd.payroll.psp.processes.DeactivateEmployeeBankAccountCoreTests.deactivateEmployeeBankAccountSuccessful",
        "com.intuit.sbd.payroll.psp.processes.DeactivateEmployeeCoreTests.deactivateEmployeeExistsActive",
        "com.intuit.sbd.payroll.psp.processes.ERFeeAddCoreTests.testAddFeeForACHSettlement",
        "com.intuit.sbd.payroll.psp.processes.ERFinancialTxRefundCoreTests.testRefundACH",
        "com.intuit.sbd.payroll.psp.processes.MovePendingTransactionToBankAccountTests.testMoveTxnsCompanyOnCreditSide",
        "com.intuit.sbd.payroll.psp.processes.PayrollSubmitCoreTests.testBackdatedPayroll():void",
        "com.intuit.sbd.payroll.psp.processes.PayrollSubmitCoreTests.testNextValidCheckDate_PastCutoffSameDay",
        "com.intuit.sbd.payroll.psp.processes.PayrollSubmitCoreTests.testPaycheckDateOnWeekend",
        "com.intuit.sbd.payroll.psp.processes.ReinitiateBankAccountRandomDebitsCoreTests.testReinitiateRandomDebits_Success",
        "com.intuit.sbd.payroll.psp.processes.RemoveOnHoldStatusCoreTests.removeOnHoldStatusCore_Success",
        "com.intuit.sbd.payroll.psp.processes.ResetBankVerifyRetryCountCoreTests.updateCompanyCore_CompanyHasPendingTxns",
        "com.intuit.sbd.payroll.psp.processes.ResetBankVerifyRetryCountCoreTests.updateCompanyCore_Successful",
        "com.intuit.sbd.payroll.psp.processes.TransactionCancelCoreTests.testImplicitCancellationOfAllTransactionsForOffloadedDebitPayroll",
        "com.intuit.sbd.payroll.psp.processes.TransactionCancelCoreTests.testSingleTransactionAlreadyExecutedForOffloadedDebitPayroll",
        "com.intuit.sbd.payroll.psp.processes.TransactionRecallCoreTests.testRecallSelectedTransactionsForPendingPayroll",
        "com.intuit.sbd.payroll.psp.processes.TransactionRecallCoreTests.testTransactionRecallWholePayrollBeforeCutoff",
        "com.intuit.sbd.payroll.psp.processes.TransactionReverseCoreTests.testAchReversalOfSingleTransactionWithFee",
        "com.intuit.sbd.payroll.psp.processes.UpdateBankReturnCoreTests.testUpdateBankReturn",
        "com.intuit.sbd.payroll.psp.processes.UpdateCompanyBankAccountCoreTests.updateCompanyBankAccountSuccessful",
        "com.intuit.sbd.payroll.psp.processes.UpdateCompanyCoreTests.updateCompanyCoreSuccess",
        "com.intuit.sbd.payroll.psp.processes.UpdateCompanyFundingModelCoreTests.testSucessfulUpdateFundingModel",
        "com.intuit.sbd.payroll.psp.processes.UpdateCompanyPINCoreTests.updateCompanyPIN_Successful",
        "com.intuit.sbd.payroll.psp.processes.UpdateEmployeeBankAccountCoreTests.updateEmployeeBankAccountBankAccountSuccessful_CreateNewOne",
        "com.intuit.sbd.payroll.psp.processes.UpdateEmployeeCoreTests.updateEmployeeTaxId",
        "com.intuit.sbd.payroll.psp.processes.UpdateServiceCoreTests.updateServiceCoreSuccess",
        "com.intuit.sbd.payroll.psp.processes.UpdateServiceStatusCoreTests.updateServiceStatusCore_Success",
        "com.intuit.sbd.payroll.psp.processes.UpdateSourcePayrollParameterCoreTests.testParameterCdChange",
        "com.intuit.sbd.payroll.psp.processes.UpdateSystemParameterCoreTests.testUpdateSystemParameterSuccess",
        "com.intuit.sbd.payroll.psp.processes.VerifyCompanyBankAccountCoreTests.verifyCompanyBankAccountSuccessful",
        "com.intuit.sbd.payroll.psp.processes.VerifyCompanyPINCoreTests.VerifyCompanyPIN_Success",
        "com.intuit.sbd.payroll.psp.processes.VoidDDFinancialTransactionCoreTests.testTxVoid",

        // Processes
        "com.intuit.sbd.payroll.psp.processes.AddEmployeeReturnRefundTransactionTests.testEEReturnRefundProcessForACHSettlementType",
        "com.intuit.sbd.payroll.psp.processes.AddEmployeeReturnTransferTransactionTests.testEEReturnTransferProcess",
        "com.intuit.sbd.payroll.psp.processes.AddEmployerReturnRefundTransactionTests.testERReturnRefundProcessForACHSettlementType",
        "com.intuit.sbd.payroll.psp.processes.AddFeeTransferTransactionTests.testFeeTransferProcessForNSF_FEE",
        "com.intuit.sbd.payroll.psp.processes.AddFeeTransferTransactionTests.testFeeTransferProcessForREVERSE_FEE",
        "com.intuit.sbd.payroll.psp.processes.AddIntuit5DayReturnTransferTransactionTests.testIntuit5DayReturnTransferProcess",
        "com.intuit.sbd.payroll.psp.processes.AddRecoverBadDebtTransactionTests.testBadDebtRecoveryProcess",
        "com.intuit.sbd.payroll.psp.processes.AddRefundTransactionTests.testDDRefundProcessForACHSettlementType",
        "com.intuit.sbd.payroll.psp.processes.AddRepaymentTransactionsTests.testRepaymentPendingReversalsDoesNotCover",
        "com.intuit.sbd.payroll.psp.processes.AddStrikeDDProcessTests.addStrikeSuccess",
        "com.intuit.sbd.payroll.psp.processes.AddWriteOffBadDebtTransactionTests.testBadDebtWriteOffProcess",
        "com.intuit.sbd.payroll.psp.processes.CancelStrikeDDProcessTests.cancelStrikeSuccess",
        "com.intuit.sbd.payroll.psp.processes.CheckDDLimitsTests.testQualifiesForIncrease_OverCompanyMax",
        "com.intuit.sbd.payroll.psp.processes.PayrollSubmitDDTests.testFraudControls_4EEsThreeExisting",
        "com.intuit.sbd.payroll.psp.processes.UpdateDDLimitsTests.testSucessfulUpdateLimits",
        "com.intuit.sbd.payroll.psp.api.other.EventTests.compareEventDetailCodesToStaticDataTest",
        "com.intuit.sbd.payroll.psp.api.other.EventDetailTests.compareEventDetailCodesToStaticDataTest",

        })

public class PreCheckInTestSuite {

}

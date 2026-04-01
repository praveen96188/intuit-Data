package psp.sap.service.interfaces
{
	import mx.collections.ArrayCollection;
	import mx.rpc.IResponder;
	
	public interface IPayrollRunService extends IPSPService
	{

		function getLineItems(pSourceSystemCd:String, pCompanyId:String, pPaycheckGseq:String, responder:IResponder):void;

		function findPaychecks(pCompanyId:String, pSourceSystemCd:String, pSourcePayrollRunId:String, responder:IResponder):void;

        function findPItems(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function findPayrollRunByPayrollRunId(pPayrollRunId:String, responder:IResponder):void;

		function cancelPayrollTransaction(companyId:String, sourceSystemCd:String, transactionIds:ArrayCollection, payrollRunId:String, responder:IResponder):void;

		function cancelAdjustment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void;

		function cancelBillPaymentTransaction(companyId:String, sourceSystemCd:String, payrollRunId:String, transactionIds:ArrayCollection, responder:IResponder):void;

		function reversePayrollRunTransactions(sourceSystemCd:String, companyId:String, transactionIds:ArrayCollection, payrollRunId:String, pChargeFee:Boolean, pFeeTxnDate:Date, pFeeSettlementType:String, pInitiateForCollection:Boolean, responder:IResponder):void;

		function findEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findVendorTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findCancelableTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void;

		function findReversableEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findIntuitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findEmployerTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findAgencyTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function findPayrollRunsByDate(companyId:String, sourceSystemCd:String, payrollTypes:ArrayCollection, fromDate:Date, toDate:Date, responder:IResponder):void;

		function findPayrollRunBalanceDue(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void;

		function findPayrollRun(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void;

		function findERPayableRefundTransactions(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function findLedgerAccountsByPayroll(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void;

		function getLedgerAccountBalance(sourceSystemCd:String, sourceCompanyId:String, ledgerAccountCd:String, responder:IResponder):void;

		function getLedgerAccountBalanceForLaw(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLawId:String, responder:IResponder):void;

		function addFinancialLedgerAdjustmentTransaction(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, debitAccountCode:String, creditAccountCode:String, pAmount:Number, pLawId:String, pNoteText:String, responder:IResponder):void;

		function addFeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pSettlementTypeCd:String, pTxnDate:Date, fees:ArrayCollection, responder:IResponder):void;

		function addFeeRedebitTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pOldTxnId:String, responder:IResponder):void;

		function addWireExpectedDateTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pCollectionStageCd:String, pActionEvent:String, pTxnDate:Date, pSendLastEmail:Boolean, responder:IResponder):void;

		function refundEmployerTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pFinancialTxAmt:Number, pTxnDate:Date, pSettlementType:String, responder:IResponder):void;

		function voidTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void;

		function cancelTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void;

		function findTransactionsByLedgerAccount(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, responder:IResponder):void;

		function findTransactionsByLedgerAccountAndPayroll(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, pPayRunId:String, responder:IResponder):void;

		function findLedgerAccountByPayrollAndLedgerCode(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLedgerAccountCode:String, responder:IResponder):void;

		function getPayrollLaws(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void;

		function findLedgerAccounts(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void;

		function redebitPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void;

		function addRefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void;

		function addWriteOffBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void;

		function addWriteOffEmployeeBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void;

		function addIntuit5DayReturnTransfer(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void;

		function voidPayrollTaxPayment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void;

		function reissuePayrollTaxPayment(companyId:String, sourceSystemCd:String, sourcePayrollRunId:String, transferTransactionId:String, responder:IResponder):void;

		function addRecoverBadDebtTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, collectionAgencyExpense:Number, responder:IResponder):void;

		function addEmployeeReturnTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void;

		function refundERPayable(pSourceSystemCd:String, pSourceCompanyId:String, settlementTypeCode:String, amount:Number, responder:IResponder):void;

		function applyERPayableToBalanceDue(pSourceSystemCd:String, pSourceCompanyId:String, payrollRunId:String, amount:Number, responder:IResponder):void;

		function addFeeTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pOfferingServiceChargeTypeCd:String, responder:IResponder):void;

		function addRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void;

		function addEmployerReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTaxAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void;

		function addEmployeeReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void;

		function getTransactionHistory(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxnId:String, responder:IResponder):void;

		function addEscalation(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pIsEmployee:Boolean, pSettlementTypeCd:String, pAmount:Number, pSettlementDate:Date, responder:IResponder):void;

		function findChaseReportForDateRange(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function addRefundRebillTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, rebill:Boolean, pFeeDebitTransactionId:String, pOverrideAmount:Number, pOverrideQuantity:int, responder:IResponder):void;

		function findPayrollUncollectedBalances(pCompanyId:String, pSourceSystemCd:String, pPayrollRunId:String, responder:IResponder):void;

		function findPayrollUnrecoveredBalances(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void;

		function findPayrollCollectedTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void;

		function findPayrollPrefundingTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void;

		function findCompanyBalance(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void;

		function findPayrollTransactionById(transactionId:String, companyId:String, responder:IResponder):void;

		function findMoneyMovementTransactions(sourceSystemCd:String, companyId:String, fromDate:Date, responder:IResponder):void;

		function findAchDetailTransactions(moneyMovementTransactionId:String, responder:IResponder, companyId:String):void;

		function getRedebitTransactionsForPayroll(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void;

		function checkPayrollForSuspectPaychecks(sourceSystemCd:String, companyId:String, sourcePayrollRunId:String, responder:IResponder):void;

		function addPrefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, payrolls:ArrayCollection, responder:IResponder):void;

		function getAllPayrollRunActions(responder:IResponder):void;

		function getTransactionTypeList(responder:IResponder):void;


	}
}

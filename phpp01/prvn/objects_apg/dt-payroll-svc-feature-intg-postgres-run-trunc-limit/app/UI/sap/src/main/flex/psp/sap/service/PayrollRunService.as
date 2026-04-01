package psp.sap.service {
    import mx.collections.ArrayCollection;
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;

    import mx.rpc.remoting.RemoteObject;

    import psp.sap.service.interfaces.IPayrollRunService;

    public class PayrollRunService extends AbstractPayrollRunService implements IPayrollRunService {
        public function PayrollRunService():void {
            remoteObjectPool = new RemoteObjectPool("payrollservice", 5);
        }

        public function get payrollRunRemoteService():RemoteObject {
            return remoteObjectPool.nextAvailable();
        }

        public function getLineItems(pSourceSystemCd:String, pCompanyId:String, pPaycheckGseq:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getLineItems(pSourceSystemCd, pCompanyId, pPaycheckGseq));
            remoteToken.addResponder(responder);
        }

        public function findPItems(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPItems(pCompanyId, pSourceSystemCd ));
            remoteToken.addResponder(responder);
        }

        public function findPaychecks(pCompanyId:String, pSourceSystemCd:String, pSourcePayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPaychecks(pCompanyId, pSourceSystemCd, pSourcePayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollRunByPayrollRunId(pPayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollRunByPayrollRunId(pPayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function cancelPayrollTransaction(companyId:String, sourceSystemCd:String, transactionIds:ArrayCollection, payrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.cancelPayrollTransaction(companyId, sourceSystemCd, transactionIds, payrollRunId));
            remoteToken.addResponder(responder);
        }

        public function cancelAdjustment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.cancelAdjustment(companyId, sourceSystemCd, payrollRunId));
            remoteToken.addResponder(responder);
        }

        public function cancelBillPaymentTransaction(companyId:String, sourceSystemCd:String, payrollRunId:String, transactionIds:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.cancelBillPaymentTransaction(companyId, sourceSystemCd, payrollRunId, transactionIds));
            remoteToken.addResponder(responder);
        }

        public function reversePayrollRunTransactions(sourceSystemCd:String, companyId:String, transactionIds:ArrayCollection, payrollRunId:String, pChargeFee:Boolean, pFeeTxnDate:Date, pFeeSettlementType:String, pInitiateForCollection:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.reversePayrollRunTransactions(sourceSystemCd, companyId, transactionIds, payrollRunId, pChargeFee, pFeeTxnDate, pFeeSettlementType, pInitiateForCollection));
            remoteToken.addResponder(responder);
        }

        public function findEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findEmployeeTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findVendorTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findVendorTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findCancelableTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findCancelableTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId));
            remoteToken.addResponder(responder);
        }

        public function findReversableEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findReversableEmployeeTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findIntuitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findIntuitTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findEmployerTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findEmployerTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findAgencyTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findAgencyTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

        public function findPayrollRunsByDate(companyId:String, sourceSystemCd:String, payrollTypes:ArrayCollection, fromDate:Date, toDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollRunsByDate(companyId, sourceSystemCd, payrollTypes, fromDate, toDate));
            remoteToken.addResponder(responder);
        }

        public function findPayrollRunBalanceDue(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollRunBalanceDue(pSourceSystemCd, pCompanyId, pSourcePayRunId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollRun(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollRun(pSourceSystemCd, pCompanyId, pSourcePayRunId));
            remoteToken.addResponder(responder);
        }

        public function findERPayableRefundTransactions(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findERPayableRefundTransactions(pCompanyId, pSourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function findLedgerAccountsByPayroll(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findLedgerAccountsByPayroll(pCompanyId, pSourceSystemCd, pSourcePayRunId));
            remoteToken.addResponder(responder);
        }

        public function getLedgerAccountBalance(sourceSystemCd:String, sourceCompanyId:String, ledgerAccountCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getLedgerAccountBalance(sourceSystemCd, sourceCompanyId, ledgerAccountCd));
            remoteToken.addResponder(responder);
        }

        public function getLedgerAccountBalanceForLaw(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLawId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getLedgerAccountBalanceForLaw(pCompanyId, pSourceSystemCd, pSourcePayRunId, pLawId));
            remoteToken.addResponder(responder);
        }

        public function addFinancialLedgerAdjustmentTransaction(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, debitAccountCode:String, creditAccountCode:String, pAmount:Number, pLawId:String, pNoteText:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addFinancialLedgerAdjustmentTransaction(pCompanyId, pSourceSystemCd, pSourcePayRunId, debitAccountCode, creditAccountCode, pAmount, pLawId, pNoteText));
            remoteToken.addResponder(responder);
        }

        public function addFeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pSettlementTypeCd:String, pTxnDate:Date, fees:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addFeeTransactions(pCompanyId, pSourceSystemCd, pSourcePayRunId, pSettlementTypeCd, pTxnDate, fees));
            remoteToken.addResponder(responder);
        }

        public function addFeeRedebitTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pOldTxnId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addFeeRedebitTransaction(pSourceSystemCd, pSourceCompanyId, pOldTxnId));
            remoteToken.addResponder(responder);
        }

        public function addWireExpectedDateTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pCollectionStageCd:String, pActionEvent:String, pTxnDate:Date, pSendLastEmail:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addWireExpectedDateTransaction(pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pCollectionStageCd, pActionEvent, pTxnDate, pSendLastEmail));
            remoteToken.addResponder(responder);
        }

        public function refundEmployerTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pFinancialTxAmt:Number, pTxnDate:Date, pSettlementType:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.refundEmployerTransaction(pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pFinancialTxAmt, pTxnDate, pSettlementType));
            remoteToken.addResponder(responder);
        }

        public function voidTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.voidTransaction(pSourceSystemCd, pSourceCompanyId, pFinTxId));
            remoteToken.addResponder(responder);
        }

        public function cancelTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.cancelTransaction(pSourceSystemCd, pSourceCompanyId, pFinTxId));
            remoteToken.addResponder(responder);
        }

        public function findTransactionsByLedgerAccount(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findTransactionsByLedgerAccount(pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd));
            remoteToken.addResponder(responder);
        }

        public function findTransactionsByLedgerAccountAndPayroll(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, pPayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findTransactionsByLedgerAccountAndPayroll(pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd, pPayRunId));
            remoteToken.addResponder(responder);
        }

        public function findLedgerAccountByPayrollAndLedgerCode(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLedgerAccountCode:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findLedgerAccountByPayrollAndLedgerCode(pCompanyId, pSourceSystemCd, pSourcePayRunId, pLedgerAccountCode));
            remoteToken.addResponder(responder);
        }

        public function getPayrollLaws(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getPayrollLaws(pCompanyId, pSourceSystemCd, pSourcePayRunId));
            remoteToken.addResponder(responder);
        }

        public function findLedgerAccounts(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findLedgerAccounts(pCompanyId, pSourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function redebitPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.redebitPayrollTransactions(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls));
            remoteToken.addResponder(responder);
        }

        public function addRefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addRefundPayrollTransactions(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls));
            remoteToken.addResponder(responder);
        }

        public function addWriteOffBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addWriteOffBadDebtTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId));
            remoteToken.addResponder(responder);
        }

        public function addWriteOffEmployeeBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addWriteOffEmployeeBadDebtTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId));
            remoteToken.addResponder(responder);
        }

        public function addIntuit5DayReturnTransfer(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addIntuit5DayReturnTransfer(pSourceSystemCd, pSourceCompanyId, pPayRunId));
            remoteToken.addResponder(responder);
        }

        public function voidPayrollTaxPayment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.voidPayrollTaxPayment(companyId, sourceSystemCd, payrollRunId));
            remoteToken.addResponder(responder);
        }

        public function reissuePayrollTaxPayment(companyId:String, sourceSystemCd:String, sourcePayrollRunId:String, transferTransactionId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.reissuePayrollTaxPayment(companyId, sourceSystemCd, sourcePayrollRunId, transferTransactionId));
            remoteToken.addResponder(responder);
        }

        public function addRecoverBadDebtTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, collectionAgencyExpense:Number, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addRecoverBadDebtTransactions(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls, collectionAgencyExpense));
            remoteToken.addResponder(responder);
        }

        public function addEmployeeReturnTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addEmployeeReturnTransferTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId));
            remoteToken.addResponder(responder);
        }

        public function refundERPayable(pSourceSystemCd:String, pSourceCompanyId:String, settlementTypeCode:String, amount:Number, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.refundERPayable(pSourceSystemCd, pSourceCompanyId, settlementTypeCode, amount));
            remoteToken.addResponder(responder);
        }

        public function applyERPayableToBalanceDue(pSourceSystemCd:String, pSourceCompanyId:String, payrollRunId:String, amount:Number, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.applyERPayableToBalanceDue(pSourceSystemCd, pSourceCompanyId, payrollRunId, amount));
            remoteToken.addResponder(responder);
        }

        public function addFeeTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pOfferingServiceChargeTypeCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addFeeTransferTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pOfferingServiceChargeTypeCd));
            remoteToken.addResponder(responder);
        }

        public function addRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addRefundTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd));
            remoteToken.addResponder(responder);
        }

        public function addEmployerReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTaxAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addEmployerReturnRefundTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTaxAmount, pTxnDate, pSettlementTypeCd));
            remoteToken.addResponder(responder);
        }

        public function addEmployeeReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addEmployeeReturnRefundTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd));
            remoteToken.addResponder(responder);
        }

        public function getTransactionHistory(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxnId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getTransactionHistory(pSourceSystemCd, pSourceCompanyId, pFinTxnId));
            remoteToken.addResponder(responder);
        }

        public function addEscalation(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pIsEmployee:Boolean, pSettlementTypeCd:String, pAmount:Number, pSettlementDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addEscalation(pSourceSystemCd, pSourceCompanyId, pPayRunId, pIsEmployee, pSettlementTypeCd, pAmount, pSettlementDate));
            remoteToken.addResponder(responder);
        }

        public function findChaseReportForDateRange(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findChaseReportForDateRange(pSourceSystemCd, pCompanyId, pFromDate, pToDate));
            remoteToken.addResponder(responder);
        }

		public function addRefundRebillTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, rebill:Boolean, pFeeDebitTransactionId:String, pOverrideAmount:Number, pOverrideQuantity:int, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(payrollRunRemoteService.addRefundRebillTransaction(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, rebill, pFeeDebitTransactionId, pOverrideAmount, pOverrideQuantity));
			remoteToken.addResponder(responder);
        }

        public function findPayrollUncollectedBalances(pCompanyId:String, pSourceSystemCd:String, pPayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollUncollectedBalances(pCompanyId, pSourceSystemCd, pPayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollUnrecoveredBalances(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollUnrecoveredBalances(pSourceSystemCd, pSourceCompanyId, pPayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollCollectedTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollCollectedTransactions(pSourceSystemCd, pSourceCompanyId, pPayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollPrefundingTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollPrefundingTransactions(pSourceSystemCd, pSourceCompanyId, pPayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function findCompanyBalance(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findCompanyBalance(pSourceSystemCd, pCompanyId));
            remoteToken.addResponder(responder);
        }

        public function findPayrollTransactionById(transactionId:String, pCompanyId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findPayrollTransactionById(transactionId, pCompanyId));
            remoteToken.addResponder(responder);
        }

        public function findMoneyMovementTransactions(sourceSystemCd:String, companyId:String, fromDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findMoneyMovementTransactions(sourceSystemCd, companyId, fromDate));
            remoteToken.addResponder(responder);
        }

        public function findAchDetailTransactions(moneyMovementTransactionId:String, responder:IResponder, companyId:String):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.findAchDetailTransactions(moneyMovementTransactionId,companyId));
            remoteToken.addResponder(responder);
        }

        public function getRedebitTransactionsForPayroll(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getRedebitTransactionsForPayroll(sourceSystemCd, companyId, payrollRunId));
            remoteToken.addResponder(responder);
        }

        public function checkPayrollForSuspectPaychecks(sourceSystemCd:String, companyId:String, sourcePayrollRunId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.checkPayrollForSuspectPaychecks(sourceSystemCd, companyId, sourcePayrollRunId));
            remoteToken.addResponder(responder);
        }

        public function addPrefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, payrolls:ArrayCollection, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.addPrefundPayrollTransactions(sourceSystemCd, companyId, settlementTypeCd, payrolls));
            remoteToken.addResponder(responder);
        }

        public function getAllPayrollRunActions(responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getAllPayrollRunActions());
            remoteToken.addResponder(responder);
        }

        public function getTransactionTypeList(responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(payrollRunRemoteService.getTransactionTypeList());
            remoteToken.addResponder(responder);
        }


    }
}

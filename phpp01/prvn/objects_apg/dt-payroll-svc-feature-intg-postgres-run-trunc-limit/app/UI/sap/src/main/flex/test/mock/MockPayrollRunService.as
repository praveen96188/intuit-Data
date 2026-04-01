package test.mock
{
import mx.collections.ArrayCollection;
import mx.rpc.IResponder;
import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;

import psp.sap.service.interfaces.IPayrollRunService;

public class MockPayrollRunService extends MockAsyncService implements IPayrollRunService
	{
		public function MockPayrollRunService()
		{
		}

		public function expectsGetLineItems(pSourceSystemCd:String, pCompanyId:String, pPaycheckGseq:String):Mock {
            return expects("getLineItems").withArgs(pSourceSystemCd, pCompanyId, pPaycheckGseq);
        }
		public function getLineItems(pSourceSystemCd:String, pCompanyId:String, pPaycheckGseq:String, responder:IResponder):void {
            record("getLineItems", pSourceSystemCd, pCompanyId, pPaycheckGseq);
			sendAsyncResult(responder,"getLineItems");
        }

		public function expectsFindPaychecks(pCompanyId:String, pSourceSystemCd:String, pSourcePayrollRunId:String):Mock {
            return expects("findPaychecks").withArgs(pCompanyId, pSourceSystemCd, pSourcePayrollRunId);
        }
		public function findPaychecks(pCompanyId:String, pSourceSystemCd:String, pSourcePayrollRunId:String, responder:IResponder):void {
            record("findPaychecks", pCompanyId, pSourceSystemCd, pSourcePayrollRunId);
			sendAsyncResult(responder,"findPaychecks");
        }

		public function expectsFindPayrollRunByPayrollRunId(pPayrollRunId:String):Mock {
            return expects("findPayrollRunByPayrollRunId").withArgs(pPayrollRunId);
        }
		public function findPayrollRunByPayrollRunId(pPayrollRunId:String, responder:IResponder):void {
            record("findPayrollRunByPayrollRunId", pPayrollRunId);
			sendAsyncResult(responder,"findPayrollRunByPayrollRunId");
        }

		public function expectsCancelPayrollTransaction(companyId:String, sourceSystemCd:String, transactionIds:ArrayCollection, payrollRunId:String):Mock {
            return expects("cancelPayrollTransaction").withArgs(companyId, sourceSystemCd, transactionIds, payrollRunId);
        }
		public function cancelPayrollTransaction(companyId:String, sourceSystemCd:String, transactionIds:ArrayCollection, payrollRunId:String, responder:IResponder):void {
            record("cancelPayrollTransaction", companyId, sourceSystemCd, transactionIds, payrollRunId);
			sendAsyncResult(responder,"cancelPayrollTransaction");
        }

		public function expectsCancelAdjustment(companyId:String, sourceSystemCd:String, payrollRunId:String):Mock {
            return expects("cancelAdjustment").withArgs(companyId, sourceSystemCd, payrollRunId);
        }
		public function cancelAdjustment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void {
            record("cancelAdjustment", companyId, sourceSystemCd, payrollRunId);
			sendAsyncResult(responder,"cancelAdjustment");
        }

		public function expectsCancelBillPaymentTransaction(companyId:String, sourceSystemCd:String, payrollRunId:String, transactionIds:ArrayCollection):Mock {
            return expects("cancelBillPaymentTransaction").withArgs(companyId, sourceSystemCd, payrollRunId, transactionIds);
        }
		public function cancelBillPaymentTransaction(companyId:String, sourceSystemCd:String, payrollRunId:String, transactionIds:ArrayCollection, responder:IResponder):void {
            record("cancelBillPaymentTransaction", companyId, sourceSystemCd, payrollRunId, transactionIds);
			sendAsyncResult(responder,"cancelBillPaymentTransaction");
        }

		public function expectsReversePayrollRunTransactions(sourceSystemCd:String, companyId:String, transactionIds:ArrayCollection, payrollRunId:String, pChargeFee:Boolean, pFeeTxnDate:Date, pFeeSettlementType:String, pInitiateForCollection:Boolean):Mock {
            return expects("reversePayrollRunTransactions").withArgs(sourceSystemCd, companyId, transactionIds, payrollRunId, pChargeFee, pFeeTxnDate, pFeeSettlementType, pInitiateForCollection);
        }
		public function reversePayrollRunTransactions(sourceSystemCd:String, companyId:String, transactionIds:ArrayCollection, payrollRunId:String, pChargeFee:Boolean, pFeeTxnDate:Date, pFeeSettlementType:String, pInitiateForCollection:Boolean, responder:IResponder):void {
            record("reversePayrollRunTransactions", sourceSystemCd, companyId, transactionIds, payrollRunId, pChargeFee, pFeeTxnDate, pFeeSettlementType, pInitiateForCollection);
			sendAsyncResult(responder,"reversePayrollRunTransactions");
        }

		public function expectsFindEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findEmployeeTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findEmployeeTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findEmployeeTransactions");
        }

		public function expectsFindVendorTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findVendorTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findVendorTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findVendorTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findVendorTransactions");
        }

		public function expectsFindCancelableTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String):Mock {
            return expects("findCancelableTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId);
        }
		public function findCancelableTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            record("findCancelableTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId);
			sendAsyncResult(responder,"findCancelableTransactions");
        }

		public function expectsFindReversableEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findReversableEmployeeTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findReversableEmployeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findReversableEmployeeTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findReversableEmployeeTransactions");
        }

		public function expectsFindIntuitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findIntuitTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findIntuitTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findIntuitTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findIntuitTransactions");
        }

		public function expectsFindEmployerTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findEmployerTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findEmployerTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findEmployerTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findEmployerTransactions");
        }

		public function expectsFindAgencyTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findAgencyTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
        }
		public function findAgencyTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findAgencyTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pFromDate, pToDate);
			sendAsyncResult(responder,"findAgencyTransactions");
        }

		public function expectsFindPayrollRunBalanceDue(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String):Mock {
            return expects("findPayrollRunBalanceDue").withArgs(pSourceSystemCd, pCompanyId, pSourcePayRunId);
        }
		public function expectsFindPayrollRunsByDate(companyId:String, sourceSystemCd:String, payrollTypes:ArrayCollection, fromDate:Date, toDate:Date):Mock {
            return expects("findPayrollRunsByDate").withArgs(companyId, sourceSystemCd, payrollTypes, fromDate, toDate);
        }
		public function findPayrollRunsByDate(companyId:String, sourceSystemCd:String, payrollTypes:ArrayCollection, fromDate:Date, toDate:Date, responder:IResponder):void {
            record("findPayrollRunsByDate", companyId, sourceSystemCd, payrollTypes, fromDate, toDate);
			sendAsyncResult(responder,"findPayrollRunsByDate");
        }

		public function findPayrollRunBalanceDue(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void {
            record("findPayrollRunBalanceDue", pSourceSystemCd, pCompanyId, pSourcePayRunId);
			sendAsyncResult(responder,"findPayrollRunBalanceDue");
        }

		public function expectsFindPayrollRun(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String):Mock {
            return expects("findPayrollRun").withArgs(pSourceSystemCd, pCompanyId, pSourcePayRunId);
        }
		public function findPayrollRun(pSourceSystemCd:String, pCompanyId:String, pSourcePayRunId:String, responder:IResponder):void {
            record("findPayrollRun", pSourceSystemCd, pCompanyId, pSourcePayRunId);
			sendAsyncResult(responder,"findPayrollRun");
        }

		public function expectsFindERPayableRefundTransactions(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("findERPayableRefundTransactions").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function findERPayableRefundTransactions(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("findERPayableRefundTransactions", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"findERPayableRefundTransactions");
        }

		public function expectsFindLedgerAccountsByPayroll(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String):Mock {
            return expects("findLedgerAccountsByPayroll").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId);
        }
		public function findLedgerAccountsByPayroll(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            record("findLedgerAccountsByPayroll", pCompanyId, pSourceSystemCd, pSourcePayRunId);
			sendAsyncResult(responder,"findLedgerAccountsByPayroll");
        }

		public function expectsGetLedgerAccountBalance(sourceSystemCd:String, sourceCompanyId:String, ledgerAccountCd:String):Mock {
            return expects("getLedgerAccountBalance").withArgs(sourceSystemCd, sourceCompanyId, ledgerAccountCd);
        }
		public function getLedgerAccountBalance(sourceSystemCd:String, sourceCompanyId:String, ledgerAccountCd:String, responder:IResponder):void {
            record("getLedgerAccountBalance", sourceSystemCd, sourceCompanyId, ledgerAccountCd);
			sendAsyncResult(responder,"getLedgerAccountBalance");
        }

		public function expectsGetLedgerAccountBalanceForLaw(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLawId:String):Mock {
            return expects("getLedgerAccountBalanceForLaw").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pLawId);
        }
		public function getLedgerAccountBalanceForLaw(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLawId:String, responder:IResponder):void {
            record("getLedgerAccountBalanceForLaw", pCompanyId, pSourceSystemCd, pSourcePayRunId, pLawId);
			sendAsyncResult(responder,"getLedgerAccountBalanceForLaw");
        }

		public function expectsAddFinancialLedgerAdjustmentTransaction(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, debitAccountCode:String, creditAccountCode:String, pAmount:Number, pLawId:String, pNoteText:String):Mock {
            return expects("addFinancialLedgerAdjustmentTransaction").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, debitAccountCode, creditAccountCode, pAmount, pLawId, pNoteText);
        }
		public function addFinancialLedgerAdjustmentTransaction(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, debitAccountCode:String, creditAccountCode:String, pAmount:Number, pLawId:String, pNoteText:String, responder:IResponder):void {
            record("addFinancialLedgerAdjustmentTransaction", pCompanyId, pSourceSystemCd, pSourcePayRunId, debitAccountCode, creditAccountCode, pAmount, pLawId, pNoteText);
			sendAsyncResult(responder,"addFinancialLedgerAdjustmentTransaction");
        }

		public function expectsAddFeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pSettlementTypeCd:String, pTxnDate:Date, fees:ArrayCollection):Mock {
            return expects("addFeeTransactions").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pSettlementTypeCd, pTxnDate, fees);
        }
		public function addFeeTransactions(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pSettlementTypeCd:String, pTxnDate:Date, fees:ArrayCollection, responder:IResponder):void {
            record("addFeeTransactions", pCompanyId, pSourceSystemCd, pSourcePayRunId, pSettlementTypeCd, pTxnDate, fees);
			sendAsyncResult(responder,"addFeeTransactions");
        }

		public function expectsAddFeeRedebitTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pOldTxnId:String):Mock {
            return expects("addFeeRedebitTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pOldTxnId);
        }
		public function addFeeRedebitTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pOldTxnId:String, responder:IResponder):void {
            record("addFeeRedebitTransaction", pSourceSystemCd, pSourceCompanyId, pOldTxnId);
			sendAsyncResult(responder,"addFeeRedebitTransaction");
        }

		public function expectsAddWireExpectedDateTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pCollectionStageCd:String, pActionEvent:String, pTxnDate:Date, pSendLastEmail:Boolean):Mock {
            return expects("addWireExpectedDateTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pCollectionStageCd, pActionEvent, pTxnDate, pSendLastEmail);
        }
		public function addWireExpectedDateTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pCollectionStageCd:String, pActionEvent:String, pTxnDate:Date, pSendLastEmail:Boolean, responder:IResponder):void {
            record("addWireExpectedDateTransaction", pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pCollectionStageCd, pActionEvent, pTxnDate, pSendLastEmail);
			sendAsyncResult(responder,"addWireExpectedDateTransaction");
        }

		public function expectsRefundEmployerTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pFinancialTxAmt:Number, pTxnDate:Date, pSettlementType:String):Mock {
            return expects("refundEmployerTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pFinancialTxAmt, pTxnDate, pSettlementType);
        }
		public function refundEmployerTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinancialTxId:String, pFinancialTxAmt:Number, pTxnDate:Date, pSettlementType:String, responder:IResponder):void {
            record("refundEmployerTransaction", pSourceSystemCd, pSourceCompanyId, pFinancialTxId, pFinancialTxAmt, pTxnDate, pSettlementType);
			sendAsyncResult(responder,"refundEmployerTransaction");
        }

		public function expectsVoidTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String):Mock {
            return expects("voidTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pFinTxId);
        }
		public function voidTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void {
            record("voidTransaction", pSourceSystemCd, pSourceCompanyId, pFinTxId);
			sendAsyncResult(responder,"voidTransaction");
        }

		public function expectsCancelTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String):Mock {
            return expects("cancelTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pFinTxId);
        }
		public function cancelTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxId:String, responder:IResponder):void {
            record("cancelTransaction", pSourceSystemCd, pSourceCompanyId, pFinTxId);
			sendAsyncResult(responder,"cancelTransaction");
        }

		public function expectsFindTransactionsByLedgerAccount(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String):Mock {
            return expects("findTransactionsByLedgerAccount").withArgs(pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd);
        }
		public function findTransactionsByLedgerAccount(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, responder:IResponder):void {
            record("findTransactionsByLedgerAccount", pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd);
			sendAsyncResult(responder,"findTransactionsByLedgerAccount");
        }

		public function expectsFindTransactionsByLedgerAccountAndPayroll(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, pPayRunId:String):Mock {
            return expects("findTransactionsByLedgerAccountAndPayroll").withArgs(pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd, pPayRunId);
        }
		public function findTransactionsByLedgerAccountAndPayroll(pSourceSystemCd:String, pSourceCompanyId:String, pLedgerAccountCd:String, pPayRunId:String, responder:IResponder):void {
            record("findTransactionsByLedgerAccountAndPayroll", pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd, pPayRunId);
			sendAsyncResult(responder,"findTransactionsByLedgerAccountAndPayroll");
        }

		public function expectsFindLedgerAccountByPayrollAndLedgerCode(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLedgerAccountCode:String):Mock {
            return expects("findLedgerAccountByPayrollAndLedgerCode").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId, pLedgerAccountCode);
        }
		public function findLedgerAccountByPayrollAndLedgerCode(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, pLedgerAccountCode:String, responder:IResponder):void {
            record("findLedgerAccountByPayrollAndLedgerCode", pCompanyId, pSourceSystemCd, pSourcePayRunId, pLedgerAccountCode);
			sendAsyncResult(responder,"findLedgerAccountByPayrollAndLedgerCode");
        }

		public function expectsGetPayrollLaws(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String):Mock {
            return expects("getPayrollLaws").withArgs(pCompanyId, pSourceSystemCd, pSourcePayRunId);
        }
		public function getPayrollLaws(pCompanyId:String, pSourceSystemCd:String, pSourcePayRunId:String, responder:IResponder):void {
            record("getPayrollLaws", pCompanyId, pSourceSystemCd, pSourcePayRunId);
			sendAsyncResult(responder,"getPayrollLaws");
        }

		public function expectsFindLedgerAccounts(pCompanyId:String, pSourceSystemCd:String):Mock {
            return expects("findLedgerAccounts").withArgs(pCompanyId, pSourceSystemCd);
        }
		public function findLedgerAccounts(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
            record("findLedgerAccounts", pCompanyId, pSourceSystemCd);
			sendAsyncResult(responder,"findLedgerAccounts");
        }

		public function expectsRedebitPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection):Mock {
            return expects("redebitPayrollTransactions").withArgs(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls);
        }
		public function redebitPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void {
            record("redebitPayrollTransactions", sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls);
			sendAsyncResult(responder,"redebitPayrollTransactions");
        }

		public function expectsAddRefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection):Mock {
            return expects("addRefundPayrollTransactions").withArgs(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls);
        }
		public function addRefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, responder:IResponder):void {
            record("addRefundPayrollTransactions", sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls);
			sendAsyncResult(responder,"addRefundPayrollTransactions");
        }

		public function expectsAddWriteOffBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String):Mock {
            return expects("addWriteOffBadDebtTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId);
        }
		public function addWriteOffBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            record("addWriteOffBadDebtTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId);
			sendAsyncResult(responder,"addWriteOffBadDebtTransaction");
        }

		public function expectsAddWriteOffEmployeeBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String):Mock {
            return expects("addWriteOffEmployeeBadDebtTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId);
        }
		public function addWriteOffEmployeeBadDebtTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            record("addWriteOffEmployeeBadDebtTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId);
			sendAsyncResult(responder,"addWriteOffEmployeeBadDebtTransaction");
        }

		public function expectsAddIntuit5DayReturnTransfer(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String):Mock {
            return expects("addIntuit5DayReturnTransfer").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId);
        }
		public function addIntuit5DayReturnTransfer(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            record("addIntuit5DayReturnTransfer", pSourceSystemCd, pSourceCompanyId, pPayRunId);
			sendAsyncResult(responder,"addIntuit5DayReturnTransfer");
        }

		public function expectsVoidPayrollTaxPayment(companyId:String, sourceSystemCd:String, payrollRunId:String):Mock {
            return expects("voidPayrollTaxPayment").withArgs(companyId, sourceSystemCd, payrollRunId);
        }
		public function voidPayrollTaxPayment(companyId:String, sourceSystemCd:String, payrollRunId:String, responder:IResponder):void {
            record("voidPayrollTaxPayment", companyId, sourceSystemCd, payrollRunId);
			sendAsyncResult(responder,"voidPayrollTaxPayment");
        }

		public function expectsReissuePayrollTaxPayment(companyId:String, sourceSystemCd:String, sourcePayrollRunId:String, transferTransactionId:String):Mock {
            return expects("reissuePayrollTaxPayment").withArgs(companyId, sourceSystemCd, sourcePayrollRunId, transferTransactionId);
        }
		public function reissuePayrollTaxPayment(companyId:String, sourceSystemCd:String, sourcePayrollRunId:String, transferTransactionId:String, responder:IResponder):void {
            record("reissuePayrollTaxPayment", companyId, sourceSystemCd, sourcePayrollRunId, transferTransactionId);
			sendAsyncResult(responder,"reissuePayrollTaxPayment");
        }

		public function expectsAddRecoverBadDebtTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, collectionAgencyExpense:Number):Mock {
            return expects("addRecoverBadDebtTransactions").withArgs(sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls, collectionAgencyExpense);
        }
		public function addRecoverBadDebtTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, settlementDate:Date, payrolls:ArrayCollection, collectionAgencyExpense:Number, responder:IResponder):void {
            record("addRecoverBadDebtTransactions", sourceSystemCd, companyId, settlementTypeCd, settlementDate, payrolls, collectionAgencyExpense);
			sendAsyncResult(responder,"addRecoverBadDebtTransactions");
        }

		public function expectsAddEmployeeReturnTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String):Mock {
            return expects("addEmployeeReturnTransferTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId);
        }
		public function addEmployeeReturnTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, responder:IResponder):void {
            record("addEmployeeReturnTransferTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId);
			sendAsyncResult(responder,"addEmployeeReturnTransferTransaction");
        }

		public function expectsRefundERPayable(pSourceSystemCd:String, pSourceCompanyId:String, settlementTypeCode:String, amount:Number):Mock {
            return expects("refundERPayable").withArgs(pSourceSystemCd, pSourceCompanyId, settlementTypeCode, amount);
        }
		public function refundERPayable(pSourceSystemCd:String, pSourceCompanyId:String, settlementTypeCode:String, amount:Number, responder:IResponder):void {
            record("refundERPayable", pSourceSystemCd, pSourceCompanyId, settlementTypeCode, amount);
			sendAsyncResult(responder,"refundERPayable");
        }

		public function expectsApplyERPayableToBalanceDue(pSourceSystemCd:String, pSourceCompanyId:String, payrollRunId:String, amount:Number):Mock {
            return expects("applyERPayableToBalanceDue").withArgs(pSourceSystemCd, pSourceCompanyId, payrollRunId, amount);
        }
		public function applyERPayableToBalanceDue(pSourceSystemCd:String, pSourceCompanyId:String, payrollRunId:String, amount:Number, responder:IResponder):void {
            record("applyERPayableToBalanceDue", pSourceSystemCd, pSourceCompanyId, payrollRunId, amount);
			sendAsyncResult(responder,"applyERPayableToBalanceDue");
        }

		public function expectsAddFeeTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pOfferingServiceChargeTypeCd:String):Mock {
            return expects("addFeeTransferTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pOfferingServiceChargeTypeCd);
        }
		public function addFeeTransferTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pOfferingServiceChargeTypeCd:String, responder:IResponder):void {
            record("addFeeTransferTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pOfferingServiceChargeTypeCd);
			sendAsyncResult(responder,"addFeeTransferTransaction");
        }

		public function expectsAddRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String):Mock {
            return expects("addRefundTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd);
        }
		public function addRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            record("addRefundTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd);
			sendAsyncResult(responder,"addRefundTransaction");
        }

		public function expectsAddEmployerReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTaxAmount:Number, pTxnDate:Date, pSettlementTypeCd:String):Mock {
            return expects("addEmployerReturnRefundTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTaxAmount, pTxnDate, pSettlementTypeCd);
        }
		public function addEmployerReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTaxAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            record("addEmployerReturnRefundTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTaxAmount, pTxnDate, pSettlementTypeCd);
			sendAsyncResult(responder,"addEmployerReturnRefundTransaction");
        }

		public function expectsAddEmployeeReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String):Mock {
            return expects("addEmployeeReturnRefundTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd);
        }
		public function addEmployeeReturnRefundTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, pSettlementTypeCd:String, responder:IResponder):void {
            record("addEmployeeReturnRefundTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, pSettlementTypeCd);
			sendAsyncResult(responder,"addEmployeeReturnRefundTransaction");
        }

		public function expectsGetTransactionHistory(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxnId:String):Mock {
            return expects("getTransactionHistory").withArgs(pSourceSystemCd, pSourceCompanyId, pFinTxnId);
        }
		public function getTransactionHistory(pSourceSystemCd:String, pSourceCompanyId:String, pFinTxnId:String, responder:IResponder):void {
            record("getTransactionHistory", pSourceSystemCd, pSourceCompanyId, pFinTxnId);
			sendAsyncResult(responder,"getTransactionHistory");
        }

		public function expectsAddEscalation(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pIsEmployee:Boolean, pSettlementTypeCd:String, pAmount:Number, pSettlementDate:Date):Mock {
            return expects("addEscalation").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pIsEmployee, pSettlementTypeCd, pAmount, pSettlementDate);
        }
		public function addEscalation(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pIsEmployee:Boolean, pSettlementTypeCd:String, pAmount:Number, pSettlementDate:Date, responder:IResponder):void {
            record("addEscalation", pSourceSystemCd, pSourceCompanyId, pPayRunId, pIsEmployee, pSettlementTypeCd, pAmount, pSettlementDate);
			sendAsyncResult(responder,"addEscalation");
        }

		public function expectsFindChaseReportForDateRange(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findChaseReportForDateRange").withArgs(pSourceSystemCd, pCompanyId, pFromDate, pToDate);
        }
		public function findChaseReportForDateRange(pSourceSystemCd:String, pCompanyId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findChaseReportForDateRange", pSourceSystemCd, pCompanyId, pFromDate, pToDate);
			sendAsyncResult(responder,"findChaseReportForDateRange");
        }

		public function expectsAddRefundRebillTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, rebill:Boolean, pFeeDebitTransactionId:String, pOverrideAmount:Number, pOverrideQuantity:int):Mock {
            return expects("addRefundRebillTransaction").withArgs(pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, rebill, pFeeDebitTransactionId, pOverrideAmount, pOverrideQuantity);
        }
		public function addRefundRebillTransaction(pSourceSystemCd:String, pSourceCompanyId:String, pPayRunId:String, pAmount:Number, pTxnDate:Date, rebill:Boolean, pFeeDebitTransactionId:String, pOverrideAmount:Number, pOverrideQuantity:int, responder:IResponder):void {
            record("addRefundRebillTransaction", pSourceSystemCd, pSourceCompanyId, pPayRunId, pAmount, pTxnDate, rebill, pFeeDebitTransactionId, pOverrideAmount, pOverrideQuantity);
			sendAsyncResult(responder,"addRefundRebillTransaction");
        }

		public function expectsFindPayrollUncollectedBalances(pCompanyId:String, pSourceSystemCd:String, pPayrollRunId:String):Mock {
            return expects("findPayrollUncollectedBalances").withArgs(pCompanyId, pSourceSystemCd, pPayrollRunId);
        }
		public function findPayrollUncollectedBalances(pCompanyId:String, pSourceSystemCd:String, pPayrollRunId:String, responder:IResponder):void {
            record("findPayrollUncollectedBalances", pCompanyId, pSourceSystemCd, pPayrollRunId);
			sendAsyncResult(responder,"findPayrollUncollectedBalances");
        }

		public function expectsFindPayrollUnrecoveredBalances(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String):Mock {
            return expects("findPayrollUnrecoveredBalances").withArgs(pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
        }
		public function findPayrollUnrecoveredBalances(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            record("findPayrollUnrecoveredBalances", pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
			sendAsyncResult(responder,"findPayrollUnrecoveredBalances");
        }

		public function expectsFindPayrollCollectedTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String):Mock {
            return expects("findPayrollCollectedTransactions").withArgs(pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
        }
		public function findPayrollCollectedTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            record("findPayrollCollectedTransactions", pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
			sendAsyncResult(responder,"findPayrollCollectedTransactions");
        }

		public function expectsFindPayrollPrefundingTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String):Mock {
            return expects("findPayrollPrefundingTransactions").withArgs(pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
        }
		public function findPayrollPrefundingTransactions(pSourceSystemCd:String, pSourceCompanyId:String, pPayrollRunId:String, responder:IResponder):void {
            record("findPayrollPrefundingTransactions", pSourceSystemCd, pSourceCompanyId, pPayrollRunId);
			sendAsyncResult(responder,"findPayrollPrefundingTransactions");
        }

		public function expectsFindCompanyBalance(pSourceSystemCd:String, pCompanyId:String):Mock {
            return expects("findCompanyBalance").withArgs(pSourceSystemCd, pCompanyId);
        }
		public function findCompanyBalance(pSourceSystemCd:String, pCompanyId:String, responder:IResponder):void {
            record("findCompanyBalance", pSourceSystemCd, pCompanyId);
			sendAsyncResult(responder,"findCompanyBalance");
        }

		public function expectsFindPayrollTransactionById(transactionId:String, companyId:String):Mock {
            return expects("findPayrollTransactionById").withArgs(transactionId, companyId);
        }
		public function findPayrollTransactionById(transactionId:String, companyId:String, responder:IResponder):void {
            record("findPayrollTransactionById", transactionId, companyId);
			sendAsyncResult(responder,"findPayrollTransactionById");
        }

		public function expectsFindMoneyMovementTransactions(sourceSystemCd:String, companyId:String, fromDate:Date):Mock {
            return expects("findMoneyMovementTransactions").withArgs(sourceSystemCd, companyId, fromDate);
        }
		public function findMoneyMovementTransactions(sourceSystemCd:String, companyId:String, fromDate:Date, responder:IResponder):void {
            record("findMoneyMovementTransactions", sourceSystemCd, companyId, fromDate);
			sendAsyncResult(responder,"findMoneyMovementTransactions");
        }

		public function expectsFindAchDetailTransactions(moneyMovementTransactionId:String,companyId:String):Mock {
            return expects("findAchDetailTransactions").withArgs(moneyMovementTransactionId,companyId);
        }
		public function findAchDetailTransactions(moneyMovementTransactionId:String, responder:IResponder,companyId:String):void {
            record("findAchDetailTransactions", moneyMovementTransactionId,companyId);
			sendAsyncResult(responder,"findAchDetailTransactions");
        }

		public function expectsGetRedebitTransactionsForPayroll(sourceSystemCd:String, companyId:String, payrollRunId:String):Mock {
            return expects("getRedebitTransactionsForPayroll").withArgs(sourceSystemCd, companyId, payrollRunId);
        }
		public function getRedebitTransactionsForPayroll(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void {
            record("getRedebitTransactionsForPayroll", sourceSystemCd, companyId, payrollRunId);
			sendAsyncResult(responder,"getRedebitTransactionsForPayroll");
        }

		public function expectsGetLatePayrolls():Mock {
            return expects("getLatePayrolls").withArgs();
        }
		public function getLatePayrolls(responder:IResponder):void {
            record("getLatePayrolls");
			sendAsyncResult(responder,"getLatePayrolls");
        }

		public function expectsCheckPayrollForSuspectPaychecks(sourceSystemCd:String, companyId:String, sourcePayrollRunId:String):Mock {
            return expects("checkPayrollForSuspectPaychecks").withArgs(sourceSystemCd, companyId, sourcePayrollRunId);
        }
		public function checkPayrollForSuspectPaychecks(sourceSystemCd:String, companyId:String, sourcePayrollRunId:String, responder:IResponder):void {
            record("checkPayrollForSuspectPaychecks", sourceSystemCd, companyId, sourcePayrollRunId);
			sendAsyncResult(responder,"checkPayrollForSuspectPaychecks");
        }

		public function expectsAddPrefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, payrolls:ArrayCollection):Mock {
            return expects("addPrefundPayrollTransactions").withArgs(sourceSystemCd, companyId, settlementTypeCd, payrolls);
        }
		public function addPrefundPayrollTransactions(sourceSystemCd:String, companyId:String, settlementTypeCd:String, payrolls:ArrayCollection, responder:IResponder):void {
            record("addPrefundPayrollTransactions", sourceSystemCd, companyId, settlementTypeCd, payrolls);
			sendAsyncResult(responder,"addPrefundPayrollTransactions");
        }

		public function expectsGetAllPayrollRunActions():Mock {
            return expects("getAllPayrollRunActions").withArgs();
        }
		public function getAllPayrollRunActions(responder:IResponder):void {
            record("getAllPayrollRunActions");
			sendAsyncResult(responder,"getAllPayrollRunActions");
        }

		public function expectsGetTransactionTypeList():Mock {
            return expects("getTransactionTypeList").withArgs();
        }
		public function getTransactionTypeList(responder:IResponder):void {
            record("getTransactionTypeList");
			sendAsyncResult(responder,"getTransactionTypeList");
        }

    public function findPItems(pCompanyId:String, pSourceSystemCd:String, responder:IResponder):void {
    }
}
}

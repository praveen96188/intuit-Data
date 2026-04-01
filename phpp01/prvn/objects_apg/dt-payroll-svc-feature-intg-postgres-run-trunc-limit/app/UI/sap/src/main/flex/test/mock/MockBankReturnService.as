package test.mock
{
	import mx.collections.ArrayCollection;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;

import psp.sap.service.interfaces.IBankReturnService;

	public class MockBankReturnService extends MockAsyncService implements IBankReturnService
	{
		public function MockBankReturnService()
		{
		}

		public function expectsFindCompanyBankReturnsByComplexSearch(pFein:String, pFromDate:Date, pToDate:Date, pAmount:Number, searchType:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean):Mock {
            return expects("findCompanyBankReturnsByComplexSearch").withArgs(pFein, pFromDate, pToDate, pAmount, searchType, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting);
        }
		public function findCompanyBankReturnsByComplexSearch(pFein:String, pFromDate:Date, pToDate:Date, pAmount:Number, searchType:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void {
            record("findCompanyBankReturnsByComplexSearch", pFein, pFromDate, pToDate, pAmount, searchType, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting);
			sendAsyncResult(responder,"findCompanyBankReturnsByComplexSearch");
        }

		public function expectsSaveBankReturnNote(pSourceSystemCode:String, pSourceCompanyId:String, transactionId:String, pBankReturnStatus:String, pNote:String):Mock {
            return expects("saveBankReturnNote").withArgs(pSourceSystemCode, pSourceCompanyId, transactionId, pBankReturnStatus, pNote);
        }
		public function saveBankReturnNote(pSourceSystemCode:String, pSourceCompanyId:String, transactionId:String, pBankReturnStatus:String, pNote:String, responder:IResponder):void {
            record("saveBankReturnNote", pSourceSystemCode, pSourceCompanyId, transactionId, pBankReturnStatus, pNote);
			sendAsyncResult(responder,"saveBankReturnNote");
        }

		public function expectsGetBankReturnExtendedInfo(bankReturns:ArrayCollection):Mock {
            return expects("getBankReturnExtendedInfo").withArgs(bankReturns);
        }
		public function getBankReturnExtendedInfo(bankReturns:ArrayCollection, responder:IResponder):void {
            record("getBankReturnExtendedInfo", bankReturns);
			sendAsyncResult(responder,"getBankReturnExtendedInfo");
        }

		public function expectsFindCompanyBankReturns(pFein:String, pFromDate:Date, pToDate:Date, pShowOpen:Boolean, pShowResolved:Boolean, pTransactionType:String, pTransactionCategory:String, pExclude5DayFunding:Boolean, includeCode:String, pAmount:Number, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean):Mock {
            return expects("findCompanyBankReturns").withArgs(pFein, pFromDate, pToDate, pShowOpen, pShowResolved, pTransactionType, pTransactionCategory, pExclude5DayFunding, includeCode, pAmount, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting);
        }
		public function findCompanyBankReturns(pFein:String, pFromDate:Date, pToDate:Date, pShowOpen:Boolean, pShowResolved:Boolean, pTransactionType:String, pTransactionCategory:String, pExclude5DayFunding:Boolean, includeCode:String, pAmount:Number, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void {
            record("findCompanyBankReturns", pFein, pFromDate, pToDate, pShowOpen, pShowResolved, pTransactionType, pTransactionCategory, pExclude5DayFunding, includeCode, pAmount, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting);
			sendAsyncResult(responder,"findCompanyBankReturns");
        }
		
		
	}
}
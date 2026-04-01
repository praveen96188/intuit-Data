package psp.sap.service.interfaces
{
	import mx.collections.ArrayCollection;
	import mx.rpc.IResponder;
	
	public interface IBankReturnService extends IPSPService
	{

		function findCompanyBankReturnsByComplexSearch(pFein:String, pFromDate:Date, pToDate:Date, pAmount:Number, searchType:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void;

		function saveBankReturnNote(pSourceSystemCode:String, pSourceCompanyId:String, transactionId:String, pBankReturnStatus:String, pNote:String, responder:IResponder):void;

		function getBankReturnExtendedInfo(bankReturns:ArrayCollection, responder:IResponder):void;

		function findCompanyBankReturns(pFein:String, pFromDate:Date, pToDate:Date, pShowOpen:Boolean, pShowResolved:Boolean, pTransactionType:String, pTransactionCategory:String, pExclude5DayFunding:Boolean, includeCode:String, pAmount:Number, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void;
		
	}
}
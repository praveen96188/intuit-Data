package psp.sap.service
{
	import mx.collections.ArrayCollection;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.mxml.RemoteObject;
	
	import psp.sap.service.interfaces.IBankReturnService;


	public class BankReturnService extends PSPService implements IBankReturnService 
	{
		public function BankReturnService():void {			
			remoteObjectPool = new RemoteObjectPool("bankreturnservice", 2, true);
		}
		
		public function get bankReturnRemoteService():RemoteObject {
			return mRemoteObjectPool.nextAvailable();
		}

		public function findCompanyBankReturnsByComplexSearch(pFein:String, pFromDate:Date, pToDate:Date, pAmount:Number, searchType:String, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(bankReturnRemoteService.findCompanyBankReturnsByComplexSearch(pFein, pFromDate, pToDate, pAmount, searchType, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting));
			remoteToken.addResponder(responder);
        }

		public function saveBankReturnNote(pSourceSystemCode:String, pSourceCompanyId:String, transactionId:String, pBankReturnStatus:String, pNote:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(bankReturnRemoteService.saveBankReturnNote(pSourceSystemCode, pSourceCompanyId, transactionId, pBankReturnStatus, pNote));
			remoteToken.addResponder(responder);
        }

		public function getBankReturnExtendedInfo(bankReturns:ArrayCollection, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(bankReturnRemoteService.getBankReturnExtendedInfo(bankReturns));
			remoteToken.addResponder(responder);
        }

		public function findCompanyBankReturns(pFein:String, pFromDate:Date, pToDate:Date, pShowOpen:Boolean, pShowResolved:Boolean, pTransactionType:String, pTransactionCategory:String, pExclude5DayFunding:Boolean, includeCode:String, pAmount:Number, pOrderBy:String, pOrderDesc:Boolean, pFirstResult:int, pMaxResults:int, isForPrinting:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(bankReturnRemoteService.findCompanyBankReturns(pFein, pFromDate, pToDate, pShowOpen, pShowResolved, pTransactionType, pTransactionCategory, pExclude5DayFunding, includeCode, pAmount, pOrderBy, pOrderDesc, pFirstResult, pMaxResults, isForPrinting));
			remoteToken.addResponder(responder);
        }
		
		
	}
}

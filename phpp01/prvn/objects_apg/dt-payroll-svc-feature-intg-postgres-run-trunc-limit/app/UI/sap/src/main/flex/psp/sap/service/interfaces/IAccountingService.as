package psp.sap.service.interfaces {
	import mx.rpc.IResponder;

	public interface IAccountingService extends IPSPService {

		function getIntuitAccountsDescription(responder:IResponder):void;

		function createBookTransfer(pFromAccountName:String, pToAccountName:String, pAmount:Number, responder:IResponder):void;

		function cancelBookTransfer(pTransactionId:String, responder:IResponder):void;

		function findBookTransferTransactions(fromDate:Date, toDate:Date, pAccount:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void;

	}
}

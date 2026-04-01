package psp.sap.service {

	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.RemoteObject;

	import psp.sap.service.interfaces.IAccountingService;

	public class AccountingService extends PSPService implements IAccountingService {
		public function AccountingService():void {
			remoteObjectPool = new RemoteObjectPool("accountingservice");
		}

		public function get accountingRemoteService():RemoteObject {
			return mRemoteObjectPool.nextAvailable();
		}

		public function getIntuitAccountsDescription(responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(accountingRemoteService.getIntuitAccountsDescription());
			remoteToken.addResponder(responder);
		}

		public function createBookTransfer(pFromAccountName:String, pToAccountName:String, pAmount:Number, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(accountingRemoteService.createBookTransfer(pFromAccountName, pToAccountName, pAmount));
			remoteToken.addResponder(responder);
		}

		public function cancelBookTransfer(pTransactionId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(accountingRemoteService.cancelBookTransfer(pTransactionId));
			remoteToken.addResponder(responder);
		}

		public function findBookTransferTransactions(fromDate:Date, toDate:Date, pAccount:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(accountingRemoteService.findBookTransferTransactions(fromDate, toDate, pAccount, pFirstIndex, pMaxResults, pSortColumn, pSortDescending));
			remoteToken.addResponder(responder);
        }

	}
}

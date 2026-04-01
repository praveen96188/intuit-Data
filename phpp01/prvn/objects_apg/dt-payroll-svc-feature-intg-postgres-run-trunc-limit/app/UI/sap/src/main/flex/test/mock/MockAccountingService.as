package test.mock {
	import mx.rpc.IResponder;

	import org.mock4as.Mock;

	import psp.sap.service.interfaces.IAccountingService;

	public class MockAccountingService extends MockAsyncService implements IAccountingService {
		public function MockAccountingService() {
		}

		public function expectsGetIntuitAccountsDescription():Mock {
			return expects("getIntuitAccountsDescription").withArgs();
		}

		public function getIntuitAccountsDescription(responder:IResponder):void {
			record("getIntuitAccountsDescription");
			sendAsyncResult(responder, "getIntuitAccountsDescription");
		}

		public function expectsCreateBookTransfer(pFromAccountName:String, pToAccountName:String, pAmount:Number):Mock {
			return expects("createBookTransfer").withArgs(pFromAccountName, pToAccountName, pAmount);
		}

		public function createBookTransfer(pFromAccountName:String, pToAccountName:String, pAmount:Number, responder:IResponder):void {
			record("createBookTransfer", pFromAccountName, pToAccountName, pAmount);
			sendAsyncResult(responder, "createBookTransfer");
		}

		public function expectsCancelBookTransfer(pTransactionId:String):Mock {
			return expects("cancelBookTransfer").withArgs(pTransactionId);
		}

		public function cancelBookTransfer(pTransactionId:String, responder:IResponder):void {
			record("cancelBookTransfer", pTransactionId);
			sendAsyncResult(responder, "cancelBookTransfer");
		}

		public function expectsFindBookTransferTransactions(fromDate:Date, toDate:Date, pAccount:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean):Mock {
            return expects("findBookTransferTransactions").withArgs(fromDate, toDate, pAccount, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
        }
		public function findBookTransferTransactions(fromDate:Date, toDate:Date, pAccount:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
            record("findBookTransferTransactions", fromDate, toDate, pAccount, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
			sendAsyncResult(responder,"findBookTransferTransactions");
        }

	}
}

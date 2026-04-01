package test.mock {

    import mx.rpc.events.ResultEvent;
    import mx.rpc.IResponder;
    import org.mock4as.Mock;
    import psp.sap.service.interfaces.IPropertyAuditService;

    public class MockPropertyAuditService extends MockAsyncService implements IPropertyAuditService {
        public function MockPropertyAuditService() {

        }

		public function expectsGetNotificationEmailHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("getNotificationEmailHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function getNotificationEmailHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("getNotificationEmailHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"getNotificationEmailHistory");
        }

		public function expectsGetFundingModelHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("getFundingModelHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function getFundingModelHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("getFundingModelHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"getFundingModelHistory");
        }

		public function expectsGetEmployeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("getEmployeeDDLimitHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function getEmployeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("getEmployeeDDLimitHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"getEmployeeDDLimitHistory");
        }

		public function expectsGetPayeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("getPayeeDDLimitHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function getPayeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("getPayeeDDLimitHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"getPayeeDDLimitHistory");
        }

		public function expectsGetCompanyDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date):Mock {
            return expects("getCompanyDDLimitHistory").withArgs(pCompanyId, pSourceSystemId, pFromDate);
        }
		public function getCompanyDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
            record("getCompanyDDLimitHistory", pCompanyId, pSourceSystemId, pFromDate);
			sendAsyncResult(responder,"getCompanyDDLimitHistory");
        }		

		public function expectsGetCompanyBankAccountPropertyAudit(pSourceSystemId:String, pCompanyId:String, pSourceCompanyBankAccountId:String):Mock {
            return expects("getCompanyBankAccountPropertyAudit").withArgs(pSourceSystemId, pCompanyId, pSourceCompanyBankAccountId);
        }
		public function getCompanyBankAccountPropertyAudit(pSourceSystemId:String, pCompanyId:String, pSourceCompanyBankAccountId:String, responder:IResponder):void {
            record("getCompanyBankAccountPropertyAudit", pSourceSystemId, pCompanyId, pSourceCompanyBankAccountId);
			sendAsyncResult(responder,"getCompanyBankAccountPropertyAudit");
        }

		public function expectsGetQuickBooksPropertyAudits(pSourceSystemId:String, pCompanyId:String):Mock {
            return expects("getQuickBooksPropertyAudits").withArgs(pSourceSystemId, pCompanyId);
        }
		public function getQuickBooksPropertyAudits(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void {
            record("getQuickBooksPropertyAudits", pSourceSystemId, pCompanyId);
			sendAsyncResult(responder,"getQuickBooksPropertyAudits");
        }

		public function expectsGetW2PrintingPreferenceHistory(pSourceSystemId:String, pCompanyId:String):Mock {
            return expects("getW2PrintingPreferenceHistory").withArgs(pSourceSystemId, pCompanyId);
        }
		public function getW2PrintingPreferenceHistory(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void {
            record("getW2PrintingPreferenceHistory", pSourceSystemId, pCompanyId);
			sendAsyncResult(responder,"getW2PrintingPreferenceHistory");
        }

    }
}
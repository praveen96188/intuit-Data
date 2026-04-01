package psp.sap.service {
import mx.logging.ILogger;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.rpc.remoting.RemoteObject;


import psp.sap.application.ClientLoggingTarget;
import psp.sap.service.interfaces.IPropertyAuditService;

public class PropertyAuditService extends PSPService implements IPropertyAuditService {
        
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

		public function PropertyAuditService()
		{            
			remoteObjectPool = new RemoteObjectPool("propertyauditservice", 2);
		}

		public function get propertyAuditRemoteService():RemoteObject {
			return remoteObjectPool.nextAvailable();
		}

		public function getNotificationEmailHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getNotificationEmailHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }

		public function getFundingModelHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getFundingModelHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getEmployeeDDLimitHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }

		public function getPayeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getPayeeDDLimitHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }

		public function getCompanyDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getCompanyDDLimitHistory(pCompanyId, pSourceSystemId, pFromDate));
			remoteToken.addResponder(responder);
        }		

		public function getCompanyBankAccountPropertyAudit(pSourceSystemId:String, pCompanyId:String, pSourceCompanyBankAccountId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getCompanyBankAccountPropertyAudit(pSourceSystemId, pCompanyId, pSourceCompanyBankAccountId));
			remoteToken.addResponder(responder);
        }

		public function getQuickBooksPropertyAudits(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getQuickBooksPropertyAudits(pSourceSystemId, pCompanyId));
			remoteToken.addResponder(responder);
        }

		public function getW2PrintingPreferenceHistory(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(propertyAuditRemoteService.getW2PrintingPreferenceHistory(pSourceSystemId, pCompanyId));
			remoteToken.addResponder(responder);
        }


        
    }
}
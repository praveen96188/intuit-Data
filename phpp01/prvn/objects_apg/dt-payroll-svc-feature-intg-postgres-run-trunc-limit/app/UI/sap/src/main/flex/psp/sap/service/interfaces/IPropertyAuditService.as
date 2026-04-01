package psp.sap.service.interfaces {
import mx.rpc.IResponder;

public interface IPropertyAuditService extends IPSPService {

		function getNotificationEmailHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function getFundingModelHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function getEmployeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function getPayeeDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function getCompanyDDLimitHistory(pCompanyId:String, pSourceSystemId:String, pFromDate:Date, responder:IResponder):void;

		function getCompanyBankAccountPropertyAudit(pSourceSystemId:String, pCompanyId:String, pSourceCompanyBankAccountId:String, responder:IResponder):void;

		function getQuickBooksPropertyAudits(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void;

		function getW2PrintingPreferenceHistory(pSourceSystemId:String, pCompanyId:String, responder:IResponder):void;
    }
}
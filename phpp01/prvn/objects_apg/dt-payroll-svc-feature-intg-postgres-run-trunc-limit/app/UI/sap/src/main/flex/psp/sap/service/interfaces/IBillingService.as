package psp.sap.service.interfaces
{
	import mx.rpc.IResponder;
	
	public interface IBillingService extends IPSPService
	{

		function findOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void;

		function claimOfferWithExpirationForCompany(offerCd:String, companyId:String, sourceSystemCd:String, expirationDate:Date, responder:IResponder):void;

		function cancelOfferForCompany(offerCd:String, companyId:String, sourceSystemCd:String, responder:IResponder):void;

		function findOfferings(pServiceCode:String, companyId:String, sourceSystemCd:String, responder:IResponder):void;

		function findFeeDetail(transactionId:String, responder:IResponder, companyId:String):void;

        function getCurrentOffering(pSourceSystemCd:String,  pCompanyId:String, pServiceCode:String, responder:IResponder):void;

		function addOfferingToCompany(offeringSKU:String, companyId:String, sourceSystemCd:String, responder:IResponder):void;
	
	}
}
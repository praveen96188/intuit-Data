package psp.sap.service
{
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.mxml.RemoteObject;
	
	import psp.sap.service.interfaces.IBillingService;


	public class BillingService extends PSPService implements IBillingService
	{
		private var billingRemoteService:RemoteObject = new RemoteObject("billingservice");
		
		public function BillingService():void {
			billingRemoteService.showBusyCursor = true;
		}

		public function findOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.findOffers(sourceSystemCd, companyId, serviceCd));
			remoteToken.addResponder(responder);
        }

		public function claimOfferWithExpirationForCompany(offerCd:String, companyId:String, sourceSystemCd:String, expirationDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.claimOfferWithExpirationForCompany(offerCd, companyId, sourceSystemCd, expirationDate));
			remoteToken.addResponder(responder);
        }

		public function cancelOfferForCompany(offerCd:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.cancelOfferForCompany(offerCd, companyId, sourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function findOfferings(pServiceCode:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.findOfferings(pServiceCode, companyId, sourceSystemCd));
			remoteToken.addResponder(responder);
        }

		public function findFeeDetail(transactionId:String, responder:IResponder, companyId:String):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.findFeeDetail(transactionId,companyId));
			remoteToken.addResponder(responder);
        }

        public function getCurrentOffering(pSourceSystemCd:String,  pCompanyId:String, pServiceCode:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.getCurrentOffering(pSourceSystemCd,  pCompanyId, pServiceCode));
			remoteToken.addResponder(responder);
        }

		public function addOfferingToCompany(offeringSKU:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(billingRemoteService.addOfferingToCompany(offeringSKU, companyId, sourceSystemCd));
			remoteToken.addResponder(responder);
        }

	   
	}
}
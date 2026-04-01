package test.mock
{
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;

import psp.sap.service.interfaces.IBillingService;
	
	import test.mock.data.BillingData;

	public class MockBillingService extends MockAsyncService implements IBillingService
	{
		public function MockBillingService()
		{
		}

		public function expectsFindOffers(companyId:String, sourceSystemCd:String, serviceCd:String):Mock {
            return expects("findOffers").withArgs(serviceCd);
        }
		public function findOffers(sourceSystemCd:String, companyId:String, serviceCd:String, responder:IResponder):void {
            record("findOffers", sourceSystemCd, companyId, serviceCd);
			sendAsyncResult(responder,"findOffers");
        }

		public function expectsClaimOfferWithExpirationForCompany(offerCd:String, companyId:String, sourceSystemCd:String, expirationDate:Date):Mock {
            return expects("claimOfferWithExpirationForCompany").withArgs(offerCd, companyId, sourceSystemCd, expirationDate);
        }
		public function claimOfferWithExpirationForCompany(offerCd:String, companyId:String, sourceSystemCd:String, expirationDate:Date, responder:IResponder):void {
            record("claimOfferWithExpirationForCompany", offerCd, companyId, sourceSystemCd, expirationDate);
			sendAsyncResult(responder,"claimOfferWithExpirationForCompany");
        }

		public function expectsCancelOfferForCompany(offerCd:String, companyId:String, sourceSystemCd:String):Mock {
            return expects("cancelOfferForCompany").withArgs(offerCd, companyId, sourceSystemCd);
        }
		public function cancelOfferForCompany(offerCd:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
            record("cancelOfferForCompany", offerCd, companyId, sourceSystemCd);
			sendAsyncResult(responder,"cancelOfferForCompany");
        }

		public function expectsFindOfferings(pServiceCode:String, companyId:String, sourceSystemCd:String):Mock {
            return expects("findOfferings").withArgs(pServiceCode, companyId, sourceSystemCd);
        }
		public function findOfferings(pServiceCode:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
            record("findOfferings", pServiceCode, companyId, sourceSystemCd);
			sendAsyncResult(responder,"findOfferings");
        }

		public function expectsFindFeeDetail(transactionId:String, companyId:String):Mock {
            return expects("findFeeDetail").withArgs(transactionId,companyId);
        }
		public function findFeeDetail(transactionId:String, responder:IResponder, companyId:String):void {
            record("findFeeDetail", transactionId,companyId);
			sendAsyncResult(responder,"findFeeDetail");
        }

        public function expectsGetCurrentOffering(pSourceSystemCd:String,  pCompanyId:String, pServiceCode:String):Mock {
            return expects("getCurrentOffering").withArgs(pSourceSystemCd,  pCompanyId, pServiceCode);
        }
        public function getCurrentOffering(pSourceSystemCd:String,  pCompanyId:String, pServiceCode:String, responder:IResponder):void {
            record("getCurrentOffering", pSourceSystemCd,  pCompanyId, pServiceCode);
			sendAsyncResult(responder,"getCurrentOffering");
        }

		public function expectsAddOfferingToCompany(offeringSKU:String, companyId:String, sourceSystemCd:String):Mock {
            return expects("addOfferingToCompany").withArgs(offeringSKU, companyId, sourceSystemCd);
        }
		public function addOfferingToCompany(offeringSKU:String, companyId:String, sourceSystemCd:String, responder:IResponder):void {
            record("addOfferingToCompany", offeringSKU, companyId, sourceSystemCd);
			sendAsyncResult(responder,"addOfferingToCompany");
        }
		
	}
}
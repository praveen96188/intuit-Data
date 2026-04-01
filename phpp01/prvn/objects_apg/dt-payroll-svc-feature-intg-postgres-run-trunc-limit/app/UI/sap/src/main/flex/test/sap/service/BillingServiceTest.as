package test.sap.service
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.model.Company;
	import psp.sap.model.Offer;
	import psp.sap.model.Offering;

    import psp.sap.model.ServiceCodeEnum;

    import test.sap.application.SAPTestBase;
	
	public class BillingServiceTest extends SAPTestBase
	{
		public static function suite():TestSuite {
			return new TestSuite(BillingServiceTest);
		}

        /***************************************************************
        * findOffers -- this tests the BillingRemoteService.findOffers function.
        * 
        * START
        */
        /*todo rewrite this public function testFindOffers():void {
        	runDataLoader("Company :: Create Basic Data", testFindOffers_Step2, 5);
        }
        
        private function testFindOffers_Step2(e:ResultEvent):void {
        	login(testFindOffers_Step3);
        }
        
        private function testFindOffers_Step3(e:ResultEvent):void {
        	mSAP.companyService.findCompany("QBOE","1234567",
        		getTestResponder(testFindOffers_Step4, 2));
        }
        
        private function testFindOffers_Step4(e:ResultEvent):void {
        	var company:Company = e.result as Company;
		    mSAP.billingService.findOffers(company.sourceSystemCd, company.companyId,
		    	getTestResponder(testFindOffers_Step5, 2));
		}			

        private function testFindOffers_Step5(e:ResultEvent):void {
        	assertNotNull("Result", e.result);
        	assertTrue("Result is ArrayCollection", e.result is ArrayCollection);
        	var offerList:ArrayCollection = ArrayCollection(e.result);
        	assertTrue("Results returned", offerList.length > 0);
        	for each (var offerObject:Object in offerList) {
        		assertTrue("Is offer", offerObject is Offer);
        		var offer:Offer = Offer(offerObject);
        		nothingNull(offer);
        	}          
        	
        	var offerForCompany:Offer = Offer(offerList.getItemAt(0));
        	mSAP.billingService.claimOfferForCompany(offerForCompany.offerCd, 
        			"1234567", 
        			"QBOE", 
        			getTestResponder(testFindOffers_Step6));
        } 

        private function testFindOffers_Step6(e:ResultEvent):void {
        	trace("Success");
        }*/
        /** END - findOffers **/

        /***************************************************************
        * findOfferings -- this tests the BillingRemoteService.findOfferings function.
        * 
        * START
        */
        /* todo rewrite this public function testFindOfferings():void {
        	runDataLoader("Company :: Create Basic Data", testFindOfferings_Step2, 5);
        }

        private function testFindOfferings_Step2(e:ResultEvent):void {
        	login(testFindOfferings_Step3);
        }

        private function testFindOfferings_Step3(e:ResultEvent):void {
		    mSAP.billingService.findOfferings(ServiceCodeEnum.DIRECT_DEPOSIT.code, getTestResponder(testFindOfferings_Step4, 2));
		}

        private function testFindOfferings_Step4(e:ResultEvent):void {
        	assertNotNull("Result", e.result);
        	assertTrue("Result is ArrayCollection", e.result is ArrayCollection);
        	var offeringList:ArrayCollection = ArrayCollection(e.result);
        	assertTrue("Results returned", offeringList.length > 0);
        	for each (var offeringObject:Object in offeringList) {
        		assertTrue("Is offer", offeringObject is Offering);
        		var offering:Offering = Offering(offeringObject);
        		nothingNull(offering);
        	}

        	var offeringForCompany:Offering = Offering(offeringList.getItemAt(0));
        	mSAP.billingService.addOfferingToCompany(offeringForCompany.SKU,
        			"Standard",
        			"1234567",
        			"QBOE",
        			getTestResponder(testFindOfferings_Step5));
        }

        private function testFindOfferings_Step5(e:ResultEvent):void {
        	trace("Success");
        }*/
        /** END - findOfferings **/
        
    }
}

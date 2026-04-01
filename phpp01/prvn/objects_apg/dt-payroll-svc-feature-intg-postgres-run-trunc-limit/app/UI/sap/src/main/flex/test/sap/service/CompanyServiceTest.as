package test.sap.service
{
    import flexunit.framework.TestSuite;
    
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    
    import psp.sap.model.Company;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.CompanyEventItem;
    import psp.sap.model.CompanyNote;
    import psp.sap.model.Contact;
    import psp.sap.model.ServiceStatus;
    import psp.sap.model.FraudEvent;
    import psp.sap.model.FraudIndicatorEnum;
    import psp.sap.model.FundingModel;
    import psp.sap.model.PropertyAudit;
    import psp.sap.model.SourceSystem;
    import psp.sap.model.Transmission;
    import psp.sap.model.companyevents.CompanyEvent;
    
    import test.sap.application.SAPTestBase;
	
	public class CompanyServiceTest extends SAPTestBase
	{
		public static function suite():TestSuite {
			return new TestSuite(CompanyServiceTest);
		}
		
		private var mCompany: Company = new Company();

		/**
		 * testFindCompany - START
		 * 
		 * Tests find company and then each sub-query for different company-related objects
		 */
        public function testFindCompany(): void {
        	this.runDataLoader("Company :: Create Basic Data", testFindCompany_Step2, 5);
        }
        
        private function testFindCompany_Step2(e:ResultEvent):void {
        	this.login(testFindCompany_Step3);	
        }
        
        private function testFindCompany_Step3(e:ResultEvent):void {
			mSAP.companyService.findCompany(
					"QBOE",
                    "1234567",					
					this.getTestResponder(testFindCompany_Step4));
        }                
                
        private function testFindCompany_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is Company);
        	mCompany = Company(e.result);
        	
        	mSAP.propertyAuditService.getCompanyDDLimitHistory(
        			mCompany.companyId, mCompany.sourceSystemCd,
        			null,
        			this.getTestResponder(testFindCompany_Step5));
        			
        }
        
        private function testFindCompany_Step5(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var ddLimitHistoryList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(ddLimitHistoryList.getItemAt(0) is PropertyAudit);
        	var ddLimitHistory: PropertyAudit = ddLimitHistoryList.getItemAt(0) as PropertyAudit;
        	
        	assertNotNull(ddLimitHistory.auditDate);
        	assertNotNull(ddLimitHistory.auditDateTime);
        	assertNotNull(ddLimitHistory.createdDate);
        	assertNotNull(ddLimitHistory.newPropertyValue);
        	assertNotNull(ddLimitHistory.oldPropertyValue);
        	assertNotNull(ddLimitHistory.propertyName);
        	assertNotNull(ddLimitHistory.propertyLabel);
        	//assertNotNull(ddLimitHistory.userFullName);
        	assertNotNull(ddLimitHistory.userId);
        	assertTrue(ddLimitHistory.propertyName == "OverrideCompanyLimitAmount");

        	mSAP.propertyAuditService.getFundingModelHistory(
        			mCompany.companyId, mCompany.sourceSystemCd,
        			null,
        			getTestResponder(testFindCompany_Step6));        			
        }        

        private function testFindCompany_Step6(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var fundingModelHistoryList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(fundingModelHistoryList.getItemAt(0) is PropertyAudit);
        	var fundingModelHistory: PropertyAudit = fundingModelHistoryList.getItemAt(0) as PropertyAudit;
        	
        	assertNotNull(fundingModelHistory.auditDate);
        	assertNotNull(fundingModelHistory.auditDateTime);
        	assertNotNull(fundingModelHistory.createdDate);
        	assertNotNull(fundingModelHistory.newPropertyValue);
        	assertNotNull(fundingModelHistory.oldPropertyValue);
        	assertNotNull(fundingModelHistory.propertyName);
        	assertNotNull(fundingModelHistory.propertyLabel);
        	//assertNotNull(fundingModelHistory.userFullName);
        	assertNotNull(fundingModelHistory.userId);
        	assertTrue(fundingModelHistory.propertyName == "FundingModel");

        	mSAP.propertyAuditService.getNotificationEmailHistory(
        			mCompany.companyId, mCompany.sourceSystemCd,null,
        			getTestResponder(testFindCompany_Step7));
        			
        }        

        private function testFindCompany_Step7(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var notificationEmailHistoryList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(notificationEmailHistoryList.getItemAt(0) is PropertyAudit);
        	var notificationEmailHistory: PropertyAudit = notificationEmailHistoryList.getItemAt(0) as PropertyAudit;
        	
        	assertNotNull(notificationEmailHistory.auditDate);
        	assertNotNull(notificationEmailHistory.auditDateTime);
        	assertNotNull(notificationEmailHistory.createdDate);
        	assertNotNull(notificationEmailHistory.newPropertyValue);
        	assertNotNull(notificationEmailHistory.oldPropertyValue);
        	assertNotNull(notificationEmailHistory.propertyName);
        	assertNotNull(notificationEmailHistory.propertyLabel);
        	//assertNotNull(notificationEmailHistory.userFullName);
        	assertNotNull(notificationEmailHistory.userId);
        	assertTrue(notificationEmailHistory.propertyName == "NotificationEmail");

        	mSAP.propertyAuditService.getEmployeeDDLimitHistory(
        			mCompany.companyId, mCompany.sourceSystemCd,
        			null,
        			getTestResponder(testFindCompany_Step8));
		}        

        private function testFindCompany_Step8(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var ddEmployeeLimitHistoryList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(ddEmployeeLimitHistoryList.getItemAt(0) is PropertyAudit);
        	var ddEmployeeLimitHistory: PropertyAudit = ddEmployeeLimitHistoryList.getItemAt(0) as PropertyAudit;
        	
        	assertNotNull(ddEmployeeLimitHistory.auditDate);
        	assertNotNull(ddEmployeeLimitHistory.auditDateTime);
        	assertNotNull(ddEmployeeLimitHistory.createdDate);
        	assertNotNull(ddEmployeeLimitHistory.newPropertyValue);
        	assertNotNull(ddEmployeeLimitHistory.oldPropertyValue);
        	assertNotNull(ddEmployeeLimitHistory.propertyName);
        	assertNotNull(ddEmployeeLimitHistory.propertyLabel);
        	//assertNotNull(ddEmployeeLimitHistory.userFullName);
        	assertNotNull(ddEmployeeLimitHistory.userId);
        	assertTrue(ddEmployeeLimitHistory.propertyName == "OverrideEmployeeLimitAmount");

        	mSAP.companyService.getLimitViolationEvents(
        			mCompany.companyId, mCompany.sourceSystemCd,
        			null,
        			null,
        			getTestResponder(testFindCompany_Step9));
        }        

        private function testFindCompany_Step9(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var ddLimitViolationHistoryList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(ddLimitViolationHistoryList.length > 0);
	        assertTrue(ddLimitViolationHistoryList.getItemAt(0) is CompanyEventItem);	        
        }
		/**
		 * testFindCompany - END
		 */ 
        
        /**
         * testServiceStatusList - START
         */ 
        public function testServiceStatusList():void {
        	this.runDataLoader("Company :: Create Basic Data", testServiceStatusList_Step2, 5);
        }
        
        private function testServiceStatusList_Step2(e:ResultEvent):void {
        	login(testServiceStatusList_Step3);	
        }

		private function testServiceStatusList_Step3(e:ResultEvent):void {        	
        	mSAP.companyService.getServiceStatusList(
				getTestResponder(testServiceStatusList_Step4));
        }
        
        private function testServiceStatusList_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var ddStatusList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(ddStatusList.getItemAt(0) is ServiceStatus);
        	var ddStatus: ServiceStatus = ddStatusList.getItemAt(0) as ServiceStatus;        	
        	assertTrue(ddStatus.serviceStatusCd != "[object Object]");
        }        
        /**
         * testServiceStatusList - END
         */ 

        /**
         * testFundingModelList - START
         */ 
        public function testFundingModelList():void {
        	this.runDataLoader("Company :: Create Basic Data", testFundingModelList_Step2, 25);
        }
        
        private function testFundingModelList_Step2(e:ResultEvent):void {
        	login(testFundingModelList_Step3);	
        }

        private function testFundingModelList_Step3(e:ResultEvent):void {
        	mSAP.companyService.getFundingModelList(
				getTestResponder(testFundingModelList_Step4));
        }
        
        private function testFundingModelList_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var fundingModelList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(fundingModelList.getItemAt(0) is FundingModel);
        	var fundingModel: FundingModel = fundingModelList.getItemAt(0) as FundingModel;
        	assertTrue(nothingNull(fundingModel));
        }        
        /**
         * testFundingModelList - END
         */ 

        /**
         * testSourceSystemList - START
         */ 
        public function testSourceSystemList():void {
        	this.runDataLoader("Company :: Create Basic Data", testSourceSystemList_Step2, 5);
        }
        
        private function testSourceSystemList_Step2(e:ResultEvent):void {
        	login(testSourceSystemList_Step3);
        }

        private function testSourceSystemList_Step3(e:ResultEvent):void {
        	mSAP.companyService.getSourceSystemList(
				getTestResponder(testSourceSystemList_Step4));
        }
        
        private function testSourceSystemList_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var sourceSystemList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(sourceSystemList.getItemAt(0) is SourceSystem);
        	var sourceSystem: SourceSystem = sourceSystemList.getItemAt(0) as SourceSystem;
        	assertTrue(nothingNull(sourceSystem));
        }        
        /**
         * testSourceSystemList - END
         */ 
         
         /**
         * testFindCompanyNotes - START
         */ 
        public function testFindCompanyNotes():void {
        	this.runDataLoader("Company :: Create Basic Data", testFindCompanyNotes_Step2, 5);
        }
        
        private function testFindCompanyNotes_Step2(e:ResultEvent):void {
        	login(testFindCompanyNotes_Step3);
        }

        private function testFindCompanyNotes_Step3(e:ResultEvent):void {
        	mSAP.companyService.findCompanyNotes(
        		"QBOE",
        		"1234567", null, null,
				getTestResponder(testFindCompanyNotes_Step4));
        }
        
        private function testFindCompanyNotes_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var notesList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(notesList.getItemAt(0) is CompanyNote);
        	var note:CompanyNote = notesList.getItemAt(0) as CompanyNote;
        	assertTrue(nothingNull(note));
        }        
        /**
         * testFindCompanyNotes - END
         */
         
         /**
         * testFindCompanyFraudEvents - START
         */ 
        public function testFindCompanyFraudEvents():void {
        	this.runDataLoader("Fraud :: Load fraud company and payroll", testFindCompanyFraudEvents_Step2, 5);
        }
        
        private function testFindCompanyFraudEvents_Step2(e:ResultEvent):void {
        	login(testFindCompanyFraudEvents_Step3);
        }

        private function testFindCompanyFraudEvents_Step3(e:ResultEvent):void {
        	mSAP.companyService.findCompanyFraudEvents(
        		null,
        		null,
        		-1,
        		null,
        		null,
                null,
				getTestResponder(testFindCompanyFraudEvents_Step4));
        }
        
        private function testFindCompanyFraudEvents_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var eventList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(eventList.getItemAt(0) is FraudEvent);
        	var event:FraudEvent = eventList.getItemAt(0) as FraudEvent;
        	assertNotNull(event.fraudIndicator);
		    assertNotNull(event.companyName);
		    assertNotNull(event.companyId);
		    assertNotNull(event.sourceSystemCd)
		    assertNotNull(event.companyEin);	    
		    assertNotNull(event.details);
		    assertNotNull(event.fraudFlagSet);
        }        
        /**
         * testFindCompanyFraudEvents - END
         */
         
         /**
         * testRemoveFraudFlag - START
         */ 
        public function testRemoveFraudFlag():void {
        	this.runDataLoader("Fraud :: Load fraud company and payroll", testRemoveFraudFlag_Step2, 5);
        }
        
        private function testRemoveFraudFlag_Step2(e:ResultEvent):void {
        	login(testRemoveFraudFlag_Step3);
        }

        private function testRemoveFraudFlag_Step3(e:ResultEvent):void {
        	mSAP.companyService.findCompanyFraudEvents(
        		null,
        		FraudIndicatorEnum.PAYROLL.code,
        		-1,
        		null,
        		null,
                null,
				getTestResponder(testRemoveFraudFlag_Step4, 2));
        }
        
        private function testRemoveFraudFlag_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var eventList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(eventList.getItemAt(0) is FraudEvent);
        	var event:FraudEvent = eventList.getItemAt(0) as FraudEvent;
        	mSAP.companyService.removeFraudFlag(event.sourceSystemCd, 
        										event.companyId,
                								getTestResponder(testRemoveFraudFlag_Step5));
        }
        
        private function testRemoveFraudFlag_Step5(e:ResultEvent):void {
        	// success
        	assertTrue(true);
        }
        /**
         * testRemoveFraudFlag - END
         */
         
		/**
         * testFindTransmissions - START
         */ 
        public function testFindTransmissions():void {
        	this.runDataLoader("Company :: Load QBDT OFX Transmission", testFindTransmissions_Step2, 5);
        }
        
        private function testFindTransmissions_Step2(e:ResultEvent):void {
        	login(testFindTransmissions_Step3);
        }

        private function testFindTransmissions_Step3(e:ResultEvent):void {
        	mSAP.companyService.findTransmissions(
        		"QBOE",
        		"123456",
        		null,
        		null,
        		null,        						
				getTestResponder(testFindTransmissions_Step4, 2));
        }
        
        private function testFindTransmissions_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var eventList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(eventList.getItemAt(0) is Transmission);
        	var transmission:Transmission = eventList.getItemAt(0) as Transmission;        	
        }                
        /**
         * testFindTransmissions - END
         */
    }
}
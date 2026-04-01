package test.sap.service
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;

    import psp.sap.model.AutoLimitIncreaseTier;
    import psp.sap.model.DirectDepositLimitSettings;
	import psp.sap.model.NachaFile;
	
	import test.sap.application.SAPTestBase;
	
	public class AdministrationServicesTest extends SAPTestBase
	{
		public static function suite():TestSuite {
			return new TestSuite(AdministrationServicesTest);
		}
		
		/**
         * AdministrationService.getDirectDepositLimitSettings 
         */
        public function testGetDirectDepositLimitSettings():void {
        	runDataLoader("Company :: Create Basic Data", testGetDirectDepositLimitSettings_Step2, 5);
        }
        
        private function testGetDirectDepositLimitSettings_Step2(e:ResultEvent):void {
        	login(testGetDirectDepositLimitSettings_Step3);
        }
                
        private function testGetDirectDepositLimitSettings_Step3(e:ResultEvent):void {
        	mSAP.administrationService.getDirectDepositLimitSettings("QBOE", 
        		getTestResponder(verifyGetDirectDepositLimitSettings, 2));
        }
        
        private function verifyGetDirectDepositLimitSettings(e:ResultEvent):void {        	
        	assertNotNull("Result", e.result);
        	assertTrue("Result is DirectDepositLimitSettings", e.result is DirectDepositLimitSettings);
        	var directDepositLimitSettings:DirectDepositLimitSettings = e.result as DirectDepositLimitSettings;
        	nothingNull(directDepositLimitSettings);        	                  	        	
        }
        
        /**
        * AdminSettingsService.saveDirectDepositLimitSettings 
        */
        public function testSaveDirectDepositLimitSettings():void {
        	runDataLoader("Company :: Create Basic Data", testSaveDirectDepositLimitSettings_Step2, 5);
        }
        
        private function testSaveDirectDepositLimitSettings_Step2(e:ResultEvent):void {
        	login(testSaveDirectDepositLimitSettings_Step3);
        }
                
        private function testSaveDirectDepositLimitSettings_Step3(e:ResultEvent):void {
        	var directDepositLimitSettings:DirectDepositLimitSettings = new DirectDepositLimitSettings();
        	directDepositLimitSettings.companyBankAccountDurationLimitForVerification = "365";
        	directDepositLimitSettings.companyBankAccountVerificationAttemptLimit = "10";
        	directDepositLimitSettings.consecutiveLimitViolationLimit = "5";
        	directDepositLimitSettings.DDCompanyLimitDuration = "8";
        	directDepositLimitSettings.DDEmployeeLimitDuration = "14";
        	directDepositLimitSettings.defaultDDCompanyLimit = "50000.00";
        	directDepositLimitSettings.defaultDDEmployeeLimit = "20000.00";
        	directDepositLimitSettings.maxDDCompanyLimitDefault = "150000.00";
        	directDepositLimitSettings.minimumNonSuspectPayrollAmount = "100.00";
            directDepositLimitSettings.defaultBPCompanyLimit = "150000.00";
        	directDepositLimitSettings.defaultBPPayeeLimit = "100.00";
            var tier:AutoLimitIncreaseTier = new AutoLimitIncreaseTier();
            tier.companyCap = "1";
            tier.daysSinceFirstPayroll = "2";
            tier.employeeCap = "3";
            tier.increaseMultiplier = "4";
            tier.payrollsRun = "5";
            tier.level = "6";
            tier.sourceSystemCd = "QBOE";
            directDepositLimitSettings.autoLimitIncreaseTiers = new ArrayCollection([tier]);        	
        	mSAP.administrationService.saveDirectDepositLimitSettings(directDepositLimitSettings, "QBOE", 
        		getTestResponder(verifySaveDirectDepositLimitSettings, 2));
        }
        
        private function verifySaveDirectDepositLimitSettings(e:ResultEvent):void {        	
        	trace("save successful");        	                  	        	
        }
        
        /**
        * AdminSettingsService.postToGems
        * cannot test this we do not have a way to create a gems report 
        */
        /*public function testPostToGems():void {
        	runDataLoader("Company :: Create Basic Data", testPostToGems_Step2, 5);
        }
        
        private function testPostToGems_Step2(e:ResultEvent):void {
        	login(testPostToGems_Step3);
        }
                
        private function testPostToGems_Step3(e:ResultEvent):void {        	            	
        	mSAP.administrationService.postToGems(getTestResponder(verifyPostToGems, 2));
        }
        
        private function verifyPostToGems(e:ResultEvent):void {        	
        	trace("save successful");        	                  	        	
        }*/
        
        /**
         * AdminSettingsService.loadACHOffloadFiles
         * AdminSettingsService.confirmOffloadFiles 
         * 
         */
        public function testLoadACHOffloadFiles():void {
        	runDataLoader("Payroll :: ACH Offload Data", testLoadACHOffloadFiles_Step2, 5);
        }
        
        private function testLoadACHOffloadFiles_Step2(e:ResultEvent):void {
        	login(testLoadACHOffloadFiles_Step3);
        }
                
        private function testLoadACHOffloadFiles_Step3(e:ResultEvent):void {         	       	            	
        	mSAP.administrationService.getNachaFilesForOffload(getTestResponder(verifyLoadACHOffloadFiles, 2));
        }
        
        private function verifyLoadACHOffloadFiles(e:ResultEvent):void {        	        	
        	assertNotNull("Result", e.result);
        	assertTrue("Result is ArrayCollection", e.result is ArrayCollection);
        	var files:ArrayCollection = e.result as ArrayCollection;
        	assertTrue("File count", files.length == 2);
        	for each(var file:NachaFile in files){
        		assertNotNull("file id", file.fileId);
        		assertNotNull("file name", file.fileName);
        		assertNotNull("finalized time", file.finalizedTime);
        		assertNotNull("total credits", file.totalCredits);
        		assertNotNull("total debits", file.totalDebits);
        		
        		// setup for testing confirmation
        		file.confirmationCode = "123";        		
        	}
        	
        	mSAP.administrationService.confirmOffloadFiles(files, getTestResponder(verifyConfirmOffloadFiles, 2));        	        	                  	        	
        }
        
        private function verifyConfirmOffloadFiles(e:ResultEvent):void {
        	trace("save successful");
        }
		
		/*
			We do not have flux setup so we cannot test these functions
			AdminSettingsService.scheduleSecondaryOffload			
			AdminSettingsService.isSecondOffloadScheduled						
	    */
		
		

	}
}
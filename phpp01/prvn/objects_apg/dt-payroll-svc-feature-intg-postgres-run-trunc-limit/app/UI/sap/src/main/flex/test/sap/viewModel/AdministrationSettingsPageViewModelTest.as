package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.model.SourceSystemEnum;
	import psp.sap.service.AdministrationService;
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.AdministrationInspectorViewModel;
	import psp.sap.viewmodel.AdministrationSettingsPageViewModel;
	import psp.sap.viewmodel.AdministrationSettingsTopicViewModel;
	
	import test.mock.MockAdministrationService;
import test.mock.data.AdministrativeData;

public class AdministrationSettingsPageViewModelTest extends AbstractPartViewModelTestBase
	{
		
		private var mViewModel: AdministrationSettingsPageViewModel = new AdministrationSettingsPageViewModel();
		private var mDataService:MockAdministrationService;
		
		public function AdministrationSettingsPageViewModelTest(methodName:String=null)
		{
			super(methodName);
		}					
		
        public static function suite() : TestSuite {
			return new TestSuite( AdministrationSettingsPageViewModelTest );            
        }		
		
		override public function setUp():void {
   			super.setUp();   			   			   			
   							
   			trackedProperties = [
   									"ACHWaitingPeriod",
   									"numDaysVerifyLimits",
									"numAttemptsVerifyLimits",
									"numViolations",
									"numDaysPerCompany",
									"numDaysPerEmployee",
									"ddLimitPerCompany",
									"ddLimitPerEmployee",
									"maxPayrollAmt",
									"minPayrollAmt",
									"sourceSystem"									
   								];
   								
   			mDataService = mSAP.administrationService as MockAdministrationService;   			   			
   			
   			viewModelToTest(AbstractPartViewModel(mViewModel));
   		}
   		
   		override public function testViewModel():void {
   			// setup load model data mock expectation
   			mDataService.expectsGetDirectDepositLimitSettings(SourceSystemEnum.QBOE.code).willReturnAsync(AdministrativeData.getDirectDepositLimitSettings());
   			
   			testActivationSequence();
   		}   		   		   		   		   						
   		
   		override protected function verifyModelDataSetup():void { 
   			
   			assertTrue(mDataService.errorMessage(), mDataService.success());
   			  			
        	assertEquals("numDaysVerifyLimits", 
        				 AdministrativeData.getDirectDepositLimitSettings().companyBankAccountDurationLimitForVerification,
        				 mViewModel.numDaysVerifyLimits);
        				 
        	assertEquals("numAttemptsVerifyLimits", 
        				 AdministrativeData.getDirectDepositLimitSettings().companyBankAccountVerificationAttemptLimit,
        				 mViewModel.numAttemptsVerifyLimits);
        				 
        	assertEquals("numViolations", 
        				 AdministrativeData.getDirectDepositLimitSettings().consecutiveLimitViolationLimit,
        				 mViewModel.numViolations);
        				 
        	assertEquals("numDaysPerCompany",
						 AdministrativeData.getDirectDepositLimitSettings().DDCompanyLimitDuration,
						 mViewModel.numDaysPerCompany);
						 
        	assertEquals("numDaysPerEmployee",
        				 AdministrativeData.getDirectDepositLimitSettings().DDEmployeeLimitDuration,
        				 mViewModel.numDaysPerEmployee);
        				 
        	assertEquals("ddLimitPerCompany", 
        				 AdministrativeData.getDirectDepositLimitSettings().defaultDDCompanyLimit,
        				 mViewModel.ddLimitPerCompany);
        				 
        	assertEquals("ddLimitPerEmployee", 
        				 AdministrativeData.getDirectDepositLimitSettings().defaultDDEmployeeLimit,
        				 mViewModel.ddLimitPerEmployee);
        				 
        	assertEquals("maxPayrollAmt", 
        				 AdministrativeData.getDirectDepositLimitSettings().maxDDCompanyLimitDefault,
        				 mViewModel.maxPayrollAmt);
        				 
        	assertEquals("minPayrollAmt", 
        				 AdministrativeData.getDirectDepositLimitSettings().minimumNonSuspectPayrollAmount,
        				 mViewModel.minPayrollAmt);
        									
			testBindableProperties();        									
   		}
   		
   		override protected function testBindableProperties():void {
   			clearPropertyChangeEventHistory();
   			
   			testBindableProperty(mViewModel, "ddLimitPerCompany", "51000.00");
   			testBindableProperty(mViewModel, "ddLimitPerEmployee", "21000.00");
   			testBindableProperty(mViewModel, "numDaysPerCompany", "9");
   			testBindableProperty(mViewModel, "numDaysPerEmployee", "15");
   			testBindableProperty(mViewModel, "numViolations", "6");
   			testBindableProperty(mViewModel, "numAttemptsVerifyLimits", "11");
   			testBindableProperty(mViewModel, "numDaysVerifyLimits", "364");
   			testBindableProperty(mViewModel, "minPayrollAmt", "200.00");
   			testBindableProperty(mViewModel, "maxPayrollAmt", "151000.00");
   			testBindableProperty(mViewModel, "ACHWaitingPeriod", "6"); 
			testBindableProperty(mViewModel, "ACHWaitingPeriod", SourceSystemEnum.EWS);   			
   		}
	}
}
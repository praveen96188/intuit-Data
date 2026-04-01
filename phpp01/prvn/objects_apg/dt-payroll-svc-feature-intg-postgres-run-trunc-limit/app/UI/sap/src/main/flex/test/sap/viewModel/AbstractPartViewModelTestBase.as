package test.sap.viewModel
{
	import flash.errors.IllegalOperationError;

    import org.as3commons.lang.ClassUtils;

    import psp.sap.application.SAP;
	import psp.sap.application.enums.ViewModelActivationStateEnum;
	import psp.sap.service.AdministrationService;
    import psp.sap.service.AuthService;
    import psp.sap.service.BankReturnService;
	import psp.sap.service.BillingService;
	import psp.sap.service.CompanyService;
	import psp.sap.service.PSPSystemInformationService;
	import psp.sap.service.PayrollRunService;
    import psp.sap.service.PropertyAuditService;
    import psp.sap.service.TaxCreditsService;
    import psp.sap.service.TaxService;
	import psp.sap.service.UserService;
	import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.CompositePartViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;
	
	import test.mock.MockAdministrationService;
    import test.mock.MockAuthService;
    import test.mock.MockBankReturnService;
	import test.mock.MockBillingService;
	import test.mock.MockCompanyService;
	import test.mock.MockEmployeeService;
	import test.mock.MockPSPSystemInformationService;
	import test.mock.MockPayrollRunService;
import test.mock.MockPropertyAuditService;
    import test.mock.MockTaxCreditsService;
    import test.mock.MockTaxService;
	import test.mock.MockUserService;
	import test.sap.application.SAPTestBase;
	
	public class AbstractPartViewModelTestBase extends SAPTestBase
	{
		private var partViewModel:AbstractPartViewModel;  
		
		public function AbstractPartViewModelTestBase(methodName:String=null)
		{
			super(methodName);
		}
		
		override public function setUp():void {
   			super.setUp();

            asyncTimeout = 1000;
   			
   			trackedEvents = [	   								
   								ViewModelEvent.MODEL_DATA_SETUP_COMPLETED,
   								ViewModelEvent.MODEL_DATA_LOADED,
   								ViewModelEvent.ACTIVATED,
   								ViewModelEvent.DEACTIVATED,
   								ViewModelEvent.CLOSE,
   								ViewModelEvent.SAVE_SUCCEEDED   								
   							]; 
   							
   			// override the data service    								
   			mSAP.administrationService = new MockAdministrationService();
   			mSAP.bankReturnService = new MockBankReturnService();
   			mSAP.billingService = new MockBillingService();
   			mSAP.companyService = new MockCompanyService();   			
   			mSAP.payrollRunService = new MockPayrollRunService();
   			mSAP.systemInformationService = new MockPSPSystemInformationService();
   			mSAP.userService = new MockUserService();
            mSAP.authService = new MockAuthService();
   			mSAP.taxService = new MockTaxService();
   			mSAP.employeeService = new MockEmployeeService();
            mSAP.propertyAuditService = new MockPropertyAuditService();
            mSAP.taxCreditsService = new MockTaxCreditsService();
   		}
   		
   		override public function tearDown():void {
   			super.tearDown();   			
   			
   			if(partViewModel != null){
				trackEventsStop(partViewModel);
   			}
   			
   			// reset the data services
   			mSAP.administrationService = new AdministrationService();
   			mSAP.bankReturnService = new BankReturnService();
   			mSAP.billingService = new BillingService();
   			mSAP.companyService = new CompanyService();   			
   			mSAP.payrollRunService = new PayrollRunService();
   			mSAP.systemInformationService = new PSPSystemInformationService();
   			mSAP.userService = new UserService();
            mSAP.authService = new AuthService();
   			mSAP.taxService = new TaxService();
            mSAP.propertyAuditService = new PropertyAuditService();
            mSAP.taxCreditsService = new TaxCreditsService();   

   		}
   		
   		/**
   		 * This is the entry point for the test it is only used to setup
   		 * the expectation for the load model data service
   		 */ 
   		// override in sub class  		
   		public function testViewModel():void {   			
   			testActivationSequence();
   		}
   		
   		protected final function testActivationSequence():void {
   			trackEventsStart(partViewModel);
   			
   			addAsyncVerifier(partViewModel, ViewModelEvent.ACTIVATED, verifyActivationSequence);
   			partViewModel.activate();
   		}
   		
   		private function verifyActivationSequence(e:ViewModelEvent):void {
   			assertEventHistory([ViewModelEvent.MODEL_DATA_LOADED,
   								ViewModelEvent.MODEL_DATA_SETUP_COMPLETED,
   								ViewModelEvent.ACTIVATED]);

            clearEventHistory();
   								
   			assertEquals("activation state", ViewModelActivationStateEnum.ACTIVATED, partViewModel.activationState);
   								
   			verifyModelDataSetup();
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Verify the expected parameters were passed to the load
   		 * 	  model data service
   		 * 2. Test data loaded by load model data function
   		 * 3. Test backing property initilization   		 
   		 */
   		// override in sub class
   		protected function verifyModelDataSetup():void {
   			testBindableProperties();
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Test bindable properties to make sure they are dispatching property change events    		 
   		 */
   		// override in sub class
   		protected function testBindableProperties():void {
   			verifyHasChangedLogic();
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Test has changed logic
   		 * 2. Test updateCanSave is called when setting view model properties 
   		 */
   		// override in sub class
   		protected function verifyHasChangedLogic():void {
   			testValidators();
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Test each validator setup in the view model 
   		 * 2. Test the boundry conditions of each validator with requirement values
   		 * 3. Verify updateCanSave is called when setting view model properties 
   		 */
   		// override in sub class
   		protected function testValidators():void {
   			testSave();
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Verify save initiated
   		 * 2. Verify expected parameters passed to the save method
   		 */
   		// override in sub class
   		protected function testSave():void {
   			verifySave(new ViewModelEvent(ViewModelEvent.SAVE_SUCCEEDED));
   			verifyRefresh(new ViewModelEvent(ViewModelEvent.MODEL_DATA_SETUP_COMPLETED));
   		}
   		
   		/**
   		 * Testing Goals
   		 * 1. Test the save was called with the expected parameters
   		 * 2. Test that the save event was dispatched
   		 * 3. Verify any parameters that need to be reset were reset to the expected parameters
   		 */
   		// override in sub class
   		protected function verifySave(e:ViewModelEvent):void {}
   		
   		/**
   		 * Testing Goals
   		 * 1. Verify any properties that need to be reset were reset to the expected values
   		 */
   		// override in sub class
   		protected function verifyRefresh(e:ViewModelEvent):void {}
   		
   		//--------- utility functions --------
   		
   		protected function viewModelToTest(viewModel:AbstractPartViewModel):void {
   			partViewModel = viewModel;
   		}
   		   		
   		protected function testRequiredStringValidator(viewModel:Object, propertyName:String):void {
   			var temp:Object = viewModel[propertyName];
   			
   			// set field to empty
   			viewModel[propertyName] = "";
   			assertPropertyChangeEventHistory([{property:propertyName, newValue:""}, {property:"canSave", newValue:false}], true);
   			assertEquals("can save", false, viewModel.canSave);
   			
   			revertProperty(viewModel, propertyName, temp);
   		}

        protected function testRequiredStringValidatorOnDTO(viewModel:Object, dtoProperty:Object, propertyName:String):void {
   			var temp:Object = dtoProperty[propertyName];

   			// set field to empty
   			dtoProperty[propertyName] = "";
   			assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
   			assertEquals("can save", false, viewModel.canSave);

   			dtoProperty[propertyName] = temp;
   		}
   		   		
   		protected function testNumberValidator(viewModel:Object, propertyName:String, minNumber:Number=NaN, maxNumber:Number=NaN):void {   			   			
   			var temp:Object = viewModel[propertyName];
   			
   			// test min
   			if(!isNaN(minNumber)){
   				minNumber--;   			
   				viewModel[propertyName] = minNumber;
   				assertPropertyChangeEventHistory([{property:propertyName, newValue:minNumber}, {property:"canSave", newValue:false}], true);
   				assertEquals("can save", false, viewModel.canSave);
   				
	   			revertProperty(viewModel, propertyName, temp);
   			}
   			
   			// test max
   			if(!isNaN(maxNumber)){
   				maxNumber++;
   				viewModel[propertyName] = maxNumber;
   				assertPropertyChangeEventHistory([{property:propertyName, newValue:maxNumber}, {property:"canSave", newValue:false}], true);
   				assertEquals("can save", false, viewModel.canSave);
   				   				
	   			revertProperty(viewModel, propertyName, temp);
   			}

            // test default
   			viewModel[propertyName] = "blah"; // not a valid number format
   			assertEquals("can save", false, viewModel.canSave);
   			revertProperty(viewModel, propertyName, temp);
   		}

        protected function testNumberValidatorDTO(viewModel:Object, dtoProperty:Object, propertyName:String, minNumber:Number=NaN, maxNumber:Number=NaN, isRequired:Boolean=false):void {
   			var temp:Object = dtoProperty[propertyName];

   			// test min
   			if(!isNaN(minNumber)){
   				minNumber--;
   				dtoProperty[propertyName] = minNumber;
   				assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
   				assertEquals("can save", false, viewModel.canSave);

	   			dtoProperty[propertyName] =  temp;
   			}

   			// test max
   			if(!isNaN(maxNumber)){
   				maxNumber++;
   				dtoProperty[propertyName] = maxNumber;
   				assertPropertyChangeEventHistory([{property:"canSave", newValue:false}], true);
   				assertEquals("can save", false, viewModel.canSave);

	   			dtoProperty[propertyName] =  temp;
   			}

            if(isRequired){
                testRequiredStringValidatorOnDTO(viewModel, dtoProperty, propertyName);
            }

            // test default
   			dtoProperty[propertyName] = "blah"; // not a valid number format
   			assertEquals("can save", false, viewModel.canSave);
   			dtoProperty[propertyName] =  temp;
   		}

        protected function testPhonNumberValidatorDTO(viewModel:Object, dtoProperty:Object, propertyName:String, isRequired:Boolean=false):void {
            var temp:Object = dtoProperty[propertyName];

            if(isRequired){
                testRequiredStringValidatorOnDTO(viewModel, dtoProperty, propertyName);
            }

            // test invalid characters
            dtoProperty[propertyName] = "blah";
   			assertEquals("can save", false, viewModel.canSave);
   			dtoProperty[propertyName] =  temp;

            // less than 10 characters
            dtoProperty[propertyName] = "123456789";
   			assertEquals("can save", false, viewModel.canSave);
   			dtoProperty[propertyName] =  temp;
        }

        protected function testEmailValidatorDTO(viewModel:Object, dtoProperty:Object, propertyName:String, isRequired:Boolean=false):void {
            var temp:Object = dtoProperty[propertyName];

            if(isRequired){
                testRequiredStringValidatorOnDTO(viewModel, dtoProperty, propertyName);
            }

            // test invalid pattern
            dtoProperty[propertyName] = "blah";
   			assertEquals("can save", false, viewModel.canSave);
   			dtoProperty[propertyName] =  temp;
        }

        protected function testStringLengthValidatorDTO(viewModel:Object, dtoProperty:Object, propertyName:String, lenght:int, isRequired:Boolean=false):void {
            var temp:Object = dtoProperty[propertyName];

            if(isRequired){
                testRequiredStringValidatorOnDTO(viewModel, dtoProperty, propertyName);
            }

            dtoProperty[propertyName] = "";
            assertEquals("can save", false, viewModel.canSave);

            for(var i:int = 1; i<length; i++){
                dtoProperty[propertyName] += "a";
   			    assertEquals("can save", false, viewModel.canSave);
            }
            dtoProperty[propertyName] =  temp;
        }
   		   		    	
   		protected function testHasChanged(viewModel:Object, propertyName:String, validValue:Object):void {
   			assertEquals("has changed", false, viewModel.hasChanged);
   			
   			var temp:Object = viewModel[propertyName];
   			
   			viewModel[propertyName] = validValue;
   			assertPropertyChangeEventHistory([{property:propertyName, newValue:validValue}], true);
   			assertEquals("has changed", true, viewModel.hasChanged);
   			
	   		revertProperty(viewModel, propertyName, temp);
   		}

        protected function testHasChangedDTO(viewModel:Object, dtoProperty:Object):void {
   			assertEquals("has changed", false, viewModel.hasChanged);

            for(var propertyName:String in dtoProperty) {
   			    var temp:Object = dtoProperty[propertyName];
                var typeOf:String = typeof(dtoProperty[propertyName]);
                switch(typeOf) {
                    case "boolean":
                        dtoProperty[propertyName] = !temp;
                        break;                    

                    case "number":
                        dtoProperty[propertyName] = 1024587;
                        break;

                    case "string":
                        dtoProperty[propertyName] = "does it really matter";
                        break;

                    case "object":
                        dtoProperty[propertyName] = ClassUtils.newInstance(ClassUtils.forInstance(dtoProperty[propertyName]));
                        break;
                }

   			    assertEquals("has changed", true, viewModel.hasChanged);

	   		    dtoProperty[propertyName] = temp;
            }
   		}
   		 		
   		protected function testSAPStartEndDateValidator(viewModel:Object, startDatePropertyName:String, endDatePropertyName:String):void {   			   			
   			var checkDaysBefore:Number = 5;
   			
			var tempStartDate:Object = viewModel[startDatePropertyName];
			var tempEndDate:Object = viewModel[endDatePropertyName];
		
   			var startDate:Date = SAP.instance.PSPDate;
   			startDate.setTime(startDate.getTime() - ((checkDaysBefore)*SAP.instance.configuration.millisecondsPerDay));
   			var endDate:Date = SAP.instance.PSPDate;
   			var startDateString:String = dateFormatter.format(startDate);
   			var endDateString:String = dateFormatter.format(endDate);
   			
   			viewModel[startDatePropertyName] = startDateString;
   			viewModel[endDatePropertyName] = endDateString;
   			assertEquals("can save", true, viewModel.canSave);
   			viewModel[startDatePropertyName] = endDateString;
   			viewModel[endDatePropertyName] = startDateString;
   			assertEquals("can save", false, viewModel.canSave);
   			viewModel[startDatePropertyName] = endDateString;
   			viewModel[endDatePropertyName] = endDateString;
   			assertEquals("can save", true, viewModel.canSave);
   			   		
   			if (viewModel[startDatePropertyName] != tempStartDate) {
   				revertProperty(viewModel, startDatePropertyName, tempStartDate);
   			}
   			//this may have been set to the same value as before
   			if (viewModel[endDatePropertyName] != tempEndDate) {
   				revertProperty(viewModel, endDatePropertyName, tempEndDate);
   			}
   		}
   		
   		protected function testDateValidator(viewModel:Object, propertyName:String, daysBefore:Number=NaN, daysAfter:Number=NaN):void {   			   			
   			var temp:Object = viewModel[propertyName];
   			
   			// test min
   			if(!isNaN(daysBefore)){
   				var timmToSubtract:Number = (daysBefore+1)*SAP.instance.configuration.millisecondsPerDay;
   				var minDate:Date = SAP.instance.PSPDate;
   				minDate.setTime(minDate.getTime() - timmToSubtract);    			
   				viewModel[propertyName] = minDate;   				
   				assertEquals("can save", false, viewModel.canSave);
   				
	   			revertProperty(viewModel, propertyName, temp);
   			}
   			
   			// test max
   			if(!isNaN(daysAfter)){
   				var timmToAdd:Number = (daysAfter+1)*SAP.instance.configuration.millisecondsPerDay;
   				var maxDate:Date = SAP.instance.PSPDate;
   				maxDate.setTime(maxDate.getTime() + timmToAdd);    			
   				viewModel[propertyName] = maxDate;
   				assertEquals("can save", false, viewModel.canSave);
   				   				
	   			revertProperty(viewModel, propertyName, temp);
   			} 
   			   			
			// test default   			   			   			  		
   			viewModel[propertyName] = "blah"; // not a valid date format
   			assertEquals("can save", false, viewModel.canSave);   			   				
   			revertProperty(viewModel, propertyName, temp);   			   			   		
   		}
   		   		
   		protected function testBindableProperty(viewModel:Object, propertyName:String, newValue:Object):void {
   			var temp:Object = viewModel[propertyName];
   			
   			viewModel[propertyName] = newValue;
   			assertPropertyChangeEventHistory([{property:propertyName, newValue:newValue}], true);
   			
   			revertProperty(viewModel, propertyName, temp);
   		}
   		
   		protected function testBindableBoolean(viewModel:Object, propertyName:String):void {   			
			var temp:Object = viewModel[propertyName];
			
			if (! (temp is Boolean)) {
				throw new IllegalOperationError(propertyName + " is not a boolean");
			}
			testBindableProperty(viewModel, propertyName, !(temp as Boolean));
   		}
   		
   		protected function revertProperty(viewModel:Object, propertyName:String, oldValue:Object):void {
   			viewModel[propertyName] = oldValue;
	   		assertPropertyChangeEventHistory([{property:propertyName, newValue:oldValue}], true);	   		
   		}

        //returns a company inspector with the part host removed so the banner is not called
        protected function createTestCompanyInspector():CompanyInspectorViewModel {
            var inspector:CompanyInspectorViewModel = new CompanyInspectorViewModel(null);
            inspector.partHost = new CompositePartViewModel();
            return inspector;
        }

	}
}

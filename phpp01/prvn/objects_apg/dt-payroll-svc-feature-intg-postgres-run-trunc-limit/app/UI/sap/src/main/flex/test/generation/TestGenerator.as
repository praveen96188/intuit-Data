package test.generation
{
    import flash.utils.getQualifiedClassName;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.controls.TextArea;
    import mx.validators.DateValidator;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import org.as3commons.lang.ClassUtils;
    import org.as3commons.reflect.Accessor;
    import org.as3commons.reflect.MetaData;
    import org.as3commons.reflect.Type;

    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.viewmodel.*;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class TestGenerator
	{
        private static var mTextArea:TextArea;
        private static var mFullyQualifiedClassName:String;
        private static var mViewModel:AbstractPartViewModel;
        private static var mValidators:ArrayCollection;

		public static function generateAbstractPartTest(fullyQualifiedClassName:String, textArea:TextArea):void {
			// generated text is placed on this text area
            mTextArea = textArea;
            mFullyQualifiedClassName = fullyQualifiedClassName;


			
			if(mFullyQualifiedClassName != null){
                if(!ClassUtils.isSubclassOf(ClassUtils.forName(mFullyQualifiedClassName), AbstractPartViewModel)) {
                    mTextArea.text = mFullyQualifiedClassName + " is not a sub class of " + ClassUtils.getFullyQualifiedName(AbstractPartViewModel);
                    return;
                } 

				// instinciate the class
				mViewModel = ClassUtils.newInstance(ClassUtils.forName(mFullyQualifiedClassName)) as AbstractPartViewModel;

				// find all of the bindable properties that are declared by the class
				var type:Type = Type.forInstance(mViewModel);
                if(viewModelHasBackingProperties(type)){
                    mViewModel.addEventListener(ViewModelEvent.BACKING_PROPERTIES_INITIALIZED, generateTestCase, false, 0, true);
                    mViewModel.reset();
                }
                // old view model pattern
                else {                    
                    var className:Array = mFullyQualifiedClassName.split("\.");

                    mTextArea.text = generateAbstractPartTestClass(className[className.length-1], mViewModel, findBindableProperties(), copyValidators());
                }
								
			}
            else {
                mTextArea.text = "Error: Class name was null!";
            }
		}

        private static function findBindableProperties():ArrayCollection {
            var type:Type = Type.forInstance(mViewModel);
            var bindableProperties:ArrayCollection = new ArrayCollection();
            var sort:Sort = new Sort();
			sort.fields = [new SortField("name", false)];
			bindableProperties.sort = sort;
            for each(var property:Accessor in type.accessors){
                if(property.declaringType.name == type.name && property.metaData.length > 0 && property.name.indexOf("alidator") == -1){
                    var bindableProperty:BindableProperty = new BindableProperty();
                    bindableProperty.type = (property.type != null) ? property.type.name : null;
                    for each(var metaData:MetaData in property.metaData){
                        if(metaData.name == MetaData.BINDABLE) {
                            bindableProperty.name = property.name;
                        }
                        else if(metaData.name == "BackingProperty") {
                            bindableProperty.isBackingProperty = true;
                        }
                    }
                    if(bindableProperty.name != null){
                        bindableProperties.addItem(bindableProperty);
                    }
                }
            }
            bindableProperties.refresh();
            return bindableProperties;
        }

        private static function copyValidators():ArrayCollection {
            var validators:ArrayCollection = new ArrayCollection();
			var sort:Sort = new Sort();
			sort.fields = [new SortField("property", false)];
			validators.sort = sort;
            // find all of the validators ... this will only find the validators added to the validators array
            for each(var validator:Validator in mViewModel.validators){
                validators.addItem(validator);
            }
            validators.refresh();
            return validators;
        }

        private static function generateTestCase(e:ViewModelEvent):void {
            var className:Array = mFullyQualifiedClassName.split("\.");
            mTextArea.text = generateAbstractPartTestClass(className[className.length-1], mViewModel, findBindableProperties(), copyValidators());
        }
		
		private static function generateAbstractPartTestClass(className:String, viewModel:AbstractPartViewModel, bindableProperties:ArrayCollection, validators:ArrayCollection):String {
			var returnClass:String = new String();

			returnClass += "package test.sap.viewModel\n";
			returnClass += "{\n";
			
			returnClass += "\timport flexunit.framework.TestSuite;\n";
			returnClass += "\timport psp.sap.viewmodel.AbstractPartViewModel;\n";
			returnClass += "\timport psp.sap.viewmodel." + className +";\n\n";
						
			returnClass += "\tpublic class " + className + "Test extends AbstractPartViewModelTestBase\n";
			returnClass += "\t{\n";
			
			returnClass += "\t\tprivate var mViewModel:" + className + " = new " + className + "();\n";
			returnClass += "\t\tprivate var mDataService:// todo add mock service type;\n";
			returnClass += "\n";
			
			returnClass += "\t\tpublic function " + className + "Test(methodName:String=null)\n";
			returnClass += "\t\t{\n";
			returnClass += "\t\t\tsuper(methodName);\n";
			returnClass += "\t\t}\n";
			returnClass += "\n";
			
			returnClass += "\t\tpublic static function suite() : TestSuite {\n";
			returnClass += "\t\t\treturn new TestSuite( " + className + "Test );\n";
			returnClass += "\t\t}\n";
			returnClass += "\n";
			
			// start setUp
			returnClass += "\t\toverride public function setUp():void {\n";
			returnClass += "\t\t\tsuper.setUp();\n";
			returnClass += "\n";
			returnClass += "\t\t\ttrackedProperties = [\n";
			for each(var property:BindableProperty in bindableProperties){
				returnClass += "\t\t\t\t\"" + property.name + "\",\n";
			}	
			returnClass += "\t\t\t\t\"canSave\"\n";
			returnClass += "\t\t\t];\n";
			returnClass += "\n";
			
			returnClass += "\t\t\tmDataService = // todo setup data service ex. mSAP.administrationService as MockAdministrationService;\n";   			   			
   			returnClass += "\n";
   			
   			returnClass += "\t\t\t// set the view model to test\n";
   			returnClass += "\t\t\tviewModelToTest(AbstractPartViewModel(mViewModel));\n";
   			returnClass += "\t\t}\n";
   			returnClass += "\n";
   			// end setUp
   			
   			// start testViewModel 
   			returnClass += "\t\toverride public function testViewModel():void {\n";
   			returnClass += "\t\t\t// setup load model data mock expectation\n";
   			returnClass += "\t\t\t// todo setup expected data load service with arguments ex. mDataService.expectsGetDirectDepositLimitSettings(SourceSystemEnum.QBDT.code).willReturnAsync(CompanyData.getSettings());\n";
   			returnClass += "\t\t\ttestActivationSequence();\n";
   			returnClass += "\t\t}\n";
   			returnClass += "\n";
   			// end testViewModel
   			
   			// start verifyModelDataSetup
   			returnClass += "\t\toverride protected function verifyModelDataSetup():void {\n";
   			returnClass += "\t\t\tassertTrue(mDataService.errorMessage(), mDataService.success());\n";
   			returnClass += "\n";
   			returnClass += "\t\t\t/* todo check loaded data ex. assertEquals(\"payrolls\", PayrollData.getPayrolls().length, mViewModel.payrolls.length);*/\n";
   			returnClass += "\n";
   			returnClass += "\t\t\t// todo also test if backing properties were initilized properly\n";
   			returnClass += "\n";
            returnClass += "\t\t\t// can save start false\n";
            returnClass += "\t\t\tassertFalse(\"can save\", mViewModel.canSave);\n";
            returnClass += "\n";
   			returnClass += "\t\t\ttestBindableProperties();\n";
   			returnClass += "\t\t}\n";
   			returnClass += "\n";
   			// start verifyModelDataSetup
   			
   			// start testBindableProperties
   			returnClass += "\t\toverride protected function testBindableProperties():void {\n";
   			returnClass += "\t\t\tclearPropertyChangeEventHistory();\n";
   			returnClass += "\n";
            returnClass += "\t\t\t// todo add a valid values\n";
   			for each(var property1:BindableProperty in bindableProperties){
   				returnClass += "\t\t\ttestBindableProperty(mViewModel, \"" + property1.name + "\", " + property1.type + ");\n";
   			}
   			returnClass += "\n";
   			if(validators.length > 0){
   				returnClass += "\t\t\tverifyHasChangedLogic();\n";
   			}
   			returnClass += "\t\t}\n";
   			returnClass += "\n";
   			// end testBindableProperties
   			
   			// assuming that if a page has validators it has a save method
   			if(validators.length > 0){
   				// start verifyHasChangedLogic
   				returnClass += "\t\toverride protected function verifyHasChangedLogic():void {\n";
   				returnClass += "\t\t\t// the generation assumes that all of the bindable properties are tied to has changed\n";
   				returnClass += "\t\t\t// make adjustments where needed.\n";
                returnClass += "\t\t\t// todo add a valid values\n";
   				for each(var property2:BindableProperty in bindableProperties){
                    if(!property2.isBackingProperty){
   					    returnClass += "\t\t\ttestHasChanged(mViewModel, \"" + property2.name + "\", null);\n";
                    }
                    else {
                        returnClass += "\t\t\ttestHasChangedDTO(mViewModel, mViewModel." + property2.name + ");\n";
                    }
   				} 
   				returnClass += "\n";
   				returnClass += "\t\t\ttestValidators();\n";
   				returnClass += "\t\t}\n";
   				returnClass += "\n";
   				// end verifyHasChangedLogic
   				
   				// start testValidators
   				returnClass += "\t\toverride protected function testValidators():void {\n";
   				returnClass += "\t\t\t// update has changed so can save is true\n";
   				returnClass += "\t\t\t// todo make cansave true\n";
   				returnClass += "\t\t\t// ex. mViewModel.numDaysVerifyLimits = \"364\";\n";
   				returnClass += "\n";
   				returnClass += "\t\t\tassertTrue(\"can save\", mViewModel.canSave);\n";
   			   	returnClass += "\n";
   			   	returnClass += "\t\t\t// todo test each validator. Test generation tries to guess how to\n";
   			   	returnClass += "\t\t\t// test each validator. It is the testers responsibility make sure the\n";
   			   	returnClass += "\t\t\t// validators are being tested correctly\n";
   			   	for each(var validator:Validator in validators){ 
   			   		if(getQualifiedClassName(validator).indexOf(":Validator") > 0 && validator.required){
   			   			returnClass += "\t\t\t// todo make sure this test is correct and add proper dto object if needed\n";
                        if(validator.source == mViewModel){
   			   			    returnClass += "\t\t\ttestRequiredStringValidator(mViewModel, \"" + validator.property + "\");\n";
                        }
                        else{
                            returnClass += "\t\t\ttestRequiredStringValidatorOnDTO(mViewModel, mViewModel. dtoProperty, \"" + validator.property + "\");\n";
                        }
   			   		}
   			   		else if(validator is NumberValidator){
   			   			returnClass += "\t\t\t// todo add min and max numbers and dto object if needed\n";
                        if(validator.source == mViewModel){
                            returnClass += "\t\t\ttestNumberValidator(mViewModel, \"" + validator.property + "\", NaN, NaN);\n";
                        }
                        else{
                            returnClass += "\t\t\ttestNumberValidatorDTO(mViewModel, mViewModel. dtoProperty, \"" + validator.property + "\", NaN, NaN);\n";
                        }

   			   		}
   			   		else if(validator is DateValidator){
   			   			returnClass += "\t\t\t// todo add min and max numbers and dto object if needed\n";
                        if(validator.source == mViewModel){
   			   			    returnClass += "\t\t\ttestDateValidator(mViewModel, \"" + validator.property + "\", NaN, NaN);\n";
                        }
                        else{
                            returnClass += "\t\t\ttestDateValidatorDTO(mViewModel, mViewModel. dtoProperty, \"" + validator.property + "\", NaN, NaN);\n";
                        }

   			   		}
                    else if(validator is SAPStartEndDateValidator){
   			   			returnClass += "\t\t\ttestSAPStartEndDateValidator(mViewModel, \"" + SAPStartEndDateValidator(validator).startDateProperty + "\", \"";
                        returnClass += SAPStartEndDateValidator(validator).endDateProperty + "\");\n";
   			   		}
   			   		else {
   			   			returnClass += "\t\t\t// todo test validator for " + validator.property + "\n";
   			   		}		   		   			   		
   				}
   				returnClass += "\n";
   				returnClass += "\t\t\ttestSave();\n";
   				returnClass += "\t\t}\n";
   				returnClass += "\n";
   				// end testValidators   				
   				
   				// start testSave
   				returnClass += "\t\toverride protected function testSave():void {\n";
   				returnClass += "\t\t\tassertTrue(\"can save\", mViewModel.canSave);\n";
   				returnClass += "\n";
   				returnClass += "\t\t\t// todo setup expected service save method ex. mDataService.expectsSaveDirectDepositLimitSettings(mViewModel.directDepositLimitSettings, SourceSystemEnum.QBDT.code);\n";
   				returnClass += "\t\t\taddAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);\n";
   				returnClass += "\t\t\t// todo only needed if data is reloaded after save addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);\n";
   				returnClass += "\n";
   				returnClass += "\t\t\tmViewModel.save();\n";
   				returnClass += "\t\t}\n";
   				returnClass += "\n";
   				// end testSave
   				
   				// start verifySave
   				returnClass += "\t\toverride protected function verifySave(e:ViewModelEvent):void {\n";
   				returnClass += "\t\t\tassertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);\n";
   				returnClass += "\n";
                returnClass += "\t\t\t// todo if your page has a refresh remove the next line the mock services will be checked in the refresh method\n";
   				returnClass += "\t\t\tassertTrue(mDataService.errorMessage(), mDataService.success());\n";
   				returnClass += "\t\t\t// todo if only the backing properties are reset and no data is reloaded\n";
   				returnClass += "\t\t\t// test the reset backing properties here\n";
   				returnClass += "\t\t}\n";
   				returnClass += "\n";
   				// end verifySave		   			   			
   				
   				// start verifyRefresh
   				returnClass += "\t\toverride protected function verifyRefresh(e:ViewModelEvent):void {\n";
   				returnClass += "\t\t\tassertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);\n";
                returnClass += "\t\t\tassertTrue(mDataService.errorMessage(), mDataService.success());\n";
   				returnClass += "\n";
   				returnClass += "\t\t\t// todo verify expected properties are set or reset\n";
   				returnClass += "\t\t\t// ie assertFalse(\"can save\", mViewModel.canSave);\n";
   				returnClass += "\t\t\t/* or assertEquals(\"numDaysVerifyLimits\",\n";
   				returnClass += "\t\t\t   mDataService.directDepositLimitSettingsMockData.companyBankAccountDurationLimitForVerification,\n";
   				returnClass += "\t\t\t   mViewModel.numDaysVerifyLimits);*/\n";
   				returnClass += "\t\t}\n";
   				returnClass += "\n";
        		// end verifyRefresh  		
   			}
   			
			returnClass += "\t}\n";
			returnClass += "}\n";
			
			return returnClass;  			
		}

        private static function viewModelHasBackingProperties(viewModelType:Type):Boolean {
            var retval:Boolean = false;
            for each(var property:Accessor in viewModelType.accessors){
                for each(var metaData:MetaData in property.metaData){
                    if(metaData.name == "BackingProperty") {
                        retval = true;
                        if(mViewModel[property.name] == null){
                            mViewModel[property.name] = ClassUtils.newInstance(ClassUtils.forName(property.type.fullName));
                        }
                    }
                }
            }
            return retval;
        }
	}
}

class BindableProperty {
    public var name:String;
    public var isBackingProperty:Boolean;
    public var type:String;
}

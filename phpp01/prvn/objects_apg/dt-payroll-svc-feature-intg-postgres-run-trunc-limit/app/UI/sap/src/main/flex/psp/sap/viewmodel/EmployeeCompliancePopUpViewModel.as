package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.EmployeeComplianceData;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.validators.SAPValidators;

    public class EmployeeCompliancePopUpViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty (context=true)]
        public var employeeInfo:EmployeeInfo;
        [Bindable] [BackingProperty (context=true)]
        public var saveMethod:String; /* Add | Edit */
        [Bindable] [BackingProperty (context=true)]
        public var employeeComplianceData:EmployeeComplianceData;


        public static function createActivator(employeeInfo:EmployeeInfo, saveMethod:String, employeeComplianceData:EmployeeComplianceData):Object {
            return {"employeeInfo":employeeInfo, "saveMethod":saveMethod, "employeeComplianceData":employeeComplianceData};
        }

        private var mSelectedState:String;
        [Bindable] [BackingProperty]
        public function get selectedState():String{
            return mSelectedState;
        }
        public function set selectedState(value:String ):void{
            mSelectedState = value;
            onStateChanged();
        }

        public var mSelectedDomain:String;
        [Bindable] [BackingProperty]
        public function get selectedDomain():String{
            return mSelectedDomain;
        }
        public function set selectedDomain(value:String ):void{
            mSelectedDomain= value;
            enableNameValueCombo = mSelectedDomain != null && mSelectedDomain != "";
        }

        [Bindable] [BackingProperty]
        public var selectedName:String;

        [Bindable] [BackingProperty]
        public var wagePlanValue:String;

        [Bindable] [BackingProperty]
        public var description:String;

        [Bindable] [BackingProperty]
        public var rulesVersion:String;

        //Drop down value list
        [Bindable]
        public var stateList:ArrayCollection = new ArrayCollection(["", "CA", "AK", "LA", "WA", "WV"]);
        [Bindable]
        public var domainList:ArrayCollection = new ArrayCollection();
        [Bindable]
        public var nameValueList:ArrayCollection = new ArrayCollection();

        [Bindable]
        public var enableDomainCombo:Boolean = false;
        [Bindable]
        public var enableNameValueCombo:Boolean = false;

        //Validators
        [Bindable]
        public var stateValidator:Validator;
        [Bindable]
        public var domainValidator:Validator;
        [Bindable]
        public var nameValidator:Validator;
        [Bindable]
        public var wagePlanValueValidator:Validator;

        public function EmployeeCompliancePopUpViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.EMPLOYEE_COMPLIANCE_EDIT;
            this.reloadOnSave = true;

            stateValidator = SAPValidators.createStringValidator(this, "selectedState", true, 1);
            domainValidator = SAPValidators.createStringValidator(this, "selectedDomain", true, 1);
            nameValidator = SAPValidators.createStringValidator(this, "selectedName", true, 1);
            wagePlanValueValidator = SAPValidators.createStringValidator(this, "wagePlanValue", true, 1);

            validators.push(stateValidator);
            validators.push(domainValidator);
            validators.push(nameValidator);
            validators.push(wagePlanValueValidator);
        }

        override protected function initializeBackingProperties():void {
            if(employeeComplianceData != null){
                selectedState = employeeComplianceData .state;
                selectedDomain = employeeComplianceData .wagePlanDomain;
                selectedName = employeeComplianceData .name;
                wagePlanValue = employeeComplianceData .wagePlanValue;
                description = employeeComplianceData .description;
                rulesVersion = employeeComplianceData .rulesVersion;
            }
        }

        private function onStateChanged():void{
            if(selectedState == ""){
                enableDomainCombo = false;
                enableNameValueCombo = false;
                return;
            }
            if(selectedState == "CA") {
                domainList.removeAll();
                domainList.source.push("");
                domainList.source.push("WorkOrLiveState");
                domainList.refresh();
                nameValueList.removeAll();
                nameValueList.source.push("");
                nameValueList.source.push("WPC");
                nameValueList.refresh();

            } else if(selectedState == "AK"){
                domainList.removeAll();
                domainList.source.push("");
                domainList.source.push("WorkState");
                domainList.refresh();
                nameValueList.removeAll();
                nameValueList.source.push("");
                nameValueList.source.push("OC");
                nameValueList.source.push("GC");
                nameValueList.refresh();
            } else if(selectedState == "LA" || selectedState == "WA"){
                domainList.removeAll();
                domainList.source.push("");
                domainList.source.push("WorkState");
                domainList.refresh();
                nameValueList.removeAll();
                nameValueList.source.push("");
                nameValueList.source.push("OC");
                nameValueList.refresh();
            }
            else if(selectedState == "WV"){
                domainList.removeAll();
                domainList.source.push("");
                domainList.source.push("WorkState");
                domainList.refresh();
                nameValueList.removeAll();
                nameValueList.source.push("");
                nameValueList.source.push("FCC");
                nameValueList.refresh();
            }
            enableDomainCombo =true;
        }

        override protected function executeSave():void {

            var eComplianceData:EmployeeComplianceData  = new EmployeeComplianceData();
            eComplianceData.state = selectedState;
            eComplianceData.wagePlanDomain = selectedDomain;
            eComplianceData.name = selectedName;
            eComplianceData.wagePlanValue = wagePlanValue;
            eComplianceData.description = description;
            eComplianceData.rulesVersion = rulesVersion;
            eComplianceData.id = employeeComplianceData.id;

            SAP.instance.employeeService.updateEmployeeComplianceData(company.sourceSystemCd,
                                                                      company.companyId,
                                                                      employeeInfo.employeeGseq,
                                                                      eComplianceData,
                                                                      saveMethod,
                                                                      createSaveResponder());

        }

    }
}

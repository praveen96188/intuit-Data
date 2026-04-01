package psp.sap.viewmodel
{
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.EmployeeComplianceData;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class EmployeeComplianceViewModel
    extends CompositePartViewModel
    {

        [Bindable] [BackingProperty]
        public var employeeInfo:EmployeeInfo;

        [Bindable]
        [ArrayElementType("psp.sap.model.EmployeeComplianceData")]
        public var complianceInfoList:ArrayCollection;

        [Bindable]
        public var hasComplianceInfo:Boolean;

        private var mComplianceEditPopUp:PopUpPartViewModel;
        private var mComplianceEditPopUpViewModel:EmployeeCompliancePopUpViewModel;

        private var complianceDataToDelete:EmployeeComplianceData;

        public function EmployeeComplianceViewModel()
        {
            this.label = CompanyInspectorPageEnum.EMPLOYEE_COMPLIANCE;
            this.reloadOnSave = true;

            mComplianceEditPopUp = addPopUpPart(CompanyInspectorPageEnum.EMPLOYEE_COMPLIANCE_EDIT);
            mComplianceEditPopUpViewModel = mComplianceEditPopUp.addNewPart(EmployeeCompliancePopUpViewModel, CompanyInspectorPageEnum.EMPLOYEE_COMPLIANCE_EDIT) as EmployeeCompliancePopUpViewModel;
            mComplianceEditPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
        }

        override protected function loadModelData():void {
            SAP.instance.employeeService.getEmployeeComplianceDataList(
                    company.sourceSystemCd,
                    company.companyId,
                    employeeInfo.employeeGseq,
                    createLoadModelDataResponder(onComplianceInfoLoaded));
        }

        public function onComplianceInfoLoaded(e:ResultEvent):void{
            complianceInfoList = e.result as ArrayCollection;
            hasComplianceInfo = complianceInfoList.length > 0;
        }

        public function viewComplianceEditPopup(complianceData:EmployeeComplianceData , saveMethod:String):void {
            mComplianceEditPopUpViewModel.setActivator(EmployeeCompliancePopUpViewModel.createActivator(employeeInfo, saveMethod, complianceData));
            mComplianceEditPopUp.closeOnSave = true;
            mComplianceEditPopUp.displayPopUp();
        }

        public function deleteComplianceData(complianceData:EmployeeComplianceData):void{
            complianceDataToDelete = complianceData;
            forceSave();
        }

        override protected function executeSave():void {
            SAP.instance.employeeService.updateEmployeeComplianceData(company.sourceSystemCd,
                                                                      company.companyId,
                                                                      employeeInfo.employeeGseq,
                                                                      complianceDataToDelete,
                                                                      "Remove",
                                                                      createSaveResponder());
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

    }
}
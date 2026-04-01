package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.EmployeeInfo;

    public class EmployeeTaxabilityViewModel
    extends AbstractPartViewModel
    {
        [Bindable] [BackingProperty]
        public var employeeInfo:EmployeeInfo;

        [Bindable]
        public var hasTaxabilityInfo:Boolean;

        [Bindable] [BackingProperty]
        [ArrayElementType("psp.sap.model.EmployeeTaxabilityInfo")]
        public var taxabilityInfoList:ArrayCollection;

        public function EmployeeTaxabilityViewModel()
        {
            this.label = CompanyInspectorPageEnum.EMPLOYEE_TAXABILITY;
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            SAP.instance.employeeService.getEmployeeTaxabilityInfo(company.sourceSystemCd,
                                                                   company.companyId,
                                                                   employeeInfo.employeeGseq,
                                                                   createLoadModelDataResponder(onTaxabilityInfoLoaded));

        }

        public function onTaxabilityInfoLoaded(e:ResultEvent):void{
            taxabilityInfoList = e.result as ArrayCollection;
            hasTaxabilityInfo = taxabilityInfoList.length > 0;
        }
    }
}
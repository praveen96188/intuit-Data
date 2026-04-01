package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.formatters.SAPDateFormatters;
	import psp.sap.model.EmployeeBankAccountHistoryItem;
	import psp.sap.model.EmployeeInfo;
	
	public class BanksEmployeeAccountHistoryViewModel extends AbstractPartViewModel
	{
		

		[Bindable] [BackingProperty(context=true)] public var employeeInfo:EmployeeInfo;
		
		[ArrayElementType("psp.sap.model.EmployeeBankAccountHistory")]
		[Bindable] public var employeeBankAccountHistories:ArrayCollection = new ArrayCollection();				
		
		public function BanksEmployeeAccountHistoryViewModel()
		{
			this.label = CompanyInspectorPageEnum.BANKS_EMPLOYEE_ACCOUNT_HISTORY;
            this.shallowCopyFields = ["employeeId", "firstName", "lastName", "middleName"];
		}

        public static function createActivator(employeeInfo:EmployeeInfo):Object {
            return {"employeeInfo":employeeInfo};
        }

		
		[Bindable ("propertyChange")]
		public function get employeeName():String {
			return employeeInfo.fullName;
		}
		
		override protected function loadModelData():void {
			employeeBankAccountHistories.removeAll();
			
			SAP.instance.companyService.getEmployeeBankAccountHistory(
					company.companyId, employeeInfo.employeeId, company.sourceSystemCd,
					createLoadModelDataResponder(onEmployeeBankAccountLoaded));										
		}
		
		public function onEmployeeBankAccountLoaded(e:ResultEvent):void {
			employeeBankAccountHistories = e.result as ArrayCollection;			
		}
						  		  	
	}
}
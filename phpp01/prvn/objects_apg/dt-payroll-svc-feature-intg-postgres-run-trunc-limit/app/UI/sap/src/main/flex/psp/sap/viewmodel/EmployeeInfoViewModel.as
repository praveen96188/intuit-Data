package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.view.UIUtils;
	
	public class EmployeeInfoViewModel extends AbstractPartViewModel
	{
		
		[Bindable]
		[ArrayElementType("psp.sap.model.EmployeeInfo")]
		public var employees:ArrayCollection;
			
		override protected function loadModelData():void {
			SAP.instance.companyService.getEmployees(
					company.companyId, company.sourceSystemCd, createLoadModelDataResponder(onEmployeesLoaded));
		}
		
		private function onEmployeesLoaded(e:ResultEvent):void {
			employees = e.result as ArrayCollection;
		}
	
	}
}
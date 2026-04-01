package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
    import psp.sap.model.EmployeeInfo;

    public class EmployeeHistoryViewModel extends AbstractPartViewModel
	{

        [Bindable] [BackingProperty (context=true)] public var employee:EmployeeInfo;

        public static function createActivator(employee:EmployeeInfo):Object {
            return {"employee":employee};
        }
        
        [Bindable]
		[ArrayElementType("psp.sap.model.PropertyAudit")]
		public var propertyHistory:ArrayCollection = new ArrayCollection();

		override protected function loadModelData():void {
			SAP.instance.employeeService.getEmployeeHistory(
					company.companyId, company.sourceSystemCd, employee.employeeGseq, createLoadModelDataResponder(onHistoryLoaded));
		}
		
		private function onHistoryLoaded(e:ResultEvent):void {
			propertyHistory = e.result as ArrayCollection;			
		}

        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var employeeName:String = employee != null ? employee.fullName : "";
            return employeeName + " History";
        }
    }
}
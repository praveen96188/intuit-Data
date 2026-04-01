package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.model.FraudEvent;
	
	public class FraudSuspectPaycheckViewModel extends AbstractPartViewModel
	{
		private var mPaycheckSort:Sort;
		public function FraudSuspectPaycheckViewModel()
		{
			mPaycheckSort = new Sort();
			mPaycheckSort.fields = [new SortField("employeeName")]
		}
		
		public var payrollFraudEvent:FraudEvent;
		
		[Bindable]
		[ArrayElementType ("psp.sap.model.SuspectPaycheck")]
		public var suspectPaychecks:ArrayCollection;
		
		override protected function loadModelData():void {
			if(payrollFraudEvent != null){
				SAP.instance.payrollRunService.checkPayrollForSuspectPaychecks(company.sourceSystemCd, company.companyId, payrollFraudEvent.sourcePayRunId, createLoadModelDataResponder(onSuspectPaychecksLoaded));
			}
			else {
				modelDataLoaded();
			}
		}
		
		private function onSuspectPaychecksLoaded(e:ResultEvent):void {
			var temp:ArrayCollection = e.result as ArrayCollection;
			temp.sort = mPaycheckSort;
			temp.refresh();
			suspectPaychecks = temp;
		}

	}
}
package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.Paycheck;

    public class EmployeeLineItemsViewModel extends AbstractPartViewModel
	{
        [Bindable] [BackingProperty(context=true)] public var paycheck:Paycheck;

        [Bindable]
		[ArrayElementType("psp.sap.model.PaycheckLineItem")]
        public var paycheckLineItems:ArrayCollection;


        public function EmployeeLineItemsViewModel() {
            this.shallowCopyFields = ["paycheckGseq", "sourceEmployeeName", "paycheckDate"]
        }

        public static function createActivator(paycheck:Paycheck):Object {
            return {"paycheck":paycheck};
        }


		override protected function loadModelData():void {
			SAP.instance.payrollRunService.getLineItems(
					company.sourceSystemCd, company.companyId, paycheck.paycheckGseq, createLoadModelDataResponder(onLineItemsLoaded));
		}
		
		private function onLineItemsLoaded(e:ResultEvent):void {
			var lLineItems:ArrayCollection = e.result as ArrayCollection;
			if(lLineItems != null) {
				var sort:Sort = new Sort();
				var sortField:SortField = new SortField("payrollItemCategory");
				sort.fields = [sortField];
				lLineItems.sort = sort;
				lLineItems.refresh();
				paycheckLineItems = lLineItems;
			}
		}


        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            if (paycheck == null) {
                return "";
            } else {
                var employeeName:String = paycheck.sourceEmployeeName != null ? paycheck.sourceEmployeeName : "";
                var paycheckDate:String = paycheck.paycheckDate != null ? SAPDateFormatters.dateFormatMedium.format(paycheck.paycheckDate) : "";
                return employeeName + " Paycheck Line Items " + paycheckDate;
            }
        }
    }
}
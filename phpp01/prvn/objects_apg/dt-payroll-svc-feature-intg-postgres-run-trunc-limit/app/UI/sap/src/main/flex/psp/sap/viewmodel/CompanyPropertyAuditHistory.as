package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.formatters.SAPCurrencyFormatter;
	
	public class CompanyPropertyAuditHistory extends CompositePartViewModel
	{
		[ArrayElementType ("psp.sap.model.PropertyAudit")]
        [Bindable] public var propertyHistory:ArrayCollection = new ArrayCollection();
		private var mDateFormatter:DateFormatter = new DateFormatter();		
					
		public var currencyFormatter:SAPCurrencyFormatter = new SAPCurrencyFormatter();	
						
		public function CompanyPropertyAuditHistory()
		{
			mDateFormatter.formatString = SAP.instance.configuration.dateTimeFormatDateOverTime;		
		}

		public function get dateFormatter():DateFormatter {
			return mDateFormatter;					
		}												
				
		protected virtual function onDataLoadCompleted(e:ResultEvent):void {
			propertyHistory.removeAll();
			propertyHistory = e.result as ArrayCollection;
		}						
		
	}
}
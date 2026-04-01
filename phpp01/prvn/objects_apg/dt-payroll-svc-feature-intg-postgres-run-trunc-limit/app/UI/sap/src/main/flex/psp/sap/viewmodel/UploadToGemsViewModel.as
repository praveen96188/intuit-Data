package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.AccountingInspectorPageEnum;
	import psp.sap.application.enums.AdministrationInspectorPageEnum;
	import psp.sap.model.GemsMonthlyBalance;
	
	public class UploadToGemsViewModel extends AbstractPartViewModel
	{				
	
		[Bindable]
		[ArrayElementType("psp.sap.model.GemsMonthlyBalance")]
		public var monthlyBalances:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var noUploadReason:String = null;
	
		public function UploadToGemsViewModel()
		{
			this.label = AccountingInspectorPageEnum.UPLOAD_TO_GEMS;	
		}	
		
		override protected function executeSave():void {											
			//Call remote function
			SAP.instance.administrationService.postToGems(createSaveResponder());
		}					

		override public function get hasChanged():Boolean {
			return true;
		}
		
		override protected function get savingMessage():String {
			return "Posting to GEMS...";
		}				
		
		override protected function loadModelData():void {
			SAP.instance.administrationService.getGemsMonthlyBalances(createLoadModelDataResponder(onBalanceFileLoaded));
		}				
		
		private function onBalanceFileLoaded(e:ResultEvent):void {
			monthlyBalances = e.result as ArrayCollection;
		}
		
		override protected function initializeBackingProperties():void {
			if (monthlyBalances.length == 0) {
				noUploadReason = "There are currently no entries to post for " + reportingMonth;
			} else if ((monthlyBalances.getItemAt(0) as GemsMonthlyBalance).uploadStatus != "Finalized") {
				noUploadReason = "The file for these entries is in an upload status of " + (monthlyBalances.getItemAt(0) as GemsMonthlyBalance).uploadStatus + " and will not be uploaded";
			} else {
				noUploadReason = null;
			}
				
		}
		
		[Bindable("propertyChange")]
		public function get reportingMonth():String {
			var date:Date = SAP.instance.PSPDate;
       		
       		//Set previous month (set day = 1 cause if it's 31 and we switch months, it could be wacky)
       		date.setMonth((date.month > 0 ? date.month - 1 : 12), 1);	       		
       		var dateFormatter:DateFormatter = new DateFormatter();
       		dateFormatter.formatString = "MMMM";
       		
       		return dateFormatter.format(date);
		}
		
		
		
	}
}

package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.model.EmployeeTaxLedgerItem;
    import psp.sap.model.LedgerItemDetailsCriterion;

    public class TaxLedgerDetailViewModel extends AbstractPartViewModel
	{
		[Bindable]
        public var ledgerItemDetailsCriterion: LedgerItemDetailsCriterion;

        [Bindable]
        public var showTaxTips:Boolean = false;
		
		[Bindable]
		[ArrayElementType ("psp.sap.model.EmployeeTaxLedgerItem")]
		public var employeeLedgerItems:ArrayCollection; 
		
		public function TaxLedgerDetailViewModel()
		{
			super();
		}
		
		public function get totalWagesTotal():Number {
			var total:Number = 0;
			for each(var ledgerItem:EmployeeTaxLedgerItem in employeeLedgerItems){
				total += ledgerItem.totalWages;
			}
			
			return total;
		}
		
		public function get taxableWagesTotal():Number {
			var total:Number = 0;
			for each(var ledgerItem:EmployeeTaxLedgerItem in employeeLedgerItems){
				total += ledgerItem.taxableWages;
			}
			
			return total;
		}

		public function get taxTipsTotal():Number {
			var total:Number = 0;
			for each(var ledgerItem:EmployeeTaxLedgerItem in employeeLedgerItems){
				total += ledgerItem.taxTips;
			}

			return total;
		}

		public function get taxAmountTotal():Number {
			var total:Number = 0;
			for each(var ledgerItem:EmployeeTaxLedgerItem in employeeLedgerItems){
				total += ledgerItem.taxAmount;
			}
			
			return total;
		}
		
		override protected function loadModelData():void {			
			SAP.instance.taxService.findEmployeeLedgerItems(ledgerItemDetailsCriterion, createLoadModelDataResponder(onLedgerItemsLoaded));
		}						
		
		private function onLedgerItemsLoaded(e:ResultEvent):void {
			employeeLedgerItems = e.result as ArrayCollection;
            if (employeeLedgerItems.length > 0) {/*    Don't want a NPE */
                /*    All items have the property so reading off of any of them is fine   */
                showTaxTips = (employeeLedgerItems.getItemAt(0) as EmployeeTaxLedgerItem).showTaxTips;
            }
        }
		
	}
}

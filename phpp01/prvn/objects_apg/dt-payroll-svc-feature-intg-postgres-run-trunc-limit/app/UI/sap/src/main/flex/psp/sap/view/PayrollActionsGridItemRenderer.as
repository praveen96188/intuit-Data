package psp.sap.view
{
	import mx.containers.VBox;
	import mx.controls.LinkButton;

	public class PayrollActionsGridItemRenderer extends VBox
	{
		private var createNonACHRedebitTx:LinkButton = new LinkButton();
		private var createACHRedebitTx:LinkButton = new LinkButton();
		private var createFee:LinkButton = new LinkButton();
		private var viewTx:LinkButton = new LinkButton();
		private var viewLedger:LinkButton = new LinkButton();		
		
		public function PayrollActionsGridItemRenderer()
		{
			super();
			
			createNonACHRedebitTx.label = "Record a non-ACH redebit transaction";
			createACHRedebitTx.label = "Create a redebit transaction";
			createFee.label = "Create a fee";
			viewTx.label = "View transactions";
			viewLedger.label = "View ledger";
		}
		
		override protected function createChildren():void {
			super.createChildren();
			
			this.addChild(createNonACHRedebitTx);
			this.addChild(createACHRedebitTx);
			this.addChild(createFee);
			this.addChild(viewTx);
			this.addChild(viewLedger);
		}
		
		override public function set data(value:Object):void {
			super.data = value;
		}		
		
	}
}
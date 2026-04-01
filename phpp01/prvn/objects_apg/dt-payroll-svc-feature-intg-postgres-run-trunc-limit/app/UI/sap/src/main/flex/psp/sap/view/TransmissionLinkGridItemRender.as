package psp.sap.view
{
	import flash.display.DisplayObjectContainer;
	import flash.events.MouseEvent;
	
	import mx.controls.DataGrid;
	import mx.controls.LinkButton;
	import mx.events.DataGridEvent;
	import mx.formatters.DateFormatter;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	import psp.sap.model.Transmission;

	public class TransmissionLinkGridItemRender extends LinkButton
	{
		private var mDateFormatter:DateFormatter;
		private var mPermissionOperation:String = null;
		private var mPermitOperation:Boolean = true;
				
		public function TransmissionLinkGridItemRender()
		{
			super();
			this.useHandCursor = true;
			this.setStyle("textAlign", "left");
			this.setStyle("color", "blue");
			
			this.addEventListener(MouseEvent.CLICK, onTransmissionSelected, false, 0, true);
			this.addEventListener(MouseEvent.ROLL_OVER, onRollover, false, 0, true);
			this.addEventListener(MouseEvent.ROLL_OUT, onRollout, false, 0, true);
			mDateFormatter = new DateFormatter();
			mDateFormatter.formatString = SAP.instance.configuration.dateTimeFormatMedium;
			permissionOperation = OperationsEnum.VIEW_OFX;
		}
		
		[Bindable]
		public function get permissionOperation():String {
			return mPermissionOperation;
		}
		
		public function set permissionOperation(value:String):void {
			mPermissionOperation = value;
			updatePermitOperation();
		}
		
		[Bindable]
		public function get permitOperation():Boolean {
			return mPermitOperation;
		}
		
		protected function set permitOperation(value:Boolean):void {
			mPermitOperation = value;
		}	
		
		protected function updatePermitOperation():void {
			permitOperation = SAP.canPerformOperation(permissionOperation);
		}
		
		override public function set owner(value:DisplayObjectContainer):void {
			super.owner = value;
			var dg:DataGrid = DataGrid(this.owner);
			var backColor:Object = dg.getStyle("backgroundColor");
			this.setStyle("selectionColor", backColor);
			this.setStyle("rollOverColor", backColor);
		}
		
		override protected function rollOverHandler(e:MouseEvent):void {
			// disable the roll over painting
		}
		
		private function onTransmissionSelected(e:MouseEvent):void {
			if(permitOperation) {
				var dge:DataGridEvent = new DataGridEvent("transmissionClick");
				dge.itemRenderer = this;
				this.owner.dispatchEvent(dge);
			}
		}
		
		private function onRollover(e:MouseEvent):void {
			if(permitOperation) {
				this.setStyle("textDecoration", "underline");
			}
		}
		
		private function onRollout(e:MouseEvent):void {
			this.setStyle("textDecoration", "none");
		}
		
		override public function set data(value:Object):void {
			super.data = value;			
			this.enabled = permitOperation;			
			this.label = mDateFormatter.format(Transmission(value).initializeDateTime);
		}
		
	}
}
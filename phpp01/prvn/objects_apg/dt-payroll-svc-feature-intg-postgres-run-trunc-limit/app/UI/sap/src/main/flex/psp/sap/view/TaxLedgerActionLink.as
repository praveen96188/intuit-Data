package psp.sap.view
{
	import flash.events.MouseEvent;
	
	public class TaxLedgerActionLink extends ActionLink
	{
		public function TaxLedgerActionLink()
		{
			super();
			this.setStyle("color", "black");
			this.clearStyle("verticalAlign");
			this.setStyle("paddingTop", 0);
			this.setStyle("paddingBottom", 0);
		}
		
		override protected function onRollover(e:MouseEvent):void {
			if(enabled){
				this.setStyle("color", "blue");
                this.setStyle("textDecoration", "underline");
			}
		}
		
		override protected function onRollout(e:MouseEvent):void {
			if(enabled){
				this.setStyle("color", "black");
                this.setStyle("textDecoration", "none");
			}
		}
		
	}
}
package psp.sap.view
{
	import mx.controls.AdvancedDataGrid;
	import mx.core.ScrollPolicy;

	public class CompanyEventResultsADG extends AdvancedDataGrid
	{
		public function CompanyEventResultsADG()
		{
			super();
		}
		
		override public function set dataProvider(value:Object):void {
			super.dataProvider = value;
			fixWidths();
		}

		//call this whenever the columns or anything is changing
		//ADG thinks it is sooooooooooooooooooooooooooooooooooo smart
		//and if the scroll policy isn't on, it just ignores the widths you set
		public function fixWidths():void {			
			this.horizontalScrollPolicy = ScrollPolicy.ON
			this.validateNow();
			this.horizontalScrollPolicy = ScrollPolicy.OFF;						
		}
		
	}
}
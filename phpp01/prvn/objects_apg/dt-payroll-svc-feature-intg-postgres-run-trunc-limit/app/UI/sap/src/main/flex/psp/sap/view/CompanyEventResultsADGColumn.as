package psp.sap.view
{
	import mx.controls.advancedDataGridClasses.AdvancedDataGridColumn;
	import mx.core.mx_internal;
	
	use namespace mx_internal;

	public class CompanyEventResultsADGColumn extends AdvancedDataGridColumn
	{
		public function CompanyEventResultsADGColumn(columnName:String=null)
		{
			super(columnName);
		}
		
		
		
		override public function set visible(value:Boolean):void {
			super.visible = value;
			if (owner) {
				(owner as CompanyEventResultsADG).fixWidths();
			}
		}
		
	}
}
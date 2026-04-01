package psp.sap.view.controls
{
	import mx.controls.dataGridClasses.DataGridColumn;
	import mx.core.ClassFactory;
	
	import psp.sap.view.CompanyLinkGridItemRenderer;
	
	

	[Event(name="companyClick", type="mx.events.DataGridEvent")]
	public class CompanyDataGridColumn extends DataGridColumn
	{
		public function CompanyDataGridColumn(columnName:String=null)
		{
			super(columnName);
			this.itemRenderer = new ClassFactory(CompanyLinkGridItemRenderer);
		}
		
	}
}
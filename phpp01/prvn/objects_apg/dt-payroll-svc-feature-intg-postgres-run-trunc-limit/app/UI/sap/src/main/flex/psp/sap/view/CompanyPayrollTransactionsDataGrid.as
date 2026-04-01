package psp.sap.view
{
	import mx.controls.DataGrid;
	import mx.core.UIComponent;
	
	public class CompanyPayrollTransactionsDataGrid extends DataGrid
	{
				
		public function CompanyPayrollTransactionsDataGrid()
		{
			super();
		}
		
		override protected function drawHeaderBackground(header:UIComponent):void
        {
            header.styleName = "directDepositHeader";
            super.drawHeaderBackground(header);
        }
		
	}
}
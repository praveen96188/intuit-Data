package psp.sap.view
{
	import flash.display.DisplayObject;
	
	import mx.controls.AdvancedDataGrid;
	import mx.core.EdgeMetrics;
	import mx.core.IUIComponent;
	import mx.styles.ISimpleStyleClient;

	public class ChaseReportDataGrid extends AdvancedDataGrid
	{
			public function ChaseReportDataGrid()
			{
				super();
			}

			override public function  get borderMetrics():EdgeMetrics
			{
				return (border as ChaseReportDataGridFooterBorder).borderMetrics;
			}

			override protected function createBorder():void
			{
		        if (!border)
		        {
		            var borderClass:Class = ChaseReportDataGridFooterBorder;
		
		            border = new borderClass();
		
		            if (border is IUIComponent)
		                IUIComponent(border).enabled = enabled;
		            if (border is ISimpleStyleClient)
		                ISimpleStyleClient(border).styleName = this;
		
		            // Add the border behind all the children.
		            addChildAt(DisplayObject(border), 0);
		
		            invalidateDisplayList();
		        }
			}
		
	}
}
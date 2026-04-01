package psp.sap.view
{
	import flash.display.DisplayObject;
	import flash.display.Shape;
	
	import mx.controls.AdvancedDataGrid;
	import mx.controls.advancedDataGridClasses.AdvancedDataGridColumn;
	import mx.controls.advancedDataGridClasses.AdvancedDataGridListData;
	import mx.controls.listClasses.IDropInListItemRenderer;
	import mx.controls.listClasses.IListItemRenderer;
	import mx.core.EdgeMetrics;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.core.mx_internal;
	import mx.formatters.CurrencyFormatter;
	import mx.skins.Border;
	use namespace mx_internal;
	
	public class ChaseReportDataGridFooterBorder extends UIComponent
	{
	
		public function ChaseReportDataGridFooterBorder()
		{
			super();
			currencyFormatter = new CurrencyFormatter();
			currencyFormatter.alignSymbol = "left";
			currencyFormatter.currencySymbol = "$";
			currencyFormatter.precision = 2;
			currencyFormatter.decimalSeparatorFrom = ".";
			currencyFormatter.decimalSeparatorTo = ".";
		}
	
		protected var border:IFlexDisplayObject;
	
		protected var overlay:Shape;
	
		protected var dataGrid:AdvancedDataGrid;
		
		protected var currencyFormatter:CurrencyFormatter;
	
		/**
		 *  create the actual border here
		 */
		override protected function createChildren():void
		{
			var borderClass:Class = (parent as AdvancedDataGrid).getStyle("borderSkin");
			border = new borderClass() as IFlexDisplayObject;
			(border as Border).styleName = styleName;	// delegate styles
			addChild(DisplayObject(border));
	
			dataGrid = parent as AdvancedDataGrid;
	
			overlay = new Shape();
			addChild(overlay);
		}
	
		/**
		 *	lay it out
		 */
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			overlay.graphics.clear();
	
			border.setActualSize(w, h);
			var bm:EdgeMetrics = (border as Border).borderMetrics;
	
			// destroy the old children
			while (numChildren > 2)
				removeChildAt(2);
	
			// make new ones
			var cols:Array = dataGrid.columns;
			var firstCol:int = dataGrid.horizontalScrollPosition;
	
			var colIndex:int = 0;
			var n:int = cols.length;
			var i:int = 0;
			while (colIndex < firstCol)
			{
				// find first visible column;
				if (cols[i++].visible)
					colIndex ++;
			}
			
			var vm:EdgeMetrics = dataGrid.viewMetrics;
	        var lineCol:uint = dataGrid.getStyle("verticalGridLineColor");
	        var vlines:Boolean = dataGrid.getStyle("verticalGridLines");
			overlay.graphics.lineStyle(1, lineCol);
	
			var xx:Number = vm.left;
			var yy:Number = h - bm.bottom - dataGrid.rowHeight;
			while (xx < w - vm.right && i < cols.length)
			{
				var col:AdvancedDataGridColumn = cols[i++];
	
				if (col is AdvancedDataGridColumn)
				{
					var fdgc:AdvancedDataGridColumn = col as AdvancedDataGridColumn;
					var renderer:IListItemRenderer = dataGrid.itemRenderer.newInstance();
					
					if (renderer is IDropInListItemRenderer)
					{
						if(col.headerText == "Release\nDate"){
							IDropInListItemRenderer(renderer).listData = new AdvancedDataGridListData("Total", 
																fdgc.dataField, i - 1, null, 
																dataGrid, -1);							
						}
						else if(col.headerText == "Debit"){
							var debitAmount:Number = new Number();						
							if(dataGrid.parent is ChaseReportDataView){
								debitAmount = (dataGrid.parent as ChaseReportDataView).report.debitTotal;
							}
							/*else if(dataGrid.parent is ChaseReportPrintDataView){
								debitAmount = (dataGrid.parent as ChaseReportPrintDataView).report.debitTotal;
							}*/
							IDropInListItemRenderer(renderer).listData = new AdvancedDataGridListData(currencyFormatter.format(debitAmount), 
																fdgc.dataField, i - 1, null, 
																dataGrid, -1);
						}
						else if(col.headerText == "Credit"){
							var creditAmount:Number = new Number();						
							if(dataGrid.parent is ChaseReportDataView){
								creditAmount = (dataGrid.parent as ChaseReportDataView).report.creditTotal;
							}
							/*else if(dataGrid.parent is ChaseReportPrintDataView){
								creditAmount = (dataGrid.parent as ChaseReportPrintDataView).report.creditTotal;
							}*/							
							IDropInListItemRenderer(renderer).listData = new AdvancedDataGridListData(currencyFormatter.format(creditAmount), 
																fdgc.dataField, i - 1, null, 
																dataGrid, -1);														
						}
						else{
							IDropInListItemRenderer(renderer).listData = new AdvancedDataGridListData("", 
																fdgc.dataField, i - 1, null, 
																dataGrid, -1);
						}
						
					}
					renderer.data = fdgc;
					addChild(DisplayObject(renderer));					
					setStyle("textAlign", "right");
					renderer.x = xx;
					renderer.y = yy;
					renderer.setActualSize(col.width - 1, dataGrid.rowHeight);
					if (vlines)
					{
						overlay.graphics.moveTo(xx + col.width, yy);
						overlay.graphics.lineTo(xx + col.width, h - bm.bottom);
					}
				}								
				xx += col.width;
			}
	        lineCol = dataGrid.getStyle("horizontalGridLineColor");
	        if (dataGrid.getStyle("horizontalGridLines"))
			{
				overlay.graphics.lineStyle(1, lineCol);
				overlay.graphics.moveTo(vm.left, yy);
				overlay.graphics.lineTo(w - vm.right, yy);
			}
		}
	
		/**
		 *  factor in the footer
		 */
		public function get borderMetrics():EdgeMetrics
		{
			var em:EdgeMetrics = (border as Border).borderMetrics.clone();
			em.bottom += dataGrid.rowHeight;
			return em;
		}
	
}

}
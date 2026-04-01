package psp.sap.view.controls
{
	import flash.events.Event;
	
	import mx.controls.AdvancedDataGrid;
	import mx.controls.scrollClasses.ScrollBar;
	import mx.events.PropertyChangeEvent;
	
	[Event(name="verticalScrollBarChanged", type="flash.events.Event")]

	public class SAPAdvancedDataGrid extends AdvancedDataGrid
	{
		public function SAPAdvancedDataGrid()
		{
			super();						
		}
				
		public var isVerticalScrollBarShowing:Boolean = false;
		
		override protected function setScrollBarProperties(totalColumns:int, visibleColumns:int,
                                        totalRows:int, visibleRows:int):void {
			super.setScrollBarProperties(totalColumns, visibleColumns, totalRows, visibleRows);                                        	
			
			isVerticalScrollBarShowing = verticalScrollBar != null && verticalScrollBar.visible;
			
			dispatchEvent(new Event("verticalScrollBarChanged"));
		}
				
		
	}
}
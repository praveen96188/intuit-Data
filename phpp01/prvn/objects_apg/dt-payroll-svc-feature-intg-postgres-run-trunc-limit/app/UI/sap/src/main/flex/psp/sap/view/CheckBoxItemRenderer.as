package psp.sap.view
{
	/* Based on code from http://www.adobe.com/cfusion/communityengine/index.cfm?event=showdetails&productId=2&postId=7262 */
		
	import flash.events.MouseEvent;
	
	import mx.controls.Alert;
	import mx.controls.CheckBox;
	import mx.controls.DataGrid;
	import flash.display.DisplayObjectContainer;
	
	public class CheckBoxItemRenderer extends CheckBox
	{
		public function CheckBoxItemRenderer()
		{
			super();
			
		}
		
		override public function validateProperties():void
		{
			super.validateProperties();
			if (listData)
			{
				
				var dg:DataGrid = DataGrid(listData.owner);
	
				var column:CheckBoxHeaderColumn =	dg.columns[listData.columnIndex];
				column.addEventListener("click",columnHeaderClickHandler, false, 0, true);
				selected = data[column.dataField];
			}
		}
		public function columnHeaderClickHandler(event:MouseEvent):void
		{
			selected = event.target.selected;
		}
		
	}
}
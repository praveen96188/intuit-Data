package psp.sap.view
{

	/* Based on code from http://www.adobe.com/cfusion/communityengine/index.cfm?event=showdetails&productId=2&postId=7262 */
	
	
	import flash.events.MouseEvent;
	
	import mx.controls.CheckBox;
	import mx.controls.DataGrid;
	import mx.events.DataGridEvent;
	
	public class CheckBoxHeaderRenderer extends CheckBox
	{			 		
		public function CheckBoxHeaderRenderer()
			{
				super();
			}
		private var _data:CheckBoxHeaderColumn;
		
		override public function get data():Object
		{
			return _data;
		}
		
		override public function set data(value:Object):void
		{
			_data = value as CheckBoxHeaderColumn;
			DataGrid(listData.owner).addEventListener(DataGridEvent.HEADER_RELEASE, sortEventHandler, false, 0, true);
			selected = _data.selected;
			enabled = _data.enabled;  
		}
	
		private function sortEventHandler(event:DataGridEvent):void
		{
			if (event.itemRenderer == this)
			{
				event.preventDefault();
			}
		}
		
		override protected function clickHandler(event:MouseEvent):void
		{
			super.clickHandler(event);
			data.enabled = enabled;  
			data.selected = selected;
			data.viewModel.selectAll = selected;
			data.dispatchEvent(event);
		} 
		
		public function onClick(event:MouseEvent):void {
			super.clickHandler(event);
			data.selected = selected;									
			data.dispatchEvent(event);
		}									
	}  
}
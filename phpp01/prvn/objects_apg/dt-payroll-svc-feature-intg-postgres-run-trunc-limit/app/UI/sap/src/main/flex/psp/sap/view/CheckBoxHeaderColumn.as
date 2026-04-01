package psp.sap.view
{
	import mx.controls.dataGridClasses.DataGridColumn;
	
	import psp.sap.viewmodel.AbstractPartViewModel;
	
	[Event(name="click", type="flash.events.MouseEvent")]

	public class CheckBoxHeaderColumn extends DataGridColumn
	{
		public function CheckBoxHeaderColumn(columnName:String=null)
		{
			super(columnName);
			this.draggable = false;
			this.sortable = false;			
		}
		
		[Bindable]
		public var viewModel: AbstractPartViewModel;
	
		/**is the checkbox selected**/
		[Bindable]
		public var selected:Boolean;				
		
		[Bindable]
		public var enabled:Boolean;
	}
}
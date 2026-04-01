package psp.sap.view.controls
{
	import mx.controls.dataGridClasses.DataGridColumn;
    import mx.events.FlexEvent;

    public class SAPDataGridColumn extends DataGridColumn
	{
		//Use this for custom ordering data when exporting
		[Bindable]
		public var exportOrder:int = -1;
		
		//Use this for a custom title during export
		[Bindable]
		public var exportTitle:String = null;
		
		//Whether we want to use this field when we export (by default, yes)
		[Bindable]
		public var useInExport:Boolean = true;

        [Bindable]
        public var fixWidth:Boolean = true; //fix the width of this column (this only works if the SAPDataGrid has fixedWidths on


        [Inspectable(category="General",defaultValue="true")]
        override public function get visible():Boolean {
            return super.visible;
        }

        override public function set visible(value:Boolean):void {
            super.visible = value;
            if (value) {
                dispatchEvent(new FlexEvent(FlexEvent.SHOW));
            } else {
                dispatchEvent(new FlexEvent(FlexEvent.HIDE));
            }
        }

        public function SAPDataGridColumn(columnName:String=null)
		{
			super(columnName);
		}
		
	}
}
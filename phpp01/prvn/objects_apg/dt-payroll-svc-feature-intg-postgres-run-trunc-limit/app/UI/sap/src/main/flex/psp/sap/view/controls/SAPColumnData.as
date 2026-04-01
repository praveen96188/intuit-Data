package psp.sap.view.controls
{
	import mx.containers.VBox;

	public class SAPColumnData extends VBox
	{	
		[Bindable]
		public var displayData:String;
		
		[Bindable]
		public var title:String = null;
		
		[Bindable]
		public var useData:Boolean = true;
		
		public function SAPColumnData()
		{
			super();
			this.visible = false;
			this.includeInLayout = false;
		}
		
	}
}
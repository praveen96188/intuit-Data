package psp.sap.view
{
	import mx.containers.Canvas;
	import mx.controls.scrollClasses.ScrollBar;

	public class BindableCanvas extends Canvas
	{
		public function BindableCanvas()
		{
			super();
		}
		
		[Bindable]
		override public function get verticalScrollBar():ScrollBar
	    {
	        return super.verticalScrollBar;
	    }
	    
	    override public function set verticalScrollBar(value:ScrollBar):void
	    {
	        super.verticalScrollBar = value;
	    } 
		
	}
}
package psp.sap.view
{
	import flash.filters.DropShadowFilter;
	
	import mx.containers.VBox;

	public class PageVBox extends VBox
	{
		public function PageVBox()
		{
			super();
			var shadow:DropShadowFilter = new DropShadowFilter();
			shadow.alpha = 0.4;
			shadow.angle = 45;
			shadow.blurX = 4;
			shadow.blurY = 4; 
			shadow.distance = 5;
			shadow.strength = 1;
			shadow.quality = 1;
			filters = [shadow];
			
			styleName = "pageBody"; 
			
			percentHeight = 100;
			percentWidth = 97;
		}
		
	}
}
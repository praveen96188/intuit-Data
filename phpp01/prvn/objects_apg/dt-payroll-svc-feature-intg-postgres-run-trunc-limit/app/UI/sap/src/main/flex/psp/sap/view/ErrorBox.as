package psp.sap.view
{
	import flash.text.TextField;
	
	import mx.containers.Box;
	import mx.core.Application;
	import mx.core.UIComponent;

	public class ErrorBox extends Box
	{
		
		
		public var changeThemeColor:Boolean = true;
	
		
		public function ErrorBox()
		{
			super();
			this.percentHeight = 100;
			this.percentWidth = 100;
			this.styleName = "errorBox";	
		}
		
		override public function set errorString(value:String):void {			
			if (value == "") {
				this.setStyle("borderStyle","none");
				this.setStyle("borderThickness","0");
				if (changeThemeColor) {
					(this.getChildAt(0) as UIComponent).setStyle("themeColor",Application.application.getStyle("themeColor"));
				}				
				invalidateDisplayList();
			} else {
				this.setStyle("borderStyle","solid");
				this.setStyle("borderThickness","1");
				if (changeThemeColor) {
					(this.getChildAt(0) as UIComponent).setStyle("themeColor","red");
				}				
				invalidateDisplayList();
			}
			super.errorString = value;
		}
		
	}
}
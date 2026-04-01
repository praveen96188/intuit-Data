package psp.sap.view.controls
{
	import flash.events.Event;
	import flash.text.StyleSheet;
	import flash.text.TextFormat;
	
	import mx.controls.Text;
	import mx.core.IUITextField;
	import mx.core.mx_internal;
	import mx.events.FlexEvent;
	
	use namespace mx_internal; 
	
	public class LinkableText extends Text
	{
		
		private var format:TextFormat;
		
		public function LinkableText()
		{
			super();
			this.addEventListener(FlexEvent.CREATION_COMPLETE, function(e:Event):void { setStyleSheet(); });			
		}
									    
	    override public function set htmlText(value:String):void {
	    	if (format != null) {		    	
	    		//glorious hack for style problem
		    	textField.styleSheet = null;
		    	textField.defaultTextFormat = format;
		    	setStyleSheet();
		    }
	    	
	    	super.htmlText = value;
	    	
	    	if (textField.defaultTextFormat.font != "Times New Roman") {
	    		format = textField.defaultTextFormat;
	    	}
	    }
	   	    
	    public function setStyleSheet():void {
			var ss:StyleSheet = textField.styleSheet;
	        
	        if(textField.styleSheet == null){	        	
	        	textField.styleSheet = new StyleSheet();
	        } 
						
			textField.styleSheet.setStyle("a:link", { textDecoration: "underline", color: "#0000FF" });
		    textField.styleSheet.setStyle("a:hover", { textDecoration: "underline", color: "#111111"});
    		textField.styleSheet.setStyle("a:active", { textDecoration: "underline", color: "#111111"});    		
	    }
	    
	    
	}
}
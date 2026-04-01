package psp.sap.view
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.TextEvent;
	import flash.geom.*;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.controls.DateField;
	import mx.core.IToolTip;
	import mx.events.DropdownEvent;
	import mx.events.FlexEvent;
	import mx.events.ValidationResultEvent;
	import mx.formatters.DateFormatter;
	import mx.managers.ToolTipManager;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	
	import psp.sap.application.SAP;
			

	/**
 	 * Adds a tooltip showing the day of the week and during input, whether or not the current text will be parsed
 	 * Adds the following keyboard shortcuts:
     * T			Today
   	 * Up/Down		Next/Previous Day	 
 	 * W/K			First/Last Day of Week
 	 * M/H			First/Last Day of Month
 	 * Y/R			First/Last day of Year
 	 * Ctrl-Down	Open the date chooser			 
 	*/ 
	public class SAPDateField extends DateField
	{
		private static var textIgnore:ArrayCollection = new ArrayCollection();		
		textIgnore.addItem("t");
		textIgnore.addItem("T");
		textIgnore.addItem("w");
		textIgnore.addItem("W");
		textIgnore.addItem("k");
		textIgnore.addItem("K");
		textIgnore.addItem("m");
		textIgnore.addItem("M");
		textIgnore.addItem("h");
		textIgnore.addItem("H");
		textIgnore.addItem("y");
		textIgnore.addItem("Y");
		textIgnore.addItem("r");
		textIgnore.addItem("R");
		
		private static var dateFormatter:DateFormatter = new DateFormatter();
		private static var longDateFormatter:DateFormatter = new DateFormatter();
		longDateFormatter.formatString = "EEE, MMM D, YYYY";
				
		private var mIsUpdating:Boolean = false;
		
		private var tt:IToolTip;
		private var validationMap:Dictionary = new Dictionary(true);	
		
		public function SAPDateField()
		{
			super();
			
			this.parseFunction = SAPDateField.stringToDate;
						
			addEventListener(KeyboardEvent.KEY_DOWN, textKeyDownHandler, false, 10, true);			
			addEventListener(FocusEvent.FOCUS_OUT, tooltipClose, false, 0, true);									
			addEventListener(DropdownEvent.OPEN, tooltipClose, false, 0, true);
					
		}
		
		
		public static function stringToDate(valueString:String, inputFormat:String):Date {
			valueString = reformatDateString(valueString);
			
			return DateField.stringToDate(valueString,inputFormat);
		}
		
		protected static function reformatDateString(date:String):String {
					
			// strip special characters
			var strippedText:String = date;
			strippedText = strippedText.replace(new RegExp("-", "g"), "");
			strippedText = strippedText.replace(new RegExp("\\.", "g"), "");
			strippedText = strippedText.replace(new RegExp("/", "g"), "");
			
	    	var newString:String = date;
	    	
	    	if(strippedText.length == 6 || strippedText.length == 8){
	    		
	    		newString = "";
	    		
	        	newString += strippedText.substring(0, 2) + "/";
	        	newString += strippedText.substring(2, 4) + "/";
	
	    		var year:String = strippedText.substring(4, strippedText.length);
			 
	    		if(year.length > 2){	            		
	    			newString += year;
	    		}
	    		else{
	    			if(parseInt(year) >= 70){
	    				newString += "19" + year;
	    			}
	    			else{
	    				newString += "20" + year;
	    			}
	    		}	            		          		            
	    	}
	    	
	    	return newString;	
		}
		
        
        
        
        //this function handles the keys...
	    protected function textKeyDownHandler(event:KeyboardEvent):void
	    {	    		    
	    	tooltipOpen();
	    	//Note: add to textIgnore to prevent the field from being updated
	    	    	
	    	var newText:String="NaN";
	    	
	    	//parent will stop propagating if it handles, so this should
	    	//only be reached if it isn't (generally if the chooser is NOT showing)	    	
	    	if (event.keyCode == 84 ) { //t
	    		newText = dateFormatter.format(new Date(SAP.instance.PSPDate));				
	    	} else if (event.keyCode == 77) { // m
	    		newText = dateFormatter.format(new Date(new Date(Date.parse(text)).setDate(1))); 
	    	} else if (event.keyCode == 72) { //h
	    		var lastDayInMonth:Date = new Date(Date.parse(text));
	    		lastDayInMonth.setMonth(lastDayInMonth.month+1,1);
	    		lastDayInMonth = subDays(lastDayInMonth,1);
	    		newText = dateFormatter.format(lastDayInMonth);
	    	} else if (event.keyCode == 89) { //y
	    		newText = dateFormatter.format(new Date(new Date(Date.parse(text)).setMonth(0,1)));
	    	} else if (event.keyCode == 82) { //r
	    		newText = dateFormatter.format(new Date(new Date(Date.parse(text)).setMonth(11,31)));	    		
	    	} else {
		    	var offset:int;
		    	if (event.keyCode == 38) { //up 										
					offset=1;				
		    	} else if (event.keyCode == 40) { // down
		    		offset=-1;		    		
		    	} else if (event.keyCode == 87) { // w 		
		    		offset = 1 - (new Date(Date.parse(text)).day);
		    	} else if (event.keyCode == 75) { //k
		    		offset = 5 - (new Date(Date.parse(text)).day);
		    	}
		    	if (offset != 0) {
		    		newText = dateFormatter.format(addDays(new Date(Date.parse(text)),offset));
		    	}	    		
	    	}

	    	if (newText.indexOf("NaN") < 0 && newText != text){
	    		text = newText;
	    		tooltipChange(null);
	    		textInput.selectionBeginIndex=0;
	    		textInput.selectionEndIndex=0;
	    	}
	    	
	    }
	    
	    private function addDays(date:Date, days:int):Date {
	    	if (days < 0) {
	    		return subDays(date,-days);
	    	}
	    	return new Date(date.time + 1000*60*60*24 * days + 3600*1000); //extra hour needed for some reason 
	    }

	    private function subDays(date:Date, days:int):Date {
	    	return new Date(date.time - 1000*60*60*24 * days);  
	    }
	    
	    	    
		private function textStopper(event:TextEvent):void
	    {
			if (textIgnore.contains(event.text)){
				event.preventDefault();
			}    
		}
		
		override protected function createChildren():void {
			super.createChildren();
			//need this to know when the textInput field has been initialized
			textInput.addEventListener(TextEvent.TEXT_INPUT, textStopper, false, 0, true);
			//this one is for user input only, but it fires AFTER it changes
			textInput.addEventListener(Event.CHANGE, tooltipChange, false, 0, true);			
		}
		
		protected function tooltipOpen():void {			
			if (! tt) {								
				tt = ToolTipManager.createToolTip(textInput.text, 0,0);
				tt.alpha = 1;				
			} 
			var p:Point = this.localToGlobal(new Point(this.x,this.y));
			tt.move(p.x-50,p.y+25);
			tooltipChange(null);														
		}
		
		protected function tooltipClose(event:Event):void {
			if (tt) {
				ToolTipManager.destroyToolTip(tt);
				tt = null;
			}
		}
		
		protected function tooltipChange(event:Event):void {
			if (tt) {										
				tt.text = longDateFormatter.format(this.parseFunction(text,formatString));				
			 	if (tt.text==""){
	    			tt.text = "??";
	    		}
			}
		}
		
		override public function validationResultHandler(e:ValidationResultEvent):void
    	{
    		validationMap[e.currentTarget] = e;
    		
    		var message:String = "";
    		for each(var event:ValidationResultEvent in validationMap){
    			if(event.currentTarget.hasOwnProperty("enabled") && event.currentTarget.enabled && event.type != ValidationResultEvent.VALID){
    				if (validationSubField != null && validationSubField != "" && event.results)
		            {
		            	var result:ValidationResult;
		            	var subFieldFound:Boolean = false
		                for (var i:int = 0; i < event.results.length; i++)
		                {
		                    result = event.results[i];
		                    // Find the result that is meant for us
		                    if (result.subField == validationSubField)
		                    {
		                    	subFieldFound = true;
		                        if (result.isError)
		                        {
		                            message += result.errorMessage;
		                        }		                        
		                        break;
		                    }
		                }
		                if(!subFieldFound){
		                	message += event.results[0].errorMessage;
		                }
		            }
		            else if (event.results && event.results.length > 0)
		            {
		                message += event.results[0].errorMessage;
		            } 
    			} 
    		}
    		// If we are valid, then clear the error string
	        if (message == "")
	        {	            
	            errorString = message;
	            dispatchEvent(new FlexEvent(FlexEvent.VALID));
	        }
	        else // If we get an invalid event
	        {	            	           	
	            if (errorString != message)
	            {
	                errorString = message;
	                dispatchEvent(new FlexEvent(FlexEvent.INVALID));
	            }
	        }
    	}

	
	}
	
	
	
}
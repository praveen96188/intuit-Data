package psp.sap.application
{
	import flash.utils.getQualifiedClassName;
	
	import mx.core.mx_internal;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.logging.LogEvent;
	import mx.logging.LogEventLevel;
	import mx.logging.targets.LineFormattedTarget;
	
	use namespace mx_internal;
	
	public class ClientLoggingTarget extends LineFormattedTarget {
		
		private static var mInstance:ClientLoggingTarget = new ClientLoggingTarget();
		
		private var mInitilized:Boolean = false;
		private var mEventCollection:Array = new Array();   
		  
		public function ClientLoggingTarget() {    		
			if(!mInitilized){
				super();    // change the defaults for these    
				super.fieldSeparator = "##";    
				super.includeTime = true;    
				super.includeDate = true;    
				super.includeCategory = true;   
				super.includeLevel = true;    
				super.level = LogEventLevel.ALL;
				super.filters = ["psp.*"]; 							
				Log.addTarget(this);				  
			}  
		}
		
		public override function logEvent(event : LogEvent) : void { 
			var category : String = ILogger(event.target).category;
			if (category.indexOf("mx.") != 0) {   
				super.logEvent(event);    
			}
		}  				
		
		/**   
		 * Super class calls this method when it has formatted the message   
		 * and wants to send it somewhere.   
		 * The default implementation does nothing.   
		 */  
		 mx_internal override function internalLog(message:String) :void {    
			 if(mEventCollection.length == 500){
			 	// throw away the first element
			 	mEventCollection.shift();
			 }
			 // add the new message to the end of the array
			 mEventCollection.push(message);  
		 }
		 
		 public static function get instance():ClientLoggingTarget {
		 	return mInstance;
		 }
		 
		 public function toString():String {
		 	var text:String = "";
		 	for each(var message:String in mEventCollection){
		 		text += message + String('\n');
		 	}
		 	return text;
		 }
		 
		 public static function getLogger(classObject:*):ILogger {
		 	return Log.getLogger(getQualifiedClassName(classObject).replace("::", "."));
		 }
	}
}
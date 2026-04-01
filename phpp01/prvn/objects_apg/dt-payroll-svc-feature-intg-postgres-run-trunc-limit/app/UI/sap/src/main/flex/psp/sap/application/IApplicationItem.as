package psp.sap.application
{
	import flash.events.IEventDispatcher;
	
	import mx.rpc.IResponder;
	
	public interface IApplicationItem extends IEventDispatcher
	{	
		/**
		 * Return a unique key for this object
		 */
		function get key():String;
			
		/**
		 * save to persistent storage any changes made to 
		 * the application item
		 */
		function save(responder:IResponder = null, saveMethod:String = null):void;
				
		/**
		 * show the application item in an inspector
		 */
		function display():void;
		
		/**
		 * close the inspector showing this application item
		 */
		function close():void;
	}
}

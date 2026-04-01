package intuit.sbd.flex.framework.model
{
	import intuit.sbd.flex.framework.model.events.ModelEvent;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	
	import psp.sap.application.IApplicationItem;
	
	/**
	 * An Entity in the domain.
	 * 
	 * As described by Eric Evans in Domain Driven Design:
	 * "An object defined primarily by it sentity" (as
	 * opposed to primarily by its attribute values.)
	 * 
	 * An entity is anything that has continuity through
	 * a life cycle and distinctions independed of
	 * attributes that are important to the application's
	 * user.
	 * 
	 * One more time:
	 * "When an object is distinguished by its identity,
	 * rather than its attributes, make this primary to 
	 * its definition.."
	 * 
	 */
	public class EntityObject 
		extends ApplicationObject
		implements IApplicationItem
	{
		
		public function EntityObject()
		{
			super();
		}
		
		public virtual function get key():String {
			throw new Error("get key() called and no overridden implementation provided");
		}
		
		/**
		 * Displays the item in its configured Inspector.
		 */
		public virtual function display():void {
			
		}
		
		public virtual function save(responder:IResponder = null, saveMethod:String = null):void {
			var delegatingResponder:AsyncResponder = new AsyncResponder(onSaveSucceeded, onSaveFaulted, responder);
			doSave(delegatingResponder, saveMethod);
		}
		
		protected virtual function doSave(responder:IResponder = null, saveMethod:String = null):void {
			// expectation is that sub-classes override and
			// call a service method that accepts the 
			// onSaveSucceed and onSaveFaulted handlers 
		}
		
		protected virtual function onSaveSucceeded(e: Object, token: Object):void {
			if (token != null && token is IResponder) {
				IResponder(token).result(e);
			}
			
			this.dispatchEvent( ModelEvent.createSaveSucceededEvent() );
		}
		
		protected virtual function onSaveFaulted(error: Object, token: Object):void {
			if (token != null && token is IResponder) {
				IResponder(token).fault(error);
			}

			this.dispatchEvent( ModelEvent.createSaveFaultedEvent(error) );
		}	
		
		// since delete is a keyword, it is difficult to have a good match for save()
		// other options include save/discard, keep/discard, save/throwAway, save/destroy, save/remove
		public virtual function discard():void {

		}	
		
		public virtual function close():void {
			
		}
		
		public virtual function equals(item:IApplicationItem):Boolean {
			return this==item;	
		}
		
	}
}
package psp.sap.view
{
	import mx.containers.ViewStack;
	import mx.core.Container;
	import mx.events.PropertyChangeEvent;
	import mx.logging.ILogger;
	
	import psp.sap.application.ClientLoggingTarget;

	public class ViewStackControlEx extends ViewStack
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		
		public function ViewStackControlEx()
		{
			super();
		}
		
		override public function set selectedIndex(value:int):void {
			var oldValue:String = selectedChildLabel;
			super.selectedIndex = value;
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "selectedChildLabel", oldValue, selectedChildLabel)); 
		} 
		
		[Bindable]
		public function get selectedChildLabel():String {
			return selectedChild != null ? selectedChild.label : null;
		}
		
		public function set selectedChildLabel(value:String):void {
			var child:Container = getChildByLabel(value);
			if (child != null)
				selectedChild = child;
		}
		
		public function getChildByLabel(value:String):Container {
			var child:Container = null;
			for (var i:int = 0; i < this.numChildren; i++) {
				child = getChildAt(i) as Container;
				if (child.label == value) {
					return child;
				}
			}
			
			logger.debug("could not find child with label: " + value);
			return null;			
		}
	}
}
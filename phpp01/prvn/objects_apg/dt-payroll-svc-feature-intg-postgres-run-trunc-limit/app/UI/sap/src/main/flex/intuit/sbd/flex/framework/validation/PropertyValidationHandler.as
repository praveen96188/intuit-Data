package intuit.sbd.flex.framework.validation
{
	import mx.events.ValidationResultEvent;
	
	public class PropertyValidationHandler
	{
		private var mProperty:String = "";
		private var mDelegateFunction: Function = null;
		
		public function PropertyValidationHandler(property:String, delegateFunction: Function)
		{
			if (property == null) 
				throw new Error("property cannot be null");
			mProperty = property;
			
			if (delegateFunction == null)
				throw new Error("delegateFunction cannot be null");
			mDelegateFunction = delegateFunction;
		}
		
		public function get delegateFunction(): Function {
			return mDelegateFunction;
		}
		
		public function set delegateFunction(value: Function): void {
			mDelegateFunction = value;
		}
		
		public function onValidationHandler(result:ValidationResultEvent):void {
			result.field = mProperty;
			mDelegateFunction(result);
		}
	}
}
package intuit.sbd.flex.framework.binding
{
	import flash.events.IEventDispatcher;
	
	import intuit.sbd.flex.framework.model.ApplicationObject;
	
	import mx.core.IMXMLObject;
	import mx.core.UIComponent;
	import mx.events.PropertyChangeEvent;
	import mx.events.ValidationResultEvent;
	
	public class ValidationResultBinding implements IMXMLObject
	{
		private var mControl: UIComponent;
		private var mSource: IEventDispatcher;
		private var mProperty: String;
		
		private var mDocument:Object;		
		
		public function ValidationResultBinding(): void {
			super();
		}
		
		//--------------------------------------------------------------------------
		//
		//  Methods: IMXMLObject
		//
		//--------------------------------------------------------------------------
		
	
		 /**
		  *  Called automatically by the MXML compiler when the Validator
		  *  is created using an MXML tag.  
		  *
		  *  @param document The MXML document containing this Validator.
		  *
		  *  @param id Ignored.
		  */
		 public function initialized(document:Object, id:String):void
		 {
			mDocument = document;
		 }
		
		
		public function onPropertyChange(event:PropertyChangeEvent):void {
			if (event.property == property) {
				var result: ValidationResultEvent =
					ApplicationObject(event.source).getValidationResultEvent(property);
				
				if (control != null)
					control.validationResultHandler(result);
			}
		}
		
		public function get control(): UIComponent {
			return mControl;
		}
		
		public function set control(value: UIComponent):void {
			mControl = value;
		}
		
		public function get source(): IEventDispatcher {
			return mSource;
		}
		
		public function set source(value: IEventDispatcher):void {
			
			if (mSource == value) return;
			
			if (mSource != null) {
				mSource.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChange);
			}
			
			mSource = value;
			
			if (mSource == null) return;
	
			mSource.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE,
									onPropertyChange,
									false,
									0.0,
									true);			
		}
		
		public function get property():String {
			return mProperty;
		}
		
		public function set property(value: String):void {
			mProperty = value;
		}
	}
}
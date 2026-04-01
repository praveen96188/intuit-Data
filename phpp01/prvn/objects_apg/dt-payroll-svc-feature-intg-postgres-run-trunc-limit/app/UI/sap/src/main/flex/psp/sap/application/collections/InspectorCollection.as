package psp.sap.application.collections
{
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;
	
	import psp.sap.application.IApplicationItem;
	import psp.sap.application.events.InspectorActionEvent;
	import psp.sap.viewmodel.AbstractInspectorViewModel;

	public class InspectorCollection extends ArrayCollectionExt
	{
		public function InspectorCollection(source:Array=null, equalityFunction:Function=null)
		{
			super(AbstractInspectorViewModel, source, equalityFunction);
		}				

		public function getInspectorAt(index:int):AbstractInspectorViewModel {
			return super.getItemAt(index) as AbstractInspectorViewModel;
		}				
		
		public function findByApplicationItem(applicationItem:IApplicationItem):AbstractInspectorViewModel {
			for each (var inspector:AbstractInspectorViewModel in this) {
				if (inspector.applicationItem.key == applicationItem.key) {
					return inspector;
				}
			}
			return null;
		}
		
		override public function removeItemAt(index:int):Object {
			var inspector:AbstractInspectorViewModel = super.removeItemAt(index) as AbstractInspectorViewModel;
			
			inspector.close();
			
			dispatchEvent(InspectorActionEvent.createClosedEvent());
			
			return inspector;
		}
		
		override public function removeAll():void {
			super.removeAll();
			
			dispatchEvent(InspectorActionEvent.createClosedEvent());
		}		
			
	}
}

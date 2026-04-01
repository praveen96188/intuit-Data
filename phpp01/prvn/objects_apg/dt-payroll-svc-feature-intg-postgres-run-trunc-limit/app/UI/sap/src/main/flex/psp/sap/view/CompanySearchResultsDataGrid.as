package psp.sap.view
{
	import flash.display.DisplayObject;
	import flash.events.FocusEvent;
	
	import mx.controls.listClasses.IListItemRenderer;
	import mx.core.mx_internal;
	
	import psp.sap.view.controls.SAPDataGrid;
	use namespace mx_internal;

	
	public class CompanySearchResultsDataGrid extends SAPDataGrid
	{
		 override protected function focusOutHandler(event:FocusEvent):void
    	 {
    	    if (itemEditorInstance && (!event.relatedObject || !itemRendererContains(itemEditorInstance, event.relatedObject)))
        	{
	            // find renderer for target
	            var target:DisplayObject = DisplayObject(event.relatedObject);
	           
	            if (target is IListItemRenderer && target.parent == null)
	            {
	            	//Do nothing for a bug that exists that's going to cause it to die, anyways
	            	trace("Not calling super.focusOutHandler method because of bug that exists.");
    	 			return;
				} else {
				 	super.focusOutHandler(event);
				}
			} else {
				super.focusOutHandler(event);
			}
    	 }
	}
}
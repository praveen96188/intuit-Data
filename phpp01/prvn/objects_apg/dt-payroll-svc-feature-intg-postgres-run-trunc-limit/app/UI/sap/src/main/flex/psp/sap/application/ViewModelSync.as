package psp.sap.application
{
    import mx.core.Container;
    import mx.core.UIComponent;

    import psp.sap.viewmodel.AbstractPartViewModel;

    public class ViewModelSync
	{
	
	    public static function findChildByLabel(view:UIComponent, someLabel:String):UIComponent {
	        //trace(someLabel + " " + view + " " + ("label" in view ? view["label"] : ""));
	        var i:int = 0;
	        var uiObject:UIComponent = null;
	
	        if(view["label"] == someLabel && view is Container)
	        {
	            return view;
	        }
	
	        try {
	            while(view.getChildAt(i)) {
	                try {
	                    var uiComponent:UIComponent = findChildByLabel(view.getChildAt(i) as UIComponent, someLabel);
	                    if(uiComponent != null) return uiComponent;
	                } catch(e:*) {
	                    //Not UIComponent ... skip
	                }
	                i++;
	            }
	        } catch(e:*) {
	            //Out of bounds
	        }
	        return null;
	    }
	
	    public static function associatePageViewModels(view:UIComponent,part:AbstractPartViewModel):void {
	        for each (var subViewModel:AbstractPartViewModel in part.partViewModels) {
	            var subView:UIComponent = findChildByLabel(view,subViewModel.label);
	            if (subView == null) {	             
	                continue;
	            }
	            if ("viewModel" in subView) {
	            	subView["viewModel"] = subViewModel;
	            }
	            associatePageViewModels(subView,subViewModel);
	        }
	    }
	    
	    	
	}
}
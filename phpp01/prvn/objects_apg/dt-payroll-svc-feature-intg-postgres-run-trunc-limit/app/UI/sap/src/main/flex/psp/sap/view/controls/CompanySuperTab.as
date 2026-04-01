package psp.sap.view.controls
{
	import flash.events.Event;
	
	import flexlib.controls.tabBarClasses.SuperTab;
	
	import mx.collections.ArrayCollection;
	import mx.controls.ToolTip;
	import mx.events.FlexEvent;
	import mx.events.ToolTipEvent;
	
	import psp.sap.model.Company;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	
	public class CompanySuperTab extends SuperTab
	{
		public function CompanySuperTab()
		{
			super();
						
			this.addEventListener(ToolTipEvent.TOOL_TIP_CREATE,onToolTipCreate,false,0,true);
			this.addEventListener(ToolTipEvent.TOOL_TIP_SHOWN, onToolTipShown, false, 0, true);
			
		}				
		
		public function onToolTipCreate(e:ToolTipEvent):void {
			var tt:HTMLToolTip = new HTMLToolTip();			
			e.toolTip = tt;	
		}
		
		private function onToolTipShown(evt:ToolTipEvent):void {
    		var tt:ToolTip = evt.toolTip as ToolTip;
    		if (this.errorString != "") {    					        
    			//this is so it doesn't go off the screen 
    			//when the tab is disabled with the error for having too many
    			tt.move(tt.x - 150, tt.y);    			
		    } 
		}					
	}
}
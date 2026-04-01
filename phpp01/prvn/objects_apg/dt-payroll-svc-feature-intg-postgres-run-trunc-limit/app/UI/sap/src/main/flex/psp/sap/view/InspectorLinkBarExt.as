package psp.sap.view
{
	import intuit.sbd.flex.framework.view.LinkBarExt;
	
	import mx.collections.IList;
	import mx.controls.Button;
	import mx.core.IFlexDisplayObject;

	public class InspectorLinkBarExt extends LinkBarExt
	{
		private var mSelectedChild:Button;
		
		[Bindable]
		public var buttonPercentWidth:Number = -1;
		public var buttonMinWidth:Number = -1;
		
		public function InspectorLinkBarExt()
		{
			super();
			tabEnabled = false;
			tabChildren = false;
		}
		
		override protected function createNavItem(
                                        label:String,
                                        icon:Class = null):IFlexDisplayObject
    	{
    		var navItem:Button = super.createNavItem(label, icon) as Button;
    		if(buttonPercentWidth != -1){
    			navItem.minWidth = buttonPercentWidth;
    		}
    		if(buttonMinWidth != -1){
    			navItem.minWidth = buttonMinWidth;
    		}
    		return navItem;
    	}
		
		override protected function hiliteSelectedNavItem(index:int):void {		
			
			super.hiliteSelectedNavItem(index);
			
			if (selectedIndex >= 0){
				selectedChild = Button(getChildAt(selectedIndex));
			}									
		}				
		
		[Bindable]
		public function get selectedChild():Button {
			return mSelectedChild;
		}
		
		public function set selectedChild(value:Button):void {
			if(mSelectedChild != null){
				mSelectedChild.setStyle("fontWeight", "normal");
			}
			
			mSelectedChild = value;
			
			if(mSelectedChild != null){
				mSelectedChild.setStyle("fontWeight", "bold");
			}
		}
		
		public override function set selectedIndex(value:int):void {
			if (value >= 0) {
				super.selectedIndex = value;	
			} else {
				super.selectedIndex = 0;
			}			
		}
		
		private var mSelectedItem:Object;
		[Inspectable(category="Common")] 
		[Bindable]
		override public function get selectedItem():Object {
			var selectedItem:Object = null;
			selectedItem = super.selectedItem;
			if(selectedItem == null && mSelectedItem != null){
				selectedItem = mSelectedItem;
			}
            // if there are topics in the collection force one of them to be selected
            else if(selectedItem == null && dataProvider != null && dataProvider is IList && dataProvider.length > 0) {
                selectedItem = IList(dataProvider).getItemAt(0);
            }

			return selectedItem;
		}
		
		override public function set selectedItem(value:Object):void {
			if (dataProvider is IList){
				selectedIndex = IList(dataProvider).getItemIndex(value);
				mSelectedItem = null;
			}			
			else{
				mSelectedItem = value;
			}
		}
				
	}
}
package psp.sap.view
{
	import intuit.sbd.flex.framework.view.LinkBarExt;
	
	import mx.controls.Button;

	public class ExplorerLinkBarExt extends LinkBarExt
	{
		private var mSelectedChild:Button;
		
		public function ExplorerLinkBarExt()
		{
			super();
			tabEnabled = false;
			tabChildren = false;
		}
		
		override protected function hiliteSelectedNavItem(index:int):void {		
			
			super.hiliteSelectedNavItem(index);
			if (selectedIndex != -1) {
				selectedChild = Button(getChildAt(selectedIndex));
				selectedChild.enabled = true;
			}
			if (index == -1) {
				selectedChild = null;
			}								
		}	
		
		[Bindable]
		public function get selectedChild():Button {
			return mSelectedChild;
		}
		
		public function set selectedChild(value:Button):void {
			if(mSelectedChild != null){
				mSelectedChild.setStyle("fontWeight", "normal");
				mSelectedChild.selected = false;
			}
			
			mSelectedChild = value;
			
			if(mSelectedChild != null){
				mSelectedChild.setStyle("fontWeight", "bold");
				mSelectedChild.selected = true;
			}
		}		
		
	}
}
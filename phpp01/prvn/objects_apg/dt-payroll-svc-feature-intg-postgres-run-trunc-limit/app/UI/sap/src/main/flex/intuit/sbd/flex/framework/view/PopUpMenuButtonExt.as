package intuit.sbd.flex.framework.view
{
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.controls.Menu;
	import mx.controls.PopUpMenuButton;
	import mx.core.mx_internal;
	import mx.events.CollectionEvent;
	
	use namespace mx_internal;
	
	public class PopUpMenuButtonExt extends PopUpMenuButton
	{
		public function PopUpMenuButtonExt()
		{
			super();
		}
		
		override public function set dataProvider(value:Object):void {
			if (dataProvider) {
				if (dataProvider is IList) {
					IList(dataProvider).removeEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged);				
				}
			}
			
			super.dataProvider = value;
			
			if (dataProvider && dataProvider is IList) {
				IList(dataProvider).addEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged,  false, 0, true);
			}
		}
		
		private function onCollectionChanged(e:CollectionEvent):void {
			if (!dataProvider)
				return;
				
			var item:* = IList(dataProvider).length > 0 ? IList(dataProvider)[0] : null;
			var popUpMenu:Menu = this.popUp as Menu;
			if (popUpMenu) {
				label = popUpMenu.itemToLabel(item);
			}
		}	
		
		override protected function clickHandler(event:MouseEvent):void {
			// simulate the first item in the list being clicked
			var popUpMenu:Menu = popUp as Menu;
			if (popUpMenu && popUpMenu.dataProvider) {
				var list:IList = dataProvider as IList;
				if (list && list.length > 0)
					Menu(popUp).selectedIndex = 0;
			}
			
			super.clickHandler(event);  		
 		}
	}
}
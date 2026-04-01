package psp.sap.view.controls
{
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.controls.Button;
	import mx.controls.TabBar;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	
	public class SAPTabBar extends TabBar
	{
		private var mSelectedChild:Button;
		
		public function SAPTabBar()
		{
			super();
		}
		
		/**
		 * Provide custom handling of dataProvider set to maintain the current selected
		 * index and not rebuild the links when a collectionChange event occurs.
		 * 
		 * The default behavior of the NavBar is rebuilding the links whenever a 
		 * collectionChange event occurs; it does this by calling 
		 * this.dataProvider = this.dataProvider.  The code below detects this scenario
		 * and short-circuits it.
		 */
		override public function set dataProvider(value:Object):void {
			// do not update the link bar in the event that a property
			// of the underlying collection was updated
			// -- this is an 'override' of the NavBar collectionChangeHandler
			//	  behavior that uses this method to rebuild the NavBar links on any
			//	  collection change
			if (handlingUpdateEvent) {
				handlingUpdateEvent = false;
				return;
			}

			if (dataProvider != null) 
				IList(dataProvider).removeEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged);
				
			
			var list:IList = value as IList;
			if (list != null) {
				list.addEventListener(CollectionEvent.COLLECTION_CHANGE,
                                           onCollectionChanged, false, int.MAX_VALUE, true);
			}				

			var prevSelectedItem:Object = this.selectedItem;	
					
			super.dataProvider = value;			
			
			if(dataProvider != null)
			{
				var newIndex:int = (super.dataProvider as IList).getItemIndex(prevSelectedItem);
				selectedIndex = newIndex;
			}
		}
		
		private var handlingUpdateEvent:Boolean = false;
		private function onCollectionChanged(e:CollectionEvent):void {
			handlingUpdateEvent  = (e.kind == CollectionEventKind.UPDATE);
		}
		
		[Inspectable(category="Common", values="true,false", default="false")]
		[Bindable]
		public var trackSelectionState:Boolean = false;
			
		[Inspectable(category="Common")] 
		[Bindable]
		public function get selectedItem():Object {
			var selectedItem:Object = null;
			if (selectedIndex != -1 && dataProvider is IList && selectedIndex < (dataProvider as IList).length)
				selectedItem = IList(dataProvider).getItemAt(selectedIndex);
			
			return selectedItem;
		}
		
		public function set selectedItem(value:Object):void {
			if (dataProvider is IList)
				selectedIndex = IList(dataProvider).getItemIndex(value);			
		}
		
		override protected function hiliteSelectedNavItem(index:int):void {
			// below is a hack -- the control code does not check for an invalid -1 index
			// before trying to access the control, i.e. getChildAt(-1)
			// the real question is -- how come -1 is coming through as the selection, it undoubtedly
			// has to do with the dataprovider extensions in this class.  Will fix that later, for
			// now, this stop gap will work
			if (index == -1) {
		        // Un-hilite the current selection.
		        if (selectedIndex != -1 && selectedIndex < numChildren)
		        {
		            var child:Button = Button(getChildAt(selectedIndex));
		            child.enabled = true;
		        }
		        return;				
			}
			if (this.getChildren() != null && this.getChildren().length > index){ 
				super.hiliteSelectedNavItem(index);				
			}
			
			if (selectedIndex >= 0){
				selectedChild = Button(getChildAt(selectedIndex));
			}
				
			dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "selectedItem", null, selectedItem) );	
		}
		
		override protected function clickHandler(event:MouseEvent):void {
			if (trackSelectionState) {
				var index:int = getChildIndex(Button(event.currentTarget));
				if (index != selectedIndex)
					hiliteSelectedNavItem(index);
			}
			
			super.clickHandler(event);
		}
		
		[Bindable]
		public function get selectedChild():Button {
			return mSelectedChild;
		}
		
		public function set selectedChild(value:Button):void {						
			mSelectedChild = value;			
		}

	}
}
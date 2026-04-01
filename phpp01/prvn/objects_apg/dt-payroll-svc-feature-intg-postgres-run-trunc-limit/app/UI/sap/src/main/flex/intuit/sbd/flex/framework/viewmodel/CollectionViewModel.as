package intuit.sbd.flex.framework.viewmodel
{
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;
	
	import mx.events.PropertyChangeEvent;

	public class CollectionViewModel extends ArrayCollectionExt
	{
		private var mSelectedIndex: int;
		
		public function CollectionViewModel(itemType:Class, source:Array=null):void {
			super(itemType, source);
				
			if (this.length > 0) this.mSelectedIndex = 0;
			else this.mSelectedIndex = -1;
			
		}
			
		[Bindable]
		public function get selectedIndex(): int {
			return mSelectedIndex;
		}		
		
		public function set selectedIndex(index: int): void {
			if (index < 0 || index >= this.length)
				return;
			
			if (index != mSelectedIndex) {
				var oldIndex: int = mSelectedIndex;
				var oldHasSelection: Boolean = hasSelection;
				var oldWasFirst: Boolean = isFirst;
				var oldCanSelectPrev: Boolean = canSelectPrevious;
				var oldWasLast: Boolean = isLast;
				var oldCanSelectNext: Boolean = canSelectNext;
				var oldSelectedItem: Object = this.selectedItem;
				
				mSelectedIndex = index;
				//onSelectedIndex(oldIndex);
				
				dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "selectedItem", oldSelectedItem, this.selectedItem));
				
				if (oldWasFirst != isFirst)
					dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "isFirst", oldWasFirst, isFirst));

				if (oldWasLast != isLast)
					dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "isLast", oldWasLast, isLast));
				
				if (oldHasSelection != hasSelection)
					dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "hasSelection", oldHasSelection, hasSelection));
					
				if (oldCanSelectPrev != canSelectPrevious)
					dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "canSelectPrevious", oldCanSelectPrev, canSelectPrevious));
									
				if (oldCanSelectNext != canSelectNext)
					dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "canSelectNext", oldCanSelectNext, canSelectNext));
			}
		}
		
		private function onSelectedIndex(oldIndex: int): void {
			this.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "selectedIndex", oldIndex, mSelectedIndex));
		}
			
		[Bindable(event="propertyChange")]
		public function get hasSelection(): Boolean {
			return !isEmpty && (mSelectedIndex != -1);
		}
		
		[Bindable(event="propertyChange")]
		public function get isEmpty(): Boolean {
			return this.length == 0;
		}
		
		[Bindable(event="propertyChange")]
		public function get canSelectPrevious(): Boolean {
			return !isEmpty && hasSelection && !isFirst;
		}
			
		[Bindable(event="propertyChange")]
		public function get canSelectNext(): Boolean {
			return !isEmpty && hasSelection && !isLast;
		}
		
		[Bindable(event="propertyChange")]
		public function get isFirst(): Boolean {
			return isEmpty ? true : mSelectedIndex == 0;
		}
		
		[Bindable(event="propertyChange")]
		public function get isLast(): Boolean {
			return isEmpty ? true : mSelectedIndex == this.length - 1;
		}
		
		public function selectNext():int {
			if (!isLast) {
				this.selectedIndex++;
			}
			return this.selectedIndex;
		}
		
		public function selectPrevious(): int {
			if (!isFirst) {
				this.selectedIndex--;
			}
			return this.selectedIndex;
		}
		
		public function selectFirst(): int {
			this.selectedIndex = 0;
			return this.selectedIndex;
		}
		
		public function selectLast(): int {
			this.selectedIndex = this.length - 1;
			return this.selectedIndex;
		}
		
		public function set selectedItem(item: Object): void {
			var index: int = this.getItemIndex(item);
			if (index == -1)
				return;
				
			this.selectedIndex = index;
			return;
		}
		
		[Bindable]
		public function get selectedItem(): Object {
			
			if (mSelectedIndex < 0)
				return null;
				
			return this.getItemAt(mSelectedIndex);
		}
	}
}
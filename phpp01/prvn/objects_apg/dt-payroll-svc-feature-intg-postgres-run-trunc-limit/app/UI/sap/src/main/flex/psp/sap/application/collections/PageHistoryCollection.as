package psp.sap.application.collections
{
    import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;

    import psp.sap.viewmodel.InspectorTopicViewModel;
    import psp.sap.viewmodel.SinglePartPageViewModel;

    public class PageHistoryCollection extends ArrayCollectionExt
	{
		public function PageHistoryCollection(source:Array=null, equalityFunction:Function=null)
		{
			super(SinglePartPageViewModel, source, equalityFunction);
		}

		private var mLastActiveTopic:InspectorTopicViewModel = null;
		
		override public function addItemAt(item:Object, index:int):void {	
			var page:SinglePartPageViewModel = item as SinglePartPageViewModel;
			if (page == null) 
				return;
			
			//TODO: this code would be better outside of this class, possibly in the inspector
			// on topic switch, clear out the history
			if (mLastActiveTopic != page.topic && !page.maintainCrossTopicHistory) {
				removeAll();
				index = 0;
				mLastActiveTopic = page.topic;
			}
			
			// if page isn't already in history, add it
			// if page is already in history; if so, remove all pages after it
			var pageIndex:int = getItemIndex(item);
			if (pageIndex == -1) {
				super.addItemAt(item, index);
			}
			else {
				for (var i:int = length - 1; i > pageIndex; i--) {
					removeItemAt(i);
				}
			}			
		}
		
		public function getPageAt(index:int, prefetch:int = 0):SinglePartPageViewModel {
			return super.getItemAt(index, prefetch) as SinglePartPageViewModel;
		}
		
		public function get currentPage():SinglePartPageViewModel {
			if (length == 0)
				return null;
				
			return getPageAt(length - 1);
		}
		
		public function get previousPage():SinglePartPageViewModel {
			if (length < 2)
				return null;
			
			return getPageAt(length - 2);
		}
		
		public function goToPreviousPage():void {
			var page:SinglePartPageViewModel = previousPage;
			if (previousPage != null)
				previousPage.activate();
		}

        public function containsLabel(label:String):Boolean {
            for each (var page:SinglePartPageViewModel in this) {
                if (page.label == label) {
                    return true;
                }
            }
            return false;
        }

        public function containsPage(label:String):Boolean {
            return containsLabel(label + "_page");
        }
	}
}
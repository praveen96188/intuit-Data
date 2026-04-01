package psp.sap.view
{
    import flash.display.DisplayObjectContainer;
    import flash.events.MouseEvent;

    import mx.containers.HBox;
    import mx.containers.ViewStack;
    import mx.controls.LinkButton;
    import mx.controls.Text;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;

    import psp.sap.application.collections.PageHistoryCollection;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.SinglePartPageViewModel;

    public class BreadCrumbDisplay extends HBox
	{		

		// the page viewModel the bread crumb is anchored to
		private var mPageViewModel:AbstractPartViewModel;
		
		public function BreadCrumbDisplay()
		{
			super();
			this.styleName = "breadCrumbContainer";
			tabEnabled = false;
			tabChildren = false;
		}
	
  		public function set pageViewModel(value:AbstractPartViewModel):void {
  			if (mPageViewModel != null && mPageViewModel.inspector != null && mPageViewModel.inspector.pageHistory != null) {
  				mPageViewModel.inspector.pageHistory.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onPageHistoryCollectionChanged);
  			}
  			
  			mPageViewModel = value;
  			createNavLinks();
  			
  			if (mPageViewModel != null) {
  				mPageViewModel.inspector.pageHistory.addEventListener(CollectionEvent.COLLECTION_CHANGE, onPageHistoryCollectionChanged, false, 0, true);
     		}
  		}
  		
  		public function get pageViewModel():AbstractPartViewModel {
  			return mPageViewModel;
  		}
  		
  		protected function onPageHistoryCollectionChanged(e:CollectionEvent):void {
  			if (e.kind != CollectionEventKind.UPDATE)
  				createNavLinks(); 
  		}  		
  		
  		protected function get pageHistory():PageHistoryCollection {
  			if (mPageViewModel == null || mPageViewModel.inspector == null) {
  				return new PageHistoryCollection();
  			}
  			else {
  				return mPageViewModel.inspector.pageHistory;
  			}
  		}
  			
		public function performClickAction(value:MouseEvent):void {
			var index:int = ((value.currentTarget) as LinkButton).data as int;
			var page:SinglePartPageViewModel = pageHistory.getPageAt(index);
			page.activate();
		}
			
		public function hideBreadCrumb():void {
			this.visible = false;
		}
		
		public function showBreadCrumb():void {
			this.visible = true;
		}
		
		override protected function initializationComplete():void {
			// find parent who is in ViewStack
			var p:DisplayObjectContainer = parent;
			while (p != null && !(p.parent is ViewStack)) {
				p = p.parent;
			}
			
			p.addEventListener(FlexEvent.SHOW, onShow, false, 0, true);
		}
		
		private function onShow(e:FlexEvent):void {
			createNavLinks();
		}

		override protected function createChildren():void {
			super.createChildren();
			createNavLinks();
		}
		
		public function createNavLinks():void {
			removeAllChildren();
					
			var headerHBox:HBox = new HBox();
			
			if (pageHistory == null || pageHistory.length < 2 || pageViewModel == null) {
				hideBreadCrumb();
				return;
			}
			
			showBreadCrumb();
			
			for(var i:int = 0; i < pageHistory.length; i++)
			{
				var page:SinglePartPageViewModel = pageHistory.getPageAt(i);
					
				var myAction:LinkButton = new LinkButton();
				if (page.breadCrumbLabel != null) {
                    myAction.label = page.breadCrumbLabel;
                } else {
                    myAction.label = page.part.label;
                }
				myAction.data = i;
				
				//Make link and add separator
				if(page != pageViewModel) {
					
					//As per bug SAP000026, click event listener is moved here so it is only attached if we want it to be clicked 
					myAction.addEventListener("click", performClickAction, false, 0, true);
						
					myAction.styleName = "breadCrumbLink";				
									
					//make a link
					headerHBox.addChild(myAction);
					
					//add separator
					var newSep:Text = new Text();
					newSep.text = " > ";
					headerHBox.addChild(newSep);
				} 
				else {
					//make black link
					myAction.styleName = "breadCrumbCurrentItem";
					
					// turn off hand cursor
					myAction.buttonMode = false;
					
					headerHBox.addChild(myAction);
					
					// stop looping when the page in the history is the page the bread crumb is attached to
					break;
				}
			}	
			
			this.addChild(headerHBox);
			
		}		
	}
}
package psp.sap.view.controls
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import flexlib.controls.SuperTabBar;
	import flexlib.events.SuperTabEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Button;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.events.FlexEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.model.Company;
	import psp.sap.viewmodel.CompanyInspectorViewModel;

	public class CompanySuperTabBar extends SuperTabBar
	{
		public function CompanySuperTabBar()
		{
			super();
			this.addEventListener(SuperTabEvent.TAB_CLOSE,onTabClose,false,0,true);
		}
		
		override protected function getTabClass():Class {
			return CompanySuperTab;
		} 
						
		override public function addChildAt(child:DisplayObject, index:int):DisplayObject {										
			var returnDisplayObject:DisplayObject = super.addChildAt(child,index); 
			
			var company:Company = ((this.dataProvider as ArrayCollection).getItemAt(index) as CompanyInspectorViewModel).company;
			CompanySuperTab(child).toolTip = company.legalName + "<br>EIN: <b>" + company.fein + "</b>";
			
			if (index==SAP.MAX_OPEN_COMPANIES){													
				//have to disable the tab later or else the sizing gets messed up		
				child.addEventListener(FlexEvent.CREATION_COMPLETE,disableLastTab,false,0,true);				
			}				
			return returnDisplayObject;
		}				

		private function disableLastTab(e:Event=null):void{
			this.getTabAt(SAP.MAX_OPEN_COMPANIES).enabled=false;
			this.getTabAt(SAP.MAX_OPEN_COMPANIES).errorString="Only " + SAP.MAX_OPEN_COMPANIES + " companies can be open at one time. You must close one of your open companies.";						 						
		}			

		public function onTabClose(e:SuperTabEvent):void
		{
			if (this.numChildren <= SAP.MAX_OPEN_COMPANIES) {
				return;
			}
			
			var tab:Button = this.getTabAt(SAP.MAX_OPEN_COMPANIES);
			if (tab.enabled == false){
				tab.enabled=true;
				tab.errorString="";		
			}
		}						
		
		private function getTabAt(index:int):CompanySuperTab {
			return this.getChildAt(index) as CompanySuperTab;
		}
		
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
		
    }
}

package psp.sap.view
{
	import mx.collections.ArrayCollection;
	import mx.controls.PopUpButton;
	import mx.events.CollectionEvent;
	import mx.events.ListEvent;
	import mx.events.PropertyChangeEvent;
	
	import psp.sap.model.Company;

	public class MRUPopUpButton extends PopUpButton
	{
		private var MRUPopUpitem:MRUPopUp;
		
		public function MRUPopUpButton()
		{		
			super();					
			
			MRUPopUpitem = new MRUPopUp();
			MRUPopUpitem.owner = this;
			MRUPopUpitem.addEventListener(ListEvent.ITEM_CLICK, handleItemClick, false, 0, true);
			popUp = MRUPopUpitem;												
		}				
		
		[Bindable]
		public function get openList():ArrayCollection {			
			return MRUPopUpitem.openList;
		}
		
		public function set openList(value:ArrayCollection):void {
			if (MRUPopUpitem.openList != null) {				
					MRUPopUpitem.openList.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged);				
			}
			
			MRUPopUpitem.openList = value;
						
			MRUPopUpitem.openList.addEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged,  false, 0, true);			
		}
		
		[Bindable]
		public function get closedList():ArrayCollection {
			return MRUPopUpitem.closedList;
		}
		
		public function set closedList(value:ArrayCollection):void {
			MRUPopUpitem.closedList = value;
		}				
		
		private function onCollectionChanged(e:CollectionEvent):void {
			if (MRUPopUpitem.openList == null)
				return;
				
			var item:Company = null;
			if(MRUPopUpitem.closedList != null && MRUPopUpitem.closedList.length > 0){
				item = MRUPopUpitem.closedList[0];
			}
			 
			if(item != null){			
				label = item.toString();
			}			
			else {
				label = "";
			}			
		}					
 		
 		private function handleItemClick(e:ListEvent):void { 			
 			// display the company
 			Company(e.itemRenderer.data).display();
 		}
 		
 		[Bindable("widthChanged")]
    	[Inspectable(category="General")]
    	[PercentProxy("percentWidth")]
 		override public function set width(value:Number):void {
 			MRUPopUpitem.width = value;
 			super.width = value;
 		}
 		 				
	}
}
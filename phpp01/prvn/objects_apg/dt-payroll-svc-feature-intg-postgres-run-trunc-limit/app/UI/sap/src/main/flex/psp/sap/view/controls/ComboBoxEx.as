package psp.sap.view.controls
{
	import flash.events.Event;
	
	import mx.controls.ComboBox;
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.events.FlexEvent;

	public class ComboBoxEx extends ComboBox
	{
		private var _ddFactory:IFactory = new ClassFactory(ComboBoxList);
		
		public function ComboBoxEx()
		{
			super();
		}
		
		override public function get dropdownFactory():IFactory
        {
            return _ddFactory;
        }        

        override public function set dropdownFactory(factory:IFactory):void
        {
            _ddFactory = factory;
        }
        
        public function adjustDropDownWidth(event:Event=null):void
        {    
            this.removeEventListener(FlexEvent.VALUE_COMMIT, adjustDropDownWidth);
                    
            if (this.dropdown == null)
            {
                callLater(adjustDropDownWidth);
            }
            else
            {                
                var ddWidth:int = this.dropdown.measureWidthOfItems(-1, this.dataProvider.length);
                if (this.dropdown.maxVerticalScrollPosition > 0)
                {                    
                    ddWidth += ComboBoxList(dropdown).getScrollbarWidth();                    
                }                
                this.dropdownWidth = Math.max(ddWidth,this.width);                
            }
        }
        
        override protected function collectionChangeHandler(event:Event):void
        {
            super.collectionChangeHandler(event);
            this.addEventListener(FlexEvent.VALUE_COMMIT, adjustDropDownWidth, false, 0, true);        
        }
        
		[Bindable("change")]
	    [Bindable("valueCommit")]		    
	    override public function get value():Object
	    {
	        if (editable)
	            return text;
	
	        var item:Object = selectedItem;
	
	        if (item == null || typeof(item) != "object")
	            return item;
	
	        
			// for some reason the sdk always expected the selected item to have a data property
			if(item.hasOwnProperty("data")){
				return item.data != null ? item.data : item.label;
			}	
			
			return item;			        
	    }        
		
	}
}
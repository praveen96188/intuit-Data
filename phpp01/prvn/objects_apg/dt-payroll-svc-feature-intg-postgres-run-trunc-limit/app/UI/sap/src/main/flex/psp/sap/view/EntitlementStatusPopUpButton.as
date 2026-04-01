package psp.sap.view {
    import flash.events.MouseEvent;

    import mx.binding.utils.BindingUtils;
    import mx.controls.PopUpButton;

    import psp.sap.model.StatusEntitlements;

    public class EntitlementStatusPopUpButton extends PopUpButton {

        private var popUpItem:EntitlementStatusPopUp;
		private var mMouseOver:Boolean = false;

        [Bindable] public var statusEntitlements:StatusEntitlements;

		public function EntitlementStatusPopUpButton()
		{
			super();

			popUpItem = new EntitlementStatusPopUp();
			popUpItem.owner = this;
			popUp = popUpItem;
			popUp.visible = false;


			BindingUtils.bindProperty(this, "mouseOver", popUpItem, "mouseOver");
            BindingUtils.bindProperty(popUpItem, "statusEntitlements", this, "statusEntitlements");
		}


		[Bindable]
		public function set mouseOver(value:Boolean):void {
			mMouseOver = value;
			if(value && !popUp.visible){
				open();
			}
			else if(!value && popUp.visible){
				close();
			}
		}

		public function get mouseOver():Boolean {
			return mMouseOver;
		}

		override protected function rollOverHandler(event:MouseEvent):void
    	{
    		mouseOver = true;
    	}

    	override protected function rollOutHandler(event:MouseEvent):void
    	{
    		// check if the mouse is over the popup
    		if(popUpItem.mouseY < 0){
    			mouseOver = false;
    		}
    	}

    	// override the tool tip to keep it from showing up over the popup
    	override public function set toolTip(value:String):void
    	{
    		super.toolTip = null;
    	}

    	override public function get toolTip():String
    	{
    		return null;
    	}
    }
}
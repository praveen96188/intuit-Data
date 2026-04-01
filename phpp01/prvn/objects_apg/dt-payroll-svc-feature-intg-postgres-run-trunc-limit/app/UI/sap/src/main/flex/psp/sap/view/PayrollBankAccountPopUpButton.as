package psp.sap.view
{
	import flash.events.MouseEvent;
	
	import mx.binding.utils.BindingUtils;
	import mx.controls.PopUpButton;
	import mx.events.DropdownEvent;

	import psp.sap.model.CompanyBankAccount;

	public class PayrollBankAccountPopUpButton extends PopUpButton
	{
		private var popUpItem:PayrollBankAccountPopUp;
		private var mMouseOver:Boolean = false;
		
		public function PayrollBankAccountPopUpButton()
		{		
			super();					
			
			popUpItem = new PayrollBankAccountPopUp();
			popUpItem.owner = this;			
			popUp = popUpItem;
			popUp.visible = false;			
			
			BindingUtils.bindProperty(this, "mouseOver", popUpItem, "mouseOver");																				
		}

        [Bindable]
        public function set employerDDDebitTxnNumber(value:String):void {
            popUpItem.employerDDDebitTxnNumber = value;
        }

        public function get employerDDDebitTxnNumber():String {
            return popUpItem.employerDDDebitTxnNumber;
        }
		
		[Bindable]
		public function set bankAccount(value:CompanyBankAccount):void {
			popUpItem.bankAccount = value;
		}
		
		public function get bankAccount():CompanyBankAccount {
			return popUpItem.bankAccount;
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
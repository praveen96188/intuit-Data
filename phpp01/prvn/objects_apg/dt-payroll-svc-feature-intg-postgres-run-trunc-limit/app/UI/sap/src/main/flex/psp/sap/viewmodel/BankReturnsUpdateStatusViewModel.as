
package psp.sap.viewmodel
{
	import mx.formatters.NumberFormatter;
	import mx.rpc.events.ResultEvent;
	import mx.validators.StringValidator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
	import psp.sap.application.enums.RiskInspectorPageEnum;
	import psp.sap.model.BankReturn;
	import psp.sap.validators.SAPValidators;
		
	
	public class BankReturnsUpdateStatusViewModel extends AbstractPartViewModel 
	{
		[Bindable] [BackingProperty (context=true, linkable=false)] public var bankReturnsSearch:BankReturn;

        // move to config
		private const MAX_NOTE_LENGTH:Number = 3998;
		private const DEFAULT_NOTE:String = "";
		
		private var mNote:String = "";	
		private var mRemainingChars:String = MAX_NOTE_LENGTH.toString();	
		private var mBankReturnUpdateNote:String;
		
		private var mNumberFormatter:NumberFormatter = new NumberFormatter();
		
		[Bindable]
		public var noteValidator:StringValidator;
		
		public function BankReturnsUpdateStatusViewModel()
		{
			 this.label = RiskInspectorPageEnum.BANK_RETURN_STATUS_UPDATE;	
			 
			 noteValidator = SAPValidators.createStringValidator(this, "note", false, 0, MAX_NOTE_LENGTH);			
			 validators.push(noteValidator);
		}

        public static function createActivator(bankReturnsSearch:BankReturn):Object {
            return {"bankReturnsSearch":bankReturnsSearch};
        }

		[Bindable]
		public function get note():String {
			return mNote;
		}
		
		public function set note(value:String):void {
			mNote = value;
			remainingChars = (MAX_NOTE_LENGTH - mNote.length).toString();
			updateCanSave();
		}
		
		[Bindable]
		public function get remainingChars():String {
			return mRemainingChars;
		}
		
		public function set remainingChars(value:String):void {
			mRemainingChars = value;		
				
		}
		
		/*override protected function writeModelValues():void {
			bankReturnUpdateNote = note;			
		}*/
		
		
		override protected function executeSave():void {			 
			SAP.instance.bankReturnService.saveBankReturnNote(bankReturnsSearch.companySourceSystemCd,
														bankReturnsSearch.companyId,
														bankReturnsSearch.txnId,
														bankReturnsSearch.statusCd == "Resolved" ? "Created" : "Resolved",
														note,
														createSaveResponder(onSaveSucceeded));		
		}		
		
		override public function get hasChanged():Boolean {
			return (note != DEFAULT_NOTE);
		}
		
		protected function onSaveSucceeded(e:ResultEvent):void {					
			cancel();
		}	
    	
    	override protected function initializeBackingProperties():void {
    	  note = DEFAULT_NOTE;
    	}
	}
}
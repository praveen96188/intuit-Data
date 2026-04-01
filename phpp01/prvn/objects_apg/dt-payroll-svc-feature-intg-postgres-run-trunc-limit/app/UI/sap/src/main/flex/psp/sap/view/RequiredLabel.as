package psp.sap.view
{
	import mx.containers.HBox;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.events.ValidationResultEvent;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	
	import psp.sap.application.SAP;

	public class RequiredLabel extends HBox
	{
		private var mText:String = "";
		private var mBlackAstrisk:Image;
		private var mRedAstrisk:Image;
		private var mValidator:Validator;				
		
		public function RequiredLabel()
		{
			super();
		}
		
		[Bindable]
		public function set text(value:String):void {
			mText = value;
		}
		
		public function get text():String {
			return mText;
		}
		
		public function set validator(value:Validator):void {
			removeValidatorListeners();
			mValidator = value;
			addValidatorListeners();
			if(mValidator != null){
				mValidator.validate(null, false);
			}				
		}				
		
		private function addValidatorListeners():void {
			if(mValidator != null){
				mValidator.addEventListener(ValidationResultEvent.VALID,
	                                							resultHandler, false, 0, true); 
	
	            mValidator.addEventListener(ValidationResultEvent.INVALID,
	                                 							resultHandler, false, 0, true);
   			}
		}
		
		private function removeValidatorListeners():void {
			if(mValidator != null){
				mValidator.removeEventListener(ValidationResultEvent.VALID,
	                                							resultHandler); 
	
	            mValidator.removeEventListener(ValidationResultEvent.INVALID,
	                                 							resultHandler);
   			}
		}
		
		public function resultHandler(event:ValidationResultEvent):void {
			for each(var result:ValidationResult in event.results){
				if(result.isError && result.errorCode == "requiredField" || result.errorCode == "lowerThanMin" || result.errorCode == "tooShort"){
					// display red
					mBlackAstrisk.visible = false;
					mBlackAstrisk.includeInLayout = false;
					mRedAstrisk.visible = true;
					mRedAstrisk.includeInLayout = true;
					// only want the first one
					return;
				}								
			}
			// display black
			mBlackAstrisk.visible = true;
			mBlackAstrisk.includeInLayout = true;
			mRedAstrisk.visible = false;
			mRedAstrisk.includeInLayout = false;
		}				 
		
		override protected function  createChildren():void {
			super.createChildren();
			setStyle("horizontalGap", "0")
			var label:Label = new Label();
			
			// add the label
			label.text = text;
			addChild(label);
			
			// add the astrisk
			mBlackAstrisk = new Image();
			mBlackAstrisk.setStyle("horizontalAlign", "left");
			mBlackAstrisk.setStyle("verticalAlign", "top");
			mBlackAstrisk.source = SAP.instance.icons.blackAsterisk;			
			addChild(mBlackAstrisk);						
			
			// add the astrisk
			mRedAstrisk = new Image();
			mRedAstrisk.toolTip = "This field is required";
			mRedAstrisk.setStyle("horizontalAlign", "left");
			mRedAstrisk.setStyle("verticalAlign", "top");
			mRedAstrisk.source = SAP.instance.icons.redAsterisk;			
			addChild(mRedAstrisk);
		}
		
	}
}
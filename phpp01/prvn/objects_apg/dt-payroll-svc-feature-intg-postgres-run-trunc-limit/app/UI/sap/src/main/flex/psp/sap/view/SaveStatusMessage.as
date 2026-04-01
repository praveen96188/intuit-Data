package psp.sap.view
{
	import flash.display.Sprite;
	import flash.filters.BlurFilter;
	
	import mx.containers.VBox;
	import mx.controls.Text;
	import mx.effects.Effect;
	import mx.effects.Fade;
	import mx.events.FlexEvent;
	import mx.rpc.Fault;
	
	public class SaveStatusMessage extends VBox
	{
		
		//Member variables
		private var mIsError:Boolean;		
		private var mErrorMessageStyle:String;
		private var mSavedMessageStyle:String
		private var mText:String
		
		//Public variables
		public var messageEffect:Effect;
		
		//Private controls
		private var textField:Text;

		//Error string constants
		private const DEFAULT_SAVE_MESSAGE:String = "The changes have been saved";
		private const DEFAULT_ERROR_MESSAGE_WITH_TECHNICAL:String = "An error occurred while saving the data.\n\nTechnical Details:\n";
		private const DEFAULT_ERROR_MESSAGE:String = "An error occurred while saving the data.\nDetails: ";
								
		private var mTextFieldWidth:int;
		
		public function get textFieldWidth():int {
			return mTextFieldWidth;
		}
		
		public function set textFieldWidth(value:int):void {
			mTextFieldWidth = value;
			textField.width = value;
		}
		
		public function SaveStatusMessage()
		{
			super();
			
			//Create text field
			textField = new Text();
			textField.automationName = "saveMessageText";
			textField.cacheAsBitmap = true;
			textField.visible = true;
			textField.includeInLayout = true;			
			textField.filters = new Array(new BlurFilter(0, 0, 0));			
				
			this.addChild(textField);
						
			//Defaults
			errorMessageStyle = "errorMessageStyle";
			savedMessageStyle = "savedMessageStyle";
			mIsError = false;
			
			//Set padding
			this.setStyle("paddingBottom", 10); 
			
			//Default effect
			var defaultEffect:Fade = new Fade();
			defaultEffect.alphaFrom = 0;
			defaultEffect.alphaTo = 1;
			defaultEffect.target = this;
			defaultEffect.duration = 1000;
			messageEffect = defaultEffect;
			
			//Add Event Listeners for object
			this.addEventListener(FlexEvent.CREATION_COMPLETE, addEventListeners, false, 0 , true);
			
			this.text = "";
		}
		
		
		/* Wrappers that you want to use*/		
		public function displaySaveMessage():void {
			setDisplayMessage(DEFAULT_SAVE_MESSAGE, false);
		}
		
		public function displayErrorMessage(error:*):void {	
			var errorString:String = "";
			
			var errorHasDetails:Boolean = false;
			
			if(error != null)
			{
				if(error is String) {
					errorString = ((error != null) ? error : '')
				} else {
					var errorObject:Object = error as Object;
					
					if(errorObject.hasOwnProperty("data")) {
						if(errorObject.data is Fault) {
							
							var faultObject:Fault = (errorObject.data) as Fault;
							errorHasDetails = true;
							
							if(faultObject.rootCause is Object && faultObject.rootCause != null)
							{
								errorString = faultObject.rootCause.message;
							} else {
								errorString = faultObject.faultString;
							}
						} else {
							errorString = (error.data != null) ? error.data.toString() : error.toString();
						}
					} 
					else if(errorObject.hasOwnProperty("message"))
					{
						var faultObjectMessage:Object = errorObject.message;
						
						if(faultObjectMessage.hasOwnProperty("rootCause"))
						{
							if(faultObjectMessage.rootCause is Object && faultObjectMessage.rootCause != null)
							{
								errorString = faultObjectMessage.rootCause.message;
							} else {
								errorString = faultObjectMessage.faultString;
							}
						} else {
							errorString = (error.message != null) ? error.message.toString() : error.toString();
						}
					} else {
						errorString = error.toString();
					}
				
				}
			}
			
			if(errorHasDetails)
			{
				setDisplayMessage(DEFAULT_ERROR_MESSAGE_WITH_TECHNICAL + errorString, true);
			} else {
				setDisplayMessage(DEFAULT_ERROR_MESSAGE + errorString, true);
			}
			
						
			
		}
		
		public function clearMessage():void {
			text = "";
		}
		
		
		//Wrapper in case you want to override default error or save messages
		public function setDisplayMessage(displayText:String, isMessageAnError:Boolean):void
		{
			isError = isMessageAnError;
			text = displayText;
		}
		
		//addEventListeners if twe are in a container that contains a viewModel
		private function addEventListeners(event:Event):void {
			if(parent != null)
			{	
				 var pObject:Sprite = getPage();
				 if(pObject != null)			
				 {
				 	pObject.addEventListener(FlexEvent.SHOW, onPageShow, false, 0 , true);
				 }
			}
		}
		
		//This is getting the page by iterating and looking for the viewModel. Note, there may be a better way,
		//so please replace if so.
		private function getPage():Sprite {
			var pObject:Object = this;
			
			while(pObject != null)
			{
				pObject = pObject.parent;
				if(pObject.hasOwnProperty("viewModel")) return (pObject as Sprite);
			}
			
			return null;
		}

		
		//Remove the text when the page is shown.
		private function onPageShow(value:FlexEvent):void {
			text = "";
		}
		
		public function triggerMessageEffect():void {
			if(messageEffect != null)
			{
				messageEffect.stop();
				messageEffect.play();
			}
		}
		
		public function hideMessage():void {
			this.visible = false;
			this.includeInLayout = false;
		}
		
		public function showMessage():void {
			this.visible = true;
			this.includeInLayout = true;
		}
		
		private function updateStyles():void {
			var switchStyle:String = (mIsError == true) ? errorMessageStyle : savedMessageStyle;;
			if(this.styleName != switchStyle) {
				this.styleName = switchStyle;
			}
		}
		
		[Bindable]
		public function set text(value:String):void {
			
			if (mText != "")
				hideMessage();
			
			mText = value;
			textField.text = mText;
			
			if(mText != "") {
				showMessage();
				triggerMessageEffect();
			}
		}
		
		public function get text():String {
			return mText;
		}
		
		public function set errorMessageStyle(value:String):void {
			mErrorMessageStyle = value;
			updateStyles();
		}
		
		public function get errorMessageStyle():String {
			return mErrorMessageStyle;
		}
		
		public function set savedMessageStyle(value:String):void {
			mSavedMessageStyle = value;
			updateStyles();
		}
		
		public function get savedMessageStyle():String {
			return mSavedMessageStyle;
		}
		
		public function set isError(value:Boolean):void {
			mIsError = value;
			updateStyles();
		}
		
		public function get isError():Boolean {
			return mIsError;
		}
		
	}
}
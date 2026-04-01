package psp.sap.view
{
	
	import flash.events.FocusEvent;
	
	import flexlib.controls.*;

	public class SAPPromptingTextInput extends PromptingTextInput { 
		
		
		override protected function handleFocusIn( event:FocusEvent ):void {									
			super.handleFocusIn(event);
			if (! this.editable){
				//undo the effects of super
				if ( super.promptFormat == "" )
				{
					super.text = super.prompt;
				} 
				else
				{
					super.htmlText = super.promptFormat.replace( /\[prompt\]/g, super.prompt );
				}
			}
		}
		
	
	
	}
		
	
}
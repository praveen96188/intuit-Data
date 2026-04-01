package psp.sap.view
{
	import mx.controls.TextArea;

	public class OFXTextArea extends TextArea
	{
		public function OFXTextArea()
		{
			super();
		}
				
		public function getLineIndexOfChar(charIndex:int):int{
        	return textField.getLineIndexOfChar(charIndex);
    	}
		
		public function scrollToSelection(selectionStart:int):void {
			var lineIndexOfChar:int = getLineIndexOfChar(selectionStart);
			this.verticalScrollPosition = lineIndexOfChar;
		}

        override public function set text(value:String):void {
            super.text = value;
            this.verticalScrollPosition = 0;
        }
    }
}
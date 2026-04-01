package psp.sap.view.controls
{
	import flash.display.Sprite;
	
	import mx.containers.FormItem;
	
	public class ErrorEatingFormItem extends FormItem
	{
		public function ErrorEatingFormItem()
		{
			super();			
		}
		
		override public function set focusPane(o:Sprite):void {			
			try {
				super.focusPane = o;
			}
			catch (e:Error) {
				//do nothing because this doesn't seem to cause any bad behavior
				//and i don't understand what it is doing one bit.
				//just that it is trying to remove a null child and it looks like flex just sucks 
			}			
		}

	}
}
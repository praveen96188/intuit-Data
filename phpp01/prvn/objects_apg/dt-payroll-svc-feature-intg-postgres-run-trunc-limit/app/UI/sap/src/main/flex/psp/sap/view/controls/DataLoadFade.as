package psp.sap.view.controls
{
	import mx.effects.Fade;
	import mx.managers.CursorManager;

	public class DataLoadFade extends Fade
	{
		private var mIsLoading:Boolean = false;
		
		public function DataLoadFade(target:Object=null)
		{
			super(target);
			duration = 500;
			alphaFrom = 0;
			alphaTo = 1;			
		}
		
		[Bindable]
		public function get isLoading():Boolean {
			return mIsLoading;
		}
		
		public function set isLoading(value:Boolean):void {

			mIsLoading = value;

			if (mIsLoading) {
				CursorManager.setBusyCursor();
				stop();				
			}
			else {
				CursorManager.removeBusyCursor();
				stop();
				play();
			}
		}
		
	}
}
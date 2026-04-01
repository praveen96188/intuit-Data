package psp.sap.view.controls
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.core.Application;
	import mx.managers.PopUpManager;
	
	import psp.sap.view.ProgressDisplayWindow;

	public class ProgressDisplayManager extends PopUpManager
	{
		protected static var mProgressDisplayWindow:ProgressDisplayWindow;
		protected static var mIsWindowVisible:Boolean = false;
		protected static var mProgressTimer:Timer;
		
		public function ProgressDisplayManager()
		{
			super();
		}
		
		protected static function getProgressDisplayWindow():ProgressDisplayWindow {
			if(mProgressDisplayWindow == null) mProgressDisplayWindow = new ProgressDisplayWindow();
			return mProgressDisplayWindow;
		}		
		
		protected static function destroyProgressWindow():void {
			mProgressDisplayWindow = null;
		}
		
		public static function get isWindowVisible():Boolean {
			return mIsWindowVisible;
		}
		
		//Call this to display the progress window
		//withAutoProgression: Adds an indeterminate progression to the window
		public static function displayProgressDisplayWindow(withAutoProgression:Boolean=true):void {
			if(!isWindowVisible)
			{
				PopUpManager.addPopUp(getProgressDisplayWindow(), Application.application as SAPApp, true, null);
				PopUpManager.centerPopUp(getProgressDisplayWindow());
				mIsWindowVisible = true;
				if(withAutoProgression) startIndeterminateProgressTimer();				
			}
		}
		
		//This starts the indeterminate progress timer task
		protected static function startIndeterminateProgressTimer():void {
			if(mProgressTimer == null) mProgressTimer = new Timer(1000);
			mProgressTimer.addEventListener(TimerEvent.TIMER, updateProgressVisual, false, 0, true);
			mProgressTimer.start();
		}
		
		//Creates an indeterminate status progression between page loads
		public static function updateProgressVisual(event:TimerEvent):void {
			
			//Make sure we aren't going passed the end or adding while it's not visible
			if(isWindowVisible && pagesComplete <= numberOfPages)
			{
				//Make sure we aren't passing a page number by our status progression
				if(pagesComplete + 0.05 != Math.ceil(pagesComplete))
					pagesComplete+= 0.05;
			}
				
			if(!isWindowVisible)
				mProgressTimer = null;
		}
		
		//This destroys the progress window and resets everything.
		public static function hideProgressDisplayWindow():void {
			if(isWindowVisible)
			{
				PopUpManager.removePopUp(getProgressDisplayWindow());
				mIsWindowVisible = false;
				mProgressTimer = null;
				resetWindowValues();
				destroyProgressWindow();
			}
		}
		
		public static function get numberOfPages():Number {
			return getProgressDisplayWindow().numberOfPages;
		}
		
		public static function set numberOfPages(value:Number):void {
			getProgressDisplayWindow().numberOfPages = value;
		}
		
		public static function get pagesComplete():Number {
			return getProgressDisplayWindow().pagesComplete;
		}
		
		public static function set pagesComplete(value:Number):void {
			getProgressDisplayWindow().pagesComplete = value;
		}
				
		//Resets the window values
		protected static function resetWindowValues():void {
			numberOfPages = 1;
			pagesComplete = 0;
		}
		
	}
}
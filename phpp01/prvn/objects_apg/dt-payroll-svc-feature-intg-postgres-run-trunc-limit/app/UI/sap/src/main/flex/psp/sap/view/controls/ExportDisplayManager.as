package psp.sap.view.controls
{
	import mx.core.Application;
	import mx.managers.PopUpManager;
	
	import psp.sap.view.ExportDataWindow;

	public class ExportDisplayManager extends PopUpManager
	{
		protected static var mExportDisplayWindow:ExportDataWindow;
		protected static var mIsWindowVisible:Boolean = false;
		
		public function ExportDisplayManager()
		{
			super();
		}
		
		protected static function getExportDisplayWindow():ExportDataWindow {
			if(mExportDisplayWindow == null) mExportDisplayWindow = new ExportDataWindow();
			return mExportDisplayWindow;
		}		
		
		public static function get isWindowVisible():Boolean {
			return mIsWindowVisible;
		}
		
		public static function displayExportDisplayWindow():void {
			if(!isWindowVisible)
			{
				PopUpManager.addPopUp(getExportDisplayWindow(), Application.application as SAPApp, true, null);
				PopUpManager.centerPopUp(getExportDisplayWindow());
				mIsWindowVisible = true;
			}
		}
		
		protected static function destroyExportDisplayWindow():void {
			mExportDisplayWindow = null;
		}
		
		public static function hideExportDisplayWindow():void {
			if(isWindowVisible)
			{
				PopUpManager.removePopUp(getExportDisplayWindow());
				mIsWindowVisible = false;
				resetWindowValues();
				destroyExportDisplayWindow();
			}
		}
		
		public static function get exportData():String {
			return getExportDisplayWindow().exportData;
		}
		
		public static function set exportData(value:String):void {
			getExportDisplayWindow().exportData = value;
		}
		
		protected static function resetWindowValues():void {
			exportData = "";
		}
		
	}
}
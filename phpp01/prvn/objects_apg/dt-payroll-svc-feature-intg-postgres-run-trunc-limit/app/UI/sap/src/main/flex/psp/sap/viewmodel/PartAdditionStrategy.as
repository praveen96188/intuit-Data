package psp.sap.viewmodel
{
	public class PartAdditionStrategy
	{
		
		//Do not automatically handle the lifecycle of the added part
		public static const MANUAL:String = "manual";
		
		//de/activate the added part when the parent is de/activated; refresh on refresh
		public static const COMPOSITE:String = "composite";
		
		//deactivate the added part when the parent is deactivated; refresh on refresh;
		public static const LAZY_COMPOSITE:String = "lazyComposite";
				
		//deactivate the added part when any other SINGLE part is activated; refresh on refresh if active
		public static const SINGLE:String = "single";
		

	}
}
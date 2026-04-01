package psp.sap.viewmodel.events
{
	public class CompanySearchViewModelEvent extends ViewModelEvent
	{
		public function CompanySearchViewModelEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		static public const SEARCH_INITIATED:String = "viewModelSearchInitiatedEvent";
		static public const SEARCH_COMPLETED:String = "viewModelSearchCompletedEvent";
		static public const SEARCH_FAULTED:String 	 = "viewModelSearchFaultedEvent";
		
		public static function createSearchInitiatedEvent():CompanySearchViewModelEvent {
			return new CompanySearchViewModelEvent(SEARCH_INITIATED);
		}
		
		public static function createSearchCompletedEvent():CompanySearchViewModelEvent {
			return new CompanySearchViewModelEvent(SEARCH_COMPLETED);
		}
		
		public static function createSearchFaultedEvent(error:Object):CompanySearchViewModelEvent {
			var e:CompanySearchViewModelEvent = new CompanySearchViewModelEvent(SEARCH_FAULTED);
			return e;
		}
		
		
	}
}
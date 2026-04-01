package psp.sap.application
{
	import flash.events.EventDispatcher;
	
	/**
	 * NOTE: this class only extends EventDispatcher to suppress annoying warnings
	 */
	public class SAPConfigurationValues extends EventDispatcher
	{
		public function SAPConfigurationValues()
		{
		}
		
		/**
		 * The default number of records per page on a queue page
		 */ 
		public const DEFAULT_GRID_PAGE_SIZE:int = 25;
		
		/**
		 * The number of milliseconds in a day
		 */ 
		public function get millisecondsPerDay():int {
			return 1000 * 60 * 60 * 24;
		}				
		
		/**
		 * The default format for use in audit history stables that
		 * dispaly a 'changed on' date.
		 */
		[Bindable("propertyChange")]		 
		public function get defaultAuditDateTimeFormat():String {
			return dateTimeFormatDateOverTime;
		}
		
		/**
		 * Long date format:  MMMM DD, YYYY
		 * 
		 * i.e. January 05, 2008
		 */
		[Bindable("propertyChange")] 
		public function get dateFormatLong():String {
			return "MMMM DD, YYYY";
		}
		
		/**
		 * SAP standard date format: MMM DD, YYYY L:NN A
		 * 
		 * i.e. Jan 05, 2008 1:00 PM
		 */
		[Bindable("propertyChange")]
		public function get dateTimeFormatMedium():String {
			return "MMM DD, YYYY L:NN A";
		}

		/**
		 * Short date format: MMM DD, YYYY
		 * 
		 * i.e. Jan 05, 2008
		 */		
		[Bindable("propertyChange")] 
		public function get dateFormatMedium():String {
			return "MMM DD, YYYY";
		}

		/**
		 * Short date format: MM/DD/YYYY
		 * 
		 * i.e. 01/05/2008
		 */
		[Bindable("propertyChange")]		
		public function get dateFormatShort():String {
			return "MM/DD/YYYY";
		}
		
		/**
		 * Short date format: MM/DD/YYYY LL:NN A
		 * 
		 * i.e. 01/05/2008 2:00 PM
		 */
		[Bindable("propertyChange")]		
		public function get dateTimeFormatShort():String {
			return "MM/DD/YYYY LL:NN A";
		}
		
		
		
		/**
		 * SSN Format: ###-##-####
		 * 
		 * i.e. 310-24-1924
		 */
		[Bindable("propertyChange")]		
		public function get ssnFormat():String {
			return "###-##-####";
		}
		
		
		
		/**
		 * The calendar date placed over the time: "MMM DD, YYYY&#13;LL:NN A"
		 * 
		 * i.e. Jan 05, 2008
		 * 		1:00 PM
		 * 
		 */
		[Bindable("propertyChange")]		
		public function get dateTimeFormatDateOverTime():String {
			return "MMM DD, YYYY\nLL:NN A";
		}
		
		/**
		 * The date format expected for usage in a PSE Core DateDTO:
		 * YYYY-MM-DD
		 */
		public function get dateTimeFormatCoreDateDTO():String {
			return "YYYY-MM-DD";
		}
		
		/**
		 * The maximum allowed currency input.  No transaction, fee, reversal, etc.
		 * may have an amount input greater than this system constant which is
		 * $999,999.99
		 */
		[Bindable("propertyChange")]
		public function get maxAllowedCurrencyValue():Object {
			return null;
		}
		
		[Bindable("propertyChange")]
		public function get minAllowedCurrencyValue():Number {
			return 0.01;
		}
		
		/**
		 * The calendar with time: "LL:NN A"
		 * 
		 * 
		 * 		1:00 PM
		 * 
		 */
		[Bindable("propertyChange")]		
		public function get timeFormat():String {
			return "LL:NN A";
		}
		
		/**
		 * The calendar with time: "LL:NN A"
		 * 
		 * 
		 * 		1:00 PM
		 * 
		 */
		[Bindable("propertyChange")]		
		public function get timeHourMinSecFormat():String {
			return "LL:NN:SS A";
		}		
		
		
		/**
		 * The maximum allowed value for number of attempts (to verify bank account), 
		 * the number of days within which verification needs to be done,
		 * ACH Waiting period,
		 * Number of days within which the direct deposit limit per company or per employee
		 * direct deposit limit is described against*/
		[Bindable("propertyChange")]
		public function get maxNum():Number {
			return 99;
		}
		
		[Bindable("propertyChange")]
		public function get minNum():Number {
			return 1;
		}
		
		[Bindable("propertyChange")]		 
		public function get specialNumberForDefault():Number{
			return -1;
		}
		/**
		 * The default message to display upon a successful save.
		 */
		[Bindable("propertyChange")]
		public function get defaultSaveSucceededMsg():String {
			return "The changes have been saved";
		}
	}
}

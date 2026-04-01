package psp.sap.model
{
	import mx.formatters.DateFormatter;
	
	import psp.sap.application.SAP;
	
	public class DateRangeEnum
	{
		public static const CUSTOM:DateRangeEnum = new DateRangeEnum("Custom", 0, 0, true);
		public static const TODAY:DateRangeEnum = new DateRangeEnum("Today", 0, 0);
		public static const YESTERDAY:DateRangeEnum = new DateRangeEnum("Yesterday", 1, 1);
		public static const LAST_2_DAYS:DateRangeEnum = new DateRangeEnum("Last 2 Days", 1, 0);
        public static const LAST_7_DAYS:DateRangeEnum = new DateRangeEnum("Last 7 Days", 7, 0);
		public static const LAST_30_DAYS:DateRangeEnum = new DateRangeEnum("Last 30 Days", 30, 0);
		public static const LAST_3_MONTHS:DateRangeEnum = new DateRangeEnum("Last 3 Months", 90, 0);
		public static const LAST_YEAR:DateRangeEnum = new DateRangeEnum("Last 12 Months", 365, 0);
		
		public static const list:Array = [CUSTOM, TODAY, YESTERDAY, LAST_2_DAYS, LAST_7_DAYS, LAST_30_DAYS, LAST_3_MONTHS, LAST_YEAR];	
		
		private var mLabel:String;
		private var mStartDateOffset:int;
		private var mEndDateOffset:int;
		private var mIsCustom:Boolean;
		private var mFormatter:DateFormatter;
		
		/**
		 * NOTE: offsets are both subtracted from today
		 */ 
		public function DateRangeEnum(label:String, startDateOffset:int = 0, endDateOffset:int = 0, isCustom:Boolean = false)
		{
			mFormatter = new DateFormatter();
			mFormatter.formatString = SAP.instance.configuration.dateFormatShort;
			
			mLabel = label;
			mStartDateOffset = startDateOffset;
			mEndDateOffset = endDateOffset;												
			mIsCustom = isCustom;
		}				
		
		public function get label():String {
			return mLabel;
		}
		
		public function get startDate():String {
			var date:Date = SAP.instance.PSPDate;
			date.time -= (SAP.instance.configuration.millisecondsPerDay * mStartDateOffset);
			return mFormatter.format(date);
		}
		
		public function get endDate():String {
			var date:Date = SAP.instance.PSPDate; 
			date.time -= (SAP.instance.configuration.millisecondsPerDay * mEndDateOffset);
			return mFormatter.format(date);
		}
		
		public function get isCustom():Boolean {
			return mIsCustom;
		}
		
		public function toString():String {
			return label;
		}		
		
		public static function findEnumFromDateRange(startDate:String, endDate:String):DateRangeEnum {
			for each (var enum:DateRangeEnum in list) {
				if (!enum.isCustom && enum.startDate == startDate && enum.endDate == endDate)
					return enum;	
			}
			// default
			return DateRangeEnum.CUSTOM;
		}

	}
}

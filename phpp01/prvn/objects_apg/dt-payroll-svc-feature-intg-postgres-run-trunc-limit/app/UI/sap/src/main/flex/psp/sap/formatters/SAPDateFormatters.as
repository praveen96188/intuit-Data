package psp.sap.formatters
{
	import mx.formatters.DateFormatter;
	
	import psp.sap.application.SAP;
	
	/**
	 * Static instances of the various formats we use matching to the values in SAP.instance.configuration
	 */	
	public class SAPDateFormatters
	{
		
		private var defaultAuditDateTimeFormat:DateFormatter;
		private var dateFormatLong:DateFormatter;			
		private var dateTimeFormatMedium:DateFormatter;
		private var dateFormatMedium:DateFormatter;
		private var dateFormatShort:DateFormatter;
		private var dateTimeFormatShort:DateFormatter;		
		private var dateTimeFormatDateOverTime:DateFormatter;
		private var dateTimeFormatCoreDateDTO:DateFormatter;
		private var timeFormat:DateFormatter;
		private var timeHourMinSecFormat:DateFormatter;
		
		public function SAPDateFormatters()
		{
			if (instance != null) {
				throw new Error("SAPDateFormatters is singleton");
			}
			defaultAuditDateTimeFormat = new DateFormatter();
			defaultAuditDateTimeFormat.formatString = SAP.instance.configuration.defaultAuditDateTimeFormat;
			dateFormatLong = new DateFormatter();
			dateFormatLong.formatString = SAP.instance.configuration.dateFormatLong;
			dateTimeFormatMedium = new DateFormatter();
			dateTimeFormatMedium.formatString = SAP.instance.configuration.dateTimeFormatMedium;
			dateFormatMedium = new DateFormatter();
			dateFormatMedium.formatString = SAP.instance.configuration.dateFormatMedium;
			dateFormatShort = new DateFormatter();
			dateFormatShort.formatString = SAP.instance.configuration.dateFormatShort;
			dateTimeFormatShort = new DateFormatter();
			dateTimeFormatShort.formatString = SAP.instance.configuration.dateTimeFormatShort;
			dateTimeFormatDateOverTime = new DateFormatter();
			dateTimeFormatDateOverTime.formatString = SAP.instance.configuration.dateTimeFormatDateOverTime;
			dateTimeFormatCoreDateDTO = new DateFormatter();
			dateTimeFormatCoreDateDTO.formatString = SAP.instance.configuration.dateTimeFormatCoreDateDTO;
			timeFormat = new DateFormatter();
			timeFormat.formatString = SAP.instance.configuration.timeFormat;
			timeHourMinSecFormat = new DateFormatter();
			timeHourMinSecFormat.formatString = SAP.instance.configuration.timeHourMinSecFormat;

		}

		private static var instance:SAPDateFormatters;		
		{
			instance = new SAPDateFormatters();
		}	
		
		public static function get defaultAuditDateTimeFormat():DateFormatter {
			return instance.defaultAuditDateTimeFormat;
		}
		public static function get dateFormatLong():DateFormatter {
			return instance.dateFormatLong;
		}
		public static function get dateTimeFormatMedium():DateFormatter {
			return instance.dateTimeFormatMedium;
		}
		public static function get dateFormatMedium():DateFormatter {
			return instance.dateFormatMedium;
		}
		public static function get dateFormatShort():DateFormatter {
			return instance.dateFormatShort;
		}
		public static function get dateTimeFormatShort():DateFormatter {
			return instance.dateTimeFormatShort;
		}
		public static function get dateTimeFormatDateOverTime():DateFormatter {
			return instance.dateTimeFormatDateOverTime;
		}
		public static function get dateTimeFormatCoreDateDTO():DateFormatter {
			return instance.dateTimeFormatCoreDateDTO;
		}
		public static function get timeFormat():DateFormatter {
			return instance.timeFormat;
		}
		public static function get timeHourMinSecFormat():DateFormatter {
			return instance.timeHourMinSecFormat;
		}

		
		
	}
	
}

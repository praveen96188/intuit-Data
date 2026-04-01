package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPSPSystemInformation")]	
	public class PSPSystemInformation
	{
		/**
		 * The build number of the deployed SAP war.
		 */
		public var buildNumber:String;
		
		/**
		 * The PSP date as reported by the server core.
		 */
		public var pspDate:String;
		
		 
		/**
		 * The database schema version.
		 */
		public var schemaVersion:String;

		public function toString():String {
			return "PSP Date: " + pspDate
					+ "\r\nSchema Version: " + schemaVersion
                    + "\r\nBuild Number:" + buildNumber;
		} 
	}
}
package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults")]
	public class SearchResults
	{
		// this is the total number of available records
		public var totalRecords:int;

        // this is the total amounts from all records
		public var totalAmount:Number;

		// records returned by the query
    	public var returnsList:ArrayCollection;

	}
}
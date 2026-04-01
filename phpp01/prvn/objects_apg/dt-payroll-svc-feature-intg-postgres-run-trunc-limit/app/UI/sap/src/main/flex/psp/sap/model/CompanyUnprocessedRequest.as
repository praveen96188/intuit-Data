/**
 * User: dmehta2
 * Date: 06/07/2023
 * Time: 3:30 PM
 */
package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyUnprocessedRequest")]
	public class CompanyUnprocessedRequest
	{		
		public var companyLegalName:String;
    	public var requestCount:int;
	}
}
package psp.sap.model
{
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyStatusSearchResult")]
	public class CompanyStatusSearchResult extends CompanySearchResult
	{		
		public var numberOfStrikes:int;
		public var balanceDue:Number;

        // exposed for sorting
        [Transient]
		public function get sourceSystemCd():String {
            return key.sourceSystemCd;
        }

        [Transient]
		public function get companyId():String {
            return key.companyId;
        }
	}
}
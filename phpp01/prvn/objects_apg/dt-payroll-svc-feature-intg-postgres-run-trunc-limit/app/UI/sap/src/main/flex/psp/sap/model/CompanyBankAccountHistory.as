package psp.sap.model
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyBankAccountHistory")]
	public class CompanyBankAccountHistory extends CompanyBankAccount
	{	
		private var mSort:Sort = new Sort();
		[ArrayElementType("psp.sap.model.PropertyAudit")]
		private var mPropertyAudit:ArrayCollection
			
		public var statusEffectiveDate:Date;
		
		public function set propertyAudit(value:ArrayCollection):void {			
			mSort.fields = [new SortField("auditDateTime", true, true)];		
			value.sort = mSort;
			value.refresh();
			mPropertyAudit = value; 
		}
		
		public function get propertyAudit():ArrayCollection {
			return mPropertyAudit;
		}
	}
}
package psp.sap.model
{
    import mx.collections.ArrayCollection;

    import mx.collections.Sort;

    import mx.collections.SortField;

    import psp.sap.application.collections.LookupCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanySearchResult")]
	//much of this is directly in Company
	//this class be extended for extra columns
	//and refactored if there are extras causing too much server work or net traffic
	public class CompanySearchResult
	{						
		public var legalName:String;
		public var key:CompanyKey;
		public var fein:String;
		public var PSID:String;
		[ArrayElementType ("psp.sap.model.CompanyServiceStatus")]
		public var services:ArrayCollection;
        [ArrayElementType ("psp.sap.model.EntitlementSearchResult")]
        public var entitlements:ArrayCollection;

        [Bindable("propertyChange")]
        [ArrayElementType ("psp.sap.model.StatusEntitlements")]
        public function groupEntitlementsByStatus():LookupCollection {
            var groups:LookupCollection = new LookupCollection(StatusEntitlements, null, "status");

            for each (var entitlement:EntitlementSearchResult in entitlements){
                if (! (groups.getItemByKey(entitlement.entitlementUnitStatus))) {
                    groups.addItem(new StatusEntitlements(entitlement.entitlementUnitStatus));
                }
                StatusEntitlements(groups.getItemByKey(entitlement.entitlementUnitStatus)).entitlements.addItem(entitlement);
            }

            var sort:Sort = new Sort();
            sort.fields = [new SortField("status")];
            groups.sort = sort;
            groups.refresh();
            return groups;
        }

	}
}
package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyLedgerAccount")]
	public class CompanyLedgerAccount
	{
        public var requiresQuarterLaw:Boolean = false;
	    public var ledgerAccountCode:String;
	    public var balance:Number; 
	    public var credit:Boolean;
	    public var name:String;
	    public var description:String;	    
        public var creditAddsToBalance:Boolean = true;

	    [ArrayElementType("psp.sap.model.ActionEvent")]
	    public var actionCollection:ArrayCollection;
	    
	    [Transient]
	    [Bindable ("propertyChange")]
	    public function get accountType():String {
	    	return credit ? "Credit" : "Debit";
	    } 	    	   	    
	}
}
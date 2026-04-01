package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTransactionType")]
	public class TransactionType
	{
        public var transactionTypeCd: String;
        public var name: String;
        public var description: String;
        public var transactionCategory: String;
        public var associationType: String;
        public var feeInd: Boolean;
        public var NACHABatchType: String;
    }
}
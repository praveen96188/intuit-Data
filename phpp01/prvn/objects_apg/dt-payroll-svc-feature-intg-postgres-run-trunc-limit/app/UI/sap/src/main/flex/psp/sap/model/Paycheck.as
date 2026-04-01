package psp.sap.model
{
    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaycheck")]
	public class Paycheck
	{
        public var sourcePaycheckId:String;
	    public var payPeriodBeginDate:Date;
	    public var payPeriodEndDate:Date;
	    public var paycheckDate:Date;
	    public var voidedDate:Date;
	    public var voidedAfterOffload:Boolean;
	    public var employeeName:String;
        public var sourceEmployeeName:String;
	    public var netPaycheckAmount:Number;
	    public var status:String;
        public var paycheckGseq:String;

        public var paycheck401k:Paycheck401k;


        [Transient]
		public function get actionCollection():ArrayCollection {
            var actionCollection:ArrayCollection = new ArrayCollection();
            actionCollection.addItem(new ActionEvent(ActionEventCode.VIEW_PAYCHECK_LINE_ITEMS, "View Line Items"));
            return actionCollection;
		}

   }
}

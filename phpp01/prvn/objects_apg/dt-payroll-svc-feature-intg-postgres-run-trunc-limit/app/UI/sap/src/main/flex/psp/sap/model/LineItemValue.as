package psp.sap.model
{

	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLineItemValue")]
    /* Represents Values paycheck Item per payroll run */
	public class LineItemValue
	{
        public var itemId:String;
        public var itemName:String;
        public var sourceDescription:String;
        public var amount:Number;
        public var totalWages:Number;
        public var taxableWages:Number;
        public var hoursWorked:Number;
        public var taxFormLine:String;

        public var irs:Boolean;
    }
}

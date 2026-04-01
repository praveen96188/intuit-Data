package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollEmployeeTransaction")]
	public class PayrollEmployeeTransaction extends PayrollTransaction
	{
        public var employeeName:String;
        public var employeeBankAccountNumber:String;
        public var employeeBankRoutingNumber:String;
        public var voidedAfterOffload:Boolean;
    	public var voidedDate:Date;
        public var emailId:String;
        public var hasInvalidEmail:Boolean;
        public var employerDDDebitTxnNumber:String;
        public var jpmcTraceId:String;

        private var mSelected:Boolean = false;
        private var mEnabled:Boolean = true;
        
        // This is used for canceled transactions
        [Transient]        
        public function get selected():Boolean {
			return mSelected;			        
        }
        
        public function set selected(value: Boolean):void {
			mSelected = value;			        
        }   
        
        //Using this to enable/disable checkboxes in grid
        [Transient]
        public function get enabled():Boolean {
        	return mEnabled;
        }              
        
        public function set enabled(value:Boolean):void {
        	mEnabled = value;
        }
	}
}
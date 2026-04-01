package psp.sap.model
{
	import mx.utils.StringUtil;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBillingTransaction")]
	public class BillingTransaction
	{
		public var financialTxnId:String;
	    public var financialAmount:Number;
	    public var financialTxnType:String;
	    public var financialReturnAmount:Number;
		   
	    public var salesTaxTxnId:String;
	    public var salesTaxAmount:Number;
		public var salesTaxReturnAmount:Number;	    	    	    	    
		
		private var mFinancialReturnAmountString:String;						
		[Transient]
		public function get financialReturnAmountString():String {
			return mFinancialReturnAmountString;
		}
		
		public function set financialReturnAmountString(value:String):void {			
			mFinancialReturnAmountString = value;			
		}
		
		[Transient]
		public function get financialReturnAmountStringValue():Number {
			if (mFinancialReturnAmountString == null || StringUtil.trim(mFinancialReturnAmountString).length == 0)
				return 0;
			else
				return parseFloat(mFinancialReturnAmountString);
		}				
		
		private var mSalesTaxReturnAmountString:String;
		[Transient]
		public function get salesTaxReturnAmountString():String {
			return mSalesTaxReturnAmountString;
		}
	
		public function set salesTaxReturnAmountString(value:String):void {
			mSalesTaxReturnAmountString = value;
		}
		
		[Transient]
		public function get salesTaxReturnAmountStringValue():Number {
			if (mSalesTaxReturnAmountString == null || StringUtil.trim(mSalesTaxReturnAmountString).length == 0)
				return 0;
			else
				return parseFloat(mSalesTaxReturnAmountString);
		}
		
		[Transient]
		[Bindable ("propertyChange")]
		public function get hasFinancialTxn():Boolean {
			return financialTxnId != null;
		}				
		
		[Transient]
		[Bindable ("propertyChange")]
		public function get hasSalesTaxTxn():Boolean {
			return salesTaxTxnId != null;
		}
		
		public function readValues():void {
			financialReturnAmountString = financialReturnAmount.toFixed(2);
			salesTaxReturnAmountString = salesTaxReturnAmount.toFixed(2);			
		}				
			
		public function writeValues():void {
			financialReturnAmount = financialReturnAmountStringValue;
			salesTaxReturnAmount = salesTaxReturnAmountStringValue;					
		}
	}
}
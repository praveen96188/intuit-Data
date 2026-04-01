package psp.sap.model
{
	import mx.collections.ArrayCollection;
	
	import psp.sap.formatters.SAPCurrencyFormatters;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawTransactions")]
	public class LawTransactions
	{
        public var agency:Agency;
		public var law:LawItem;
		public var currentTaxesSum:Number;
		[ArrayElementType ("psp.sap.model.TaxTransaction")]		
		public var taxTransactions:ArrayCollection;
		
		[Transient]
		public function label():String {
			return agency.agencyAbbrev + " " + this.law.name + " " + SAPCurrencyFormatters.currencyFormatterNoSymbol.format(this.currentTaxesSum);
		}
	}
}
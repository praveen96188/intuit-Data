package psp.sap.formatters
{
	import mx.formatters.CurrencyFormatter;
    import mx.formatters.NumberBaseRoundType;

	public class SAPCurrencyFormatter extends CurrencyFormatter
	{
		private static var mCurrencyFormatter:SAPCurrencyFormatter = new SAPCurrencyFormatter();

        public var hideZero:Boolean = false;
		
		public function SAPCurrencyFormatter()
		{
			super();
			
			/* Defaults */
			this.precision = 2;
			this.currencySymbol = "$";
			this.decimalSeparatorFrom=".";
			this.decimalSeparatorTo = ".";
			this.useNegativeSign = true;
			this.useThousandsSeparator = true;
			this.alignSymbol = "left";
            this.rounding = NumberBaseRoundType.NEAREST;
					
		}
		
		[Bindable ("propertyChange")]
		public static function get currencyFormatter():SAPCurrencyFormatter {
			return mCurrencyFormatter;
		}

        override public function format(value:Object):String {
            var s:String = super.format(value);
            var s2:String = s.replace(/[^0-9]/g, "");
            if (hideZero && s2.match(/^0*$/)) {
                return "";
            }
            return s;
        }
    }
}
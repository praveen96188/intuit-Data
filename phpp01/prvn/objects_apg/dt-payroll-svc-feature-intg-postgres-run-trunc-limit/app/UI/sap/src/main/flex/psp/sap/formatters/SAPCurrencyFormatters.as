package psp.sap.formatters
{
	import mx.formatters.CurrencyFormatter;
    import mx.formatters.NumberBaseRoundType;

	public class SAPCurrencyFormatters
	{
		private static var mDefaultFormatter:SAPCurrencyFormatter = new SAPCurrencyFormatter();
		private static var mWholeDollarsFormatter:CurrencyFormatter;
		private static var mCurrencyFormatterNoSymbol:CurrencyFormatter;
        private static var mCurrencyFormatterNoSymbolNoComma:CurrencyFormatter;
        private static var mCurrencyFormatterHideZero:SAPCurrencyFormatter;
		
		{
			mWholeDollarsFormatter = new CurrencyFormatter();
			mWholeDollarsFormatter.precision = 0;
			mWholeDollarsFormatter.currencySymbol = "$";
			mWholeDollarsFormatter.decimalSeparatorFrom=".";
			mWholeDollarsFormatter.decimalSeparatorTo = ".";
			mWholeDollarsFormatter.useNegativeSign = true;
			mWholeDollarsFormatter.useThousandsSeparator = true;
			mWholeDollarsFormatter.alignSymbol = "left";
			mWholeDollarsFormatter.rounding = NumberBaseRoundType.NEAREST;
			
			mCurrencyFormatterNoSymbol = new CurrencyFormatter();
			mCurrencyFormatterNoSymbol.precision = 2;
			mCurrencyFormatterNoSymbol.currencySymbol = "";
			mCurrencyFormatterNoSymbol.decimalSeparatorFrom=".";
			mCurrencyFormatterNoSymbol.decimalSeparatorTo = ".";
			mCurrencyFormatterNoSymbol.useNegativeSign = true;
			mCurrencyFormatterNoSymbol.useThousandsSeparator = true;
			mCurrencyFormatterNoSymbol.alignSymbol = "left";
            mCurrencyFormatterNoSymbol.rounding = NumberBaseRoundType.NEAREST;

            mCurrencyFormatterNoSymbolNoComma = new CurrencyFormatter();
			mCurrencyFormatterNoSymbolNoComma.precision = 2;
			mCurrencyFormatterNoSymbolNoComma.currencySymbol = "";
			mCurrencyFormatterNoSymbolNoComma.decimalSeparatorFrom=".";
			mCurrencyFormatterNoSymbolNoComma.decimalSeparatorTo = ".";
			mCurrencyFormatterNoSymbolNoComma.useNegativeSign = true;
			mCurrencyFormatterNoSymbolNoComma.useThousandsSeparator = false;
			mCurrencyFormatterNoSymbolNoComma.alignSymbol = "left";
            mCurrencyFormatterNoSymbolNoComma.rounding = NumberBaseRoundType.NEAREST;

            mCurrencyFormatterHideZero = new SAPCurrencyFormatter();
            mCurrencyFormatterHideZero.hideZero = true;
		}
		
		public static function get defaultFormatter():SAPCurrencyFormatter {
			return mDefaultFormatter;
		}

        public static function get defaultFormatterHideZero():SAPCurrencyFormatter {
            return mCurrencyFormatterHideZero;
        }
		
		public static function get wholeDollarFormatter():CurrencyFormatter {
			return mWholeDollarsFormatter;
		}
		
		[Bindable ("propertyChange")]
		public static function get currencyFormatterNoSymbol():CurrencyFormatter {
			return mCurrencyFormatterNoSymbol;
		}

        public static function get currencyFormatterNoSymbolNoComma():CurrencyFormatter {
            return mCurrencyFormatterNoSymbolNoComma;
        }
    }
}
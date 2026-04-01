/**
 * User: dweinberg
 * Date: 8/22/12
 * Time: 5:06 PM
 */
package psp.sap.formatters {
    import mx.formatters.NumberFormatter;
    import mx.utils.StringUtil;

    public class SAPRateFormatter {

        public static function formatRateAsPercentage(value:Number):String {
            return StringUtil.substitute("{0}%", format(value * 100));
        }

        public static function formatPercentageAsInput(value:Number):String {
            return format(value);
        }

        public static function formatPercentage(value:Number):String {
            return StringUtil.substitute("{0}%", format(value));
        }

        private static function format(value:Number):String {
            if (isNaN(value)) {
                return "";
            }

            var numberFormatter:NumberFormatter = new NumberFormatter();
            numberFormatter.precision = 10;
            numberFormatter.useThousandsSeparator = false;
            numberFormatter.rounding = "nearest";
            var rate:String = numberFormatter.format(value);

            var lastIndex:int = -1;
            for (var i:int = rate.length - 1; i >= 2; i--) {
                var lastDigit:String = rate.charAt(i);
                var lastDigitM1:String = rate.charAt(i - 1);
                var lastDigitM2:String = rate.charAt(i - 2);
                if (lastDigit == "0" && lastDigitM1 != "." && lastDigitM2 != ".") {
                    lastIndex = i;
                }
                if (lastDigit != "0") {
                    break;
                }
            }
            if (lastIndex > 0) {
                rate = rate.slice(0, lastIndex);
            }

            return rate;
        }
    }
}

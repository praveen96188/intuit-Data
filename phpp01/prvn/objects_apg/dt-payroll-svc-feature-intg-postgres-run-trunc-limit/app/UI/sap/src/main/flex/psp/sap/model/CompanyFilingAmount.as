/**
 * User: dweinberg
 * Date: 8/22/12
 * Time: 1:27 PM
 */
package psp.sap.model {
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.formatters.SAPRateFormatter;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyFilingAmount")]
    public class CompanyFilingAmount {
        public var name:String;
        public var value:String;
        public var previousQuarterValue:String;
        public var isRate:Boolean;
        public var hasCurrentValue:Boolean;

        public var newValue:String;

        public function formattedValue():String {
            if (!hasCurrentValue) {
                return "";
            } else if (isRate) {
                return SAPRateFormatter.formatPercentage(parseFloat(value));
            } else {
                return SAPCurrencyFormatters.defaultFormatter.format(value);
            }
        }
    }
}

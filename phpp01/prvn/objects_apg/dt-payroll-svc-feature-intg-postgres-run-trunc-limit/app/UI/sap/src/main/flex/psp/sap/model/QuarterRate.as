/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 5:03 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarterRate")]
    public class QuarterRate {

        public var quarter:Quarter;
        public var currentPercentage:Number;
        public var newPercentage:Number;

        public var newPercentageText:String;

        public var yearText:String;
        public var quarterText:String;

        public var canDelete:Boolean;

        public function get quarterValue():Quarter {
            return new Quarter(parseInt(yearText), parseInt(quarterText.charAt(1)));
        }

        public function get newPercentageTextValue():Number {
            return parseFloat(newPercentageText);
        }

        public function synchronizeTransients():void {
            quarter = quarterValue;
            newPercentage = newPercentageTextValue;
        }
    }
}

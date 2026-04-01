/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:23 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawRate")]
    public class LawRate {
        public function LawRate() {
        }

        public var law:LawItem;
        public var hasCurrentRate:Boolean;
        public var currentPercentage:Number;
        public var newPercentage:Number;

        public var minPercentage:Number;
        public var maxPercentage:Number;
        public var maxPrecision:Number;

        public var hasValuesInsteadOfRanges:Boolean;

        [Transient]
        public function get lawName():String {
            return law.name;
        }

        public var newPercentageText:String;

        public function get newPercentageTextValue():Number {
            return parseFloat(newPercentageText);
        }

        public function synchronizeTransients():void {
            newPercentage = newPercentageTextValue;
        }
    }
}

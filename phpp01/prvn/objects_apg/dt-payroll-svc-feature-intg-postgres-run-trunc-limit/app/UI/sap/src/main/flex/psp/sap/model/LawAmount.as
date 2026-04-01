/**
 * Created by IntelliJ IDEA.
 * User: RahulM974
 * Date: 1/19/12
 * Time: 12:43 PM
 */
package psp.sap.model {
    import mx.formatters.NumberBaseRoundType;
    import mx.formatters.NumberFormatter;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawAmount")]
    public class LawAmount {
        public var law:String;
        public var lawId:String;
        public var amount:Number;
        private var mNumberFormatter:NumberFormatter = new NumberFormatter();

        [Transient] public var splitAmount:Number=0;
        [Transient]
        public var liabilityAdjustmentAmount:String = "";
        [Transient]
        public var newAmount:String = "";

        public function initializeNumberFormat():void {
            mNumberFormatter.precision = 2;
            mNumberFormatter.useThousandsSeparator = false;
            mNumberFormatter.rounding = NumberBaseRoundType.NEAREST;
        }

        public function calculateNewAmount():void {
            var split:Number = isNaN(splitAmount) ? 0 : splitAmount;
            newAmount = mNumberFormatter.format(amount + split);
            if(liabilityAdjustmentAmount != null && liabilityAdjustmentAmount.length > 0) {
                newAmount = mNumberFormatter.format(parseFloat(mNumberFormatter.format(newAmount)) + parseFloat(mNumberFormatter.format(liabilityAdjustmentAmount)));
            }
        }

         public function calculateLiability():void {
            var split:Number = isNaN(splitAmount) ? 0 : splitAmount;
            var newAmountNumber:Number = 0;
            if(newAmount != null && newAmount.length > 0) {
                newAmountNumber = parseFloat(mNumberFormatter.format(newAmount));
            }
            liabilityAdjustmentAmount = mNumberFormatter.format(newAmountNumber - amount - split);
        }
    }
}

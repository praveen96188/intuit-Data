/**
 * User: dweinberg
 * Date: 12/5/12
 * Time: 4:28 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTemplateQuarterAmount")]
    public class TemplateQuarterAmount {
        public var paymentTemplateCd:String;
        public var quarter:Quarter;
        public var amount:Number;
        public var isAnnual:Boolean;

        [Transient]
        public function get isCredit():Boolean {
            return amount > 0;
        }

        [Transient]
        public function get creditDebit():String {
            if (amount == 0) {
                return "";
            }
            return isCredit ? "Credit" : "Debit";
        }

        //convenience for sorting
        [Transient]
        public function get quarterNumber():int {
            return quarter.quarter;
        }

        [Transient]
        public function get yearNumber():int {
            return quarter.year;
        }

        public function get label():String {
            if (isAnnual) {
                return paymentTemplateCd + " " + quarter.year;
            } else {
                return paymentTemplateCd + " " + quarter.year + " " + "Q" + quarter.quarter;
            }
        }
    }
}

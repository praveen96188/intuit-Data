/**
 * User: dweinberg
 * Date: 2/23/12
 * Time: 6:08 PM
 */
package psp.sap.model {
    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxRepaymentOptions")]
    public class TaxRepaymentOptions {


        public function TaxRepaymentOptions(newPaymentMethod:String, updateAll:Boolean, recreate:Boolean) {
            this.newPaymentMethod = newPaymentMethod;
            this.updateAll = updateAll;
            this.recreate = recreate;
        }

        public var newPaymentMethod:String;
        public var updateAll:Boolean;
        public var recreate:Boolean;
    }
}

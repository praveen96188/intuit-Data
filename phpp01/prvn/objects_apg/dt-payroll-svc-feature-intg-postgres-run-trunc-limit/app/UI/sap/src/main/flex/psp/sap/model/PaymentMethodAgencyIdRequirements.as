/**
 * User: dweinberg
 * Date: 3/4/13
 * Time: 4:41 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentMethodAgencyIdRequirements")]
    public class PaymentMethodAgencyIdRequirements {
        public function PaymentMethodAgencyIdRequirements() {
        }

        public var paymentMethod:String;
        [ArrayElementType("psp.sap.model.AgencyIdRequirement")]
        public var requirements:ArrayCollection;
    }
}

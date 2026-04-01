/**
 * User: dweinberg
 * Date: 3/6/13
 * Time: 4:53 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDepositFrequencyCollection")]
    public class DepositFrequencyCollection {
        public function DepositFrequencyCollection() {
        }

        [ArrayElementType("psp.sap.model.DepositFrequency")]
        public var depositFrequencies:ArrayCollection;
        [ArrayElementType("String")]
        public var availableFrequencies:ArrayCollection;
        public var defaultDepositFrequency:String;

    }
}

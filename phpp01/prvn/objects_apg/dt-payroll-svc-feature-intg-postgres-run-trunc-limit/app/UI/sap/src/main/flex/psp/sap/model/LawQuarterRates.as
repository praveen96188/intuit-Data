/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 5:01 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawQuarterRates")]
    public class LawQuarterRates {
        public var law:LawItem;
        [ArrayElementType("psp.sap.model.QuarterRate")]
        public var rates:ArrayCollection;

    }
}

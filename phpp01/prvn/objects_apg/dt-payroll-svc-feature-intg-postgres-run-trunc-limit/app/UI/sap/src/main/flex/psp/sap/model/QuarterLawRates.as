/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:22 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarterLawRates")]
    public class QuarterLawRates {
        public var quarter:Quarter;
        public var underBlackout:Boolean;
        [ArrayElementType("psp.sap.model.LawRate")]
        public var lawRates:ArrayCollection;


    }
}

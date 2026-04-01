/**
 * User: dweinberg
 * Date: 3/8/13
 * Time: 1:07 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarterCompanyFilingAmounts")]
    public class QuarterCompanyFilingAmounts {
        public function QuarterCompanyFilingAmounts() {
        }
        public var quarter:Quarter;
        [ArrayElementType("psp.sap.model.CompanyFilingAmount")]
        public var amounts:ArrayCollection;

    }
}

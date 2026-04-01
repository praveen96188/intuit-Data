/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:35 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemCollection")]
    public class EmployeeLineItemCollection {

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var compensationItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var preTaxDeductionItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var employeeTaxItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var postTaxDeductionItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var employerTaxItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var taxableEmployerContributionItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var noTaxAffectEmployerContributionItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var directDepositItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var taxableAdditionItems:ArrayCollection;

        [ArrayElementType("psp.sap.model.LineItemValue")]
        public var noTaxAffectAdditionItems:ArrayCollection;

        public var netPay:Number;

    }
}

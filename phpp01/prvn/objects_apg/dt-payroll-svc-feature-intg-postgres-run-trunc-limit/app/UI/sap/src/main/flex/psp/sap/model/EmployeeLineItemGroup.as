/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:36 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemGroup")]
    public class EmployeeLineItemGroup {

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var compensations:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var preTaxDeductions:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var employeeTaxes:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var postTaxDeductions:ArrayCollection;

        public var netPay:Number;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var employerTaxes:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var taxableEmployerContributions:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var noTaxAffectEmployerContributions:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var directDeposits:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var taxableAdditions:ArrayCollection;

        [ArrayElementType ("psp.sap.model.LineItemValue")]
        public var noTaxAffectAdditions:ArrayCollection;

    }
}

/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 3:27 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaystubDetails")]
    public class PaystubDetails {
        public var paystubSeq:String;
        public var addressLine1:String;
        public var addressLine2:String;
        public var addressLine3:String;
        public var addressLine4:String;
        public var addressLine5:String;
        public var checkNumber:String;
        public var fedFilingStatus:String;
        public var fedAllowances:Number;
        public var fedExtra:Number;
        public var stateFilingStatus:String;
        public var fedClaimDependent:Number;
        public var fedDeductions:Number;
        public var fedOtherIncome:Number;
        public var fedMultipleJobs:String;
        public var fedW4EmpPref:String;
        public var stateAllowances:Number;
        public var stateExtra:Number;
        public var payBeginDate:Date;
        public var payEndDate:Date;
        public var paycheckDate:Date;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var nonTaxCompanyItems:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var taxCompanyItems:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var taxableEarnings:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var taxes:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var taxAdjustments:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPayItem")]
        public var preTaxDeductions:ArrayCollection;
        [ArrayElementType("psp.sap.model.PstubPaidTimeOffItem")]
        public var paidTimeOffs:ArrayCollection;
        public var netCurrentAmount:Number;
        public var netYtdAmount:Number;
    }
}

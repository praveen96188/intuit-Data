/**
 * User: ihannur
 * Date: 11/26/12
 * Time: 11:04 AM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLedgerItemDetailsCriterion")]
    public class LedgerItemDetailsCriterion {
        public var sourceSystemCd:String;
        public var companyId:String;
        public var payrollDate:Date;
        public var payrollRunId:String;
        public var voidId:String;
        public var templateCd:String;
        public var lawId:String;
        public var isQTD:Boolean;
        public var isYTD:Boolean;
        public var includeNotPostedPayments:Boolean;
    }
}

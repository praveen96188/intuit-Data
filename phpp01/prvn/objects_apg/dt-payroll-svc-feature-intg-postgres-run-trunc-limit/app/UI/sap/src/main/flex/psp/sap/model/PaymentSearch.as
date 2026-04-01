/**
 * User: dweinberg
 * Date: 1/17/12
 * Time: 11:56 AM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentSearch")]
    public class PaymentSearch {
        public var searchType:String;
        public var status:String;
        public var agencyAbbrev:String;
        public var paymentTemplate:String;
        public var paymentMethod:String;
        public var companyIds:String;
        public var settlementStartDate:Date;
        public var settlementEndDate:Date;
        public var initiationStartDate:Date;
        public var initiationEndDate:Date;
        public var quarter:Quarter;
        public var overduePaymentsOnly:Boolean;
    }
}

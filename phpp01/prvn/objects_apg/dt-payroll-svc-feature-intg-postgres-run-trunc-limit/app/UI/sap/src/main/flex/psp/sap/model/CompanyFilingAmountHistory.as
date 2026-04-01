/**
 * User: dweinberg
 * Date: 3/8/13
 * Time: 11:02 AM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyFilingAmountHistory")]
    public class CompanyFilingAmountHistory extends CompanyFilingAmount {
        public var effectiveQuarter:Quarter;
        public var invalidDate:Date;
        public var modifiedDate:Date;
        public var modifiedBy:String;
    }
}

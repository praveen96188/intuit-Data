/**
 * User: dweinberg
 * Date: 3/14/12
 * Time: 1:41 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxExemptInfo")]
    public class TaxExemptInfo {
        public var exemptStatus:String;
        public var expirationDate:Date;
        public var isCurrentlyExempt:Boolean;


    }
}

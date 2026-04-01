/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:22 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawFlags")]
    public class LawFlags {
        public var law:LawItem;
        public var inactive:Boolean;
        public var exempt:Boolean;
        public var reimbursable:Boolean;
    }
}

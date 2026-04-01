/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:36 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemQuarter")]
    public class EmployeeLineItemQuarter extends EmployeeLineItemGroup {
        public var quarter:int;
    }
}

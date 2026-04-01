/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:36 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemPaycheck")]
    public class EmployeeLineItemPaycheck extends EmployeeLineItemGroup {
        public var paycheckDate:Date;
        public var isPaycheckVoid:Boolean;
        public var sourcePayrollRunId:String;
        public var isELA:Boolean;
    }
}

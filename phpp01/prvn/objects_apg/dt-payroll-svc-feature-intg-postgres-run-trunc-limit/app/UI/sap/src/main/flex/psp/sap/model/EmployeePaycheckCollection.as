/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:37 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeePaycheckCollection")]
    public class EmployeePaycheckCollection extends EmployeeLineItemCollection{
        [ArrayElementType("psp.sap.model.EmployeeLineItemPaycheck")]
        public var paychecks:ArrayCollection;
    }
}

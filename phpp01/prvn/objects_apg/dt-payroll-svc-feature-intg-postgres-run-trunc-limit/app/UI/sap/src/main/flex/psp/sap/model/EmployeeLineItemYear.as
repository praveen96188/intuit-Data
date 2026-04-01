/**
 * User: dweinberg
 * Date: 9/10/12
 * Time: 5:37 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeLineItemYear")]
    public class EmployeeLineItemYear extends EmployeeLineItemCollection {
        public var year:int;

        [ArrayElementType("psp.sap.model.EmployeeLineItemQuarter")]
        public var quarters:ArrayCollection;

        [Transient]
        [ArrayElementType("psp.sap.model.RotatedPaycheck")]
        public var rotatedPaychecks:ArrayCollection;
    }
}

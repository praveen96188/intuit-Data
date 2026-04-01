/**
 * User: dweinberg
 * Date: 11/1/11
 * Time: 12:54 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDataSyncItems")]
    public class DataSyncItems {
        [ArrayElementType("String")]
        public var employeeIds:ArrayCollection;
        [ArrayElementType("String")]
        public var payrollItemIds:ArrayCollection;
        [ArrayElementType("String")]
        public var paychecks:ArrayCollection;
        [ArrayElementType("String")]
        public var priorPayments:ArrayCollection;
        [ArrayElementType("String")]
        public var liabilityAdjustments:ArrayCollection;
        [ArrayElementType("String")]
        public var liabilityChecks:ArrayCollection;
        [ArrayElementType("String")]
        public var qbdtPayrollTransactions:ArrayCollection;
    }
}

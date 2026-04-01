/**
 * User: dweinberg
 * Date: 11/2/11
 * Time: 9:52 AM
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQBDTTokens")]
    public class QBDTTokens {
        public var highToken:String;
        public var payrollTxNextId:String;
        public var paycheckNextId:String;
        public var employeeNextId:String;
        public var payrollItemNextId:String;
    }
}

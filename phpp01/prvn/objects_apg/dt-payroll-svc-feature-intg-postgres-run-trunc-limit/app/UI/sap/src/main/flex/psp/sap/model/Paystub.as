/**
 * User: ihannur
 * Date: 6/26/13
 * Time: 3:23 PM
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaystub")]
    public class Paystub {
        public var paystubSeq:String;
        public var employeeSeq:String;
        public var paycheckDate:Date;

    }
}

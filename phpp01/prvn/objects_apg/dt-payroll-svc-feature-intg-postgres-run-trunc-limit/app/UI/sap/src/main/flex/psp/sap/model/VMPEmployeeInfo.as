/**
 * User: ihannur
 * Date: 6/19/13
 * Time: 3:49 PM
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPVMPEmployeeInfo")]
    public class VMPEmployeeInfo {

        public var employeeSeq:String;
        public var firstName:String;
        public var lastName:String;
        public var middleName:String;
        public var socialSecurityNumber:String;
        public var emailAddress:String;
        public var userId:String;
        public var consumerId:String;

        [Transient]
        public function get fullNameForward():String {
            var retFirstName:String = firstName == null ? "" : firstName;
            var retLastName:String = lastName == null ? "" : lastName;

            return retFirstName + ((middleName != null && middleName != "") ? (" " + middleName) : "") + " " + retLastName;
        }

    }
}

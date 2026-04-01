/**
 * User: dweinberg
 * Date: 2/11/13
 * Time: 3:01 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHEnrollmentDetail")]
    public class ACHEnrollmentDetail extends EnrollmentDetail {
        public var rejectionReason:String;
        public var creationDate:Date;
        public var modifiedDate:Date;
        public var effectiveDate:Date;
        public var aid:String;
    }
}

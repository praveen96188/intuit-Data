/**
 * User: dweinberg
 * Date: 1/11/13
 * Time: 10:36 AM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentSearch")]
    public class RAFEnrollmentSearch {
        public var status:String;
        public var PSID_or_EIN:String;
        public var creationDateStart:Date;
        public var creationDateEnd:Date;
        public var lastUpdateDateStart:Date;
        public var lastUpdateDateEnd:Date;


        public function RAFEnrollmentSearch(status:String, PSID_or_EIN:String, creationDateStart:Date, creationDateEnd:Date, lastUpdateDateStart:Date, lastUpdateDateEnd:Date) {
            this.status = status;
            this.PSID_or_EIN = PSID_or_EIN;
            this.creationDateStart = creationDateStart;
            this.creationDateEnd = creationDateEnd;
            this.lastUpdateDateStart = lastUpdateDateStart;
            this.lastUpdateDateEnd = lastUpdateDateEnd;
        }
    }
}

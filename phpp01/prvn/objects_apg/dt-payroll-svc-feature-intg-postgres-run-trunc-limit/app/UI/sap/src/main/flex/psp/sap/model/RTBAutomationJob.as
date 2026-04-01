/**
 * Created by anandp233 on 2/19/14.
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRTBAutomationJob")]
    public class RTBAutomationJob {
        public var jobName:String;
        public var shortDescription:String;
        public var description:String;

        [Transient]
        public function toString():String {
            return shortDescription;
        }

    }
}

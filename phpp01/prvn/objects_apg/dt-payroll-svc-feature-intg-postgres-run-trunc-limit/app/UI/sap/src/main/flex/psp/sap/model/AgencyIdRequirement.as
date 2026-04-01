/**
 * User: dweinberg
 * Date: 3/4/13
 * Time: 4:42 PM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyIdRequirement")]
    public class AgencyIdRequirement {
        public function AgencyIdRequirement() {
        }

        public var isFulfilled:Boolean;
        public var requirementString:String;
    }
}

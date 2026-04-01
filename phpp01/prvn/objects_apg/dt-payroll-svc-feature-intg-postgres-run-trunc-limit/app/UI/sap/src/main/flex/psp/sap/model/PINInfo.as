/**
 * User: dweinberg
 * Date: 12/18/12
 * Time: 11:46 AM
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPINInfo")]
    public class PINInfo {
        public var pinCreated:Boolean;
        public var pinLocked:Boolean;
    }
}

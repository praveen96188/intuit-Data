/**
 * User: ihannur
 * Date: 6/26/13
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPstubPaidTimeOffItem")]
    public class PstubPaidTimeOffItem {
        public var name:String;
        public var ytdUsed:String;
        public var available:String;
    }
}

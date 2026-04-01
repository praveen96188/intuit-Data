/**
 * User: ihannur
 * Date: 6/26/13
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPstubPayItem")]
    public class PstubPayItem {
        public var name:String;
        public var rate:String;
        public var quantity:String;
        public var currentAmount:Number;
        public var ytdAmount:Number;

    }
}

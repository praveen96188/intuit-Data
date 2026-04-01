/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 5/3/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLimitRule")]
    public class DirectDepositLimitRule {
        public var id:String;
        public var description:String;
        public var sourceSystem:String;
    }
}

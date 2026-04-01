/**
 * Created by IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 8/24/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingEmployeeDetail")]

    public class UsageBillingEmployeeDetail {
        public var paycheckDate:Date;
        public var checkNumber:String;
        public var employeeName:String;
        public var companyName:String;
        public var ein:String;
    }
}

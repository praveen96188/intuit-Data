/**
 * Created by IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 8/24/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingInvoiceDetail")]
    public class UsageBillingInvoiceDetail {
            public var subscriptionFee:Number;
            public var employeeFee:Number;
            public var credit:Number;
            public var total:Number;
            public var numEmployessPaidPrevMonth:Number;
            public var billPOID:String;
            public var isPayrollItem:Boolean;
            public var payrollItemChargeId:String;
        }
}

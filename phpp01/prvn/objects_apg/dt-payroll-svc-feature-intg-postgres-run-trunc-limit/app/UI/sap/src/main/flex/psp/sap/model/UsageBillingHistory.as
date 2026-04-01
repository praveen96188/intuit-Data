/**
 * Created by IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 8/24/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {

    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingHistory")]
    public class UsageBillingHistory {
            public var billingStartDate:Date;
            public var billingEndDate:Date;
            public var statementDate:Date;
            public var subscriptionFee:Number;
            public var employeeFee:Number;
            public var credit:Number;
            public var total:Number;
            public var numEmployessPaidPrevMonth:Number;
            public var billId:String;
        }
}

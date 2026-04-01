/**
 * Created by IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 8/24/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingInvoice")]
    public class UsageBillingInvoice {
            public var statementDate:Date;
            public var statementDateCounter:int = 0;
            public var billPOID:String;
            public var invoiceDetail:UsageBillingInvoiceDetail;
        }
}

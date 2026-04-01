/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/23/12
 * Time: 1:21 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingDetail")]

    public class UsageBillingDetail {
		public var usagePeriodEndDate:Date;
		public var usagePeriodStartDate:Date;
    	public var employeeDetails:ArrayCollection;
    	public var numEmployeesBilled:Number;
        public var numCompaniesBilled:Number;
        public var isMultiEin:Boolean;
    }
}

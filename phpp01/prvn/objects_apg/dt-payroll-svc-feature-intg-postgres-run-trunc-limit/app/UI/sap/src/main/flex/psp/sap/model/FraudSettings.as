package psp.sap.model
{
    import mx.utils.StringUtil;

    import psp.sap.formatters.SAPCurrencyFormatters;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFraudSettings")]
	public class FraudSettings
	{
	   	//Employee Paid Fraud Setting
        public var fraudEEPaidMax:String;

        //Employee Paid Fraud Payroll Setting
    	public var fraudEEPaidMaxXPayrolls:String;

        public var fraudEEAcctUpdateMax:String;
        public var fraudEEAcctUpdateXDays:String;

    	//Employee Paid Round Dollar Amount
    	public var fraudEERoundPaidXPayrolls:String;
        public var fraudEERoundPaidXAmount:String;

    	//Payee Paid Round Dollar Amount
    	public var fraudBPRoundPaidXPayrolls:String;
        public var fraudBPRoundPaidXAmount:String;

      	//Employee Paid X Times in Y Days
    	public var fraudEEPaidXTimes:String;
    	public var fraudEENumberOfDaysMultiplePaychecks:String;
    	
    	//Newly Added Employee Paid X Percent Greater Than Any Other Employee
    	public var fraudEENewEmployeeAddedXDays:String;
     	public var fraudEEPercentGreaterThanOtherEEs:String;

    	//Total Payroll Fraud Setting
    	public var fraudPRMax:String;

        //Total Payroll Fraud Payroll Setting
    	public var fraudPRMaxXPayrolls:String;
    
    	//Employee Paid Percentage Increase Setting
    	public var fraudEEPercentIncreaseMax:String;
    	
    	//Employee Paid Percentage Increase Payroll Setting
    	public var fraudEEPercentIncreaseMaxXPayrolls:String;    
    	
    	//Company Paid Percentage Increase Setting
    	public var fraudPRPercentIncreaseMax:String;
    	
    	//Company Paid Percentage Increase Payroll Setting
    	public var fraudPRPercentIncreaseMaxXPayrolls:String;
    	
    	//Company Number of Payrolls Submitted Within X Days of Sign Up
    	public var fraudPRNumberOfDaysForXPayrolls:String;
    	public var fraudPRNumberOfPayrollsInXDays:String;
        public var fraudPRXPayrollAmount:String;

        public var fraudEEPaidXAmtWithinYAcctUpdateDays:String;
        //Employee has spike in pay after bank account update
        public var fraudEENumberOfPaychecksSpikeInPay:String;
        public var fraudEEPercentGreaterThanAverage:String;
        public var fraudEENumberOfDaysBankAcctUpdated:String;

        //Percent of employees paid to same bank (routing number)
        public var fraudPRNumberOfPayrollsToCheckSameBank:String;
        public var fraudPRPercentEmployeesPaidSameBank:String;
        public var fraudPRTotalEmployeesToCheckSameBank:String;

        public var fraudPREmployeesSameBankAccountMax:String;

        public var fraudDDInactivityDays:String;
        public var fraudDDInactivityPayrollAmount:String;

        public var fraudPayeePaidMax:String;
        public var fraudPayeePaidMaxXPayrolls:String;
        public var fraudPayeePaidXTimes:String;
        public var fraudPayeeNumberOfDaysMultiplePayments:String;

        public var fraudBPAcctUpdateMax:String;
        public var fraudBPAcctUpdateXDays:String;
        public var fraudPayeePaidXAmtWithinYAcctUpdateDays:String;

        public var fraudBPMax:String;
        public var fraudBPMaxXPayrolls:String;

        public var fraudBPInactivityDays:String;
        public var fraudBPInactivityPayrollAmount:String;

        public var fraudBPNumberOfDaysForXPayments:String;
        public var fraudBPNumberOfPaymentsInXDays:String;
        public  var fraudBPXPayrollAmount:String;

        public var fraudBPNumberOfPaymentsToCheckSameBank:String;
        public var fraudBPPercentPayeesPaidSameBank:String;
        public var fraudBPTotalPayeesToCheckSameBank:String;

    }
}
package test.mock.data
{
	import psp.sap.model.DirectDepositLimitSettings;
	import psp.sap.model.FraudSettings;
	
	public class AdministrativeData
	{
		public static function getFraudSettings():FraudSettings{			
			var fraudSetting:FraudSettings = new FraudSettings();

            fraudSetting.fraudEEPaidMax = "3";
            fraudSetting.fraudPRMax = "3";

            fraudSetting.fraudEEAcctUpdateMax = "5000";
            fraudSetting.fraudEEAcctUpdateXDays = "10";

            fraudSetting.fraudEENewEmployeeAddedXDays = "3";
            fraudSetting.fraudEENumberOfDaysBankAcctUpdated = "3";
            fraudSetting.fraudEENumberOfDaysMultiplePaychecks = "3";
            fraudSetting.fraudEENumberOfPaychecksSpikeInPay = "3";
            fraudSetting.fraudEEPaidMaxXPayrolls = "3";
            fraudSetting.fraudEEPaidXTimes = "3";
            fraudSetting.fraudEEPercentGreaterThanAverage = "3";
            fraudSetting.fraudEEPercentGreaterThanOtherEEs = "3";
            fraudSetting.fraudEEPercentIncreaseMax = "3";
            fraudSetting.fraudEEPercentIncreaseMaxXPayrolls = "3";
            fraudSetting.fraudEERoundPaidXPayrolls = "3";
            fraudSetting.fraudEERoundPaidXAmount = "10000.00";
            fraudSetting.fraudBPRoundPaidXAmount = "10000.00";
            fraudSetting.fraudPREmployeesSameBankAccountMax = "3";
            fraudSetting.fraudPRMaxXPayrolls = "3";
            fraudSetting.fraudPRNumberOfDaysForXPayrolls = "3";
            fraudSetting.fraudPRNumberOfPayrollsInXDays = "3";
            fraudSetting.fraudPRNumberOfPayrollsToCheckSameBank = "3";
            fraudSetting.fraudPRPercentEmployeesPaidSameBank = "3";
            fraudSetting.fraudPRPercentIncreaseMax = "3";
            fraudSetting.fraudPRPercentIncreaseMaxXPayrolls = "3";
            fraudSetting.fraudPRTotalEmployeesToCheckSameBank = "3";

            fraudSetting.fraudDDInactivityDays = "180";
            fraudSetting.fraudDDInactivityPayrollAmount = "5000.00";

            fraudSetting.fraudPayeePaidMax = "3";
            fraudSetting.fraudPayeePaidMaxXPayrolls = "3";
            fraudSetting.fraudPayeePaidXTimes = "3";
            fraudSetting.fraudPayeeNumberOfDaysMultiplePayments = "3";

            fraudSetting.fraudBPAcctUpdateMax = "5000";
            fraudSetting.fraudBPAcctUpdateXDays = "10";

            fraudSetting.fraudBPMax = "3";
            fraudSetting.fraudBPMaxXPayrolls = "3";

            fraudSetting.fraudBPInactivityDays = "180";
            fraudSetting.fraudBPInactivityPayrollAmount = "5000.00";

    		return fraudSetting;
		}
		
		public static function getDirectDepositLimitSettings():DirectDepositLimitSettings {
			var directDepositLimitSettingsMockData:DirectDepositLimitSettings = new DirectDepositLimitSettings();

	        directDepositLimitSettingsMockData.companyBankAccountDurationLimitForVerification = "365";
	        directDepositLimitSettingsMockData.companyBankAccountVerificationAttemptLimit = "10";
	        directDepositLimitSettingsMockData.consecutiveLimitViolationLimit = "5";
	        directDepositLimitSettingsMockData.DDCompanyLimitDuration = "8";
	        directDepositLimitSettingsMockData.DDEmployeeLimitDuration = "14";
	        directDepositLimitSettingsMockData.defaultDDCompanyLimit = "50000.00";
	        directDepositLimitSettingsMockData.defaultDDEmployeeLimit = "20000.00";
	        directDepositLimitSettingsMockData.maxDDCompanyLimitDefault = "150000.00";
	        directDepositLimitSettingsMockData.minimumNonSuspectPayrollAmount = "100.00";

            directDepositLimitSettingsMockData.defaultBPCompanyLimit = "50000.00";
            directDepositLimitSettingsMockData.defaultBPPayeeLimit = "20000.00";            
        	
        	return directDepositLimitSettingsMockData;
		}
	}
}
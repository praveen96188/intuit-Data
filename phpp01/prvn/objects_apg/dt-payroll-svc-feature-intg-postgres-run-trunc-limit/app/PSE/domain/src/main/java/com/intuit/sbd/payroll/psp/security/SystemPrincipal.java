package com.intuit.sbd.payroll.psp.security;

/**
 * Valid System Principals
 */

/*
 * Note: Whenever a SystemPrinciple is added/ removed or updated, Mike Kinasz (Mike_Kinasz@Intuit.com) needs to be notified so message process can be updated.
 */

public enum SystemPrincipal {
	EWSAdapter {
		public String getId() {
			return "EWSAdapter";
		}
	},
	DISAdapter {
		public String getId() {
			return "DISAdapter";
		}
	},
	QBOEAdapter {
		public String getId() {
			return "QBOEAdapter";
		}
	},
	QBDTAdapter {
		public String getId() {
			return "QBDTAdapter";
		}
	},
	QBDTWSAdapter {
		public String getId() {
			return "QBDTWSAdapter";
		}
	},
    BRMAdapter {
    		public String getId() {
    			return "BRMAdapter";
    		}
    	},
	SAPAdapter {
		public String getId() {
			return "SAPAdapter";
		}
	},
	TestAdapter {
		public String getId() {
			return "TestAdapter";
		}
	},
    CdmAdapter {
        public String getId() {
            return "CdmAdapter";
        }
    },
	BatchJob {
		public String getId() {
			return "BatchJob";
		}
	},
	CRISIntegrationBatchJob {
		public String getId() {
			return "CRISIntegrationBatchJob";
		}
	},
	AchReturnsBatchJob {
		public String getId() {
			return "AchReturnsBatchJob";
		}
	},
	AchTransactionsBatchJob {
		public String getId() {
			return "AchTransactionsBatchJob";
		}
	},
	MissedAchTransactionsBatchJob {
		public String getId() {
			return "MissedAchTransactionsBatchJob";
		}
	},
	MissedPayrollsBatchJob {
		public String getId() {
			return "MissedPayrollsBatchJob";
		}
	},
	GemsAccountsReceivableBatchJob {
		public String getId() {
			return "GemsAccountsReceivableBatchJob";
		}
	},
	GemsGeneralLedgerBatchJob {
		public String getId() {
			return "GemsGeneralLedgerBatchJob";
		}
	},
	AchOffloadBatchJob {
		public String getId() {
			return "AchOffloadBatchJob";
		}
	},
	CloudMigration {
		public String getId() {
			return "CloudMigration";
		}
	},
	SalesTaxExceptionsBatchJob {
		public String getId() {
			return "SalesTaxExceptionsBatchJob";
		}
	},
	FraudulentPayrollsBatchJob {
		public String getId() {
			return "FraudulentPayrollsBatchJob";
		}
	},
	ForecastBatchJob {
		public String getId() {
			return "ForecastBatchJob";
		}
	},
	AS400Migration {
		public String getId() {
			return "AS400MigrationBatchJob";
		}
	},
	CRISMigration {
		public String getId() {
			return "CRISMigration";
		}
	},
	AS400DataSyncBatchJob {
		public String getId() {
			return "AS400DataSyncBatchJob";
		}
	},
	EmailGateway {
		public String getId() {
			return "EmailGatewayBatchJob";
		}
	},
	FeeEventsBatchJob {
		public String getId() {
			return "FeeEventsBatchJob";
		}
	},
	LedgerBalanceBatchJob {
		public String getId() {
			return "LedgerBalanceBatchJob";
		}
	},
    LedgerOperationsBatchJob {
        public String getId() {
            return "LedgerOperationsBatchJob";
        }
    },
	UnitTest {
		public String getId() {
			return "UnitTest";
		}
	},
	EftpsEnrollmentBatchJob {
		public String getId() {
			return "EftpsEnrollmentBatchJob";
		}
	},
	EftpsPaymentsBatchJob {
		public String getId() {
			return "EftpsPaymentsBatchJob";
		}
	},
	EftpsFileCommBatchJob {
		public String getId() {
			return "EftpsFileCommBatchJob";
		}
	},
	EftpsAgeOutBatchJob {
		public String getId() {
			return "EftpsAgeOutBatchJob";
		}
	},
	RAFProcessorBatchJob {
		public String getId() {
			return "RAFProcessorBatchJob";
		}
	},
	HPDEBatchJob {
		public String getId() {
			return "HPDEBatchJob";
		}
	},
	ThirdParty401kBatchJob {
		public String getId() {
			return "ThirdParty401kBatchJob";
		}
	},
    JPMCDirectDepositScreeningBatchJob {
        public String getId() {
            return "JPMCDDScreeningBatchJob";
        }
    },
	StateReportBatchJob {
		public String getId() {
			return "StateReportBatchJob";
		}
	},
	TriggerAmendmentBatchJob {
		public String getId() {
			return "TriggerAmendmentBatchJob";
		}
	},
	TaxCredits {
		public String getId() {
			return "TaxCredits";
		}
	},
	TFASimulatorBatchJob {
		public String getId() {
			return "TFASimulatorBatchJob";
		}
	},
	ERSBatchJob {
		public String getId() {
			return "ERSBatchJob";
		}
	},
	AMOBatchJob {
		public String getId() {
			return "AMOBatchJob";
		}
	},
	IVRAdapter {
		public String getId() {
			return "IVRAdapter";
		}
	},
    PTCAdapter {
        public String getId() {
            return "PTCAdapter";
        }
    },
	TESBatchJob {
		public String getId() {
			return "TESBatchJob";
		}
	},
	IOPSyncBatchJob {
		public String getId() {
			return "IOPSyncBatchJob";
		}
	},
	ComplianceToolkit {
		public String getId() {
			return "ComplianceToolkit";
		}
	},
	ERSToolkit {
		public String getId() {
			return "ERSToolkit";
		}
	},
	PrintedCheckBatchJob {
		public String getId() {
			return "PrintedCheckBatchJob";
		}
	},
	ProcessZeroPayments {
		public String getId() {
			return "ProcessZeroPayments";
		}
	},
	ReconPlusBatchJob {
		public String getId() {
			return "ReconPlusBatchJob";
		}
	},
	AtfDataExtractBatchJob {
		public String getId() {
			return "AtfDataExtractBatchJob";
		}
	},
	EdiPaymentsBatchJob {
		public String getId() {
			return "EdiPaymentsBatchJob";
		}
	},
	EdiFileCommBatchJob {
		public String getId() {
			return "EdiFileCommBatchJob";
		}
	},
	CheckPrintBatchJob {
		public String getId() {
			return "CheckPrintBatchJob";
		}
	},
	RetryUnprocessedQbdtRqBatchJob {
		public String getId() {
			return "RetryUnprocessedQbdtRqBatchJob";
		}
	},
	VANSimulatorBatchJob {
		public String getId() {
			return "VANSimulatorBatchJob";
		}
	},
	AchDebitOffloadBatchJob {
		public String getId() {
			return "AchDebitOffload";
		}
	},
	EoqSUITaxAdjustmentsBatchJob {
		public String getId() {
			return "EoqSUITaxAdjustments";
		}
	},
    IRSDepositFrequencyBatchJob {
        public String getId() {
            return "IRSDepositFrequency";
        }
    },
	PSPToEMSBSDataSyncBatchJob {
		public String getId() {
			return "PSPToEMSBSDataSync";
		}
	},
    MonthlyFeeBatchJob {
        public String getId() {
            return "MonthlyFeeBatchJob";
        }
    },
	EMSBSToBRMDataSyncBatchJob {
		public String getId() {
			return "EMSBSToBRMDataSync";
		}
	},
    EmployeeTotalsCalculationBatchJob {
        public String getId() {
            return "EmployeeTotalsCalculation";
        }
	},
    EmployeePayrollItemsCalcBatchJob {
        public String getId() {
            return "EmployeePayrollItemsCalc";
        }
    },
    EmployeeW2TotalsCalculationBatchJob {
        public String getId() {
            return "EmployeeW2TotalsCalculation";
        }
    },
    SendW2DataToTFSBatchJob {
        public String getId() {
            return "SendW2DataToTFSBatchJob";
        }
    },
    SendMonthlyDataToTFSBatchJob {
        public String getId() {
            return "SendMonthlyDataToTFSBatchJob";
        }
    },
    ScheduledEmails {
        public String getId() {
            return "ScheduledEmails";
        }
    },
    FsetFilingBatchJob {
        public String getId() {
            return "FsetFilingBatchJob";
        }
    },
    WorkersCompBatchJob {
        public String getId() {
            return "WorkersCompBatchJob";
        }
    },
    AnnualBillingBatchJob {
        public String getId() {
            return "AnnualBillingBatchJob";
        }
    },
    LiabilityAdjustmentsCleanup {
        public String getId() {
            return "LiabilityAdjustmentsCleanup";
        }
    },
    AgencyDataExchange {
        public String getId() {
            return "AgencyDataExchange";
        }
    },
    MassEntitlementDisableTool {
        public String getId() {
            return "MassEntitlementDisableTool";
        }
	},
    ACHEnrollmentBatchJob {
        public String getId() {
            return "ACHEnrollmentBatchJob";
        }
    },
    ACHDeEnrollmentBatchJob {
        public String getId() {
            return "ACHDeEnrollmentBatchJob";
        }
    },
    ACHEnrollmentResponseBatchJob {
        public String getId() {
            return "ACHEnrollmentResponseBatchJob";
        }
    },
    EnrollmentDeleteSection {
        public String getId() {
            return "EnrollmentDeleteSelection";
        }
    },
    SUICreditsBatchJob {
        public String getId() {
            return "SUICreditsBatchJob";
        }
    },
    SUIRatePaymentsCleanup {
        public String getId() {
            return "SUIRatePaymentsCleanup";
        }
    },
	AssistedUsageDataSyncProcess {
		public String getId() {
			return "AssistedUsageDataSyncProcess";
		}
	},
    BRMUsageErrorFileProcess {
        public String getId() {
            return "BRMUsageErrorFileProcess";
        }
    },
    PayrollAPI {
        public String getId() {
            return "PayrollAPI";
        }
    },
	DDMigrationFramework {
		public String getId() {
			return "DDMigrationFramework";
		}
	},
	AssistedUsageReportProcess {
		public String getId() {
      return "AssistedUsageReportProcess";
    }
  },
	RTBAutomationBatchJob {
		public String getId() {
			return "RTBAutomationBatchJob";
		}
	},
	SoxDBUserReportBatchJob {
		public String getId() {
			return "SoxDBUserReportBatchJob";
		}
	},
	SoxReportBatchJob {
		public String getId() {
			return "SoxReportBatchJob";
		}
	},
	EntityEventBatchJob {
		public String getId() {
			return "EntityEventBatchJob";
		}
	},
	EntityEventRetryBatchJob {
		public String getId() {
			return "EntityEventRetryBatchJob";
		}
	},
	ACHTraceIdProcessor {
		public String getId() {
			return "ACHTraceIdProcessor";
		}
	},
	TaxTransactionProcessor {
		public String getId() {
			return "TaxTransactionProcessor";
		}
	},
	NextDayPayrollMigrator {
		public String getId() {
			return "NextDayPayrollMigrator";
		}
	},
	EntityPublisher {
		public String getId() {
			return "EntityPublisher";
    }
  },
	PubSub {
		public String getId() {
			return "PubSub";
		}
	},
	EntityInitialLoadProcessor {
		public String getId() {
			return "EntityInitialLoadProcessor";
		}
	},
	EVSCompanyProcessor {
		public String getId() {
			return "EVSCompanyProcessor";
		}
	},
	BulkWorkforceInviteProcessor {
		public String getId() { return "BulkWorkforceInviteProcessor"; }
	},
	DataReencryptionProcessor {
		public String getId() {
			return "DataReencryptionProcessor";
		}
	},
	MTLCompanyToOnHoldProcessor{
		public String getId() {return "MTLCompanyToOnHoldProcessor";}
	},
	PSPToSMSMigrationProcessor {
		public String getId() { return "PSPToSMSMigrationProcessor"; }
	},
	RealTimeRetryProcessor {
		public String getId() { return "RealTimeRetryProcessor"; }
	},
	;
	/*
	 * Note: Whenever a SystemPrinciple is added/ removed or updated, Mike Kinasz (Mike_Kinasz@Intuit.com) needs to be notified so message process can be updated.
	 */


	abstract public String getId();

	public boolean isCustomer() {
		return this == EWSAdapter || this == QBOEAdapter || this == QBDTAdapter || this == QBDTWSAdapter || this == TaxCredits;
	}
}

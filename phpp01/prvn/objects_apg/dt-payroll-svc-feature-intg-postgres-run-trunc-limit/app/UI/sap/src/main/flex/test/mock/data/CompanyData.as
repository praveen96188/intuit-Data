package test.mock.data
{
    import mx.collections.ArrayCollection;

    import psp.sap.application.enums.CheckPrintBatchStatusEnum;
    import psp.sap.model.Address;
    import psp.sap.model.ChaseReport;
    import psp.sap.model.CheckPrintingBatch;
    import psp.sap.model.CommunicationPrefEnum;
    import psp.sap.model.Company;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.CompanyContacts;
    import psp.sap.model.CompanyDdLimits;
    import psp.sap.model.CompanyEventGroup;
    import psp.sap.model.CompanyEventGroupItem;
    import psp.sap.model.CompanyEventItem;
    import psp.sap.model.CompanyEventQueryReturn;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.CompanyLegalInfo;
    import psp.sap.model.CompanyServiceStatus;
    import psp.sap.model.CompanyStatus;
    import psp.sap.model.Contact;
    import psp.sap.model.DisplayStatus;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.FraudEvent;
    import psp.sap.model.Offer;
    import psp.sap.model.PropertyAudit;
    import psp.sap.model.RandomDebitTransaction;
    import psp.sap.model.SearchResults;
    import psp.sap.model.ServiceStatus;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.model.VendorInfo;
    import psp.sap.model.companyevents.CompanyEventDetail;
    import psp.sap.model.companyevents.CompanyEventEmail;
    import psp.sap.model.companyevents.CompanyEventEmailParam;

    public class CompanyData
	{
		public static function getCompanyContacts():CompanyContacts {
            var companyContacts:CompanyContacts = new CompanyContacts();
			var contacts:ArrayCollection = new ArrayCollection();
			var employeeContactInfo:Contact = new Contact();
			
			employeeContactInfo.address = new Address();
			employeeContactInfo.address.addressLine1 = "244 Apple Road";
			employeeContactInfo.address.city = "Reno";
			employeeContactInfo.address.state = "NV";
			employeeContactInfo.address.zipCode = "89503";
			employeeContactInfo.contactId = "12321";
			employeeContactInfo.email = "apple@york.com";
			employeeContactInfo.firstName = "John";
			employeeContactInfo.lastName = "Wayne";
			employeeContactInfo.phoneNumber = "702-232-3829";            
			employeeContactInfo.prefix = "Mr.";
            employeeContactInfo.communicationPref = CommunicationPrefEnum.EMAIL;

			contacts.addItem(employeeContactInfo);
            companyContacts.contacts = contacts;
			
			return companyContacts;
		}
		
		public static function getPropertyAuditCollection():ArrayCollection {
			var propertAudits:ArrayCollection = new ArrayCollection();
			propertAudits.addItem(CompanyData.buildPropertyAudit(new PropertyAudit()));
			propertAudits.addItem(CompanyData.buildPropertyAudit(new PropertyAudit()));
			propertAudits.addItem(CompanyData.buildPropertyAudit(new PropertyAudit()));
			propertAudits.addItem(CompanyData.buildPropertyAudit(new PropertyAudit()));
			
			return propertAudits;
		}
		
		private static function buildPropertyAudit(propertAudit:PropertyAudit):PropertyAudit {
			propertAudit.auditDate = new Date();
			propertAudit.newPropertyValue = "new";
			propertAudit.oldPropertyValue = "old";
			propertAudit.propertyName = "property";
			propertAudit.userId = "user";
			return propertAudit;
		}
		
		public static function getCompany():Company {
			var company:Company = new Company();
			company.sourceSystemCd = SourceSystemEnum.QBOE.code;
			company.companyId = "1234";
            company.fein = "999999999";
					
			// todo add more properties
			
			return company;
		}

		public static function getDisplayStatus():DisplayStatus {
			var ds:DisplayStatus = new DisplayStatus();
			ds.displayStatus = "Active";
			ds.displaySubStatus = "Active Current";
			ds.displayDetails = "None of your business";
			return ds;			
		}
		
		public static function getCompanyBankAccount():CompanyBankAccount {
			var bankAccount:CompanyBankAccount = new CompanyBankAccount();
			bankAccount.verifyRetryCount = 1;
			bankAccount.bankAccountStatusCd = "PendingVerification";
			bankAccount.accountId = "587";
            bankAccount.accountNumber = "634522";
			bankAccount.sourceBankAccountId = "978";
            bankAccount.routingNumber = "111111118";
            bankAccount.accountType  = "Checking";
            bankAccount.bankName = "Bank Name";
            bankAccount.sourceBankAccountName = "QB Bank";
			return bankAccount;
		}
				
		public static function getCompanyBankAccountNoRetries():CompanyBankAccount {
			var bank:CompanyBankAccount = getCompanyBankAccount();
			bank.verifyRetryCount = 0;
			return bank;
		}

		public static function getCompanyBankAccountAtRetryLimit():CompanyBankAccount {
			var bank:CompanyBankAccount = getCompanyBankAccount();
			bank.verifyRetryCount = 2;
			return bank;
		}
		
		public static function getCompanyBankAccountActive():CompanyBankAccount {
			var bank:CompanyBankAccount = getCompanyBankAccount();
			bank.bankAccountStatusCd = "Active";
			return bank;			
		}
		
		
		public static function getRandomDebitTransactions():ArrayCollection {
			var randomDebits:ArrayCollection = new ArrayCollection();
			
			var rd1:RandomDebitTransaction = new RandomDebitTransaction();
			rd1.amount1 = "0.23";
			rd1.amount2 = "0.63";
			rd1.offloadedDate = new Date(2009,5,5,5,5,5,5);
			rd1.settlementDate = new Date(2009,5,6,6,6,6,6);
			randomDebits.addItem(rd1);
			
			var rd2:RandomDebitTransaction = new RandomDebitTransaction();
			rd2.amount1 = "0.11";
			rd2.amount2 = "0.22";
			randomDebits.addItem(rd2);
			 						
			
			return randomDebits;
		}
		
		public static function getEmployees():ArrayCollection {
			var ees:ArrayCollection = new ArrayCollection();
			
			var ee1:EmployeeInfo = new EmployeeInfo();
			ee1.firstName = "Little";
			ee1.middleName = "Bobby";
			ee1.lastName = "Tables";
			ee1.employeeId = "5";
			ee1.employeeGseq = "a";
			ee1.status = "Active";
			ees.addItem(ee1);
			
			var ee2:EmployeeInfo = new EmployeeInfo();
			ee2.firstName = "James";
			ee2.middleName = "K";
			ee2.lastName = "Polk";
			ee2.employeeId = "6";
			ee2.employeeGseq = "b";
			ee2.status = "Inactive";
			ees.addItem(ee2);
					
			return ees;  			
		}

        public static function getVendors():ArrayCollection {
            var vendors:ArrayCollection = new ArrayCollection();

            var vendor:VendorInfo = new VendorInfo();
            vendor.name = "vendor1";
            vendor.email = "vendor@abc.com";
            vendor.phone = "123-456-7890";
            vendor.sourceId = "123";
            vendors.addItem(vendor);

            vendor = new VendorInfo();
            vendor.name = "vendor2";
            vendor.email = "vendor2@abc.com";
            vendor.phone = "123-456-1234";
            vendor.sourceId = "124";
            vendors.addItem(vendor);

            return vendors;
        }
		
		public static function getEmployees4Employees():ArrayCollection {
			var ees:ArrayCollection = new ArrayCollection();
			
			var ee1:EmployeeInfo = new EmployeeInfo();
			ee1.firstName = "Little";
			ee1.middleName = "Bobby";
			ee1.lastName = "Tables";
			ee1.employeeId = "5";
			ee1.employeeGseq = "a";
			ee1.status = "Active";
			ees.addItem(ee1);
			
			var ee2:EmployeeInfo = new EmployeeInfo();
			ee2.firstName = "James";
			ee2.middleName = "K";
			ee2.lastName = "Polk";
			ee2.employeeId = "6";
			ee2.employeeGseq = "b";
			ee2.status = "Active";
			ees.addItem(ee2);
			
			var ee3:EmployeeInfo = new EmployeeInfo();
			ee3.firstName = "Jonny";
			ee3.middleName = "B";
			ee3.lastName = "Good";
			ee3.employeeId = "7";
			ee3.employeeGseq = "c";
			ee3.status = "Active";
			ees.addItem(ee3);			

			var ee4:EmployeeInfo = new EmployeeInfo();
			ee4.firstName = "John";
			ee4.middleName = "B";
			ee4.lastName = "Good";
			ee4.employeeId = "8";
			ee4.employeeGseq = "d";
			ee4.status = "Active";
			ees.addItem(ee4);
					
			return ees;  			
		}		
		
		public static function getEmployees1Employee():ArrayCollection {
			var ees:ArrayCollection = new ArrayCollection();
			var ee1:EmployeeInfo = new EmployeeInfo();
			ee1.firstName = "Little";
			ee1.middleName = "Bobby";
			ee1.lastName = "Tables";
			ee1.employeeId = "5";
			ee1.employeeGseq = "a";
			ee1.status = "Active";
			ees.addItem(ee1);
			return ees;
		}
	

		public static function getChaseReports():ArrayCollection {
			var returnList:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i < 5; i++){
				var chaseReport:ChaseReport = new ChaseReport();
				returnList.addItem(chaseReport);
			}
			
			return returnList;
		}

        public static function getCompanyLegalInfo():CompanyLegalInfo {
			var companyLegalInfo:CompanyLegalInfo = new CompanyLegalInfo();
			companyLegalInfo.legalName = "Acme Company";
			companyLegalInfo.doingBusinessAs = "Acme Computers";
			
			companyLegalInfo.address = new Address();
			companyLegalInfo.address.addressLine1 = "123 Main Street";
			companyLegalInfo.address.addressLine2 = "Suite A3";
			companyLegalInfo.address.city = "Reno";
			companyLegalInfo.address.state = "NV";
			companyLegalInfo.address.zipCode = "12345";
			companyLegalInfo.address.zipCodeExtension = "1234";
			return companyLegalInfo;
		}
        
        public static function getFraudEvents():ArrayCollection {
			var returnList:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 1; i <= 5; i++){
				var fraudEvent:FraudEvent = new FraudEvent();
				fraudEvent.eventTimeStamp = new Date("10/0" + i + "/2009"); 
				fraudEvent.payrollAmount = i;
                fraudEvent.sourcePayRunId = "7987";
                fraudEvent.fraudIndicator = "Payroll";
                fraudEvent.companyId = "4658";
                fraudEvent.sourceSystemCd = SourceSystemEnum.QBDT.code;
                fraudEvent.fraudFlagSet = true;
				returnList.addItem(fraudEvent);
			}
			
			return returnList;
		}
		
		public static function getCompanyEvents():CompanyEventQueryReturn {
			var eventReturn:CompanyEventQueryReturn = new CompanyEventQueryReturn();			

			
			var events:ArrayCollection = new ArrayCollection();
			var event1:CompanyEventItem = new CompanyEventItem();
			event1.creatorId = "James Polk";
			event1.lastNoteDate = new Date("05/01/2009");
			event1.eventGroupCode = "OrchestraInstruments";
			var event1Details:ArrayCollection = new ArrayCollection();
			var event1Detail1:CompanyEventDetail = new CompanyEventDetail();
			event1Detail1.eventDetailTypeCd = "SourcePayrollRunId";
			event1Detail1.name = "Source Payroll Run ID";
			event1Detail1.value = "6";
			event1Details.addItem(event1Detail1);
			event1.companyEventDetails = event1Details;
			var event1Emails:ArrayCollection = new ArrayCollection();
			var event1Email1:CompanyEventEmail = new CompanyEventEmail();
			event1Email1.effectiveDate = "04/20/2009";
			event1Email1.status = "Sent";
			var event1Email1Params:ArrayCollection = new ArrayCollection();
			var event1Email1Param1:CompanyEventEmailParam = new CompanyEventEmailParam();
			event1Email1Param1.paramType = "PayrollAdminFirstName";
			event1Email1Param1.paramValue = "Jim";
			event1Email1Params.addItem(event1Email1Param1);
			event1Email1.emailParams = event1Email1Params;
			event1.companyEventEmails = event1Emails;
			event1.eventDate = new Date("04/20/2009");
			event1.eventTypeCd = "Viola";
			event1.eventTypeName = "Viola";
			event1.eventTypeDescription = "The viola was on a {SourcePayrollRunId}";
			events.addItem(event1);
			
			var event2:CompanyEventItem = new CompanyEventItem();
			event2.creatorId = "Rutherford Hayes";
			event2.lastNoteDate = null;
			event2.eventGroupCode = "Fish";
			var event2Details:ArrayCollection = new ArrayCollection();
			var event2Detail1:CompanyEventDetail = new CompanyEventDetail();
			event2Detail1.eventDetailTypeCd = "SourcePayrollRunId";
			event2Detail1.name = "Source Payroll Run ID";
			event2Detail1.value = "6";
			event2Details.addItem(event2Detail1);
			event2.companyEventDetails = event2Details;					
			event2.eventDate = new Date("04/30/2009");
			event2.eventTypeCd = "Salmon";
			event2.eventTypeName = "Salmon";
			event2.eventTypeDescription = "The salmon was on a {SourcePayrollRunId}";
			events.addItem(event2);
			
			eventReturn.events = events;			
			
			
			
			return eventReturn;		
		}
			
		public static function getCompanyEventsOther():CompanyEventQueryReturn {
			var eventReturn:CompanyEventQueryReturn = new CompanyEventQueryReturn();			
		

			var events:ArrayCollection = new ArrayCollection();			
			
			var event2:CompanyEventItem = new CompanyEventItem();
			event2.creatorId = "Rutherford Hayes";
			event2.lastNoteDate = null;
			event2.eventGroupCode = "Fish";
			var event2Details:ArrayCollection = new ArrayCollection();
			var event2Detail1:CompanyEventDetail = new CompanyEventDetail();
			event2Detail1.eventDetailTypeCd = "SourcePayrollRunId";
			event2Detail1.name = "Source Payroll Run ID";
			event2Detail1.value = "6";
			event2Details.addItem(event2Detail1);
			event2.companyEventDetails = event2Details;					
			event2.eventDate = new Date("04/30/2009");
			event2.eventTypeCd = "Salmon";
			event2.eventTypeName = "Salmon";
			event2.eventTypeDescription = "The salmon was on a {SourcePayrollRunId}";
			events.addItem(event2);
			
			eventReturn.events = events;			
			
			
			
			return eventReturn;				
		}
		
		public static function getCompanyEventCreators():ArrayCollection {
			var creators:ArrayCollection = new ArrayCollection();
			creators.addItem("James Polk");
			creators.addItem("Rutherford Hayes");
			creators.addItem("Agent Andy");
			creators.addItem("FeeEventsBatchJob");
			return creators;
		}
		
		public static function getCompanyEventGroups():ArrayCollection {
			var groups:ArrayCollection = new ArrayCollection();
		
			var group1:CompanyEventGroup = new CompanyEventGroup();
			group1.eventGroupCode = "OrchestraInstruments";
			group1.name = "OrchestraInstruments";				
			var group1Items:ArrayCollection = new ArrayCollection();
			var group1Item1:CompanyEventGroupItem = new CompanyEventGroupItem();
			group1Item1.eventTypeCd = "Viola";
			group1Item1.eventTypeName = "Viola";
			group1Items.addItem(group1Item1);
			var group1Item2:CompanyEventGroupItem = new CompanyEventGroupItem();
			group1Item2.eventTypeCd = "Cello";
			group1Item2.eventTypeName = "Cello";
			group1Items.addItem(group1Item2);			
			var group1Item3:CompanyEventGroupItem = new CompanyEventGroupItem();
			group1Item3.eventTypeCd = "DoubleBass";
			group1Item3.eventTypeName = "Double Bass";
			group1Items.addItem(group1Item3);			
			group1.children  = group1Items;
			groups.addItem(group1);
			
			var group2:CompanyEventGroup = new CompanyEventGroup();
			group2.eventGroupCode = "BandInstruments";
			group2.name = "BandInstruments";
			var group2Items:ArrayCollection = new ArrayCollection();
			var group2Item1:CompanyEventGroupItem = new CompanyEventGroupItem();
			group2Item1.eventTypeCd = "Oboe";
			group2Item1.eventTypeName = "Oboe";
			group2Items.addItem(group2Item1);
			var group2Item2:CompanyEventGroupItem = new CompanyEventGroupItem();
			group2Item2.eventTypeCd = "FrenchHorn";
			group2Item2.eventTypeName = "French Horn";
			group2Items.addItem(group2Item2);		
			group2.children = group2Items;
			groups.addItem(group2);	
			
			var group3:CompanyEventGroup = new CompanyEventGroup();
			group3.eventGroupCode = "Fish";
			group3.name = "Fish";
			var group3Items:ArrayCollection = new ArrayCollection();
			var group3Item1:CompanyEventGroupItem = new CompanyEventGroupItem();
			group3Item1.eventTypeCd = "Salmon";
			group3Item1.eventTypeName = "Salmon";
			group3Items.addItem(group3Item1);
			group3.children = group3Items;
			groups.addItem(group3);
			
			return groups;
		}

        public static function getCompanyStatus():CompanyStatus {
            var companyStatus:CompanyStatus = new CompanyStatus();
            companyStatus.serviceStatusCollection = new ArrayCollection();
            var companyServiceStatus:CompanyServiceStatus = new CompanyServiceStatus();
            companyServiceStatus.status = new ServiceStatus();            
            companyServiceStatus.status.serviceSubStatusList = new ArrayCollection();
            companyStatus.serviceStatusCollection.addItem(companyServiceStatus);
            return companyStatus;
        }

        public static function getCompanyDdLimits():CompanyDdLimits {
            var companyDdLimits:CompanyDdLimits = new CompanyDdLimits();
            companyDdLimits.perEmployeeLimit = 5000;
            companyDdLimits.perPayrollLimit = 10000;
            return companyDdLimits;
        }

        public static function getCompanyOffers():ArrayCollection {
            var companyOffers:ArrayCollection = new ArrayCollection();
            var offer:Offer = new Offer();
            offer.description = "blah";
            offer.effectiveDate = new Date("10/24/2009");
            offer.expirationDate = new Date("12/20/2009");
            offer.name = "blah";
            offer.offerCd = "bl";
            companyOffers.addItem(offer);
            return companyOffers;
        }

        public static function getCheckPrintingBatchs():SearchResults {
            var result:SearchResults = new SearchResults();
            var resultList:ArrayCollection = new ArrayCollection();

            for (var i:int = 0; i<5; i++) {
                var batch:CheckPrintingBatch = new CheckPrintingBatch();
                batch.companyKey = new CompanyKey(SourceSystemEnum.QBDT.toString(), "123272727");
                batch.ein = "123456789";
                batch.legalName = "Some legal name";
                batch.paycheckCount = i;
                batch.paycheckDate = new Date("10/24/2009");
                batch.printBatchId = "1234";
                if(i % 2 == 0){
                    batch.printStatus = CheckPrintBatchStatusEnum.PENDING.toString();
                } else if(i % 3 == 0) {
                    batch.printStatus = CheckPrintBatchStatusEnum.SENT_TO_PRINTER.code.toString();
                    batch.sentToPrinterDate = new Date("12/24/2009"); 
                } else {
                    batch.printStatus = CheckPrintBatchStatusEnum.ERROR.toString();
                    batch.printMessage = "Printer Exploded!";
                    batch.sentToPrinterDate = new Date("12/24/2009");
                }
                
                resultList.addItem(batch);
            }

            result.returnsList = resultList;
            result.totalRecords = 500;
            return result;
        }

	}
}

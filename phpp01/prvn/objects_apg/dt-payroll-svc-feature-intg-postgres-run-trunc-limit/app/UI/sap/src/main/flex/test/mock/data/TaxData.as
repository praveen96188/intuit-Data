package test.mock.data
{
    import mx.collections.ArrayCollection;

    import psp.sap.model.Agency;
    import psp.sap.model.EmployeeTaxLedgerItem;
    import psp.sap.model.LawItem;
    import psp.sap.model.LawTransactions;
    import psp.sap.model.PaymentMethodNote;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.PaymentTemplateQuarterPayment;
    import psp.sap.model.PropertyAudit;
    import psp.sap.model.SearchResults;
    import psp.sap.model.TaxPaymentYear;
    import psp.sap.model.TaxPaymentsQueueItem;
    import psp.sap.model.TaxTransaction;

    public class TaxData
	{
		
		public static function getNoteHistory():ArrayCollection{
			var noteHistory:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i<2; i++)
			{
			var note:PaymentMethodNote = new PaymentMethodNote();
			note.createdDate = new Date(2008, 5, 12);
			note.notes = "This is note number " + i;
			
			noteHistory.addItem(note);
			}
			
			return noteHistory;
		}

		

		public static function getPropertyAuditHistory():ArrayCollection{
			var propertyAuditHistory:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i<10; i++)
			{
				var employee:PropertyAudit = new PropertyAudit();
				employee.createdDate = new Date(2008, 3, 25);
				employee.auditDate = new Date(2008, 5, 25);
				employee.newPropertyValue = "New Val" + i;
				employee.oldPropertyValue = "Old Val" + i;
				employee.propertyName = "Prop Name" + i;
				employee.userId = "88902" + i;

				propertyAuditHistory.addItem(employee);
			}
			
			return propertyAuditHistory;
		}
		
		public static function getGlobalTaxPayments():SearchResults {
			var searchResults:SearchResults = new SearchResults();
			var returnList:ArrayCollection = new ArrayCollection();			
			
			var taxPaymentsQueueItem:TaxPaymentsQueueItem = new TaxPaymentsQueueItem();
			taxPaymentsQueueItem.agencyId = "IRS";
			taxPaymentsQueueItem.amount = 1;
			taxPaymentsQueueItem.id = "0192-E813-1928-1282";
			taxPaymentsQueueItem.companyLegalName = "Bob's Chicken Shack";
			taxPaymentsQueueItem.companyId = "1000";
			
			returnList.addItem(taxPaymentsQueueItem);
			
			searchResults.totalRecords = returnList.length;
			searchResults.returnsList = returnList;
			return searchResults;
		}
			
		public static function getTaxPaymentYears():ArrayCollection {
			var arrayCollection:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 2000; i<2015; i++){
				var taxPaymentYear:TaxPaymentYear = new TaxPaymentYear();
				taxPaymentYear.year = i.toString();
				taxPaymentYear.paymentTemplates = new ArrayCollection();
				for(var j:int = 0; j<10; j++){
					var paymentTemplate:PaymentTemplate = new PaymentTemplate();
					paymentTemplate.paymentTemplateCd = "id_" + i + j;
					paymentTemplate.paymentTemplateName = "name_" + i + j;
					taxPaymentYear.paymentTemplates.addItem(paymentTemplate);
				}
				arrayCollection.addItem(taxPaymentYear);
			}
			
			return arrayCollection;
		}
		

		
		public static function getPaymentTemplateQuarters():ArrayCollection {
			var paymentQuarters:ArrayCollection = new ArrayCollection();
			for(var i:int = 2000; i<2005; i++){
				var Q1:PaymentTemplateQuarterPayment = new PaymentTemplateQuarterPayment();
				Q1.year = i.toString();
				Q1.paymentsMadeTotal = 100.00;
				Q1.pendingPaymentsTotal = 100.00;
				Q1.quarter = "Q1";
				Q1.quarterPaymentsTotal = 200.00;
				Q1.paymentTemplateCd = "IRS-941/944";
				Q1.paymentTemplateName = "IRS-941/944";
				paymentQuarters.addItem(Q1);				
				
				var Q2:PaymentTemplateQuarterPayment = new PaymentTemplateQuarterPayment();
				Q2.year = i.toString();
				Q2.paymentsMadeTotal = 200.00;
				Q2.pendingPaymentsTotal = 200.00;
				Q2.quarter = "Q2";
				Q2.quarterPaymentsTotal = 400.00;
				Q2.paymentTemplateCd = "IRS-941/944";
				Q2.paymentTemplateName = "IRS-941/944";
				paymentQuarters.addItem(Q2);
				
				var Q3:PaymentTemplateQuarterPayment = new PaymentTemplateQuarterPayment();
				Q3.year = i.toString();
				Q3.paymentsMadeTotal = 300.00;
				Q3.pendingPaymentsTotal = 300.00;
				Q3.quarter = "Q3";
				Q3.quarterPaymentsTotal = 600.00;
				Q3.paymentTemplateCd = "IRS-941/944";
				Q3.paymentTemplateName = "IRS-941/944";
				paymentQuarters.addItem(Q3);
				
				var Q4:PaymentTemplateQuarterPayment = new PaymentTemplateQuarterPayment();
				Q4.year = i.toString();
				Q4.paymentsMadeTotal = 400.00;
				Q4.pendingPaymentsTotal = 400.00;
				Q4.quarter = "Q4";
				Q4.quarterPaymentsTotal = 800.00;
				Q4.paymentTemplateCd = "IRS-941/944";
				Q4.paymentTemplateName = "IRS-941/944";
				paymentQuarters.addItem(Q4);
			}
			return paymentQuarters;			
		}
		

		private static const WH:LawItem = getLawItem("1","WH",new ArrayCollection(),false);
		private static const FICA:LawItem = getLawItem("2","FICA", new ArrayCollection([0.124,0]),true);
		private static const MED:LawItem = getLawItem("3","MED",new ArrayCollection([0.029,0]),true); 
		private static const FUTA:LawItem = getLawItem("5","FUTA",new ArrayCollection([0.0008,0]),true);
		private static const SDI:LawItem = getLawItem("12","SDI",new ArrayCollection(),true,0.001,0.1); //faking this as unlimited so i can test that one
		

		public static function getIrsPaymentTemplates():ArrayCollection {
			return getPaymentTemplates([getIrs941944PaymentTemplate(), getIrs940PaymentTemplate()]);			
		}		
		
		public static function getIrs941944PaymentTemplate():PaymentTemplate {
			return getPaymentTemplate("1","941/944",getPossibleDf(),getLawItems([WH, FICA, MED]));			
		}
		
		public static function getIrs940PaymentTemplate():PaymentTemplate {
			return getPaymentTemplate("2","940",getPossibleDf(),getLawItems([FUTA]));
		}
		
		public static function getCAEDDAgency():Agency {
			return getAgency("2","California - EDD",getCAEDDPaymentTemplates());	
		}	
		
		public static function getMadeUpAgency(id:String="3"):Agency {
			return getAgency(id,"Ohio Department of Silly Walks "+id,new ArrayCollection());
		}
		
		public static function getCAEDDPaymentTemplates():ArrayCollection {
			return getPaymentTemplates([getCAEDDPITPaymentTemplate()]);
		}	
		
		public static function getCAEDDPITPaymentTemplate():PaymentTemplate {
			return getPaymentTemplate("6","PIT/SDI",getPossibleDf(),getLawItems([WH, SDI]));
		}
		
		public static function getPossibleDf():ArrayCollection {
			return new ArrayCollection(["Weekly","Semi-Weekly", "Monthly", "Quarterly", "Anually"]);
		}
		
		public static function getAgency(id:String, name:String, paymentTemplates:ArrayCollection):Agency {
			var agency:Agency = new Agency();
			agency.agencyId = id;
			agency.agencyAbbrev= name;
			agency.paymentTemplates = paymentTemplates;
			return agency;
		}

		private static function getPaymentTemplates(templates:Array):ArrayCollection {
			return new ArrayCollection(templates);
		}
		
		private static function getPaymentTemplate(code:String, name:String, possibleDf:ArrayCollection, lawItems:ArrayCollection):PaymentTemplate {
			var pt:PaymentTemplate = new PaymentTemplate();
			pt.paymentTemplateCd = code;
			pt.paymentTemplateName = name;
			pt.lawItems = lawItems;			
			pt.possibleDepositFrequencies = possibleDf;
			return pt;
		}
		
		private static function getLawItems(lawItems:Array):ArrayCollection {
			return new ArrayCollection(lawItems);
		}
		
		private static function getLawItem(lawId:String, name:String, possibleRates:ArrayCollection, editable:Boolean, min:Number=NaN, max:Number=NaN):LawItem {
			var li:LawItem = new LawItem();
			li.lawId = lawId;
			li.name = name;
			return li;
		}

        
        public static function getAgencyHistory():ArrayCollection {
			return new ArrayCollection(
				[
					getPropertyAudit(new Date(2009,3,1,9,5),"FICA","FICA Rate Future","","12.40%","mgotchy"),
					getPropertyAudit(new Date(2009,3,1,9,5),"FICA","FICA Effective Date","","05/01/09","mgotchy"),
					getPropertyAudit(new Date(2009,3,3,11,5),"FICA","FICA Status","Active","Hold","mgotchy"),
					getPropertyAudit(new Date(2009,3,1,11,30),"Deposit Frequency","Deposit Frequency","Monthly","Semi-Weekly","mgotchy"),
					getPropertyAudit(new Date(2009,4,1,1,15),"FICA","FICA Rate","0%","12.40%","system")
				]);		
						
		}
		
		public static function getTaxPaymentHistory():ArrayCollection {
			return new ArrayCollection(
				[					
					getPropertyAudit(new Date(2009,3,1,9,5),"Status","Status","Pending\nExecution","Executed\nIn Agency","system"),
					getPropertyAudit(new Date(2009,3,3,11,5),"Status","Status","Executed\nIn Agency","Agency Rejection\nTaxpayer Not Enrolled","system"),
					getPropertyAudit(new Date(2009,3,3,11,30),"Deposit Frequency","Deposit Frequency","Monthly","Semi-Monthly","mgotchy"),
					getPropertyAudit(new Date(2008,3,2,9,5),"Paid Date","Paid Date","","Apil 3, 2009","system"),
					getPropertyAudit(new Date(2009,3,3,11,40),"Agency ID","Agency ID","12-345678","12-435678","mgotchy"),
					getPropertyAudit(new Date(2009,3,4,12,5),"Paid Date","Paid Date","April 3, 2009","April 4, 2009","system"),
					getPropertyAudit(new Date(2009,3,4,12,10),"Status","Status","Agency Rejection\nTaxpayer Not Enrolled","Pending\nRe-execution","mgotchy"),
					getPropertyAudit(new Date(2009,3,4,12,25),"Status","Status","Pending\nRe-execution","Re-execution\nIn Agency","system"),
					getPropertyAudit(new Date(2009,3,4,12,25),"Status","Status","Re-execution\nIn Agency","Successful\nRe-executed","system")
				]);					
		}
		
		private static function getPropertyAudit(date:Date, category:String, prop:String, oldValue:String, newValue:String, userId:String):PropertyAudit {
			var pa:PropertyAudit = new PropertyAudit();
			pa.auditDate = date;
			pa.category = category;
			pa.createdDate = date;
			pa.newPropertyValue = newValue;
			pa.oldPropertyValue = oldValue;
			pa.propertyName = prop;
			pa.userId = userId;
			return pa;
		}
		
		public static function getTaxTransactions():ArrayCollection {
			var taxTransactions:ArrayCollection = new ArrayCollection();
			for(var i:int = 0; i<8; i++){
				var lawTransactions:LawTransactions = new LawTransactions();
				lawTransactions.law = WH;
				lawTransactions.currentTaxesSum = 10000;
				lawTransactions.taxTransactions = new ArrayCollection();
				for(var j:int = 0; j < 100; j++){
					var taxTransaction:TaxTransaction = new TaxTransaction();
					taxTransaction.checkPaymentDate = new Date();
					taxTransaction.currentTaxes = 21310;
					taxTransaction.currentWages = 32420;
					taxTransaction.paymentMethod = "EFE";
					taxTransaction.paymentStatus = "Complete";
					taxTransaction.QTDTaxes = 12403;
					taxTransaction.QTDWages = 34203;
					taxTransaction.submissionDate = new Date();
					taxTransaction.txnDescription = "Take on Return";
					taxTransaction.YTDTaxes = 32420;
					taxTransaction.YTDWages = 23420;
					lawTransactions.taxTransactions.addItem(taxTransaction);					
				}
				taxTransactions.addItem(lawTransactions);
			}
			return taxTransactions;
		}

		public static function getEmployeeLedgerItems():ArrayCollection {
			var ledgerItems:ArrayCollection = new ArrayCollection();
			for(var i:int = 0; i<100; i++){
				var employeeLedgerItem:EmployeeTaxLedgerItem = new EmployeeTaxLedgerItem();
				employeeLedgerItem.employeeName = "a really long name, a really long last name";
				employeeLedgerItem.socialSecurityNumber = "555-55-5555";
				employeeLedgerItem.taxableWages = 1000000.00;
				employeeLedgerItem.taxAmount = 1000000.00;
				employeeLedgerItem.totalWages = 1000000.00;
				ledgerItems.addItem(employeeLedgerItem);
			}
			
			return ledgerItems;
		}
		

				
				

	}
}

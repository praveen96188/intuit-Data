package test.sap.service
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
	
	import psp.sap.model.ActionEvent;
	import psp.sap.model.BillingTransaction;
	import psp.sap.model.CompanyLedgerAccount;
	import psp.sap.model.MoneyMovementTransaction;
	import psp.sap.model.OfferingServiceChargeTypeEnum;
	import psp.sap.model.Paycheck;
	import psp.sap.model.PayrollBillingTransactions;
	import psp.sap.model.PayrollEmployeeTransaction;
	import psp.sap.model.PayrollRun;
	import psp.sap.model.PayrollTransaction;
	import psp.sap.model.TransactionType;
	
	import test.sap.application.SAPTestBase;
	
	public class PayrollRunServiceTest extends SAPTestBase
	{
		public static function suite():TestSuite {
			return new TestSuite(PayrollRunServiceTest);
		}

        /***************************************************************
        * PayrollRunSearch -- this tests the PayrollRunRemoteService.refundEmployerTransaction function.
        * 
        * START
        */
        public function testPayrollRunSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollRunSearch_Step2, 5);
        }
        
        private function testPayrollRunSearch_Step2(e:ResultEvent):void {
        	login(testPayrollRunSearch_Step3);
        }
        
        private function testPayrollRunSearch_Step3(e:ResultEvent):void {
		    mSAP.payrollRunService.findPayrollRunsByDate("1234567",
		    	"QBOE",
                new ArrayCollection(["Regular"]), null, null,
		    	getTestResponder(testPayrollRunSearch_Step4, 2));
		}			

        private function testPayrollRunSearch_Step4(e:ResultEvent):void {            
            // check result
			assertTrue(e.result != null);
			// result should be a collection
        	assertTrue(e.result is ArrayCollection);
        	var payrollRunList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(payrollRunList.length > 0);
        	// a collection of Payroll Runs
        	assertTrue(payrollRunList.getItemAt(0) is PayrollRun);
        	var payrollRun:PayrollRun = payrollRunList.getItemAt(0) as PayrollRun;        	
        	// check the bank account
        	assertNotNull("bank account", payrollRun.bankAccount);
        	assertNotNull("sourceBankAccountName", payrollRun.bankAccount.bankName);
        	assertNotNull("accountType", payrollRun.bankAccount.accountType);
        	assertNotNull("accountNumber", payrollRun.bankAccount.accountNumber);
        	assertNotNull("routingNumber", payrollRun.bankAccount.routingNumber);
        	// check action collection
        	assertTrue(payrollRun.actionCollection is ArrayCollection);
        	var actionCollection: ArrayCollection = payrollRun.actionCollection as ArrayCollection;
        	assertTrue(actionCollection.length > 0);
        	// check one of the actions        	        
        	assertTrue(actionCollection.getItemAt(0) is ActionEvent);
        	var payrollRunActionEvent: ActionEvent = actionCollection.getItemAt(0) as ActionEvent;
        	// check all properties
        	assertTrue(nothingNull(payrollRunActionEvent))        	
        } 
        /** END - PayrollRunSearch **/

        /***************************************************************
        * PayrollLedgerAccountSearch -- this tests the search for ledger accounts.
        * 
        * START
        */
        public function testPayrollLedgerAccountSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollLedgerAccountSearch_Step2, 5);		
        }
        
        private function testPayrollLedgerAccountSearch_Step2(e:ResultEvent):void {
        	login(testPayrollLedgerAccountSearch_Step3);
        }
        
        private function testPayrollLedgerAccountSearch_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findLedgerAccountsByPayroll("1234567", 
        			"QBOE", 
        			"BatchTest05", 
        			getTestResponder(testPayrollLedgerAccountSearch_Step4, 2));
        }           
        
        private function testPayrollLedgerAccountSearch_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	var list:ArrayCollection = e.result as ArrayCollection;
        	assertTrue(list.length > 0);
        	for each (var ledgerAccount:CompanyLedgerAccount in list) {
        		assertTrue(this.nothingNull(ledgerAccount));
        	}
        	testPayrollLedgerAccountSearch_Step5();
        }

        private function testPayrollLedgerAccountSearch_Step5():void {
        	mSAP.payrollRunService.findLedgerAccounts("1234567",
        			"QBOE",
        			getTestResponder(testPayrollLedgerAccountSearch_Step6, 2));
        }

        private function testPayrollLedgerAccountSearch_Step6(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	var list:ArrayCollection = e.result as ArrayCollection;
        	assertTrue(list.length > 0);
        	for each (var ledgerAccount:CompanyLedgerAccount in list) {
        		assertTrue(this.nothingNull(ledgerAccount));
        	}
        }  		
        /** END - PayrollLedgerAccountSearch **/

        /***************************************************************
        * PayrollLedgerTransactionsSearch -- this tests the search for ledger transactions.
        *
        * START
        */
        public function testPayrollLedgerTransactionSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollLedgerTransactionSearch_Step2, 5);
        }

        private function testPayrollLedgerTransactionSearch_Step2(e:ResultEvent):void {
        	login(testPayrollLedgerTransactionSearch_Step3);
        }
        
        private function testPayrollLedgerTransactionSearch_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findTransactionsByLedgerAccountAndPayroll(
        			"QBOE",
        			"1234567",
        			"DDFutureLiability",
        			"BatchTest05",
        			getTestResponder(testPayrollLedgerTransactionSearch_Step4, 2));
        }

        private function testPayrollLedgerTransactionSearch_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	var list:ArrayCollection = e.result as ArrayCollection;
        	assertTrue(list.length > 0);
        	for each (var ledgerTransaction:PayrollTransaction in list) {
        		assertNotNull(ledgerTransaction.amount);
        		assertNotNull(ledgerTransaction.txnDate);
        		assertNotNull(ledgerTransaction.txnType);
        		assertNotNull(ledgerTransaction.status);
        		assertNotNull(ledgerTransaction.credit);
        	}

        	mSAP.payrollRunService.findTransactionsByLedgerAccount(
        			"QBOE",
        			"1234567",
        			"DDFutureLiability",
        			getTestResponder(testPayrollLedgerTransactionSearch_Step5, 2));
        }

        private function testPayrollLedgerTransactionSearch_Step5(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	var list:ArrayCollection = e.result as ArrayCollection;
        	assertTrue(list.length > 0);
        	for each (var ledgerTransaction:PayrollTransaction in list) {
        		assertNotNull(ledgerTransaction.amount);
        		assertNotNull(ledgerTransaction.txnDate);
        		assertNotNull(ledgerTransaction.txnType);
        		assertNotNull(ledgerTransaction.status);
        		assertNotNull(ledgerTransaction.credit);
        	}
        }
        /** END - PayrollLedgerAccountSearch **/

        /***************************************************************
        * PayrollEEFinancialTransactionsSearch -- this tests the search for employee financial txns.
        * 
        * START
        */
        public function testPayrollEEFinancialTransactionsSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollEEFinancialTransactionsSearch_Step2, 5);		
        }
        
        private function testPayrollEEFinancialTransactionsSearch_Step2(e:ResultEvent):void {
        	login(testPayrollEEFinancialTransactionsSearch_Step3);
        }
        
        private function testPayrollEEFinancialTransactionsSearch_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployeeTransactions("1234567", 
        			"QBOE", 
        			"BatchTest05",
        			null,
        			null, 
        			getTestResponder(testPayrollEEFinancialTransactionsSearch_Step4, 2));
        }
        
        private function testPayrollEEFinancialTransactionsSearch_Step4(e:ResultEvent):void {
        	assertNotNull(e.result);
        	assertTrue(e.result is ArrayCollection);
        	var eeTransactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("Number of transactions", 2, eeTransactions.length);        	
        }
        /** END - PayrollEEFinancialTransactionsSearch **/
        
        /***************************************************************
        * PaycheckSearch -- this tests the search for employee paychecks
        *
        * START
        *
        */
//        public function testPaycheckSearch():void {
//        	runDataLoader("Payroll :: Multiple payrolls with Fed and CA taxes", testPaycheckSearch_Step2, 5);
//        }
//
//        private function testPaycheckSearch_Step2(e:ResultEvent):void {
//        	login(testPaycheckSearch_Step3);
//        }
//
//        private function testPaycheckSearch_Step3(e:ResultEvent):void {
//        	mSAP.payrollRunService.findEmployeePaychecks("1234567",
//        			"GEMINI",
//        			"Payroll1",
//        			false,
//        			getTestResponder(testPaycheckSearch_Step4, 2));
//        }
//
//        private function testPaycheckSearch_Step4(e:ResultEvent):void {
//        	assertNotNull(e.result);
//        	assertTrue(e.result is ArrayCollection);
//        	var paychecks:ArrayCollection = e.result as ArrayCollection;
//        	assertEquals("Number of Paychecks", 3, paychecks.length);
//        	for each(var paycheck:Paycheck in paychecks){
//        		assertNotNull("sourcePaycheckId", paycheck.sourcePaycheckId);
//        		assertNotNull("paycheckDate", paycheck.paycheckDate);
//        		assertEquals("voidedAfterOffload", false, paycheck.voidedAfterOffload);
//        		assertNotNull("employeeName", paycheck.employeeName);
//        	}
//        }
        /** END - PayrollEEFinancialTransactionsSearch **/

        /***************************************************************
        * PayrollERFinancialTransactionsSearch -- this tests the search for employer financial txns.
        * 
        * START
        */
        public function testPayrollERFinancialTransactionsSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollERFinancialTransactionsSearch_Step2, 5);		
        }
        
        private function testPayrollERFinancialTransactionsSearch_Step2(e:ResultEvent):void {
        	login(testPayrollERFinancialTransactionsSearch_Step3);
        }
        
        private function testPayrollERFinancialTransactionsSearch_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployerTransactions("1234567", 
        			"QBOE", 
        			"BatchTest05",
        			null,
        			null, 
        			getTestResponder(testPayrollERFinancialTransactionsSearch_Step4, 2));
        }
        
        private function testPayrollERFinancialTransactionsSearch_Step4(e:ResultEvent):void {
        	assertNotNull(e.result);
        	assertTrue(e.result is ArrayCollection);
        	var erTransactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("Number of transactions", 1, erTransactions.length);        	
        }
        /** END - PayrollERFinancialTransactionsSearch */

        /***************************************************************
        * PayrollIntuitFinancialTransactionsSearch -- this tests the search for intuit financial txns.
        * 
        * START
        */
        public function testPayrollIntuitFinancialTransactionsSearch():void {
        	runDataLoader("Company :: Create Basic Data", testPayrollIntuitFinancialTransactionsSearch_Step2, 5);		
        }
        
        private function testPayrollIntuitFinancialTransactionsSearch_Step2(e:ResultEvent):void {
        	login(testPayrollIntuitFinancialTransactionsSearch_Step3);
        }
        
        private function testPayrollIntuitFinancialTransactionsSearch_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findIntuitTransactions("1234567", 
        			"QBOE", 
        			"BatchTest05",
        			null,
        			null, 
        			getTestResponder(testPayrollIntuitFinancialTransactionsSearch_Step4, 2));
        }
        
        private function testPayrollIntuitFinancialTransactionsSearch_Step4(e:ResultEvent):void {
        	assertNotNull(e.result);        	
        }
        /** END - PayrollIntuitFinancialTransactionsSearch */
        
        /***************************************************************
        * AgencyTransactionSearch -- this tests the search for agency financial txns.
        * 
        * START
        */
//        public function testAgencyFinancialTransactionsSearch():void {
//        	runDataLoader("Payroll :: Multiple payrolls with Fed and CA taxes", testAgencyFinancialTransactionsSearch_Step2, 5);
//        }
//
//        private function testAgencyFinancialTransactionsSearch_Step2(e:ResultEvent):void {
//        	login(testAgencyFinancialTransactionsSearch_Step3);
//        }
//
//        private function testAgencyFinancialTransactionsSearch_Step3(e:ResultEvent):void {
//        	mSAP.payrollRunService.findAgencyTransactions("1234567",
//        			"GEMINI",
//        			"Payroll1",
//        			null,
//        			null,
//        			getTestResponder(testAgencyFinancialTransactionsSearch_Step4, 2));
//        }
//
//        private function testAgencyFinancialTransactionsSearch_Step4(e:ResultEvent):void {
//        	assertNotNull(e.result);
//        	assertTrue(e.result is ArrayCollection);
//        	var transactions:ArrayCollection = e.result as ArrayCollection;
//        	assertEquals("Number of transactions", 9, transactions.length);
//        }
        /** END - PayrollERFinancialTransactionsSearch */

        /***************************************************************
        * CancelPayrollTransaction -- this tests cancelling a payroll transaction.
        * 
        * START
        */
        public function testCancelPayrollTransaction():void {
        	runDataLoader("Company :: Create Basic Data", testCancelPayrollTransaction_Step2, 5);		
        }
        
        private function testCancelPayrollTransaction_Step2(e:ResultEvent):void {
        	login(testCancelPayrollTransaction_Step3);
        }
        
        private function testCancelPayrollTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.cancelPayrollTransaction("1234567",
				"QBOE",
				new ArrayCollection(["EEBA2PS1"]),
				"BatchTest05",
				getTestResponder(testCancelPayrollTransaction_Step4, 2));	        	
        }
        
        private function testCancelPayrollTransaction_Step4(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployeeTransactions(
        		"1234567",
        		"QBOE",
        		"BatchTest05",
        		null,
        		null,
        		getTestResponder(testCancelPayrollTransaction_Step5, 2));
        }
        
        private function testCancelPayrollTransaction_Step5(e:ResultEvent):void {
        	assertTrue("Not null", e.result != null);
        	assertTrue("Is an ArrayCollection", e.result is ArrayCollection);
        	var txnList:ArrayCollection = e.result as ArrayCollection;
        	var ourTxnFound:Boolean = false;
        	for each (var txnObj:Object in txnList) {
				assertTrue("Is employee txn", txnObj is PayrollEmployeeTransaction);
				var txn:PayrollEmployeeTransaction = PayrollEmployeeTransaction(txnObj);
				if (txn.transactionId == "EEBA2PS1") {
					ourTxnFound = true;
					assertEquals("Transaction Status", txn.status, "Cancelled");
				}
        	}
        	assertTrue("Found cancelled transaction", ourTxnFound);
        }
        /** END - CancelPayrollTransaction */
        
        /***************************************************************
        * TransactionTypeList -- this tests cancelling a payroll transaction.
        * 
        * START
        */
        public function testTransactionTypeList():void {
        	runDataLoader("Company :: Create Basic Data", testTransactionTypeList_Step2, 5);		
        }
        
        private function testTransactionTypeList_Step2(e:ResultEvent):void {
        	login(testTransactionTypeList_Step3);
        }
        
        private function testTransactionTypeList_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.getTransactionTypeList(
        		getTestResponder(testTransactionTypeList_Step4, 2));
        }
        
        private function testTransactionTypeList_Step4(e:ResultEvent):void {
        	assertTrue(e.result != null);
        	assertTrue(e.result is ArrayCollection);
        	var TransactionTypeList: ArrayCollection = e.result as ArrayCollection;
        	assertTrue(TransactionTypeList.getItemAt(0) is TransactionType);
        	var transactionType: TransactionType = TransactionTypeList.getItemAt(0) as TransactionType;
        	assertTrue(nothingNull(transactionType));
        }
        /** END - CancelPayrollTransaction */
        
        /**
        * RefundFinancialTransaction -- this tests the PayrollRunRemoteService.refundEmployerTransaction function.
        * 
        * START
        */
        public function testRefundFinancialTransaction():void {
        	runDataLoader("Payroll :: Issue Refund Test", testRefundFinancialTransaction_Step2, 5);		
        }
        
        private function testRefundFinancialTransaction_Step2(e:ResultEvent):void {
        	login(testRefundFinancialTransaction_Step3);
        }
        
        private function testRefundFinancialTransaction_Step3(e:ResultEvent):void {
			mSAP.payrollRunService.findEmployerTransactions("123272727",
				"QBOE",
				"BatchId01",
				null,
				null,
				getTestResponder(testRefundFinancialTransaction_Step4, 2)); 	
        }
        
        private function testRefundFinancialTransaction_Step4(e:ResultEvent):void {
        	var txnId:String = null
        	
        	for each (var txn:PayrollTransaction in (e.result as ArrayCollection)) {
        		if (txn.status == "Returned") {
        			txnId = txn.id;
        			break;
        		}	
        	}
        	
        	assertNotNull("Transaction Id", txnId);
        	
        	mSAP.payrollRunService.refundEmployerTransaction(
        		"QBOE", 
        		"123272727", 
        		txnId,
        		50.0, 
        		new Date("2008", "5", "1"), 
        		"ACH", 
        		getTestResponder(testRefundFinancialTransaction_Step5, 2));
        	  	
        }
        
        private function testRefundFinancialTransaction_Step5(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - RefundFinancialTransaction */
        
        /**
        * AddEscalationTransaction -- this tests the PayrollRunRemoteService.addEscalation function.
        * 
        * START
        */
        public function testAddEscalationTransaction():void {
        	runDataLoader("Payroll :: Add Escalation Test", testAddEscalationTransaction_Step2, 5);		
        }
        
        private function testAddEscalationTransaction_Step2(e:ResultEvent):void {
        	login(testAddEscalationTransaction_Step3);
        }
        
        private function testAddEscalationTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.addEscalation("QBOE",
        		"123272727",
        		"MyBatch1",
        		false,
        		"Wire",
        		100.00,
        		new Date(2007, 8, 1),
        		getTestResponder(testAddEscalationTransaction_Step4, 2));
        }
        
        private function testAddEscalationTransaction_Step4(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployerTransactions("123272727",
        		"QBOE",
        		"MyBatch1",
        		null,
        		null,
        		getTestResponder(testAddEscalationTransaction_Step5, 2));
        }
        
        private function testAddEscalationTransaction_Step5(e:ResultEvent):void {
        	var employerTxns:ArrayCollection = e.result as ArrayCollection;
        	
        	assertEquals("Number of txns", employerTxns.length, 2);
        	
        	var escalationTxn:PayrollTransaction = null;
        	for each (var txn:PayrollTransaction in employerTxns) {
        		if (txn.txnType == "EmployerEscalationCredit") {
        			escalationTxn = txn;	
        		}
        	}
        	assertNotNull("Escalation Transaction", escalationTxn);
        	
        	assertEquals("Transaction amount", escalationTxn.amount, 100.0);
        }
        /** END - AddEscalationTransaction */

        /**
        * ReverseEmployeeTransaction -- this tests the PayrollRunRemoteService.reversePayrollRunTransactions function.
        * 
        * START
        */
        public function testReverseEmployeeTransaction():void {
        	runDataLoader("Payroll :: Reverse Transaction Test", testReverseEmployeeTransaction_Step2, 10);
        }
        
        private function testReverseEmployeeTransaction_Step2(e:ResultEvent):void {
        	login(testReverseEmployeeTransaction_Step3);
        }
        
        private function testReverseEmployeeTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployeeTransactions("123272727",
        		"QBOE",
        		"BatchId01",
        		null,
        		null,
        		getTestResponder(testReverseEmployeeTransaction_Step4, 3));	
        }
        
        private function testReverseEmployeeTransaction_Step4(e:ResultEvent):void {
        	var eeTxns:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("Number of txns", eeTxns.length, 4);
        	
        	var txn1:PayrollTransaction = eeTxns.getItemAt(0) as PayrollTransaction;
        	var txn2:PayrollTransaction = eeTxns.getItemAt(2) as PayrollTransaction;

        	var txnIds:Array = [txn1.transactionId, txn2.transactionId];
        	
        	mSAP.payrollRunService.reversePayrollRunTransactions("QBOE",
        		"123272727",
        		new ArrayCollection(txnIds),        		
        		"BatchId01",
        		true,
        		null,
        		"ACH",
        		false,
        		getTestResponder(testReverseEmployeeTransaction_Step5, 3));        		       	
        }
        
        private function testReverseEmployeeTransaction_Step5(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployeeTransactions("123272727",
        		"QBOE",
        		"BatchId01",
        		null,
        		null,
        		getTestResponder(testReverseEmployeeTransaction_Step6, 3));	
        }
        
        private function testReverseEmployeeTransaction_Step6(e:ResultEvent):void {
        	var eeTxns:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("Number of txns", eeTxns.length, 6);
        	
        	var reversalCount:Number = 0;
        	for each (var txn:PayrollTransaction in eeTxns) {
				if (txn.txnType == "EmployeeDdReversalDebit") {
					reversalCount++;
				}        		
        	}
        	
        	assertEquals("Reversal Txn Count", 2, reversalCount);
        }
        /** END - ReverseEmployeeTransaction */

        /**
        * AddFeeTransaction -- this tests the PayrollRunRemoteService.addFeeTransaction function.
        *
        * START
        */
        public function testAddFeeTransaction():void {
        	runDataLoader("Payroll :: Add Employer Fee Test", testAddFeeTransaction_Step2, 10);
        }

        private function testAddFeeTransaction_Step2(e:ResultEvent):void {
        	login(testAddFeeTransaction_Step3);
        }
        
        private function testAddFeeTransaction_Step3(e:ResultEvent):void {
//        	mSAP.payrollRunService.addFeeTransactions("123272727",
//        		"QBOE",
//        		"BatchId01",
//        		"ACH",
//        		new Date(2007, 8, 1),
//        		100.0,
//        		0,
//        		0,
//        		getTestResponder(testAddFeeTransaction_Step4, 3));
        }

        private function testAddFeeTransaction_Step4(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddFeeTransaction */

        /**
        * AddFeeRedebitTransaction -- this tests the PayrollRunRemoteService.addFeeTransaction function.
        *
        * START
        */
        public function testAddFeeRedebitTransaction():void {
        	runDataLoader("Payroll :: Add Redebit Test", testAddFeeRedebitTransaction_Step2, 10);
        }

        private function testAddFeeRedebitTransaction_Step2(e:ResultEvent):void {
        	login(testAddFeeRedebitTransaction_Step3);
        }

        private function testAddFeeRedebitTransaction_Step3(e:ResultEvent):void {
        	/*mSAP.payrollRunService.addFeeRedebitTransaction("QBOE",
        		"123272727",
        		"123123",
        		getTestResponder(testAddFeeRedebitTransaction_Step3));*/

        }
        /** END - AddFeeRedebitTransaction */

        /**
        * VoidTransaction -- this tests the PayrollRunRemoteService.addFeeTransaction function.
        *
        * START
        */
        public function testVoidTransaction():void {
        	runDataLoader("Payroll :: Void Txn ACH Test", testVoidTransaction_Step2, 10);
        }

        private function testVoidTransaction_Step2(e:ResultEvent):void {
        	login(testVoidTransaction_Step3);
        }
        
        private function testVoidTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployerTransactions("123272727",
        		"QBOE",
        		"BatchId01",
        		null,
        		null,
        		getTestResponder(testVoidTransaction_Step4));
        }

        private function testVoidTransaction_Step4(e:ResultEvent):void {
        	var employerTxns:ArrayCollection = e.result as ArrayCollection;

        	var finTxId:String = null;

        	for each (var txn:PayrollTransaction in employerTxns) {
        		if (txn.status == "Completed") {
        			finTxId = txn.id;
        		}
        	}

        	assertNotNull("Executed transaction id", finTxId);            
        	mSAP.payrollRunService.voidTransaction("QBOE",
        		"123272727",
        		finTxId,
        		getTestResponder(testVoidTransaction_Step5));
        }

        private function testVoidTransaction_Step5(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - VoidTransaction */

        /**
        * CancelTransaction -- this tests the PayrollRunRemoteService.cancelEmployeeTransaction function.
        *
        * START
        */
        public function testCancelTransaction():void {
        	runDataLoader("Payroll :: Cancel Employer Txn Test", testCancelTransaction_Step2, 10);
        }

        private function testCancelTransaction_Step2(e:ResultEvent):void {
        	login(testCancelTransaction_Step3);
        }
        
        private function testCancelTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findEmployerTransactions("123272727",
        		"QBOE",
        		"BatchId01",
        		null,
        		null,
        		getTestResponder(testCancelTransaction_Step4));
        }

        private function testCancelTransaction_Step4(e:ResultEvent):void {
        	var employerTxns:ArrayCollection = e.result as ArrayCollection;

        	var finTxId:String = null;

        	for each (var txn:PayrollTransaction in employerTxns) {
        		if (   (txn.txnType == "EmployerDdRejectRefundCredit")
        		    && (txn.status == "Created")) {
        			finTxId = txn.id;
        		}
        	}

        	assertNotNull("Executed transaction id", finTxId);

        	mSAP.payrollRunService.cancelTransaction("QBOE",
        		"123272727",
        		finTxId,
        		getTestResponder(testCancelTransaction_Step5));
        }

        private function testCancelTransaction_Step5(e:ResultEvent):void {

        }
        /** END - CancelTransaction */        

        /**
        * AddWriteOffBadDebtTransaction -- this tests the PayrollRunRemoteService.addWriteOffBadDebtTransaction function.
        *
        * START
        */
        public function testAddWriteOffBadDebtTransaction():void {
        	runDataLoader("Payroll :: Add Bad Debt Write-Off Test", testAddWriteOffBadDebtTransaction_Step2, 10);
        }

        private function testAddWriteOffBadDebtTransaction_Step2(e:ResultEvent):void {
        	login(testAddWriteOffBadDebtTransaction_Step3);
        }
        
        private function testAddWriteOffBadDebtTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.addWriteOffBadDebtTransaction("QBOE",
        		"1234567",
        		"BatchTest05",
        		getTestResponder(testAddWriteOffBadDebtTransaction_Step4));
        }

        private function testAddWriteOffBadDebtTransaction_Step4(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddWriteOffBadDebtTransaction */

        /**
        * AddRecoverBadDebtTransaction -- this tests the PayrollRunRemoteService.addRecoverBadDebtTransaction function.
        *
        * START
        */
        public function testAddRecoverBadDebtTransaction():void {
        	runDataLoader("Payroll :: Add Recover Bad Debt Txn With Fee Test", testAddRecoverBadDebtTransaction_Step2, 10);
        }

        private function testAddRecoverBadDebtTransaction_Step2(e:ResultEvent):void {
        	login(testAddRecoverBadDebtTransaction_Step3);
        }
        
        private function testAddRecoverBadDebtTransaction_Step3(e:ResultEvent):void {        	        	
        	mSAP.payrollRunService.findPayrollUnrecoveredBalances("QBDT",
        		"123272727",
        		"BatchId01",        		
        		getTestResponder(testAddRecoverBadDebtTransaction_Step4, 2));
        }
        
        private function testAddRecoverBadDebtTransaction_Step4(e:ResultEvent):void {
        	assertTrue("return type", e.result is ArrayCollection);
        	var transactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("number of transactions", 1, transactions.length);
        	        	
        	var unrecoveredTransactions:PayrollBillingTransactions = 
        				transactions.getItemAt(0) as PayrollBillingTransactions;
        				
//        	if(unrecoveredTransactions.ddTransaction != null){
//        		unrecoveredTransactions.ddTransaction.readValues();
//        		unrecoveredTransactions.ddTransaction.writeValues();
//        	}
        	
        	if(unrecoveredTransactions.taxTransaction != null){
        		unrecoveredTransactions.taxTransaction.readValues();
        		unrecoveredTransactions.taxTransaction.writeValues();
        	}
        	
        	for each(var feeTxn:BillingTransaction in unrecoveredTransactions.feeTransactions){
        		feeTxn.readValues();
        		feeTxn.writeValues();        		
        	}
        	
        	var returnTransactions:ArrayCollection = new ArrayCollection();
        	returnTransactions.addItem(unrecoveredTransactions);        	        	
        	        	
        	mSAP.payrollRunService.addRecoverBadDebtTransactions(
        		"QBDT",
        		"123272727",        		
        		"Wire",
        		mSAP.PSPDate,
        		returnTransactions,
                0,
        		getTestResponder(testAddRecoverBadDebtTransaction_Step5, 2));
        }

        private function testAddRecoverBadDebtTransaction_Step5(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddRecoverBadDebtTransaction */

        /**
        * AddFeeTransferTransaction -- this tests the PayrollRunRemoteService.addFeeTransferTransaction function.
        *
        * START
        */
        public function testAddFeeTransferTransaction():void {
        	runDataLoader("Payroll :: Add Fee Transfer Test", testAddFeeTransferTransaction_Step2, 10);
        }

        private function testAddFeeTransferTransaction_Step2(e:ResultEvent):void {
        	login(testAddFeeTransferTransaction_Step3);
        }
        
        private function testAddFeeTransferTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.addFeeTransferTransaction("QBOE",
        		"1234567",
        		"BatchTest05",
        		75.00,
        		OfferingServiceChargeTypeEnum.NSF.code,
        		getTestResponder(testAddFeeTransferTransaction_Step4));
        }

        private function testAddFeeTransferTransaction_Step4(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddFeeTransferTransaction */

        /**
        * AddRefundTransaction -- this tests the PayrollRunRemoteService.addRefundTransaction function.
        *
        * START
        */
        public function testAddRefundTransaction():void {
        	runDataLoader("Payroll :: Add Refund Txn Test", testAddRefundTransaction_Step2, 10);
        }

        private function testAddRefundTransaction_Step2(e:ResultEvent):void {
        	login(testAddRefundTransaction_Step3);
        }
        
        private function testAddRefundTransaction_Step3(e:ResultEvent):void {
        	// TODO -- Company 123272727 does not exist
        	mSAP.payrollRunService.addRefundTransaction("QBOE",
        		"1234567",
        		"BatchTest05",
        		1705.81,
        		new Date(2007, 11, 10),
        		"ACH",
        		getTestResponder(testAddRefundTransaction_Step4));
        }

        private function testAddRefundTransaction_Step4(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddRefundTransaction */

        /**
        * AddEmployerReturnRefundTransaction -- this tests the PayrollRunRemoteService.addEmployerReturnRefundTransaction function.
        *
        * START
        */
        public function testAddEmployerReturnRefundTransaction():void {
        	runDataLoader("Payroll :: Add Employer Return Refund Txn Test", testAddEmployerReturnRefundTransaction_Step2, 10);
        }

        private function testAddEmployerReturnRefundTransaction_Step2(e:ResultEvent):void {
        	login(testAddEmployerReturnRefundTransaction_Step3);
        }
        
        private function testAddEmployerReturnRefundTransaction_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.addEmployerReturnRefundTransaction("QBOE",
        		"1234567",
        		"BatchTest05",
        		1705.81,
                1705.82,
        		new Date(2007, 8, 15),
        		"ACH",
        		getTestResponder(testAddEmployerReturnRefundTransaction_Step4));
        }

        private function testAddEmployerReturnRefundTransaction_Step4(e:ResultEvent):void {
        	trace("Success!");
        }
        /** END - AddEmployerReturnRefundTransaction */
        
        public function testPayrollGetUncollectedAmounts():void {
        	runDataLoader("Payroll :: Add Redebit Test (QBDT)", testPayrollGetUncollectedAmounts_Step2, 5);
        }
        
        private function testPayrollGetUncollectedAmounts_Step2(e:ResultEvent):void {
        	login(testPayrollGetUncollectedAmounts_Step3);
        }
        
		private function testPayrollGetUncollectedAmounts_Step3(e:ResultEvent):void {
		    mSAP.payrollRunService.findPayrollUncollectedBalances(
                "8574536",
                "QBDT",		    	
		    	"BatchTest09",
		    	getTestResponder(testPayrollGetUncollectedAmounts_Step4, 2));        	
        }
        
        private function testPayrollGetUncollectedAmounts_Step4(e:ResultEvent):void {
            // check result
            assertNotNull("null result event", e.result);
			// result should be a collection
        	assertTrue("expected result type", e.result is ArrayCollection);
        	var transactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("number of transactions", 1, transactions.length);
        	
        	var payrollUncollectedTxns:PayrollBillingTransactions = transactions.getItemAt(0) as PayrollBillingTransactions;
        	
        	// should *never* be null        	     
        	assertNotNull("payrollRunId", payrollUncollectedTxns.payrollRunId);
        	assertNotNull("uncollectedFeeTransactions", payrollUncollectedTxns.feeTransactions);
        	
        	// tests particular to this set of data
//        	assertNotNull("uncollectedPayrollTransaction", payrollUncollectedTxns.ddTransaction);
//        	assertNotNull("uncollectedPayrollTransaction.feeTxnId", payrollUncollectedTxns.ddTransaction.financialTxnId);
//        	assertNotNull("uncollectedPayrollTransaction.feeTxnType", payrollUncollectedTxns.ddTransaction.financialTxnType);
//        	assertNotNull("uncollectedPayrollTransaction.feeAmount", payrollUncollectedTxns.ddTransaction.financialAmount);
//        	assertNotNull("uncollectedPayrollTransaction.feeRedebitAmount", payrollUncollectedTxns.ddTransaction.financialReturnAmount);
//        	assertEquals(	"feeAmount does not equal feeRedebitAmount",
//        					payrollUncollectedTxns.ddTransaction.financialAmount,
//        					payrollUncollectedTxns.ddTransaction.financialReturnAmount);
//        	assertNull("uncollectedPayrollTransaction.salesTaxTxnId", payrollUncollectedTxns.ddTransaction.salesTaxTxnId);
        					
        	
        	assertEquals("payrollUncollectedTxns.uncollectedFeeTransactions.length", payrollUncollectedTxns.feeTransactions.length, 1);
        	
        	// fee txn tests
        	var uncollectedFeeTxn:BillingTransaction = payrollUncollectedTxns.feeTransactions.getItemAt(0) 
        															as BillingTransaction;
        	assertNotNull("uncollectedFeeTxn.feeTxnId", uncollectedFeeTxn.financialTxnId);
        	assertNotNull("uncollectedFeeTxn.feeTxnType", uncollectedFeeTxn.financialTxnType);
        	assertNotNull("uncollectedFeeTxn.feeAmount", uncollectedFeeTxn.financialAmount);
        	assertNotNull("uncollectedFeeTxn.feeRedebitAmount", uncollectedFeeTxn.financialReturnAmount);
			assertEquals(	"feeAmount does not equal feeRedebitAmount",
							uncollectedFeeTxn.financialAmount, 
        					uncollectedFeeTxn.financialReturnAmount);

			// associated sales tax txn tests
			// sales tax not included any more due to changes in the sales tax manager
        	/*assertNotNull("uncollectedFeeTxn.salesTaxTxnId", uncollectedFeeTxn.salesTaxTxnId);
        	assertNotNull("uncollectedFeeTxn.salesTaxAmount", uncollectedFeeTxn.salesTaxAmount);
        	assertNotNull("uncollectedFeeTxn.salesTaxRedebitAmount", uncollectedFeeTxn.salesTaxRedebitAmount);
			assertEquals(	"salesTaxAmount does not equal salesTaxRedebitAmount",
							uncollectedFeeTxn.salesTaxAmount, 
        					uncollectedFeeTxn.salesTaxRedebitAmount);*/       	
        }
        
        public function testPayrollAddRedebits():void {
        	runDataLoader("Payroll :: Add Redebit Test (QBDT)", testPayrollAddRedebits_Step2, 5);
        }
        
        private function testPayrollAddRedebits_Step2(e:ResultEvent):void {
        	login(testPayrollAddRedebits_Step3);
        }
        
        private function testPayrollAddRedebits_Step3(e:ResultEvent):void {
		    mSAP.payrollRunService.findPayrollUncollectedBalances(
                "8574536",
                "QBDT",		    	
		    	"BatchTest09", 
		    	getTestResponder(testPayrollAddRedebits_Step4, 2));        	
        }
        
        private function testPayrollAddRedebits_Step4(e:ResultEvent):void {
        	assertNotNull("uncollected payroll not found", e.result);
        	assertTrue("wrong result type for uncollected payroll", e.result is ArrayCollection);
        	
        	var transactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("number of transactions", 1, transactions.length);
        	        	
        	var unrecoveredTransactions:PayrollBillingTransactions = 
        				transactions.getItemAt(0) as PayrollBillingTransactions;
        				
//        	if(unrecoveredTransactions.ddTransaction != null){
//        		unrecoveredTransactions.ddTransaction.readValues();
//        		unrecoveredTransactions.ddTransaction.writeValues();
//        	}
        	
        	if(unrecoveredTransactions.taxTransaction != null){
        		unrecoveredTransactions.taxTransaction.readValues();
        		unrecoveredTransactions.taxTransaction.writeValues();
        	}
        	
        	for each(var feeTxn:BillingTransaction in unrecoveredTransactions.feeTransactions){
        		feeTxn.readValues();
        		feeTxn.writeValues();        		
        	}
        	
        	var returnTransactions:ArrayCollection = new ArrayCollection();
        	returnTransactions.addItem(unrecoveredTransactions);        	        	
        	        	
        	mSAP.payrollRunService.redebitPayrollTransactions(
        		"QBDT",
        		"8574536",        		
        		"Wire",
        		mSAP.PSPDate,
        		returnTransactions,
        		getTestResponder(testPayrollAddRedebits_Step5, 2));
        }
        
        private function testPayrollAddRedebits_Step5(e:ResultEvent):void {
        	trace("Success!");
        }

		/**
        * testfindMoneyMovementTransactions -- this tests the PayrollRunRemoteService.findMoneyMovementTransactions function.
        *
        * START
        */
        public function testfindMoneyMovementTransactions():void {
        	runDataLoader("Payroll :: Reverse Transaction Test", testfindMoneyMovementTransactions_Step2, 10);
        }

        private function testfindMoneyMovementTransactions_Step2(e:ResultEvent):void {
        	login(testfindMoneyMovementTransactions_Step3);
        }
        
        private function testfindMoneyMovementTransactions_Step3(e:ResultEvent):void {
        	mSAP.payrollRunService.findMoneyMovementTransactions("QBOE",
												        		"123272727",        		
												        		null,        		
												        		getTestResponder(testfindMoneyMovementTransactions_Step4));
        }

        private function testfindMoneyMovementTransactions_Step4(e:ResultEvent):void {
        	assertNotNull("There is a result", e.result);
        	assertTrue("result is an array collection", e.result is ArrayCollection);
        	var transactions:ArrayCollection = e.result as ArrayCollection;
        	assertEquals("Number of transactions", 3, transactions.length);
        	for each(var transaction:MoneyMovementTransaction in transactions){
        		assertNotNull("ach reason", transaction.achReason);
        		assertNotNull("ach amount", transaction.achAmount);
        		assertNotNull("bank Account", transaction.bankAccount);
        		assertNotNull("creation Date", transaction.creationDate);
        		assertNotNull("settlement Date", transaction.settlementDate);
        		assertNotNull("spcf Id", transaction.spcfId);
        	}        	        
        }
        /** END - testfindMoneyMovementTransactions */

    }
}

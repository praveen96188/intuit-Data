package test.mock.data
{
    import mx.collections.ArrayCollection;

    import psp.sap.model.AgencyTransaction;
    import psp.sap.model.BillingTransaction;
    import psp.sap.model.CompanyBankAccount;
    import psp.sap.model.MoneyMovementTransaction;
    import psp.sap.model.Paycheck;
    import psp.sap.model.PayrollACHDetailSet;
    import psp.sap.model.PayrollBillingTransactions;
    import psp.sap.model.PayrollEmployeeTransaction;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;
    import psp.sap.model.PropertyAudit;
    import psp.sap.model.SuspectPaycheck;

    public class PayrollData
    {
        public static function findPayrollRun():ArrayCollection {
            var payrolls:ArrayCollection = new ArrayCollection();

            var payroll:PayrollRun = new PayrollRun();
            payroll.bankAccount = PayrollData.getBankAccount();
            payroll.hasVoidedPaycheck = false;
            payroll.id = "payroll1";
            payroll.paycheckDate = new Date(2008, 5, 25);
            payroll.paycheckSettlementDate = new Date(2008, 5, 23);
            payroll.payrollNetAmount = 5000.00;
            payroll.payrollRunDate = new Date(2008, 5, 22);
            payroll.payrollRunStatus = "Pending";
            payroll.sourcePayRunId = "sourceId1";
            payroll.actionCollection = new ArrayCollection();
            payrolls.addItem(payroll);

            return payrolls;
        }

        public static function findPayrollRun2():PayrollRun {
            var payroll:PayrollRun = new PayrollRun();
            payroll.bankAccount = PayrollData.getBankAccount();
            payroll.hasVoidedPaycheck = false;
            payroll.id = "payroll1";
            payroll.paycheckDate = new Date(2008, 5, 25);
            payroll.paycheckSettlementDate = new Date(2008, 5, 23);
            payroll.payrollNetAmount = 5000.00;
            payroll.payrollRunDate = new Date(2008, 5, 22);
            payroll.payrollRunStatus = "Pending";
            payroll.sourcePayRunId = "10111213";
            payroll.actionCollection = new ArrayCollection();
            return payroll;
        }

        public static function getCompanyPayrolls():ArrayCollection {
            var payrolls:ArrayCollection = new ArrayCollection();

            var payroll:PayrollRun = new PayrollRun();
            payroll.bankAccount = PayrollData.getBankAccount();
            payroll.hasVoidedPaycheck = false;
            payroll.id = "payroll1";
            payroll.paycheckDate = new Date(2008, 5, 25);
            payroll.paycheckSettlementDate = new Date(2008, 5, 23);
            payroll.payrollNetAmount = 5000.00;
            payroll.payrollRunDate = new Date(2008, 5, 22);
            payroll.payrollRunStatus = "Pending";
            payroll.sourcePayRunId = "sourceId1";
            payroll.actionCollection = new ArrayCollection();
            payrolls.addItem(payroll);

            var payroll2:PayrollRun = new PayrollRun();
            payroll2.bankAccount = PayrollData.getBankAccount();
            payroll2.hasVoidedPaycheck = false;
            payroll2.id = "payroll2";
            payroll2.paycheckDate = new Date(2008, 5, 19);
            payroll2.paycheckSettlementDate = new Date(2008, 5, 15);
            payroll2.payrollNetAmount = 5000.00;
            payroll2.payrollRunDate = new Date(2008, 5, 14);
            payroll2.payrollRunStatus = "Pending";
            payroll2.sourcePayRunId = "sourceId2";
            payroll2.actionCollection = new ArrayCollection();
            payrolls.addItem(payroll2);

            return payrolls;
        }

        private static function getBankAccount():CompanyBankAccount {
            var bankAccount:CompanyBankAccount = new CompanyBankAccount();
            bankAccount.accountId = "accountId";
            bankAccount.accountNumber = "123456789";
            bankAccount.accountType = "Checking";
            bankAccount.bankAccountStatusCd = "Active";
            bankAccount.bankName = "B of A";
            bankAccount.routingNumber = "111111118";
            bankAccount.sourceBankAccountId = "sourceAccountId";
            bankAccount.sourceBankAccountName = "sourceAccountName";

            return bankAccount;
        }

        public static function getPayrollTransactions():ArrayCollection {
            var transactions:ArrayCollection = new ArrayCollection();

            transactions.addItem(PayrollData.buildPayrollTransaction(new PayrollTransaction(), "Created", "ER Payroll Debit"));
            transactions.addItem(PayrollData.buildPayrollTransaction(new PayrollTransaction(), "Created", "ER Payroll Debit"));

            return transactions;
        }

        public static function getPayrollEmployeeTransactions():ArrayCollection {
            var transactions:ArrayCollection = new ArrayCollection();

            for(var i:int=0; i<3; i++){
                var transaction:PayrollEmployeeTransaction = new PayrollEmployeeTransaction();
                PayrollData.buildPayrollTransaction(PayrollTransaction(transaction), (i % 2) ? "Executed" : "Created", "EmployeeDdCredit");
                PayrollData.buildPayrollEmployeeTransaction(transaction);
                transactions.addItem(transaction);
            }

            return transactions;
        }

        public static function getPayrollPaychecks():ArrayCollection {
            var paychecks:ArrayCollection = new ArrayCollection();

            paychecks.addItem(PayrollData.buildPaycheck(new Paycheck()));
            paychecks.addItem(PayrollData.buildPaycheck(new Paycheck()));
            paychecks.addItem(PayrollData.buildPaycheck(new Paycheck()));
            paychecks.addItem(PayrollData.buildPaycheck(new Paycheck()));

            return paychecks;
        }

        public static function getAgencyTransactions():ArrayCollection {
            var transactions:ArrayCollection = new ArrayCollection();

            for(var i:int=0; i<3; i++){
                var transaction:AgencyTransaction = new AgencyTransaction();
                PayrollData.buildPayrollTransaction(PayrollTransaction(transaction), "Created", "ER Payroll Debit");
                PayrollData.buildAgencyTransaction(transaction);
                transactions.addItem(transaction);
            }

            return transactions;
        }

        private static function buildPayrollTransaction(transaction:PayrollTransaction, status:String, transactionType:String):PayrollTransaction {
            transaction.amount = 100.00;
            transaction.credit = false;
            transaction.returnCd = "R01";
            transaction.settlementType = "ACH";
            transaction.sourcePayRunId = "10111213";
            transaction.status = status;
            transaction.transactionId = "123456";
            transaction.txnType = transactionType;
            transaction.actionCollection = new ArrayCollection();

            return transaction;
        }

        private static function buildPayrollEmployeeTransaction(transaction:PayrollEmployeeTransaction):PayrollEmployeeTransaction {
            transaction.txnType = "EmployeeDdCredit";
            transaction.credit = true;
            transaction.employeeBankAccountNumber = "01230123";
            transaction.employeeBankRoutingNumber = "111111118";
            transaction.employeeName = "Chilly Man";
            transaction.voidedAfterOffload = false;

            return transaction;
        }

        private static function buildAgencyTransaction(transaction:AgencyTransaction):AgencyTransaction {
            transaction.agencyAbbreviation = "CA-EDD";
            transaction.agencyName = "EDD";
            transaction.taxAbbreviation = "SDI";
            transaction.taxDescription = "State Dis. Ins.";
            transaction.txnType = "Agency Tax Credit";

            return transaction;
        }

        private static function buildPaycheck(paycheck:Paycheck):Paycheck {
            paycheck.employeeName = "Chilly Man";
            paycheck.netPaycheckAmount = 500.00;
            paycheck.sourcePaycheckId = "0123789";
            paycheck.voidedAfterOffload = false;

            return paycheck;
        }

        public static function getMoneyMovementTransactions():ArrayCollection {
            var transactions:ArrayCollection = new ArrayCollection();

            for(var i:int=0; i<3; i++){
                var transaction:MoneyMovementTransaction = new MoneyMovementTransaction();
                PayrollData.buildMoneyMovementTransaction(transaction);
                transactions.addItem(transaction);
            }

            return transactions;
        }

        private static function buildMoneyMovementTransaction(moneyMovementTransaction:MoneyMovementTransaction):MoneyMovementTransaction {
            moneyMovementTransaction.achAmount = 100.00;
            moneyMovementTransaction.achReason = "reason";
            moneyMovementTransaction.bankAccount = PayrollData.getBankAccount();
            moneyMovementTransaction.checkDate = new Date();
            moneyMovementTransaction.creationDate = new Date();
            moneyMovementTransaction.settlementDate = new Date();
            moneyMovementTransaction.spcfId = "spcfId";

            return moneyMovementTransaction;
        }

        public static function getMoneyMovementTransactionDetail():PayrollACHDetailSet {
            var detailSet:PayrollACHDetailSet = new PayrollACHDetailSet();

            detailSet.feeTransactions = new ArrayCollection();
            for(var i:int = 0; i<8; i++){
                var feeTransaction:PayrollTransaction = new PayrollTransaction();
                feeTransaction.amount = 10000;
                feeTransaction.description = "Long Fee Name" + i;
                detailSet.feeTransactions.addItem(feeTransaction);
            }
            detailSet.feeTransactionsTotal = 100000;

            detailSet.taxTransactions = new ArrayCollection();
            for(var j:int = 0; j<9; j++){
                var agencyTransaction:AgencyTransaction = new AgencyTransaction();
                agencyTransaction.amount = 100000;
                agencyTransaction.agencyAbbreviation = "CA-EDD";
                agencyTransaction.taxAbbreviation = "A very very long law" + j;
                detailSet.taxTransactions.addItem(agencyTransaction);
            }
            detailSet.taxTransactionsTotal = 100000;

            detailSet.taxCreditTransactions = new ArrayCollection();
            for(var k:int = 0; k<10; k++){
                var agencyTransactionCredit:AgencyTransaction = new AgencyTransaction();
                agencyTransactionCredit.amount = 100000;
                agencyTransactionCredit.agencyAbbreviation = "CA-EDD";
                agencyTransactionCredit.taxAbbreviation = "A very very long law" + k;
                detailSet.taxCreditTransactions.addItem(agencyTransactionCredit);
            }
            detailSet.taxCreditTransactionsTotal = 100000;

            detailSet.ddTransactions = new ArrayCollection();
            for(var l:int = 0; l<11; l++){
                var employeeTransaction:PayrollEmployeeTransaction = new PayrollEmployeeTransaction();
                employeeTransaction.amount = 100000;
                employeeTransaction.employeeName = "this is a long name for " + l;
                employeeTransaction.employeeBankAccountNumber = "123456789012";
                employeeTransaction.employeeBankRoutingNumber = "123456789";
                detailSet.ddTransactions.addItem(employeeTransaction);
            }
            detailSet.ddTransactionsTotal = 100000;

            return detailSet;
        }

        public static function getBillingTransactions():ArrayCollection {
            var billingTransactions:PayrollBillingTransactions = new PayrollBillingTransactions();
            billingTransactions.checkDate = new Date();
            billingTransactions.taxTransaction = PayrollData.buildBillingTransaction(3.00, "taxTxn");
//            billingTransactions.ddTransaction = PayrollData.buildBillingTransaction(1.00, "ddTxn");
            billingTransactions.feeTransactions = new ArrayCollection();
            billingTransactions.feeTransactions.addItem(PayrollData.buildBillingTransaction(2.00, "feeTxn", "fee", 5.00, "salesTaxTxn"));
            billingTransactions.initiationDate = null;
            billingTransactions.payrollRunId = "1257";

            var collection:ArrayCollection = new ArrayCollection();
            collection.addItem(billingTransactions);
            return collection;
        }

        private static function buildBillingTransaction(financialAmount:Number, financialTxnId:String, financialTxnType:String=null, salesTaxAmount:Number=NaN, salesTaxTxnId:String=null):BillingTransaction {
            var billingTransaction:BillingTransaction = new BillingTransaction();
            billingTransaction.financialAmount = financialAmount;
            billingTransaction.financialReturnAmount = financialAmount;
            billingTransaction.financialTxnId = financialTxnId;
            billingTransaction.financialTxnType = financialTxnType;
            billingTransaction.salesTaxAmount = salesTaxAmount;
            billingTransaction.salesTaxReturnAmount = salesTaxAmount;
            billingTransaction.salesTaxTxnId = salesTaxTxnId;
            return billingTransaction;
        }

        public static function getPayrollTransactionHistroy():ArrayCollection {
            var returnList:ArrayCollection = new ArrayCollection();

            for(var i:int = 0; i < 10; i++){
                var propertyAudit:PropertyAudit = new PropertyAudit();
                // vm test does not check any of the values on the property audit
                returnList.addItem(propertyAudit);
            }

            return returnList;
        }

        public static function getSuspectPaychecks():ArrayCollection {
            var paychecks:ArrayCollection = new ArrayCollection();
            for(var i:int = 0; i<5; i++){
                var suspectPaycheck:SuspectPaycheck = new SuspectPaycheck();
                suspectPaycheck.employeeName = "A" + i;
                paychecks.addItem(suspectPaycheck);
            }

            return paychecks;
        }

    }
}

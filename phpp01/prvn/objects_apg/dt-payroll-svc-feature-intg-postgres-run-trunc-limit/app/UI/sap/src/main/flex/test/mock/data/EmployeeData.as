package test.mock.data
{
    import mx.collections.ArrayCollection;

    import psp.sap.model.EmployeeBankAccountFraud;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.Paycheck;
    import psp.sap.model.PropertyAudit;

    public class EmployeeData
	{
		public static function getEmployeeFraudBankAccounts():ArrayCollection{
			var employeeFraudBankAccounts:ArrayCollection = new ArrayCollection();
			var employeeFraudBankAccount:EmployeeBankAccountFraud = new EmployeeBankAccountFraud();
			var employeeFraudBankAccount2:EmployeeBankAccountFraud = new EmployeeBankAccountFraud();
			var employeeList:ArrayCollection = new ArrayCollection();
			var employeeList2:ArrayCollection = new ArrayCollection();
			
			var employeeInfo1:EmployeeInfo = new EmployeeInfo();
			employeeInfo1.firstName = "Douglas";
			employeeInfo1.lastName = "Johnson";
			employeeList.addItem(employeeInfo1);
			
			var employeeInfo2:EmployeeInfo = new EmployeeInfo();
			employeeInfo2.firstName = "Chris";
			employeeInfo2.lastName = "Applewood";
			employeeList.addItem(employeeInfo2);
			
			var employeeInfo3:EmployeeInfo = new EmployeeInfo();
			employeeInfo3.firstName = "Mark";
			employeeInfo3.lastName = "Stevenson";
			employeeList.addItem(employeeInfo3);
			
			var employeeInfo4:EmployeeInfo = new EmployeeInfo();
			employeeInfo4.firstName = "Larry";
			employeeInfo4.lastName = "Wood";
			employeeList.addItem(employeeInfo4);
			
			var employeeInfo5:EmployeeInfo = new EmployeeInfo();
			employeeInfo5.firstName = "Larry";
			employeeInfo5.lastName = "Wood";
			employeeList2.addItem(employeeInfo5);
			
			var employeeInfo6:EmployeeInfo = new EmployeeInfo();
			employeeInfo6.firstName = "Larry";
			employeeInfo6.lastName = "Wood";
			employeeList2.addItem(employeeInfo6);
			
			employeeFraudBankAccount.bankAccountNumber = "123213123";
			employeeFraudBankAccount.bankName = "Bank of America";
			employeeFraudBankAccount.employeeInfo = employeeList;
			
			employeeFraudBankAccount2.bankAccountNumber = "234234234";
			employeeFraudBankAccount2.bankName = "Bank of America";
			employeeFraudBankAccount2.employeeInfo = employeeList2;
			
			employeeFraudBankAccounts.addItem(employeeFraudBankAccount);
			employeeFraudBankAccounts.addItem(employeeFraudBankAccount2);
			
			return employeeFraudBankAccounts;
		}

		public static function getEmployeeInfo():EmployeeInfo{			
			var employeeInfo:EmployeeInfo = new EmployeeInfo();
			employeeInfo.dd = true;
			employeeInfo.employeeGseq = "1248-2912-492E-1928";
			employeeInfo.employeeId = "123";
			employeeInfo.firstName = "Charles";
			employeeInfo.lastName = "Manchesterbillingsworth";
			employeeInfo.middleName = "Oregano";
			employeeInfo.status = "Active";
			employeeInfo.stateLive = "CA";
			employeeInfo.stateWork = "CA";
			employeeInfo.firstPayDate = new Date(2008, 1, 1);
			employeeInfo.lastPayDate = new Date(2009,5, 1);
			return employeeInfo;
		}

		public static function getMultipleEmployees():ArrayCollection {
			var employees:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i<10; i++)
			{
				var newEmployee:EmployeeInfo = getEmployeeInfo();
				newEmployee.firstName = "Rick";
				newEmployee.lastName = "Sanchez" + i
				newEmployee.employeeId = "123" + i;
				employees.addItem(newEmployee);
			}
			
			return employees;
		}


		public static function getEmployeeProfileHistory():ArrayCollection{
			var employeeProfileHistory:ArrayCollection = new ArrayCollection();
			
			for(var i:int = 0; i<10; i++)
			{
				var employee:PropertyAudit = new PropertyAudit();
				employee.createdDate = new Date(2008, 3, 25);
				employee.auditDate = new Date(2008, 5, 25);
				employee.newPropertyValue = "New Val" + i;
				employee.oldPropertyValue = "Old Val" + i;
				employee.propertyName = "Prop Name" + i;
				employee.userId = "88902" + i;

				employeeProfileHistory.addItem(employee);
			}
			
			return employeeProfileHistory;
		}
		
		
		
		public static function getEmployeePaychecks():ArrayCollection {
			var retPaychecks:ArrayCollection = new ArrayCollection();
			
			var paycheck1:Paycheck = new Paycheck();
			paycheck1.netPaycheckAmount = 500.00;
			paycheck1.sourcePaycheckId = "EE1Paycheck"
			paycheck1.voidedAfterOffload = false;	
			paycheck1.paycheckDate = new Date();
			retPaychecks.addItem(paycheck1);
			
			var paycheck2:Paycheck = new Paycheck();
			paycheck2.netPaycheckAmount = 500.00;
			paycheck2.sourcePaycheckId = "EE2Paycheck"
			paycheck2.voidedAfterOffload = false;	
			paycheck2.paycheckDate = new Date();
			retPaychecks.addItem(paycheck2);
			
			var paycheck3:Paycheck = new Paycheck();
			paycheck3.netPaycheckAmount = 500.00;
			paycheck3.sourcePaycheckId = "EE3Paycheck"
			paycheck3.voidedAfterOffload = false;	
			paycheck3.paycheckDate = new Date();
			retPaychecks.addItem(paycheck3);
			
			return retPaychecks;
		}
		

	}
}
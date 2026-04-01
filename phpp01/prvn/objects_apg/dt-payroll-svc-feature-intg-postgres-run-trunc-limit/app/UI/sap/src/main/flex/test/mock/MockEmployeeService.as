package test.mock
{
    import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;

    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.EmployeeComplianceData;
    import psp.sap.service.interfaces.IEmployeeService;
	
	public class MockEmployeeService extends MockAsyncService implements IEmployeeService
	{

		public function expectsCheckEmployeeBankAccountFraud(sourceSystemCd:String, companyId:String, payrollRunId:String):Mock {
            return expects("checkEmployeeBankAccountFraud").withArgs(sourceSystemCd, companyId, payrollRunId);
        }
		public function checkEmployeeBankAccountFraud(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void {
            record("checkEmployeeBankAccountFraud", sourceSystemCd, companyId, payrollRunId);
			sendAsyncResult(responder,"checkEmployeeBankAccountFraud");
        }

		public function expectsGetEmployeeProfileHistory(sourceSystemCd:String, companyId:String, employeeId:String):Mock {
            return expects("getEmployeeProfileHistory").withArgs(sourceSystemCd, companyId, employeeId);
        }
		public function getEmployeeProfileHistory(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
            record("getEmployeeProfileHistory", sourceSystemCd, companyId, employeeId);
			sendAsyncResult(responder,"getEmployeeProfileHistory");
        }

		public function expectsGetEmployeeTaxabilityInfo(sourceSystemCd:String, companyId:String, employeeId:String):Mock {
            return expects("getEmployeeTaxabilityInfo").withArgs(sourceSystemCd, companyId, employeeId);
        }
		public function getEmployeeTaxabilityInfo(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
            record("getEmployeeTaxabilityInfo", sourceSystemCd, companyId, employeeId);
			sendAsyncResult(responder,"getEmployeeTaxabilityInfo");
        }

		public function expectsGetEmployeeComplianceData(pWagePlanId:String):Mock {
            return expects("getEmployeeComplianceData").withArgs(pWagePlanId);
        }
		public function getEmployeeComplianceData(pWagePlanId:String, responder:IResponder):void {
            record("getEmployeeComplianceData", pWagePlanId);
			sendAsyncResult(responder,"getEmployeeComplianceData");
        }

		public function expectsGetEmployeeProfilePaycheckDetail(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, paycheckFromDate:Date, paycheckToDate:Date):Mock {
            return expects("getEmployeeProfilePaycheckDetail").withArgs(pSourceSystemCd, pCompanyId, pEmployeeId, paycheckFromDate, paycheckToDate);
        }
		public function getEmployeeProfilePaycheckDetail(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, paycheckFromDate:Date, paycheckToDate:Date, responder:IResponder):void {
            record("getEmployeeProfilePaycheckDetail", pSourceSystemCd, pCompanyId, pEmployeeId, paycheckFromDate, paycheckToDate);
			sendAsyncResult(responder,"getEmployeeProfilePaycheckDetail");
        }

		public function expectsGetEmployeeProfileQTDYTDDetails(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String):Mock {
            return expects("getEmployeeProfileQTDYTDDetails").withArgs(pSourceSystemCd, pCompanyId, pEmployeeId);
        }
		public function getEmployeeProfileQTDYTDDetails(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
            record("getEmployeeProfileQTDYTDDetails", pSourceSystemCd, pCompanyId, pEmployeeId);
			sendAsyncResult(responder,"getEmployeeProfileQTDYTDDetails");
        }

        public function getEmployeePaychecks(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
            record("getEmployeePaychecks", pSourceSystemCd, pCompanyId, pEmployeeId);
			sendAsyncResult(responder,"getEmployeePaychecks");
        }

        public function expectsGetEmployeePaychecks(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String):Mock {
            return expects("getEmployeePaycheckDetails").withArgs(pSourceSystemCd, pCompanyId, pEmployeeId);
        }

        public function getEmployeeHistory(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
            record("getEmployeeHistory", pSourceSystemCd, pCompanyId, pEmployeeId);
			sendAsyncResult(responder,"getEmployeeHistory");
        }

        public function expectsGetEmployeeHistory(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String):Mock {
            return expects("getEmployeeHistory").withArgs(pSourceSystemCd, pCompanyId, pEmployeeId);
        }

		public function expectsGetEmployeeComplianceDataList(sourceSystemCd:String, companyId:String, employeeId:String):Mock {
            return expects("getEmployeeComplianceDataList").withArgs(sourceSystemCd, companyId, employeeId);
        }
		public function getEmployeeComplianceDataList(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
            record("getEmployeeComplianceDataList", sourceSystemCd, companyId, employeeId);
			sendAsyncResult(responder,"getEmployeeComplianceDataList");
        }

		public function expectsUpdateEmployeeComplianceData(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, pComplianceData:EmployeeComplianceData, pOperation:String):Mock {
            return expects("updateEmployeeComplianceData").withArgs(pSourceSystemCd, pCompanyId, pEmployeeId, pComplianceData, pOperation);
        }
		public function updateEmployeeComplianceData(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, pComplianceData:EmployeeComplianceData, pOperation:String, responder:IResponder):void {
            record("updateEmployeeComplianceData", pSourceSystemCd, pCompanyId, pEmployeeId, pComplianceData, pOperation);
			sendAsyncResult(responder,"updateEmployeeComplianceData");
        }

		public function expectsFindEmployeesByCriteria(sourceSystemCd:String, pCompanyId:String, pSourceEmployeeId:String, pSsn:String, pName:String, pLiveState:String, pWorkState:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean):Mock {
            return expects("findEmployeesByCriteria").withArgs(sourceSystemCd, pCompanyId, pSourceEmployeeId, pSsn, pName, pLiveState, pWorkState, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
        }
		public function findEmployeesByCriteria(sourceSystemCd:String, pCompanyId:String, pSourceEmployeeId:String, pSsn:String, pName:String, pLiveState:String, pWorkState:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
            record("findEmployeesByCriteria", sourceSystemCd, pCompanyId, pSourceEmployeeId, pSsn, pName, pLiveState, pWorkState, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
			sendAsyncResult(responder,"findEmployeesByCriteria");
        }

        public function expectsUpdateEmployeeSeasonal(pSourceCompanyId:String, pEmployeeId:String, pEmployeeInfo:EmployeeInfo):Mock {
            return expects("updateEmployeeSeasonal").withArgs(pSourceCompanyId, pEmployeeId, pEmployeeInfo);
        }
        public function updateEmployeeSeasonal(pSourceCompanyId:String, pEmployeeId:String, pEmployeeInfo:EmployeeInfo, responder:IResponder):void {
            record("updateEmployeeSeasonal",pSourceCompanyId, pEmployeeId, pEmployeeInfo);
            sendAsyncResult(responder,"updateEmployeeSeasonal");
        }

    }
}
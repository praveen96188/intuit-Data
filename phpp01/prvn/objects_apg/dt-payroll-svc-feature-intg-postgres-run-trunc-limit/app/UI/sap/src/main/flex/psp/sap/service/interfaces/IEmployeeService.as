package psp.sap.service.interfaces
{
	import mx.rpc.IResponder;

    import psp.sap.model.EmployeeComplianceData;
	import psp.sap.model.EmployeeInfo;

public interface IEmployeeService extends IPSPService
	{

		function checkEmployeeBankAccountFraud(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void;

		function getEmployeeProfileHistory(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void;

		function getEmployeeTaxabilityInfo(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void;

		function getEmployeeComplianceData(pWagePlanId:String, responder:IResponder):void;

		function getEmployeeProfilePaycheckDetail(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, paycheckFromDate:Date, paycheckToDate:Date, responder:IResponder):void;

		function getEmployeeProfileQTDYTDDetails(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void;

        function getEmployeePaychecks(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void;

        function getEmployeeHistory(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void;

		function getEmployeeComplianceDataList(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void;

		function updateEmployeeComplianceData(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, pComplianceData:EmployeeComplianceData, pOperation:String, responder:IResponder):void;

		function findEmployeesByCriteria(sourceSystemCd:String, pCompanyId:String, pSourceEmployeeId:String, pSsn:String, pName:String, pLiveState:String, pWorkState:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void;

		function updateEmployeeSeasonal(pSourceCompanyId:String, pEmployeeId:String, pEmployeeInfo: EmployeeInfo, responder:IResponder):void;
	}
}
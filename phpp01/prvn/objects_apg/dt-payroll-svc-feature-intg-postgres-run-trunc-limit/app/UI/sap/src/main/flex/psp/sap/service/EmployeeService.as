package psp.sap.service
{

	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.RemoteObject;

	import psp.sap.model.EmployeeInfo;
    import psp.sap.model.EmployeeComplianceData;
    import psp.sap.service.interfaces.IEmployeeService;
	
	public class EmployeeService extends PSPService implements IEmployeeService
	{
		        		
		public function EmployeeService():void {
			remoteObjectPool = new RemoteObjectPool("employeeservice", 2, true);
		}

        public function get employeeRemoteService():RemoteObject {
            return remoteObjectPool.nextAvailable();
        }

		public function checkEmployeeBankAccountFraud(sourceSystemCd:String, companyId:String, payrollRunId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.checkEmployeeBankAccountFraud(sourceSystemCd, companyId, payrollRunId));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeProfileHistory(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeProfileHistory(sourceSystemCd, companyId, employeeId));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeTaxabilityInfo(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeTaxabilityInfo(sourceSystemCd, companyId, employeeId));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeComplianceData(pWagePlanId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeComplianceData(pWagePlanId));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeProfilePaycheckDetail(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, paycheckFromDate:Date, paycheckToDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeProfilePaycheckDetail(pSourceSystemCd, pCompanyId, pEmployeeId, paycheckFromDate, paycheckToDate));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeProfileQTDYTDDetails(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeProfileQTDYTDDetails(pSourceSystemCd, pCompanyId, pEmployeeId));
			remoteToken.addResponder(responder);
        }

		public function getEmployeePaychecks(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeePaychecks(pSourceSystemCd, pCompanyId, pEmployeeId));
			remoteToken.addResponder(responder);
        }

        public function getEmployeeHistory(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(employeeRemoteService.getEmployeeHistory(pSourceSystemCd, pCompanyId, pEmployeeId));
            remoteToken.addResponder(responder);
        }

		public function getEmployeeComplianceDataList(sourceSystemCd:String, companyId:String, employeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.getEmployeeComplianceDataList(sourceSystemCd, companyId, employeeId));
			remoteToken.addResponder(responder);
        }

		public function updateEmployeeComplianceData(pSourceSystemCd:String, pCompanyId:String, pEmployeeId:String, pComplianceData:EmployeeComplianceData, pOperation:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.updateEmployeeComplianceData(pSourceSystemCd, pCompanyId, pEmployeeId, pComplianceData, pOperation));
			remoteToken.addResponder(responder);
        }

		public function findEmployeesByCriteria(sourceSystemCd:String, pCompanyId:String, pSourceEmployeeId:String, pSsn:String, pName:String, pLiveState:String, pWorkState:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(employeeRemoteService.findEmployeesByCriteria(sourceSystemCd, pCompanyId, pSourceEmployeeId, pSsn, pName, pLiveState, pWorkState, pFirstIndex, pMaxResults, pSortColumn, pSortDescending));
			remoteToken.addResponder(responder);
        }
		/*
        	PSP-18296 Starts
        */
		public function updateEmployeeSeasonal(pSourceCompanyId:String, pEmployeeId:String, pEmployeeInfo: EmployeeInfo, responder:IResponder):void {
			var remoteToken:AsyncToken =
					AsyncToken(employeeRemoteService.updateEmployeeSeasonal(pSourceCompanyId, pEmployeeId, pEmployeeInfo));
			remoteToken.addResponder(responder);
		}
		/*
        	PSP-18296 Ends
        */
	}
}
/**
 * Created with IntelliJ IDEA.
 * User: ihannur
 * Date: 6/20/13
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
package test.mock {
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.service.interfaces.IViewMyPaycheckService;

    public class MockViewMyPaycheckService extends MockAsyncService implements IViewMyPaycheckService {
        public function MockViewMyPaycheckService() {
        }

		public function expectsGetEmployeesInfo(sourceSystemCd:String, companyId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean):Mock {
            return expects("getEmployeesInfo").withArgs(sourceSystemCd, companyId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
        }
		public function getEmployeesInfo(sourceSystemCd:String, companyId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
            record("getEmployeesInfo", sourceSystemCd, companyId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending);
			sendAsyncResult(responder,"getEmployeesInfo");
        }

		public function expectsGetEmployeeInfo(pEmployeeId:String):Mock {
            return expects("getEmployeeInfo").withArgs(pEmployeeId);
        }
		public function getEmployeeInfo(pEmployeeId:String, responder:IResponder):void {
            record("getEmployeeInfo", pEmployeeId);
			sendAsyncResult(responder,"getEmployeeInfo");
        }

		public function expectsFindPaystubs(pEmployeeId:String, pFromDate:Date, pToDate:Date):Mock {
            return expects("findPaystubs").withArgs(pEmployeeId, pFromDate, pToDate);
        }
		public function findPaystubs(pEmployeeId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
            record("findPaystubs", pEmployeeId, pFromDate, pToDate);
			sendAsyncResult(responder,"findPaystubs");
        }

		public function expectsGetPaystubDetails(pPaystubId:String,companyId:String):Mock {
            return expects("getPaystubDetails").withArgs(pPaystubId,companyId);
        }
		public function getPaystubDetails(pPaystubId:String, responder:IResponder, companyId:String):void {
            record("getPaystubDetails", pPaystubId,companyId);
			sendAsyncResult(responder,"getPaystubDetails");
        }

		public function expectsRemoveConsumerId(pEmployeeId:String):Mock {
            return expects("removeConsumerId").withArgs(pEmployeeId);
        }
		public function removeConsumerId(pEmployeeId:String, responder:IResponder):void {
            record("removeConsumerId", pEmployeeId);
			sendAsyncResult(responder,"removeConsumerId");
        }

		public function expectsFindVMPEmployee(searchMethod:String, searchInput:String):Mock {
            return expects("findVMPEmployee").withArgs(searchMethod, searchInput);
        }
		public function findVMPEmployee(searchMethod:String, searchInput:String, responder:IResponder):void {
            record("findVMPEmployee", searchMethod, searchInput);
			sendAsyncResult(responder,"findVMPEmployee");
        }
    }
}

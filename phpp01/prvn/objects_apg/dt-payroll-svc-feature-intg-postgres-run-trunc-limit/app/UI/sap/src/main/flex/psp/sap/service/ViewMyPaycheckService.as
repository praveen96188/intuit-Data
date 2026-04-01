/**
 * Created with IntelliJ IDEA.
 * User: ihannur
 * Date: 6/20/13
 * Time: 2:29 PM
 */
package psp.sap.service {
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.mxml.RemoteObject;

    import psp.sap.service.interfaces.IViewMyPaycheckService;
    import psp.sap.model.VMPEmployeePaginationDetails;

    public class ViewMyPaycheckService extends PSPService implements IViewMyPaycheckService {

        public function ViewMyPaycheckService():void {
            remoteObjectPool = new RemoteObjectPool("viewmypaycheckservice", 2, true);
        }

        public function get viewMyPaycheckRemoteService():RemoteObject {
            return remoteObjectPool.nextAvailable();
        }

		public function getEmployeesInfo(sourceSystemCd:String, companyId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.getEmployeesInfo(sourceSystemCd, companyId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending));
			remoteToken.addResponder(responder);
        }

		public function getEmployeeInfo(pEmployeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.getEmployeeInfo(pEmployeeId));
			remoteToken.addResponder(responder);
        }

		public function findPaystubs(pEmployeeId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.findPaystubs(pEmployeeId, pFromDate, pToDate));
			remoteToken.addResponder(responder);
        }

		public function getPaystubDetails(pPaystubId:String, responder:IResponder,companyId:String):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.getPaystubDetails(pPaystubId,companyId));
			remoteToken.addResponder(responder);
        }

		public function removeConsumerId(pEmployeeId:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.removeConsumerId(pEmployeeId));
			remoteToken.addResponder(responder);
        }

		public function findVMPEmployee(searchMethod:String, searchInput:String, currentPaginationDetails:VMPEmployeePaginationDetails, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(viewMyPaycheckRemoteService.findVMPEmployee(searchMethod, searchInput, currentPaginationDetails));
			remoteToken.addResponder(responder);
        }

        public function findVMPEmployeeCount(searchMethod:String, searchInput:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(viewMyPaycheckRemoteService.findVMPEmployeeCount(searchMethod, searchInput));
            remoteToken.addResponder(responder);
        }
    }
}

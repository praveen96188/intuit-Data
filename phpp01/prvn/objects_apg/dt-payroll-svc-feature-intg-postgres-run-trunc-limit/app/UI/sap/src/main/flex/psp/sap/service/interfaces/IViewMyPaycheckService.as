/**
 * Created with IntelliJ IDEA.
 * User: ihannur
 * Date: 6/20/13
 * Time: 2:28 PM
 */
package psp.sap.service.interfaces {
    import mx.rpc.IResponder;

import psp.sap.model.VMPEmployeePaginationDetails;

public interface IViewMyPaycheckService extends IPSPService{

		function getEmployeesInfo(sourceSystemCd:String, companyId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void;

		function getEmployeeInfo(pEmployeeId:String, responder:IResponder):void;

		function findPaystubs(pEmployeeId:String, pFromDate:Date, pToDate:Date, responder:IResponder):void;

		function getPaystubDetails(pPaystubId:String, responder:IResponder, companyId:String):void;

		function removeConsumerId(pEmployeeId:String, responder:IResponder):void;

		function findVMPEmployee(searchMethod:String, searchInput:String, currentPaginationDetails:VMPEmployeePaginationDetails, responder:IResponder):void;

        function findVMPEmployeeCount(searchMethod:String, searchInput:String, responder:IResponder):void;

    }
}

/**

 * Created by anandp233 on 2/25/14.

 */

package psp.sap.service.interfaces.rtbAutomation {
import psp.sap.service.interfaces.*;

    import mx.rpc.IResponder;

    import psp.sap.service.interfaces.IPSPService;


public interface IRTBAutomationService extends IPSPService {

    function getAutomationJobList(responder:IResponder):void;

    function getAutomationJobEnum(responder:IResponder):void;

    function duplicateEmployee(oldEmployee:String, newEmployee:String, psId:String, responder:IResponder):void;

    function findEmployeeName(companyId:String, employeeId:String, responder:IResponder):void;

    function findCompanyName(psId:String, responder:IResponder):void;

    function duplicatePitem(oldPItem:String, newPItem:String, psId:String, responder:IResponder):void;

    function findPitemName(companyId:String, pitemId:String, responder:IResponder):void;

    function processUnprocessedRequests(psId:String, responder:IResponder):void;

    function findCompanyNameAndUnprocessedRequest(psId:String, responder:IResponder):void;





    }

}


/**

 * Created by anandp233 on 2/25/14.

 */

package psp.sap.service.rtbAutomation {
import psp.sap.service.*;
import psp.sap.service.*;

    import flash.utils.ByteArray;



    import mx.rpc.AsyncToken;

    import mx.rpc.IResponder;

    import mx.rpc.remoting.RemoteObject;

import psp.sap.service.interfaces.rtbAutomation.IRTBAutomationService;


public class RTBAutomationService extends PSPService implements IRTBAutomationService {

        public function RTBAutomationService() {

            remoteObjectPool = new RemoteObjectPool("rtbautomationservice", 2);

        }

        public function get rtbAutomationRemoteService():RemoteObject {

            return remoteObjectPool.nextAvailable();

        }

        public function duplicateEmployee(oldEmployee:String, newEmployee:String,psid:String,responder:IResponder):void {

            var remoteToken:AsyncToken =

                    AsyncToken(rtbAutomationRemoteService.duplicateEmployee(oldEmployee, newEmployee,psid));

            remoteToken.addResponder(responder);

        }

        public function getAutomationJobList(responder:IResponder):void {

            var remoteToken:AsyncToken =

                    AsyncToken(rtbAutomationRemoteService.getAutomationJobList());

            remoteToken.addResponder(responder);

     }
    public function getAutomationJobEnum(responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.getAutomationJobEnum());

        remoteToken.addResponder(responder);

    }

    public function findEmployeeName(companyId:String,employeeId:String,responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.findEmployeeName(companyId,employeeId));

        remoteToken.addResponder(responder);

    }

    public function findCompanyName(psId:String,responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.findCompanyName(psId));

        remoteToken.addResponder(responder);

    }
    public function duplicatePitem(oldPItem:String, newPItem:String,psid:String,responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.duplicatePItem(oldPItem, newPItem,psid));

        remoteToken.addResponder(responder);

    }

    public function findPitemName(companyId:String,PItemId:String,responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.findPItemName(companyId,PItemId));

        remoteToken.addResponder(responder);

    }

    public function processUnprocessedRequests(psid:String, responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.processUnprocessedRequests(psid));

        remoteToken.addResponder(responder);

    }

    public function findCompanyNameAndUnprocessedRequest(psId:String, responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(rtbAutomationRemoteService.findCompanyNameAndUnprocessedRequest(psId));

        remoteToken.addResponder(responder);

    }


    }

}


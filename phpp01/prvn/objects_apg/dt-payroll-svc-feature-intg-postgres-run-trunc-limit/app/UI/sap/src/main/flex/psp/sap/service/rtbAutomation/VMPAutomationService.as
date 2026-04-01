package psp.sap.service.rtbAutomation {
import psp.sap.service.*;
import psp.sap.service.*;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.rpc.remoting.RemoteObject;
import psp.sap.service.interfaces.rtbAutomation.IVMPAutomationService;

/**
 * This class is used for update Realm id based on Source company id
 * @author dkumar19
 */
public class VMPAutomationService extends PSPService implements IVMPAutomationService {

    public function VMPAutomationService() {

        remoteObjectPool = new RemoteObjectPool("vmpAutomationService", 2);
    }

    public function get vmpAutomationRemoteService():RemoteObject {

        return remoteObjectPool.nextAvailable();

    }

    public function getAutomationJobList(responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(vmpAutomationRemoteService.getAutomationJobList());

        remoteToken.addResponder(responder);

    }

    public function updateRealmId(psId:String,responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(vmpAutomationRemoteService.updateRealmId(psId));

        remoteToken.addResponder(responder);

    }

}
}

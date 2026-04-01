package psp.sap.service.interfaces.rtbAutomation {
import psp.sap.service.interfaces.*;
import mx.rpc.IResponder;
import psp.sap.service.interfaces.IPSPService;
/**
 * This interface provides the ability for a calling client to Update Realm id
 * base on certain input parameters
 * @author dkumar19
 */

public interface IVMPAutomationService extends IPSPService {

    function getAutomationJobList(responder:IResponder):void;
    function updateRealmId(psId:String,responder:IResponder):void;

    }
}

package psp.sap.service {
import mx.collections.ArrayCollection;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.rpc.remoting.mxml.RemoteObject;

import psp.app.util.CookieUtil;

import psp.sap.service.interfaces.IUserService;

public class UserService extends PSPService implements IUserService {
    public function UserService():void {
        remoteObjectPool = new RemoteObjectPool("userservice", 2, true);
    }

    public function get userRemoteService():RemoteObject {
        return remoteObjectPool.nextAvailable();
    }

    public function sapLogout(pCorpId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.sapLogout(pCorpId));
        remoteToken.addResponder(responder);
    }

    public function sapssoLogout(responder:IResponder):void {
        CookieUtil.deleteCookie("sso_param_iv");
        CookieUtil.deleteCookie("sso_param_returnValues");
        CookieUtil.deleteCookie("JSESSIONID");
        CookieUtil.deleteCookie("ssoLogoutCookie");
    }

    public function getAllRoles(domainId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getAllRoles(domainId));
        remoteToken.addResponder(responder);
    }

    public function getAllRoleObjects(responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getAllRoleObjects());
        remoteToken.addResponder(responder);
    }

    public function getAllOperations(responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getAllOperations());
        remoteToken.addResponder(responder);
    }

    public function getAllUsersByOperation(operationId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getAllUsersByOperation(operationId));
        remoteToken.addResponder(responder);
    }

    public function addNewUserData(corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.addNewUserData(corpId, firstName, lastName, roleIds));
        remoteToken.addResponder(responder);
    }

    public function updateUserData(uniqueId:String, corpId:String, firstName:String, lastName:String, roleIds:ArrayCollection, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.updateUserData(uniqueId, corpId, firstName, lastName, roleIds));
        remoteToken.addResponder(responder);
    }

    public function getUsersInDomain(domainId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getUsersInDomain(domainId));
        remoteToken.addResponder(responder);
    }

    public function searchUsersInDomain(pDomainId:String, pFirstName:String, pLastName:String, pCorpId:String, pFirstIndex:int, pMaxResults:int, pSortColumn:String, pSortDescending:Boolean, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.searchUsersInDomain(pDomainId, pFirstName, pLastName, pCorpId, pFirstIndex, pMaxResults, pSortColumn, pSortDescending));
        remoteToken.addResponder(responder);
    }

    public function removeUser(corpId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.removeUser(corpId));
        remoteToken.addResponder(responder);
    }

    public function updatePreference(corpId:String, key:String, value:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.updatePreference(corpId, key, value));
        remoteToken.addResponder(responder);
    }

    public function updatePreferences(corpId:String, settings:ArrayCollection, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.updatePreferences(corpId, settings));
        remoteToken.addResponder(responder);
    }

    public function getUserSettings(corpId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.getUserSettings(corpId));
        remoteToken.addResponder(responder);
    }

    public function resetSettings(corpId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.resetSettings(corpId));
        remoteToken.addResponder(responder);
    }

    public function unlockUser(corpId:String, responder:IResponder):void {
        var remoteToken:AsyncToken =
                AsyncToken(userRemoteService.unlockUser(corpId));
        remoteToken.addResponder(responder);
    }

}
}
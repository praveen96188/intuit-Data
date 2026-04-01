package psp.sap.service
{
    import mx.collections.ArrayCollection;
    import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.rpc.remoting.mxml.RemoteObject;

import psp.app.util.CookieUtil;
import psp.sap.model.User;

import psp.sap.service.interfaces.IAuthService;
    import psp.sap.service.interfaces.IUserService;

public class AuthService extends PSPService implements IAuthService
	{
		public function AuthService():void {
			remoteObjectPool = new RemoteObjectPool("authservice", 2, true);
		}

    	public function get authRemoteService():RemoteObject {
			return remoteObjectPool.nextAvailable();
		}

		public function login(username:String, password:String, alwaysCreateNewToken:Boolean, responder:IResponder):void {
			CookieUtil.deleteCookie("sso_param_iv");
			CookieUtil.deleteCookie("sso_param_returnValues");
			CookieUtil.deleteCookie("ssoLogoutCookie");
			var remoteToken:AsyncToken =
				AsyncToken(authRemoteService.login(username, password, alwaysCreateNewToken));
			remoteToken.addResponder(responder);
        }

		public function loginSSO(unAuthenticatedUser:User, responder:IResponder):void {
			CookieUtil.deleteCookie("ssoLogoutCookie");
			var remoteToken:AsyncToken =
				AsyncToken(authRemoteService.loginSSO(unAuthenticatedUser));
			remoteToken.addResponder(responder);
        }

	}
}

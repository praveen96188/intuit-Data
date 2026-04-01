package psp.sap.service.interfaces
{
    import mx.rpc.IResponder;

	import psp.sap.model.User;

	public interface IAuthService extends IPSPService
	{

		function login(username:String, password:String, alwaysCreateNewToken:Boolean, responder:IResponder):void;

		function loginSSO(unAuthenticatedUser:User, responder:IResponder):void;

	}
}
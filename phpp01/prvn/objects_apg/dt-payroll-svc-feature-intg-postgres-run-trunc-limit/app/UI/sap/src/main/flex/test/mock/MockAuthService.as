package test.mock
{
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.service.interfaces.IAuthService;

    public class MockAuthService extends MockAsyncService implements IAuthService
	{

		public function expectsLogin(username:String, password:String, alwaysCreateNewToken:Boolean):Mock {
            return expects("login").withArgs(username, password, alwaysCreateNewToken);
        }
		public function login(username:String, password:String, alwaysCreateNewToken:Boolean, responder:IResponder):void {
            record("login", username, password, alwaysCreateNewToken);
			sendAsyncResult(responder,"login");
        }

        public function expectsLoginSSO(corpId:String, authToken:String):Mock {
            return expects("loginSSO").withArgs(corpId, authToken);
        }
        public function loginSSO(corpId:String, authToken:String, responder:IResponder):void {
            record("loginSSO", corpId, authToken);
			sendAsyncResult(responder,"loginSSO");
        }


    }
}
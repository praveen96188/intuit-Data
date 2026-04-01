package psp.sap.viewmodel
{
	import flash.events.EventDispatcher;
	
	import mx.logging.ILogger;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.ClientLoggingTarget;
	import psp.sap.application.SAP;
	import psp.sap.viewmodel.events.ViewModelEvent;
	import psp.sap.viewmodel.events.ViewModelFaultEvent;
	
	[Event(name=ViewModelEvent.LOGIN_SUCCEEDED, 	type="psp.sap.viewmodel.events.ViewModelEvent")]
	[Event(name=ViewModelEvent.LOGIN_FAILED, 		type="psp.sap.viewmodel.events.ViewModelEvent")]
	[Event(name=ViewModelFaultEvent.LOGIN_FAULTED, 	type="psp.sap.viewmodel.events.ViewModelFaultEvent")]
	[Event(name=ViewModelEvent.CLOSE, 				type="psp.sap.viewmodel.events.ViewModelEvent")]
	[Bindable]
	public class LoginViewModel
	extends EventDispatcher
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		
		public function LoginViewModel()
		{
		}
		
		private var mUserName:String = "";
		private var mPassword:String = "";
		private var mCanLogin:Boolean = false;
		
		
		public function get userName():String {
			return mUserName;
		}
		
		public function set userName(value:String):void {
			mUserName = value;
			canLogin = requiredFieldsEntered();
		}
		
		public function get password():String {
			return mPassword;
		}
		
		public function set password(value:String):void {
			mPassword = value;
			canLogin = requiredFieldsEntered();
		}
		
		public function get canLogin():Boolean {
			return mCanLogin;	
		}
		
		protected function set canLogin(value:Boolean):void {
			mCanLogin = value;
		}
		
		private function requiredFieldsEntered():Boolean {
			return (mUserName != null 
					&& mUserName.length > 0
					&& mPassword != null
					&& mPassword.length > 0);
		}
			
		public function login():void {
			this.canLogin = false;
			
			SAP.instance.session.login(	this.userName, 
										this.password,
										onLoginSucceeded,
										onLoginFailed,
										onLoginFaulted);
		}
		
		private function onLoginSucceeded(e:ResultEvent):void {
			logger.info("User logged in");
			dispatchEvent( ViewModelEvent.createLoginSucceededEvent() );
			this.close();
		}	
		
		private function onLoginFailed(e:ResultEvent):void {
			logger.info("User failed login");
			this.canLogin = requiredFieldsEntered();
			dispatchEvent( ViewModelEvent.createLoginFailedEvent() );
		}
		
		private function onLoginFaulted(e:FaultEvent):void {
			logger.info("Login faulted Details:" + e.message);
			this.canLogin = requiredFieldsEntered();
			dispatchEvent( ViewModelFaultEvent.createLoginFaultedEvent(e) );
		}
					
		public function close():void {
			dispatchEvent( ViewModelEvent.createCloseEvent() );
		}

	}
}
package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import psp.sap.application.SAP;
	import psp.sap.service.UserService;
	import psp.sap.viewmodel.LoginViewModel;
	import psp.sap.viewmodel.events.ViewModelEvent;
	import psp.sap.viewmodel.events.ViewModelFaultEvent;

    import test.mock.MockAuthService;
    import test.mock.MockUserService;
	import test.mock.ResponseTypeEnum;
import test.mock.data.UserData;
import test.sap.application.SAPTestBase;
		
	public class LoginViewModelTest extends SAPTestBase
	{
		private var mViewModel:LoginViewModel = new LoginViewModel();
		private var mDataService:MockAuthService;
		
		private var userName:String = "userName";
		private var password:String = "password";

		public function LoginViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( LoginViewModelTest );
		}

		override public function setUp():void {
			super.setUp();
			
			trackedEvents = [	   								
   								ViewModelEvent.LOGIN_SUCCEEDED,
   								ViewModelEvent.LOGIN_FAILED,
   								ViewModelFaultEvent.LOGIN_FAULTED,
   								ViewModelEvent.CLOSE   								   								
   							]; 			
			
			mViewModel = new LoginViewModel();
						                                                         						                                                         
			// override the data service    								   			
   			SAP.instance.userService = new MockUserService();
   			mDataService = SAP.instance.authService as MockAuthService;   									                                                         		
		}
		
		override public function tearDown():void {
   			super.tearDown();   			
   			
   			if(mViewModel != null){
				trackEventsStop(mViewModel);
   			}
   			
   			// reset the data services   			
   			SAP.instance.userService = new UserService();
   		}

		public function testViewModel():void {
			trackEventsStart(mViewModel);
			
			// test can login
			assertEquals("canLogin", false, mViewModel.canLogin);
			mViewModel.userName = userName;
			assertEquals("canLogin", false, mViewModel.canLogin);
			mViewModel.password = password;
			assertEquals("canLogin", true, mViewModel.canLogin);
			mViewModel.userName = "";
			assertEquals("canLogin", false, mViewModel.canLogin);
			mViewModel.userName = userName;
			assertEquals("canLogin", true, mViewModel.canLogin);
			
			// setup load model data mock expectation
			mDataService.expectsLogin(userName, password, true).willReturnAsync(UserData.buildUser());
			addAsyncVerifier(mViewModel, ViewModelEvent.LOGIN_SUCCEEDED, testLoginFail);
			
			mViewModel.login();												
		}
		
		protected function testLoginFail(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEventHistory([ViewModelEvent.LOGIN_SUCCEEDED]);

			mDataService.expectsLogin(userName, password, true).willReturnAsync(null);
			addAsyncVerifier(mViewModel, ViewModelEvent.LOGIN_FAILED, testLoginFault);
			
			mViewModel.login();
		}
		
		protected function testLoginFault(e:ViewModelEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEventHistory([ViewModelEvent.LOGIN_FAILED]);

			mDataService.expectsLogin(userName, password, true).willFaultAsync("LDAP user data not found in database");
			addAsyncVerifier(mViewModel, ViewModelFaultEvent.LOGIN_FAULTED, testClose);
			
			mViewModel.login();
		}
		
		protected function testClose(e:ViewModelFaultEvent):void {
			assertTrue(mDataService.errorMessage(), mDataService.success());
			
			assertEventHistory([ViewModelFaultEvent.LOGIN_FAULTED]);									
		}						
	}
}

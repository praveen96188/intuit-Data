package test.sap.viewModel
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	
	import psp.sap.viewmodel.AbstractPartViewModel;
	import psp.sap.viewmodel.AdministrationManageRolesViewModel;
	
	import test.mock.MockRepository;
	import test.mock.MockUserService;
import test.mock.data.UserData;

public class AdministrationManageRolesViewModelTest extends AbstractPartViewModelTestBase
	{
		private var mViewModel:AdministrationManageRolesViewModel = new AdministrationManageRolesViewModel();
		private var mDataService:MockUserService;

		public function AdministrationManageRolesViewModelTest(methodName:String=null)
		{
			super(methodName);
		}

		public static function suite() : TestSuite {
			return new TestSuite( AdministrationManageRolesViewModelTest );
		}

		override public function setUp():void {
			super.setUp();

			trackedProperties = [
				"operations",
				"roles",
			];

			mDataService = mSAP.userService as MockUserService;

			// set the view model to test
			viewModelToTest(AbstractPartViewModel(mViewModel));
		}

		override public function testViewModel():void {
			// setup load model data mock expectation
			mDataService.expectsGetAllOperations().willReturnAsync(UserData.getOperationData());
			mDataService.expectsGetAllRoleObjects().willReturnAsync(UserData.getRoleData());
			testActivationSequence();
		}

		override protected function verifyModelDataSetup():void {
			assertTrue(mDataService.errorMessage(), mDataService.success());

			assertEquals("operations",
				(MockRepository.instance.getTestObject(MockRepository.TEST_USER_OPERATIONS) as ArrayCollection).length,			
				mViewModel.operations.length);

			assertEquals("roles",
				(MockRepository.instance.getTestObject(MockRepository.TEST_USER_ROLES) as ArrayCollection).length,			
				mViewModel.roles.length);

			testBindableProperties();
		}

		override protected function testBindableProperties():void {
			clearPropertyChangeEventHistory();

			testBindableProperty(mViewModel, "operations", new ArrayCollection());
			testBindableProperty(mViewModel, "roles", new ArrayCollection());
		}

	}
}

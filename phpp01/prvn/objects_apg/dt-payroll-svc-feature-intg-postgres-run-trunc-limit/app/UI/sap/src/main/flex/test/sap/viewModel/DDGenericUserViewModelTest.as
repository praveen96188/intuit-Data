package test.sap.viewModel {
    import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.User;
    import psp.sap.model.UserOperation;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.DDGenericUserViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockUserService;
    import test.mock.data.UserData;

    public class DDGenericUserViewModelTest extends AbstractPartViewModelTestBase {
        private var mViewModel:DDGenericUserViewModel = new DDGenericUserViewModel();
        private var mDataService:MockUserService;

        private var mockUser:User;

        public function DDGenericUserViewModelTest(methodName:String = null) {
            mViewModel.domainId = "DDUI";
            super(methodName);
        }

        public static function suite():TestSuite {
            return new TestSuite(DDGenericUserViewModelTest);
        }

        override public function setUp():void {
            super.setUp();

            trackedProperties = [
                "DDMEnabled",
                "DDAuthEnabled",
                "DDMEnabled",
                "roleId",
                "allRoles",
                "allUsers",
                "corpIdValidator",
                "corpId",
                "lastName",
                "firstName",
                "firstNameValidator",
                "DDAuthEnabled",
                "lastNameValidator",
                "canSave"
            ];

            mDataService = mSAP.userService as MockUserService;

            mockUser = new User();
            mockUser.firstName = "FirstName";
            mockUser.lastName = "LastName";
            mockUser.corpId = "123";
            mockUser.roleIds = new ArrayCollection(["Role"]);

            // setup logged in user
            var user:User = new User();
            var operations:ArrayCollection = new ArrayCollection();
            var operation:UserOperation = new UserOperation();
            operation.domainId = "DDUI";
            operation.operationId = OperationsEnum.AUTH_ADD_UPDATE_USERS;
            operations.addItem(operation);
            user.grantedOperations = operations;
            mSAP.session.user = user;

            // set the view model to test
            viewModelToTest(AbstractPartViewModel(mViewModel));
        }

        override public function testViewModel():void {
            // setup load model data mock expectation
            mDataService.expectsGetUsersInDomain("DDUI").willReturnAsync(UserData.getUsers());
            mDataService.expectsGetAllRoles("DDUI").willReturnAsync(UserData.getRoleNames());

            testActivationSequence();
        }

        override protected function verifyModelDataSetup():void {
            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("allUsers",
                    UserData.getUsers().length,
                    mViewModel.allUsers.length);

            assertEquals("allRoles",
                    UserData.getRoleNames().length,
                    mViewModel.allRoles.length);

            assertEquals("firstName", "", mViewModel.firstName);
            assertEquals("lastName", "", mViewModel.lastName);
            assertEquals("corpId", "", mViewModel.corpId);
            assertEquals("roleId", "", mViewModel.roleId);

            testBindableProperties();
        }

        override protected function testBindableProperties():void {
            clearPropertyChangeEventHistory();

            testBindableProperty(mViewModel, "roleId", "role");
            testBindableProperty(mViewModel, "allRoles", new ArrayCollection());
            testBindableProperty(mViewModel, "allUsers", new ArrayCollection());
            testBindableProperty(mViewModel, "corpId", "corp");
            testBindableProperty(mViewModel, "lastName", "last");
            testBindableProperty(mViewModel, "firstName", "first");

            testValidators();
        }

        override protected function testValidators():void {
            // update has changed so can save is true
            mViewModel.firstName = mockUser.firstName;
            mViewModel.lastName = mockUser.lastName;
            mViewModel.corpId = mockUser.corpId;

            assertEquals("can save", true, mViewModel.canSave);

            testRequiredStringValidator(mViewModel, "firstName");
            testRequiredStringValidator(mViewModel, "lastName");
            testRequiredStringValidator(mViewModel, "corpId");

            testAddSave();
        }

        protected function testAddSave():void {
            assertEquals("can save", true, mViewModel.canSave);

            mViewModel.firstName = mockUser.firstName;
            mViewModel.lastName = mockUser.lastName;
            mViewModel.corpId = mockUser.corpId;
            mViewModel.roleId = String(mockUser.roleIds.getItemAt(0));
            mDataService.expectsAddNewUserData(mockUser.corpId, mockUser.firstName, mockUser.lastName, mockUser.roleIds);
            mDataService.expectsGetUsersInDomain("DDUI").willReturnAsync(UserData.getUsers());
            mDataService.expectsGetAllRoles("DDUI").willReturnAsync(UserData.getRoleNames());
            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyAddSave);
            addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyAddRefresh);

            mViewModel.onAddNewUser();
        }

        protected function verifyAddSave(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
        }

        protected function verifyAddRefresh(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("allUsers",
                    UserData.getUsers().length,
                    mViewModel.allUsers.length);

            assertEquals("allRoles",
                    UserData.getRoleNames().length,
                    mViewModel.allRoles.length);

            assertEquals("firstName", "", mViewModel.firstName);
            assertEquals("lastName", "", mViewModel.lastName);
            assertEquals("corpId", "", mViewModel.corpId);
            assertEquals("roleId", "", mViewModel.roleId);

            testRemoveSave();
        }

        protected function testRemoveSave():void {
            mDataService.expectsRemoveUser(mockUser.corpId);
            mDataService.expectsGetUsersInDomain("DDUI").willReturnAsync(UserData.getUsers());
            mDataService.expectsGetAllRoles("DDUI").willReturnAsync(UserData.getRoleNames());
            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifyRemoveSave);
            addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRemoveRefresh);

            mViewModel.goToRemoveDDMUserData(mockUser);
        }

        protected function verifyRemoveSave(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
        }

        protected function verifyRemoveRefresh(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);

            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("allUsers",
                    UserData.getUsers().length,
                    mViewModel.allUsers.length);

            assertEquals("allRoles",
                    UserData.getRoleNames().length,
                    mViewModel.allRoles.length);

            assertEquals("firstName", "", mViewModel.firstName);
            assertEquals("lastName", "", mViewModel.lastName);
            assertEquals("corpId", "", mViewModel.corpId);
            assertEquals("roleId", "", mViewModel.roleId);
        }

    }
}

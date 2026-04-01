package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;
    import mx.utils.ObjectUtil;

    import psp.sap.model.DirectDepositLimitSettings;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.AdministrationSettingsEditPageViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockAdministrationService;
    import test.mock.data.AdministrativeData;

    public class AdministrationSettingsPageEditViewModelTest extends AbstractPartViewModelTestBase
    {
        private var mViewModel: AdministrationSettingsEditPageViewModel = new AdministrationSettingsEditPageViewModel();
        private var mDataService:MockAdministrationService;

        public function AdministrationSettingsPageEditViewModelTest(methodName:String=null)
        {
            super(methodName);
        }

        public static function suite() : TestSuite {
            return new TestSuite( AdministrationSettingsPageEditViewModelTest );
        }

        override public function setUp():void {
            super.setUp();

            trackedProperties = [
                "backingDTO",
                "autoLimitIncreaseTiers",
                "canSave"
            ];

            mDataService = mSAP.administrationService as MockAdministrationService;

            // set the view model to test
            viewModelToTest(AbstractPartViewModel(mViewModel));
        }

        override public function testViewModel():void {
            // setup load model data mock expectation
            mDataService.expectsGetDirectDepositLimitSettings(SourceSystemEnum.QBDT.code).willReturnAsync(AdministrativeData.getDirectDepositLimitSettings());

            testActivationSequence();
        }

        override protected function verifyModelDataSetup():void {
            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertTrue("backingDTO",
                    ObjectUtil.compare(AdministrativeData.getDirectDepositLimitSettings(), mViewModel.backingDTO) == 0);

            assertTrue("autoLimitIncreaseTiers",
                    ObjectUtil.compare(AdministrativeData.getDirectDepositLimitSettings().autoLimitIncreaseTiers,
                    mViewModel.autoLimitIncreaseTiers) == 0);

            testBindableProperties();
        }

        override protected function testBindableProperties():void {
            clearPropertyChangeEventHistory();

            testBindableProperty(mViewModel, "backingDTO", new DirectDepositLimitSettings());
            testBindableProperty(mViewModel, "autoLimitIncreaseTiers", new ArrayCollection());

            verifyHasChangedLogic();
        }

        override protected function verifyHasChangedLogic():void {
            testHasChangedDTO(mViewModel, mViewModel.backingDTO);

            testValidators();
        }

        override protected function testValidators():void {
            // update has changed so can save is true
            mViewModel.backingDTO.companyBankAccountDurationLimitForVerification = "364";
            assertEquals("can save", true, mViewModel.canSave);

            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "companyBankAccountDurationLimitForVerification", 1, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "companyBankAccountVerificationAttemptLimit", 1, 99, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "consecutiveLimitViolationLimit", 1, 99, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "DDCompanyLimitDuration", 1, 99, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "DDEmployeeLimitDuration", 1, 99, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "defaultDDCompanyLimit", 0.01, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "defaultDDEmployeeLimit", 0.01, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "maxDDCompanyLimitDefault", 0.01, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "minimumNonSuspectPayrollAmount", 0.01, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "defaultBPCompanyLimit", 0.01, NaN, true);
            testNumberValidatorDTO(mViewModel, mViewModel.backingDTO, "defaultBPPayeeLimit", 0.01, NaN, true);

            testSave();
        }

        override protected function testSave():void {
            assertTrue("can save", mViewModel.canSave);

            mDataService.expectsSaveDirectDepositLimitSettings(mViewModel.backingDTO, SourceSystemEnum.QBDT.code);
            mDataService.expectsGetDirectDepositLimitSettings(SourceSystemEnum.QBDT.code).willReturnAsync(AdministrativeData.getDirectDepositLimitSettings());

            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
            addAsyncVerifier(mViewModel, ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, verifyRefresh);
            mViewModel.save();
        }

        override protected function verifySave(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
        }

        override protected function verifyRefresh(e:ViewModelEvent):void {
            assertEventHistory([ViewModelEvent.MODEL_DATA_SETUP_COMPLETED]);
            assertTrue(mDataService.errorMessage(), mDataService.success());

            // for this view model, the load model data is called after save reseting the form
            assertEquals("can save", false, mViewModel.canSave);
            assertTrue("backingDTO",
                    ObjectUtil.compare(AdministrativeData.getDirectDepositLimitSettings(), mViewModel.backingDTO) == 0);

            assertTrue("autoLimitIncreaseTiers",
                    ObjectUtil.compare(AdministrativeData.getDirectDepositLimitSettings().autoLimitIncreaseTiers,
                    mViewModel.autoLimitIncreaseTiers) == 0);
        }

    }
}
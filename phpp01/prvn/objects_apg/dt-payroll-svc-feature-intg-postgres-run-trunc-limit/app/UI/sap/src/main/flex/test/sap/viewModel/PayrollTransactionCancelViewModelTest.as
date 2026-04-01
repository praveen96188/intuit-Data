package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.collections.ArrayCollection;

    import psp.sap.model.Company;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.PayrollEmployeeTransaction;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTypeEnum;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
    import psp.sap.viewmodel.PayrollTransactionCancelViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.mock.MockPayrollRunService;
    import test.mock.data.PayrollData;

    public class PayrollTransactionCancelViewModelTest extends AbstractPartViewModelTestBase
    {
        private var mViewModel:PayrollTransactionCancelViewModel = new PayrollTransactionCancelViewModel();
        private var mDataService:MockPayrollRunService;
        private var mCompanyKey:CompanyKey;
        private var mPayroll:PayrollRun;

        public function PayrollTransactionCancelViewModelTest(methodName:String=null)
        {
            super(methodName);
        }

        public static function suite() : TestSuite {
            return new TestSuite( PayrollTransactionCancelViewModelTest );
        }

        override public function setUp():void {
            super.setUp();

            trackedProperties = [
                "employeeTransactions",
                "payrollRun",
                "selected",
                "canSave"
            ];

            mDataService = mSAP.payrollRunService as MockPayrollRunService;

            mCompanyKey = new CompanyKey();
            mCompanyKey.sourceSystemCd = SourceSystemEnum.QBDT.code;
            mCompanyKey.companyId = "1234";
            mViewModel.companyKey = mCompanyKey;

            mPayroll = new PayrollRun();
            mPayroll.sourcePayRunId = "payrollRun123";
            mPayroll.payrollType = PayrollTypeEnum.REGULAR.toString();

            mViewModel.payrollRun = mPayroll;            

            // set the view model to test
            viewModelToTest(AbstractPartViewModel(mViewModel));
        }

        override public function testViewModel():void {
            // setup load model data mock expectation
            mDataService.expectsFindCancelableTransactions(mCompanyKey.companyId,
                    mCompanyKey.sourceSystemCd,
                    mPayroll.sourcePayRunId)
                    .willReturnAsync(PayrollData.getPayrollEmployeeTransactions());
            testActivationSequence();
        }

        override protected function verifyModelDataSetup():void {
            assertTrue(mDataService.errorMessage(), mDataService.success());

            assertEquals("employeeTransactions", 2, mViewModel.employeeTransactions.length);

            for each(var txn:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertFalse("selectedTxn", txn.selected);
            }

            assertFalse("selected", mViewModel.selected);
            assertFalse("canSave", mViewModel.canSave);

            testBindableProperties();
        }

        override protected function testBindableProperties():void {
            clearPropertyChangeEventHistory();

            testBindableProperty(mViewModel, "employeeTransactions", new ArrayCollection());
            testBindableProperty(mViewModel, "selected", true);

            testValidators();
        }

        override protected function testValidators():void {
            // init not checked
            assertFalse("selected", mViewModel.selected);
            assertFalse("canSave", mViewModel.canSave);

            // test selection check boxes
            // check the column header
            mViewModel.selectAll = true;
            assertTrue("selected", mViewModel.selected);
            for each(var txn:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertTrue("selectedTxn", txn.selected);
            }
            assertTrue("canSave", mViewModel.canSave);

            // assumes the previous lines passed
            var transaction:PayrollEmployeeTransaction = mViewModel.employeeTransactions[0];
            // un-check one of the txns
            transaction.selected = false;
            assertFalse("selected", mViewModel.selected);
            assertTrue("canSave", mViewModel.canSave);

            // re-check the box
            transaction.selected = true;
            assertTrue("selected", mViewModel.selected);
            assertTrue("canSave", mViewModel.canSave);

            // uncheck all of the transactions
            mViewModel.selectAll = false;
            assertFalse("selected", mViewModel.selected);
            for each(var txn2:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertFalse("selectedTxn", txn2.selected);
            }
            assertFalse("canSave", mViewModel.canSave);

            testSave();
        }

        override protected function testSave():void {
            mViewModel.selectAll = true;
            assertTrue("can save", mViewModel.canSave);

            mDataService.expectsCancelPayrollTransaction(mCompanyKey.companyId,
                    mCompanyKey.sourceSystemCd,
                    new ArrayCollection(["123456", "123456"]),
                    mPayroll.sourcePayRunId);
            mDataService.expectsFindCancelableTransactions(mCompanyKey.companyId,
                    mCompanyKey.sourceSystemCd,
                    mPayroll.sourcePayRunId)
                    .willReturnAsync(PayrollData.getPayrollEmployeeTransactions());;
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

            assertEquals("employeeTransactions", 2, mViewModel.employeeTransactions.length);

            for each(var txn:PayrollEmployeeTransaction in mViewModel.employeeTransactions){
                assertFalse("selectedTxn", txn.selected);
            }

            assertFalse("selected", mViewModel.selected);
            assertFalse("canSave", mViewModel.canSave);
        }

    }
}
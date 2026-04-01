package test.sap.viewModel
{
    import flexunit.framework.TestSuite;

    import mx.rpc.events.ResultEvent;

    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;
    import psp.sap.model.Company;
    import psp.sap.model.OfferingServiceChargeTypeEnum;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.SourceSystemEnum;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.CompanyPayrollsTopicViewModel;
    import psp.sap.viewmodel.PayrollLedgerFeeTransferViewModel;
    
    import psp.sap.viewmodel.events.ViewModelEvent;

    import test.data.MockCompanyService;
    import test.sap.application.SAPTestBase;


    public class PayrollLedgerFeeTransferViewModelTest extends SAPTestBase
    {
        private var mCompanyInspector:CompanyInspectorViewModel;
        private var mCompany:Company;
        private var mViewModel:PayrollLedgerFeeTransferViewModel;
        private const PAYROLL_RUN:String = "BatchTest05";
        private const COMPANY_ID:String = "1234567";

        public function PayrollLedgerFeeTransferViewModelTest(methodName:String=null)
        {
            super(methodName);
        }

        public static function suite() : TestSuite {
            return new TestSuite( PayrollLedgerFeeTransferViewModelTest );
        }

        override public function setUp():void {
            super.setUp();
            this.asyncTimeout *= 10;
            trackedEvents = [
                ViewModelEvent.SAVE_SUCCEEDED
            ];
        }

        override public function tearDown():void {
            super.tearDown();
            if(mViewModel != null)
                trackEventsStop(mViewModel);
        }

        public function testLoadFeeInfo():void {
            runDataLoader("Payroll :: Add Fee Transfer Test", testLoadFeeInfo_Step2,10);
        }

        private function testLoadFeeInfo_Step2(e:ResultEvent):void {
            login(testLoadFeeInfo_Step3);
        }

        private function testLoadFeeInfo_Step3(e:ResultEvent):void {

            mCompanyInspector = new CompanyInspectorViewModel();
            mCompany = MockCompanyService.DB.getCompanyAt(0);

            mCompany.companyId = COMPANY_ID;
            mCompany.sourceSystemCd = SourceSystemEnum.QBOE.code;
            mCompanyInspector.company = mCompany;

            mViewModel = PayrollLedgerFeeTransferViewModel(mCompanyInspector.findPart(CompanyInspectorPageEnum.PAYROLL_LEDGER_FEE_TRANSFER));                  
            trackEventsStart(mViewModel);

            var myPayrollRun:PayrollRun = new PayrollRun();
            myPayrollRun.sourcePayRunId = PAYROLL_RUN;

            addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyFeeLoaded);
            mViewModel.activate();
        }


        private function verifyFeeLoaded(e:ViewModelEvent):void {

            // should load with return fee type selected and no amount
            assertEquals("Can Save", false, mViewModel.canSave);
            assertEquals("Amount", "", mViewModel.amount);
            assertEquals("Fee Type", OfferingServiceChargeTypeEnum.NSF.code, mViewModel.offeringServiceChargeTypeCd)
                // max amount should be loaded and set durring load model data
            assertEquals("Max Amount", 75, mViewModel.amountValidator.maxValue as Number);

                // add valid amount
            mViewModel.amount = "50";
            assertEquals("Can Save", true, mViewModel.canSave);

                // add invalid amount > 75
            mViewModel.amount = "100";
            assertEquals("Can Save", false, mViewModel.canSave);

                // change back to valid amount, change type and save
            mViewModel.amount = "50";
            mViewModel.offeringServiceChargeTypeCd = OfferingServiceChargeTypeEnum.Reversal.code;
            assertEquals("Can Save", true, mViewModel.canSave);
            assertEquals("Fee Type", OfferingServiceChargeTypeEnum.Reversal.code, mViewModel.offeringServiceChargeTypeCd)
            assertEquals("Can Save", true, mViewModel.canSave);
            addAsyncVerifier(mViewModel, ViewModelEvent.SAVE_SUCCEEDED, verifySave);
            mViewModel.save();
        }

        private function verifySave(e:ViewModelEvent):void {
            this.assertEventHistory([ViewModelEvent.SAVE_SUCCEEDED]);
        }

    }
}
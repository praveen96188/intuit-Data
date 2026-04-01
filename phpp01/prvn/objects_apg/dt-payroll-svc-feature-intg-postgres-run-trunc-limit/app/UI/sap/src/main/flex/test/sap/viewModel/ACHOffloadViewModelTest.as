package test.sap.viewModel
{
import flexunit.framework.TestSuite;

import mx.collections.ArrayCollection;

import psp.sap.application.SAP;
import psp.sap.application.collections.NachaFileGroup;
import psp.sap.model.NachaFile;
import psp.sap.viewmodel.ACHOffloadViewModel;
import psp.sap.viewmodel.AbstractPartViewModel;
import psp.sap.viewmodel.events.ViewModelEvent;

import test.mock.MockAdministrationService;
import test.mock.MockRepository;
import test.mock.data.ACHOffloadData;
import test.mock.data.UserData;


public class ACHOffloadViewModelTest extends AbstractPartViewModelTestBase
{
    private var mViewModel:ACHOffloadViewModel;
    private var mDataService:MockAdministrationService;

    private const OPERATOR_LOGIN:String = "AutoLogin";
    private const OPERATOR_PASSWORD:String = "operator";

    public function ACHOffloadViewModelTest(methodName:String=null)
    {
        super(methodName);
    }

    public static function suite() : TestSuite {
        return new TestSuite( ACHOffloadViewModelTest );
    }

    override public function setUp():void {
        super.setUp();

        mSAP.session.user = UserData.getUser("Operator");

        mViewModel = new ACHOffloadViewModel();

        trackedProperties = [
            "canRequestSecondaryOffload",
            "ACHFileGroups",
            "cannotRequestSecondaryOffloadReason",
            "canSave"
        ];

        mDataService = mSAP.administrationService as MockAdministrationService;

        // set the view model to test
        viewModelToTest(AbstractPartViewModel(mViewModel));
    }

    override public function testViewModel():void {
        //this is easier than actually logging in as an operator... a bit of a hack, but whatever
        mViewModel.canRequestSecondaryOffload = true;
        mViewModel.cannotRequestSecondaryOffloadReason = "";

        //setup preconditions for test
        //in this test it is before 5 PM and a second offload has not been scheduled
        var pspDate:Date = SAP.instance.PSPDate;
        pspDate.hours = 13;
        SAP.instance.setPSPDate(pspDate.time);


        // setup load model data mock expectation

        mDataService.expectsGetNachaFilesForOffload().willReturnAsync(ACHOffloadData.getLongOffloadData());
        mDataService.expectsIsSecondOffloadScheduled().willReturnAsync(false);


        testActivationSequence();
    }

    override protected function verifyModelDataSetup():void {
        assertTrue(mDataService.errorMessage(), mDataService.success());

        assertTrue(mViewModel.canRequestSecondaryOffload);
        assertEquals("no disabled message","",mViewModel.cannotRequestSecondaryOffloadReason);

        //check ach files returned match the files in the groups
        //test the files are grouped correctly by finalized date and trans date
        //test that the groups are sorted properly (finalized desc, trans desc)
        //test that files in group are ordered properly (as returned from service)
        var achFiles:ArrayCollection = MockRepository.instance.getTestObject(MockRepository.TEST_ACH_OFFLOAD) as ArrayCollection;
        var achFileGroups:ArrayCollection = mViewModel.ACHFileGroups;

        assertEquals("count of groups",4,achFileGroups.length);

        assertEquals("count of group 1",1,(achFileGroups[0] as NachaFileGroup).achFiles.length);
        assertEquals("count of group 2",2,(achFileGroups[1] as NachaFileGroup).achFiles.length);
        assertEquals("count of group 3",1,(achFileGroups[2] as NachaFileGroup).achFiles.length);
        assertEquals("count of group 4",4,(achFileGroups[3] as NachaFileGroup).achFiles.length);

        assertNachaFileId(achFileGroups,0,0,"id8");

        assertNachaFileId(achFileGroups,1,0,"id6");
        assertNachaFileId(achFileGroups,1,1,"id7");

        assertNachaFileId(achFileGroups,2,0,"id5");

        assertNachaFileId(achFileGroups,3,0,"id1");
        assertNachaFileId(achFileGroups,3,1,"id2");
        assertNachaFileId(achFileGroups,3,2,"id3");
        assertNachaFileId(achFileGroups,3,3,"id4");


        testBindableProperties();
    }

    private function assertNachaFileId(groups:ArrayCollection, group:int, file:int, id:String):void {
        assertEquals("id must match",id,((groups[group] as NachaFileGroup).achFiles[file] as NachaFile).fileId);
    }

    override protected function testBindableProperties():void {
        clearPropertyChangeEventHistory();

        testBindableProperty(mViewModel, "canRequestSecondaryOffload", false);
        testBindableProperty(mViewModel, "ACHFileGroups", new ArrayCollection());
        testBindableProperty(mViewModel, "cannotRequestSecondaryOffloadReason", "Omigod look behind you!!");

        verifyHasChangedLogic();

    }

    override protected function verifyHasChangedLogic():void {
        //only thing that changes here is the confirmation code
        //but that is in a collection and not a property
        //so we will test it manually

        assertFalse(mViewModel.canSave);

        ((mViewModel.ACHFileGroups[3] as NachaFileGroup).achFiles[2] as NachaFile).confirmationCode = "i'd buy that for a dollar";

        assertTrue(mViewModel.canSave);

        ((mViewModel.ACHFileGroups[3] as NachaFileGroup).achFiles[2] as NachaFile).confirmationCode = "";

        assertFalse(mViewModel.canSave);

        testValidators();
    }


    override protected function testValidators():void {
        //no validators

        testSave();
    }

    override protected function testSave():void {
        ((mViewModel.ACHFileGroups[3] as NachaFileGroup).achFiles[2] as NachaFile).confirmationCode = "sadlkhdsakj";
        mDataService.expectsGetNachaFilesForOffload().willReturnAsync(ACHOffloadData.getLongOffloadData());
        mDataService.expectsIsSecondOffloadScheduled().willReturnAsync(false);

        assertEquals("can save", true, mViewModel.canSave);
        mDataService.expectsConfirmOffloadFiles(new ArrayCollection([((mViewModel.ACHFileGroups[3] as NachaFileGroup).achFiles[2])]));

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
    }

    public function testShortFormatFile():void{
        assertEquals("forward slashes", "asd.txt",mViewModel.shortFormatFile("dsakj/basdsad/asd.txt"));
        assertEquals("backslashes", "asd.txt",mViewModel.shortFormatFile("sadasd\\asdsada\\asd.txt"));
        assertEquals("short","asd.txt",mViewModel.shortFormatFile("asd.txt"));
    }

    public function testOffloadsAfterFive():void {
        mDataService.expectsGetNachaFilesForOffload().willReturnAsync(ACHOffloadData.getLongOffloadData());       

        mViewModel.canRequestSecondaryOffload = true;
        mViewModel.cannotRequestSecondaryOffloadReason = "";

        var pspDate:Date = SAP.instance.PSPDate;
        pspDate.hours = 18;
        SAP.instance.setPSPDate(pspDate.time);

        addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyOffloadsAfterFive);
        mViewModel.activate();
    }

    //no need to go to whole 9 yards here again--just checking the values are what i want
    private function verifyOffloadsAfterFive(e:ViewModelEvent):void {
        assertFalse(mViewModel.canRequestSecondaryOffload);
        assertEquals("disabled because after five","Second Offload can be initiated before 5:00 PM only",mViewModel.cannotRequestSecondaryOffloadReason);
    }

    public function testOffloadsSecondOffloadRequested():void {
        mDataService.expectsGetNachaFilesForOffload().willReturnAsync(ACHOffloadData.getLongOffloadData());
        mDataService.expectsIsSecondOffloadScheduled().willReturnAsync(true);

        mViewModel.canRequestSecondaryOffload = true;
        mViewModel.cannotRequestSecondaryOffloadReason = "";

        var pspDate:Date = SAP.instance.PSPDate;
        pspDate.hours = 14;
        SAP.instance.setPSPDate(pspDate.time);        

        addAsyncVerifier(mViewModel, ViewModelEvent.ACTIVATED, verifyOffloadsSecondRequested);
        mViewModel.activate();
    }


    public function verifyOffloadsSecondRequested(e:ViewModelEvent):void {
        assertFalse(mViewModel.canRequestSecondaryOffload);
        assertEquals("disabled because already requested","Second Offload has already been scheduled for today",mViewModel.cannotRequestSecondaryOffloadReason);
    }

}
}

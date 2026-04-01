
package psp.sap.viewmodel {

import mx.collections.ArrayCollection;
import mx.controls.Alert;

import psp.sap.application.SAP;
import psp.sap.model.EmployeeInfo;

public class EditSeasonalViewModel extends AbstractPartViewModel {

    private var mEditSeasonalPopup:PopUpPartViewModel;


    private var mSelectedSeason:String;
    [Bindable] [BackingProperty]
    public function get selectedSeason():String{
        return mSelectedSeason;
    }
    public function set selectedSeason(value:String ):void{
        mSelectedSeason = value;
    }


    [Bindable]
    public var seasonalList:ArrayCollection = new ArrayCollection(["", "N", "Y"]);
    [Bindable]
    public var enableSave:Boolean;

    private var mEmployeeInfo:EmployeeInfo;
    [Bindable] [BackingProperty (context=true)]
    public function get employeeInfo():EmployeeInfo {
        return mEmployeeInfo;
    }
    public function set employeeInfo(value:EmployeeInfo):void {
        mEmployeeInfo = value;
    }

    private var mSourceCompanyId:String;
    [Bindable] [BackingProperty]
    public function get sourceCompanyId():String{
        return mSourceCompanyId;
    }
    public function set sourceCompanyId(value:String ):void{
        mSourceCompanyId = value;
    }

    public function EditSeasonalViewModel() {
        super();
        reloadOnSave = true;
        enableSave = false;
    }


    override protected function executeSave():void {
        employeeInfo.isSeasonal=selectedSeason;
        SAP.instance.employeeService.updateEmployeeSeasonal(sourceCompanyId,employeeInfo.employeeId, employeeInfo,createSaveResponder());
    }
}
}

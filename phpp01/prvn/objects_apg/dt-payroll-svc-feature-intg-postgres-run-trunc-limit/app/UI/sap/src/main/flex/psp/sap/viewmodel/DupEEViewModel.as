/**
 * Created by: smodgil
 * Description: To Create Duplication Employee View
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel {
import mx.controls.Alert;
import mx.rpc.events.ResultEvent;
import psp.sap.application.SAP;
import psp.sap.application.enums.RTBPageEnum;
import psp.sap.viewmodel.events.ViewModelEvent;


public class DupEEViewModel extends AbstractPartViewModel {
    private const MAX_LENGTH:Number = 20;
    public function DupEEViewModel() {
        super();
        this.label = RTBPageEnum.DUP_EMP_VIEW;
    }


    [Bindable]
    [BackingProperty]
    public var oldEmployee:String="";

    [Bindable]
    [BackingProperty]
    public var newEmployee:String="";

    [Bindable]
    [BackingProperty]
    public var psId:String="";


    [Bindable]
    [BackingProperty]
    public var oldEmpName:String="";

    [Bindable]
    [BackingProperty]
    public var newEmpName:String="";

    [Bindable]
    [BackingProperty]
    public var companyName:String="";


    [Bindable]
    public var canEditEmployee:Boolean = false;

    [Bindable]
    public var mViewModel:DupEEViewModel;

    public function close():void {
        dispatchEvent( ViewModelEvent.createCloseEvent() );
    }

    override protected function executeSave():void {
        SAP.instance.rtbAutomationService.duplicateEmployee(oldEmployee,newEmployee,psId,createRtbAutomationSaveResponder());

    }

    public  function fetchCompanyName():void {
        if(psId.length>0 && companyName.length==0){
            SAP.instance.showProgress("Fetching company name");
            SAP.instance.rtbAutomationService.findCompanyName(psId,createLoadModelDataResponder(onCompanyNameLoadSucceeded));
        }
    }

    public  function clearCompanyName():void {
        if(companyName.length>0){
                companyName="";
        }
    }

    public  function fetchOldEmployeeName():void {
        if(psId.length>0 && oldEmployee.length>0 && oldEmpName.length==0){
            SAP.instance.showProgress("Fetching employee name");
            SAP.instance.rtbAutomationService.findEmployeeName(psId,oldEmployee,createLoadModelDataResponder(onOldEmpDataLoadSucceeded));
        }
    }

    public  function clearOldEmployeeName():void {
        if (oldEmpName.length>0){
            oldEmpName="";
        }
    }

    public  function fetchNewEmployeeName():void {
        if(psId.length>0 && newEmployee.length>0 && newEmpName.length==0){
            SAP.instance.showProgress("Fetching employee name");
            SAP.instance.rtbAutomationService.findEmployeeName(psId,newEmployee,createLoadModelDataResponder(onNewEmpDataLoadSucceeded));
        }
    }

    public  function clearNewEmployeeName():void {
        if (newEmpName.length>0){
            newEmpName="";
        }
    }

    protected function onCompanyNameLoadSucceeded(e:ResultEvent):void {
        companyName = "CompanyName: "+e.result as String;
        SAP.instance.hideProgress();
        if(companyName=="CompanyName: Company doesn't exist"){
            psId="";
            canEditEmployee=false;
        }else{
            canEditEmployee=true;
        }
    }

    protected function onOldEmpDataLoadSucceeded(e:ResultEvent):void {
        oldEmpName = "EmployeeName: "+e.result as String;
        SAP.instance.hideProgress();
        if(oldEmpName=="EmployeeName: Employee doesn't exist"){
            oldEmployee="";
        }
    }

    protected function onNewEmpDataLoadSucceeded(e:ResultEvent):void {
        newEmpName = "EmployeeName: "+e.result as String;
        SAP.instance.hideProgress();
        if(newEmpName=="EmployeeName: Employee doesn't exist"){
            newEmployee="";
        }
    }
}


}

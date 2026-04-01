/**
 * Created by: smodgil
 * Description: To Create Duplication Employee View
 * Date:02/08/2019
 *
 */
package psp.sap.viewmodel {
import flash.system.System;

import mx.controls.Alert;
import mx.rpc.events.ResultEvent;
import psp.sap.application.SAP;
import psp.sap.application.enums.RTBPageEnum;
import psp.sap.viewmodel.events.ViewModelEvent;
import mx.collections.ArrayCollection;
import psp.sap.model.RTBAutomationJob;
import flash.net.URLRequest;
import flash.net.navigateToURL;
import flash.net.URLRequestMethod;
import mx.utils.URLUtil;
import mx.core.Application;
import psp.app.util.CookieUtil;




public class DupPIViewModel extends AbstractPartViewModel {
    private const MAX_LENGTH:Number = 20;
    public function DupPIViewModel() {
        super();
        this.label = RTBPageEnum.DUP_PI_VIEW;
    }

    [Bindable]
    [BackingProperty]
    private var mSelectedAutomationJob:RTBAutomationJob;

    [Bindable]
    [BackingProperty]
    public var oldPitemID:String="";

    [Bindable]
    [BackingProperty]
    public var newPitemID:String="";

    [Bindable]
    [BackingProperty]
    public var psId:String="";


    [Bindable]
    [BackingProperty]
    public var oldPitemName:String="";

    [Bindable]
    [BackingProperty]
    public var newPitemName:String="";

    [Bindable]
    [BackingProperty]
    public var companyName:String="";


    [Bindable]
    [BackingProperty]
    public var selectedOption:String="";



    [Bindable]
    public var canEditPitem:Boolean = false;

    [Bindable]
    public var mViewModel:DupPIViewModel;

    [Bindable]
    public var automationJobList:ArrayCollection=new ArrayCollection([
        SAP.instance.rtbAutomationService.getAutomationJobEnum(createLoadModelDataResponder(onDataLoadSucceeded))
    ] );


    [Bindable]
    public var rtbJobList:ArrayCollection;



    public function close():void {
        dispatchEvent( ViewModelEvent.createCloseEvent() );
    }

    override protected function executeSave():void {
        SAP.instance.rtbAutomationService.duplicatePitem(oldPitemID,newPitemID,psId,createRtbAutomationSaveResponder());

    }


    protected function onDataLoadSucceeded(e:ResultEvent):void {
        automationJobList = e.result as ArrayCollection;
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

    public  function fetchOldPitemName():void {
        if(psId.length>0 && oldPitemID.length>0 && oldPitemName.length==0){
            SAP.instance.showProgress("Fetching PItem name");
            SAP.instance.rtbAutomationService.findPitemName(psId,oldPitemID,createLoadModelDataResponder(onOldPItemDataLoadSucceeded));
        }
    }

    public  function clearOldPitemName():void {
        if (oldPitemName.length>0){
            oldPitemName="";
        }
    }

    public  function fetchNewPitemName():void {
        if(psId.length>0 && newPitemID.length>0 && newPitemName.length==0){
            SAP.instance.showProgress("Fetching PItem name");
            SAP.instance.rtbAutomationService.findPitemName(psId,newPitemID,createLoadModelDataResponder(onNewPItemDataLoadSucceeded));
        }
    }

    public  function clearNewPitemName():void {
        if (newPitemName.length>0){
            newPitemName="";
        }
    }

    protected function onCompanyNameLoadSucceeded(e:ResultEvent):void {
        companyName = "CompanyName: "+e.result as String;
        SAP.instance.hideProgress();
        if(companyName=="CompanyName: Company doesn't exist"){
            psId="";
            canEditPitem=false;
        }else{
            canEditPitem=true;
        }
    }

    protected function onOldPItemDataLoadSucceeded(e:ResultEvent):void {
        oldPitemName = "PItemName: "+e.result as String;
        SAP.instance.hideProgress();
        if(oldPitemName=="PItemName: PItem doesn't exist"){
            oldPitemID="";
            newPitemID="";
        }
    }

    protected function onNewPItemDataLoadSucceeded(e:ResultEvent):void {
        newPitemName = "PItemName: "+e.result as String;
        SAP.instance.hideProgress();
        if(newPitemID==oldPitemID)
        {
            newPitemID="";
        }
        else if(newPitemName=="PItemName: PItem doesn't exist"){
            newPitemID="";
        }
    }
}


}
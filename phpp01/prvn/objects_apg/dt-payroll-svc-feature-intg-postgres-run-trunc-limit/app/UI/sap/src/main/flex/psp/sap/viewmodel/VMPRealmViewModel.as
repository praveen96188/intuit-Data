package psp.sap.viewmodel {
import mx.controls.Alert;
import mx.rpc.events.ResultEvent;
import psp.sap.application.SAP;
import psp.sap.application.enums.RTBPageEnum;
import psp.sap.viewmodel.events.ViewModelEvent;

public class VMPRealmViewModel extends AbstractPartViewModel {
    private const MAX_LENGTH:Number = 20;
    public function VMPRealmViewModel() {
        super();
        this.label =RTBPageEnum.REALM_ID_VIEW;
    }

    [Bindable]
    [BackingProperty]
    public var psId:String="";

    [Bindable]
    public var canEditRealmId:Boolean = false;

    [Bindable]
    public var mViewModel:VMPRealmViewModel;

    public function close():void {
        dispatchEvent( ViewModelEvent.createCloseEvent() );
    }

    override protected function executeSave():void {

        if(psId.length>0) {
            SAP.instance.vmpAutomationService.updateRealmId(psId, createRtbAutomationSaveResponder());
        }
    }

    public  function clearSourceCompanyId():void {
        if(psId.length>0){
            psId="";
        }
    }
}
}

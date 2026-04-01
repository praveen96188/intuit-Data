package psp.sap.viewmodel {
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.application.enums.RTBPageEnum;
import psp.sap.viewmodel.events.ViewModelEvent;
import psp.sap.model.CompanyUnprocessedRequest;

public class UnprocessedToProcessedReqViewModel extends AbstractPartViewModel {

    public function UnprocessedToProcessedReqViewModel() {
        super();
        this.label = RTBPageEnum.UPR_REQ_VIEW;
    }


    [Bindable]
    [BackingProperty]
    public var psId:String = "";

    [Bindable]
    [BackingProperty]
    public var companyUnprocessedRequest:String = "";

    [Bindable]
    [BackingProperty]
    public var unprocessedRequest:String = "";

    [Bindable]
    public var canProcess:Boolean = false;


    [Bindable]
    public var mViewModel:UnprocessedToProcessedReqViewModel;

    private var mCompanyUnprocessedRequest:CompanyUnprocessedRequest = new CompanyUnprocessedRequest();

    public function close():void {
        dispatchEvent(ViewModelEvent.createCloseEvent());
    }

    override protected function executeSave():void {
        if (psId.length > 0) {
            SAP.instance.rtbAutomationService.processUnprocessedRequests(psId, createRtbAutomationSaveResponder());
        }
    }

    public function clearSourceCompanyId():void {
        if (psId.length > 0) {
            psId = "";
            unprocessedRequest = "";
            companyUnprocessedRequest = "";
        }
    }


    public function fetchCompanyNameAndUnprocessedRequest():void {
        if (psId.length > 0 && companyUnprocessedRequest.length == 0) {
            SAP.instance.showProgress("Fetching company name and unprocessed request count");
            SAP.instance.rtbAutomationService.findCompanyNameAndUnprocessedRequest(psId, createLoadModelDataResponder(onCompanyNameAndUprLoadSucceeded));
        }
    }

    protected function onCompanyNameAndUprLoadSucceeded(e:ResultEvent):void {
        mCompanyUnprocessedRequest = e.result as CompanyUnprocessedRequest;
        SAP.instance.hideProgress();
        if (mCompanyUnprocessedRequest == null) {
            psId = "";
            companyUnprocessedRequest = "Company doesn't exist";
            canProcess = false;
        } else {
            companyUnprocessedRequest = "CompanyName: " + mCompanyUnprocessedRequest.companyLegalName + " Unprocessed Request: " + mCompanyUnprocessedRequest.requestCount as String;
            canProcess = true;
            if (mCompanyUnprocessedRequest.requestCount == 0) {
                canProcess = false;
            }
        }
    }

    public function clearCompanyName():void {
        if (companyUnprocessedRequest.length > 0) {
            companyUnprocessedRequest = "";
            unprocessedRequest = "";
        }
    }
}
}

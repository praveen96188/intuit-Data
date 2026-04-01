package psp.sap.viewmodel {
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.model.TaxCompanyServiceInfo;

public class CompanyDisplayCancellationInfoViewModel extends AbstractPartViewModel{

    [Bindable]
    [BackingProperty]
    public var cancellationInfo:TaxCompanyServiceInfo;

    override protected function loadModelData():void {
        SAP.instance.companyService.getCompanyCancellationInfo(companyKey.sourceSystemCd,
                                                               companyKey.companyId,
                                                               createLoadModelDataResponder(onCompanyCancellationInfoLoaded));

    }

    public function onCompanyCancellationInfoLoaded(e:ResultEvent):void {
        cancellationInfo = e.result as TaxCompanyServiceInfo;
    }

}
}
package psp.sap.viewmodel
{
import psp.sap.application.enums.RiskInspectorPageEnum;
import psp.sap.application.enums.RiskInspectorTopicEnum;

public class IPBasedFSTopicViewModel extends InspectorTopicViewModel
{
    public function IPBasedFSTopicViewModel(inspector:AbstractInspectorViewModel)
    {
        super(inspector, RiskInspectorTopicEnum.IP_BASED_FRAUD_FILTERING);

        addSinglePart(RiskInspectorPageEnum.IP_BASED_FRUAD_FILTERING, IPBasedFraudSearchViewModel);
    }
}
}

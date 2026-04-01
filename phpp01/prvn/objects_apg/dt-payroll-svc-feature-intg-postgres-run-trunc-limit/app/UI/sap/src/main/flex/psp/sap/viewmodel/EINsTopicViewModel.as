package psp.sap.viewmodel {
    import psp.sap.application.enums.EINManagementInspectorPageEnum;
    import psp.sap.application.enums.EINManagementInspectorTopicEnum;

    public class EINsTopicViewModel extends InspectorTopicViewModel {
        public function EINsTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, EINManagementInspectorTopicEnum.EINs);

            addSinglePart(EINManagementInspectorPageEnum.EINS, EINSearchViewModel, "EIN List");           
            addSinglePart(EINManagementInspectorPageEnum.ADD_EIN, EINAddCompanyViewModel);
		}
    }
}
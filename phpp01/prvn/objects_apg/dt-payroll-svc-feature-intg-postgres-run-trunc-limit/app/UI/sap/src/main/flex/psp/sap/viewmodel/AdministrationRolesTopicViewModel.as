package psp.sap.viewmodel
{
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;

    public class AdministrationRolesTopicViewModel extends InspectorTopicViewModel
	{
		public function AdministrationRolesTopicViewModel(inspector:AbstractInspectorViewModel)
		{
			super(inspector, AdministrationInspectorTopicEnum.ROLES);
			
			addSinglePart(AdministrationInspectorPageEnum.MANAGE_ROLES, AdministrationManageRolesViewModel);
						
		}		
	}
}
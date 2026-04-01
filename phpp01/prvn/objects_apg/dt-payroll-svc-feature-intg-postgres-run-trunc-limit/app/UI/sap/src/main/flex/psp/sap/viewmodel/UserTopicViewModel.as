package psp.sap.viewmodel
{
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.AdministrationInspectorTopicEnum;

    public class UserTopicViewModel extends InspectorTopicViewModel
    {
        public function UserTopicViewModel(inspector:AbstractInspectorViewModel)
        {
            super(inspector, AdministrationInspectorTopicEnum.USERS);

            var authView:DDGenericUserViewModel = DDGenericUserViewModel(addSinglePart(AdministrationInspectorPageEnum.DD_AUTHTOOL_USERS, DDGenericUserViewModel, "Manage SAP Authorization Tool Users").part);
            authView.domainId = "AUTH";

            var dduiView:DDGenericUserViewModel = DDGenericUserViewModel(addSinglePart(AdministrationInspectorPageEnum.DDM_USERS, DDGenericUserViewModel, "SAP Users").part);
            dduiView.domainId = "DDUI";

            addSinglePart(AdministrationInspectorPageEnum.DD_USER_EDIT, DDMUserEditViewModel, "Edit SAP User");
            addSinglePart(AdministrationInspectorPageEnum.DD_EDIT_ROLES, DDEditRoleViewModel, "Edit Role");
        }


    }
}
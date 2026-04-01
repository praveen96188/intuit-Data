package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;
    import psp.sap.model.User;
    import psp.sap.validators.SAPValidators;

    public class DDMUserEditViewModel extends AbstractPartViewModel
    {

        [Bindable] [BackingProperty (context=true, hasChanged=true, linkable=false)] public var userDetails:User;

        [Bindable] [BackingProperty (context=true, hasChanged=false, linkable=false)] public var roleList:ArrayCollection = new ArrayCollection();

        private const MAX_LENGTH:Number = 80;
        private const MAX_CORPID_LENGTH:Number = 50;


        public function DDMUserEditViewModel()
        {
            reloadOnSave = true;
        }

        public static function createActivator(userDetails:User, roleList:ArrayCollection):Object {
            return {"userDetails":userDetails, "roleList":roleList};
        }

        override protected function initializeBackingProperties():void {
            this.clearValidators();

            validators.push(SAPValidators.createStringValidator(userDetails, "firstName", true, 0, MAX_LENGTH));

            validators.push(SAPValidators.createStringValidator(userDetails, "lastName", true, 0, MAX_LENGTH));
            
            validators.push(SAPValidators.createStringValidator(userDetails, "corpId", true, 0, MAX_CORPID_LENGTH));

            validators.push(SAPValidators.createRequiredFieldValidator(userDetails, "roleIds"));
        }


        override protected function executeSave():void {
            SAP.instance.userService.updateUserData(userDetails.uniqueId,
                    userDetails.corpId,
                    userDetails.firstName,
                    userDetails.lastName,
                    userDetails.roleIds,
                    createSaveResponder());

        }

    }
}
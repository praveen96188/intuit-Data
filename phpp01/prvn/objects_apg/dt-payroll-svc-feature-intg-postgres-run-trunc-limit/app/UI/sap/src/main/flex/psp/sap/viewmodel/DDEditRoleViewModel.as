package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.validators.StringValidator;

    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.model.UserOperation;
    import psp.sap.validators.SAPValidators;

    public class DDEditRoleViewModel extends AbstractPartViewModel
    {
        private var mName:String;
        private const MAX_LENGTH:Number = 80;

        private var mUserOp:UserOperation = new UserOperation();

        private var mRolePermissions:ArrayCollection;
        [Bindable]
        public var nameValidator:StringValidator;

        public function DDEditRoleViewModel()
        {
            nameValidator = SAPValidators.createStringValidator(this, "name", true, 0, MAX_LENGTH);
            validators.push(nameValidator);
        }
        [Bindable]
        public function get name():String {
            return mName;
        }


        public function set name(value:String):void {
            mName = value;
            updateCanSave();
        }

        protected function get userOp():UserOperation {
            return mUserOp;
        }

        protected function set userOp(value:UserOperation):void {
            mUserOp = value;
        }
        override protected function initializeBackingProperties():void {

        }

        [Bindable]
        public function get rolePermissions():ArrayCollection{
            return mRolePermissions;
        }

        public function set rolePermissions(value:ArrayCollection):void {
            mRolePermissions = value;
            updateCanSave();
        }


        override protected function executeSave():void {
            var saveRoleOperationsList:ArrayCollection;
                // Build the arraycollection to be updated
            for each(var roleOp:UserOperation in rolePermissions){
                if(roleOp.selected == true)
                    saveRoleOperationsList.addItem(roleOp);
            }
        }
    }
}

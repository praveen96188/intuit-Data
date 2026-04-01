package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.rpc.events.ResultEvent;
    import mx.validators.StringValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.SearchResults;
    import psp.sap.model.User;
    import psp.sap.validators.SAPValidators;

    public class DDGenericUserViewModel extends AbstractPartViewModel
    {
        public const DEFAULT_PAGE_SIZE:int = 15;
        private const MAX_LENGTH:Number = 80;
        private const MAX_CORPID_LENGTH:Number = 50;
        private const DEFAULT_STRING:String = "";
        private var mFirstName:String;
        private var mLastName:String;
        private var mCorpId:String;
        private var mRoleId:String;
        private var mSearchFirstName:String;
        private var mSearchLastName:String;
        private var mSearchCorpId:String;


        public var domainId:String;
        protected var saveMethod:String;
        protected var userDetails:User;
        private var mAllUsers:ArrayCollection = new ArrayCollection();

        [Bindable]
        public var searchResults:PaginationCollection = new PaginationCollection(null, DEFAULT_PAGE_SIZE);

        [Bindable]
        public var allRoles:ArrayCollection = new ArrayCollection();


        [Bindable]
        public var firstNameValidator:StringValidator;

        [Bindable]
        public var lastNameValidator:StringValidator;

        [Bindable]
        public var corpIdValidator:StringValidator;


        public function DDGenericUserViewModel()
        {
            reloadOnSave = true;

            firstNameValidator = SAPValidators.createStringValidator(this, "firstName", true, 0, MAX_LENGTH);
            validators.push(firstNameValidator);

            lastNameValidator = SAPValidators.createStringValidator(this, "lastName", true, 0, MAX_LENGTH);
            validators.push(lastNameValidator);

            corpIdValidator = SAPValidators.createStringValidator(this, "corpId", true, 0, MAX_CORPID_LENGTH);
            validators.push(corpIdValidator);
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.userService.searchUsersInDomain(domainId, mSearchFirstName, mSearchLastName, mSearchCorpId, searchResults.startIndex, searchResults.pageSize, searchResults.sortBy, searchResults.sortDesc, createLoadModelDataResponder(onLoadSucceeded));
            SAP.instance.userService.getAllRoles(domainId, createLoadModelDataResponder(onLoadRolesSucceeded));
        }

        protected function onLoadSucceeded(e:ResultEvent):void {
            searchResults.searchResults = SearchResults(e.result);
        }

        // bind roles to the combobox
        protected function onLoadRolesSucceeded(e:ResultEvent):void{
            allRoles = e.result as ArrayCollection;
        }

        [Bindable]
        public function get allUsers():ArrayCollection {
            return mAllUsers;
        }

        public function set allUsers(value:ArrayCollection):void {
            mAllUsers = value;
        }


        public static function canEditUser(user:User):Boolean {

            if(user.isUserDataCustodian()) {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_DATACUSTODIAN);
            } else if(user.isUserHelpDesk()) {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_HELP_DESK);
            } else {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_USERS);
            }

        }

        public static function canRemoveUser(user:User):Boolean {

            var userLoggedInCorpId:String = SAP.instance.session.user.corpId;

                    //Stop us from removing ourselves
            if(userLoggedInCorpId == user.corpId) return false;


            if(user.isUserDataCustodian()) {
                return SAP.canPerformOperation(OperationsEnum.AUTH_REMOVE_DATACUSTODIAN);
            } else if(user.isUserHelpDesk()) {
                return SAP.canPerformOperation(OperationsEnum.AUTH_REMOVE_HELP_DESK);
            } else {
                return SAP.canPerformOperation(OperationsEnum.AUTH_REMOVE_USERS);
            }


        }

        public function pageDataGridLoadDataFunction():void {
            refresh();
        }

        [Bindable]
        public function get firstName():String {
            return mFirstName;
        }

        public function set firstName(value:String):void {
            mFirstName = value;
            updateCanSave();
        }

        [Bindable]
        public function get lastName():String {
            return mLastName;
        }

        public function set lastName(value:String):void {
            mLastName = value;
            updateCanSave();
        }

        [Bindable]
        public function get corpId():String {
            return mCorpId;
        }

        public function set corpId(value:String):void {
            mCorpId = value;
            updateCanSave();
        }

        [Bindable]
        public function get roleId():String {
            return mRoleId;
        }

        public function set roleId(value:String):void {
            mRoleId = value;
            updateCanSave();
        }

        override public function get hasChanged():Boolean {
            return ((firstName != DEFAULT_STRING)||
                    (lastName != DEFAULT_STRING)||
                    (corpId != DEFAULT_STRING));
        }

        [Bindable]
        public function get searchFirstName():String {
            return mSearchFirstName;
        }

        public function set searchFirstName(value:String):void {
            mSearchFirstName = value;
            updateCanSave();
        }

        [Bindable]
        public function get searchLastName():String {
            return mSearchLastName;
        }

        public function set searchLastName(value:String):void {
            mSearchLastName = value;
            updateCanSave();
        }

        [Bindable]
        public function get searchCorpId():String {
            return mSearchCorpId;
        }

        public function set searchCorpId(value:String):void {
            mSearchCorpId = value;
            updateCanSave();
        }

        public function onAddNewUser():void{
            saveMethod = "Add";
            save();
        }


        public function onUsersClick():void{
            inspector.getPage(AdministrationInspectorPageEnum.DD_AUTHTOOL_USERS).activate();
        }

        [Bindable("propertyChange")]
        public virtual function get DDAuthEnabled():Boolean {
            return domainId == "DDUI";
        }

        [Bindable("propertyChange")]
        public function get DDMEnabled():Boolean {
            return domainId == "AUTH";
        }

        public function onDDMClick():void{
            inspector.getPage(AdministrationInspectorPageEnum.DDM_USERS).activate();
        }

        public function goToEditDDMUserData(userDetails:User):void {
            topic.findPage(AdministrationInspectorPageEnum.DD_USER_EDIT).activatePage(DDMUserEditViewModel.createActivator(userDetails, allRoles));
        }

        public function goToRemoveDDMUserData(userDetails:User):void {
            saveMethod = "Remove";
            this.userDetails = userDetails;
            forceSave();
        }

        public function unlockUser(userDetails:User):void {
            saveMethod = "Unlock";
            this.userDetails = userDetails;
            forceSave();
        }

        override protected function initializeBackingProperties():void {
            firstName = DEFAULT_STRING;
            lastName = DEFAULT_STRING;
            corpId = DEFAULT_STRING;
            roleId = DEFAULT_STRING;
        }

        public function canAddChosenUser():Boolean {
            if(roleId == "DATACUST") {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_DATACUSTODIAN);
            } else if(roleId == "HELPDESK") {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_HELP_DESK);
            } else {
                return SAP.canPerformOperation(OperationsEnum.AUTH_ADD_UPDATE_USERS);
            }
        }

        override protected function evaluateCanSave():Boolean {
            return canAddChosenUser() && super.evaluateCanSave();
        }

        override protected function executeSave():void{
            if(saveMethod == "Add"){
                SAP.instance.userService.addNewUserData(corpId,
                        firstName,
                        lastName,
                        new ArrayCollection([roleId]),
                        createSaveResponder());
            } else if(saveMethod == "Remove"){
                SAP.instance.userService.removeUser(userDetails.corpId,
                        createSaveResponder());
            } else if(saveMethod == "Unlock"){
                SAP.instance.userService.unlockUser(userDetails.corpId,
                        createSaveResponder());
            }
        }

    }
}
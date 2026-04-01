package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;

    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.QBDTTokens;
    import psp.sap.validators.SAPValidators;

    public class DataSyncManageViewModel extends AbstractPartViewModel {

        private const ACTION_STOP:int = 0;
        private const ACTION_PUSH:int = 1;

        [Bindable]
        public var tokens:QBDTTokens = new QBDTTokens();

        [Bindable]
        [BackingProperty]
        public var selectedAction:int = ACTION_STOP;

        //individual stop actions
        [Bindable] [BackingProperty] public var stopAllPayrollItemsSelected:Boolean;
        [Bindable] [BackingProperty] public var stopAllEmployeesSelected:Boolean;
        [Bindable] [BackingProperty] public var stopAllPayrollTxnsSelected:Boolean;
        [Bindable] [BackingProperty] public var stopAllPaychecksSelected:Boolean;

        [Bindable] [BackingProperty] public var stopAllDataSelected:Boolean;
        [Bindable] [BackingProperty] public var stopAllPriorYearTxnsPaychecksSelected:Boolean;
        [Bindable] [BackingProperty] public var stopAllEEsWithoutCurrentYearPayrollSelected:Boolean;

        //individual push actions
        [Bindable] [BackingProperty] public var pushPayrollItemsSelected:Boolean;
        [Bindable] [BackingProperty] public var pushEmployeesSelected:Boolean;
        [Bindable] [BackingProperty] public var pushCurrentYearPayrollTxnsSelected:Boolean;
        [Bindable] [BackingProperty] public var pushCurrentYearPaychecksSelected:Boolean;

        [Bindable] [BackingProperty] public var pushAllPriorYearTxnsPaychecksSelected:Boolean;


        [Bindable]
        [BackingProperty]
        public var memoString:String;

        [Bindable]
        public var actionRequiredValidator:NumberValidator;

        [Bindable]
        public var memoRequiredValidator:Validator;

        [Bindable]
        public var caseId:String;

        public function DataSyncManageViewModel() {
            super();
            this.label = CompanyInspectorPageEnum.DATA_SYNC_SEARCH_VIEW;
            this.reloadOnActivate = false;
            this.reloadOnSave = true;

            memoRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "memoString", true);
            validators.push(memoRequiredValidator);

            actionRequiredValidator = SAPValidators.createNumberValidator(mSelectedActions, "length", true, 1, null);
            actionRequiredValidator.triggerEvent = "collectionChange";
            validators.push(actionRequiredValidator);
        }


        override protected function loadModelData():void {
            SAP.instance.taxService.getQBDTTokens(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onTokensLoaded));
        }

        private function onTokensLoaded(e:ResultEvent):void {
            tokens = QBDTTokens(e.result);
        }

        [ArrayElementType("String")]
        private var mSelectedActions:ArrayCollection = new ArrayCollection();

        [Bindable("propertyChange")]
        public function get selectedActions():ArrayCollection {
            var actions:Array = [];
            if (selectedAction == ACTION_STOP) {
                if (stopAllPayrollItemsSelected) actions.push("stopAllPayrollItems");
                if (stopAllEmployeesSelected) actions.push("stopAllEmployees");
                if (stopAllPayrollTxnsSelected) actions.push("stopAllPayrollTxns");
                if (stopAllPaychecksSelected) actions.push("stopAllPaychecks");
                if (stopAllDataSelected) actions.push("stopAllData");
                if (stopAllPriorYearTxnsPaychecksSelected) actions.push("stopAllPriorYearTxnsPaychecks");
                if (stopAllEEsWithoutCurrentYearPayrollSelected) actions.push("stopAllEEsWithoutCurrentYearPayroll");
            } else if (selectedAction == ACTION_PUSH) {
                if (pushEmployeesSelected) actions.push("pushEmployees");
                if (pushPayrollItemsSelected) actions.push("pushPayrollItems");
                if (pushCurrentYearPayrollTxnsSelected) actions.push("pushCurrentYearPayrollTxns");
                if (pushCurrentYearPaychecksSelected) actions.push("pushCurrentYearPaychecks");
                if (pushAllPriorYearTxnsPaychecksSelected) actions.push("pushAllPriorYearTxnsPaychecks");
            }
            mSelectedActions.source = actions;
            mSelectedActions.refresh();
            return mSelectedActions;
        }

        public function get selectedActionString():String {
            if (selectedAction == ACTION_PUSH)  {
                return "Push";
            } else if (selectedAction == ACTION_STOP) {
                return "Stop";
            }
            return null;
        }

        override protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            mSelectedActions = selectedActions;
            return super.evaluateIsValid(fireEvents);
        }

        override public function get hasChanged():Boolean {
            return true;
        }


        override protected function executeSave():void {
            SAP.instance.taxService.updateDataSyncTokens(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    selectedActionString,
                    selectedActions,
                    memoString,caseId,
                    createSaveResponder(onSaveSucceeded));            
        }


        private function onSaveSucceeded(e:ResultEvent):void {
            memoString = "";

            stopAllPayrollItemsSelected = false;
            stopAllEmployeesSelected = false;
            stopAllPayrollTxnsSelected = false;
            stopAllPaychecksSelected = false;

            stopAllDataSelected = false;
            stopAllPriorYearTxnsPaychecksSelected = false;
            stopAllEEsWithoutCurrentYearPayrollSelected = false;

            pushPayrollItemsSelected = false;
            pushEmployeesSelected = false;
            pushCurrentYearPayrollTxnsSelected = false;
            pushCurrentYearPaychecksSelected = false;

            pushAllPriorYearTxnsPaychecksSelected = false;

        }
    }
}

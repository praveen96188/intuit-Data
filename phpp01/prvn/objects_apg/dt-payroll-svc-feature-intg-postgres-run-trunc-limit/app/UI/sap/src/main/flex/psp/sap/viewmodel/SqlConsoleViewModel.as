package psp.sap.viewmodel {

    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.AdministrationInspectorPageEnum;
    import psp.sap.model.SqlExecutionResult;
    import psp.sap.validators.SAPValidators;

public class SqlConsoleViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty]
        public var sqlStatement:String;

        [Bindable] [BackingProperty]
        public var reason:String;

        [Bindable] [BackingProperty]
        public var expectedRowCount:String;

        [Bindable]
        public var expectedRowCountValidator:NumberValidator;

        [Bindable]
        public var executionTime:String;

        [Bindable]
        public var rowCount:int;

        [Bindable]
        public var showErrorMessage:Boolean = false;

        [Bindable]
        public var errorMessage:String;

        public function SqlConsoleViewModel() {
            this.label = AdministrationInspectorPageEnum.SQL_ADMINISTRATION;
            this.reloadOnActivate = true;
            this.reloadOnSave = false;

            expectedRowCountValidator = SAPValidators.createNumberValidator(this, "expectedRowCount", true, null, null, true, 0);
            validators.push(expectedRowCountValidator);
        }

        override protected function initializeDefaults():void {
            sqlStatement = "";
            reason = "";
            expectedRowCount = "";
            executionTime = "";
            rowCount = 0;
            errorMessage = "";
            showErrorMessage = false;
        }

        override protected function executeSave():void {
            SAP.instance.administrationService.executeSql(sqlStatement, reason, parseInt(expectedRowCount),
                                                                   createSaveResponder(onSaveSucceeded, onSaveFaulted))
        }

        protected function onSaveSucceeded(e:ResultEvent):void {
            var sqlStatementResult:SqlExecutionResult = e.result as SqlExecutionResult;
            sqlStatement = sqlStatementResult.sqlStatement;
            reason = sqlStatementResult.reason;
            expectedRowCount = sqlStatementResult.expectedRowCount.toString();
            executionTime = sqlStatementResult.executionTime;
            rowCount = sqlStatementResult.rowCount;
            errorMessage = sqlStatementResult.errorMessage;
            showErrorMessage = (errorMessage != null && errorMessage.length > 0);

            // stop default behavior of showing 'your changes have been saved'
            saveFaulted = showErrorMessage;
            saveMsg = showErrorMessage ? "Epic Failure" : "";
        }

        protected function onSaveFaulted(e:FaultEvent, token:Object = null):void {
            sqlStatement = sqlStatement
            executionTime = "";
            rowCount = 0;
        }
    }
}
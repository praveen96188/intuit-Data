package psp.sap.viewmodel {
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.SettlementTypeEnum;
    import psp.sap.model.Transaction;
    import psp.sap.validators.SAPValidators;

    public class RefundDebitPopUpViewModel extends AbstractPartViewModel {

        private var mSettlementType:SettlementTypeEnum;

        private const EMPTY_STRING:String = "";

        protected var DEFAULT_SETTLEMENT_TYPE:SettlementTypeEnum = SettlementTypeEnum.ACH;

        [Bindable]
        public var transaction:Transaction = null;

        [Bindable]
        [BackingProperty]
        public var notes:String = EMPTY_STRING;

        [Bindable]
        public var settlementTypes:Array = SettlementTypeEnum.ach_check_wire;

        [Bindable]
        public var notesValidator:Validator;

        public function RefundDebitPopUpViewModel() {

            notesValidator = SAPValidators.createStringValidator(this, "notes", true, 1, 3900);
            validators.push(notesValidator);
            this.reloadOnSave = true;
        }

        override protected function onActivating():void {
            notes = EMPTY_STRING;
            settlementType = DEFAULT_SETTLEMENT_TYPE;
        }

        override protected function executeSave():void {
            if(transaction != null) {
                SAP.instance.taxService.createRefundDebit(transaction.transactionId, notes, settlementTypeCode, createSaveResponder(),company.companyId);
            }
        }

        [Bindable("propertyChange")]
        public function get canSelectNonStandardSettlementTypes():Boolean {
            return SAP.canPerformOperation(OperationsEnum.SELECT_NON_STANDARD_SETTLEMENT_TYPE);
        }

        [Bindable]
        public function get settlementType():SettlementTypeEnum {
            return mSettlementType;
        }

        public function set settlementType(value:SettlementTypeEnum):void {
            if (value == null)
                value = DEFAULT_SETTLEMENT_TYPE;

            mSettlementType = value;

        }

        protected function get settlementTypeCode():String {
            return mSettlementType.code;
        }
    }
}

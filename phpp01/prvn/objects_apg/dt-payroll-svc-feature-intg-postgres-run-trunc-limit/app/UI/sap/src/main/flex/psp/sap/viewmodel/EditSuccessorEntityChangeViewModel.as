package psp.sap.viewmodel {
    import mx.core.UIComponent;
    import mx.validators.Validator;

import psp.sap.application.SAP;

import psp.sap.validators.SAPValidators;

    public class EditSuccessorEntityChangeViewModel extends AbstractPartViewModel {
        public static const SUCCESSOR:String = "Successor";
        public static const NON_SUCCESSOR:String = "NonSuccessor";

        [Bindable]
        public var effectiveDateValidator:Validator;
        [Bindable]
        public var oldEinValidator:Validator;
        [Bindable]
        [BackingProperty]
        public var oldEIN:String;
        [Bindable]
        [BackingProperty]
        public var effectiveDate:String = "";
        private var mSuccessor:String;

        public function EditSuccessorEntityChangeViewModel() {
            oldEinValidator = SAPValidators.createEinValidator(this, "oldEIN", true);
            var fromValidationDate:Date = SAP.instance.PSPDate;
            fromValidationDate.setDate(fromValidationDate.getDate() - (9*365));
            effectiveDateValidator = SAPValidators.createDateValidator(this, "effectiveDate", true, 0, -1, fromValidationDate, "Effective Date is way in the past.", null);

        }

        override protected function initializeBackingProperties():void {
            clearValidators();
            validators.push(oldEinValidator);
            validators.push(effectiveDateValidator);
            validators.push(SAPValidators.createRequiredFieldValidator(this, "successor", true));
            successor = NON_SUCCESSOR;
        }

        override protected function loadModelData():void {
            modelDataLoaded();
        }

        [Bindable]
        [BackingProperty]
        public function get successor():String {
            return mSuccessor;
        }

        public function set successor(value:String):void {
            mSuccessor = value;
            effectiveDateValidator.enabled = (value == SUCCESSOR);
            oldEinValidator.enabled = (value == SUCCESSOR);
            if (!effectiveDateValidator.enabled && effectiveDateValidator.listener != null) {
                UIComponent(effectiveDateValidator.listener).errorString = "";
            }
            if (!oldEinValidator.enabled && oldEinValidator.listener != null) {
                UIComponent(oldEinValidator.listener).errorString = "";
            }
        }

    }
}
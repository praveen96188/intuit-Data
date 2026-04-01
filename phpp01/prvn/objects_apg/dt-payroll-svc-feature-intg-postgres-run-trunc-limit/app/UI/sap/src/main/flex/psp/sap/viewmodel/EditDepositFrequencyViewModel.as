/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 5:36 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;
    import flash.utils.Dictionary;

    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.DepositFrequency;
    import psp.sap.model.DepositFrequencyCollection;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.validators.SAPValidators;

    public class EditDepositFrequencyViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditDepositFrequencyViewModel() {
            reloadOnSave = true;
        }


        [Bindable]
        public var defaultDepositFrequency:String;

        [Bindable]
        [ArrayElementType("String")]
        public var availableFrequencies:ArrayCollection;


        [Bindable]
        [BackingProperty (recursive=true)]
        [ArrayElementType("psp.sap.model.DepositFrequency")]
        public var depositFrequencies:ArrayCollection;

        [ArrayElementType("psp.sap.model.DepositFrequency")]
        private var tempDepositFrequencies:ArrayCollection;

        private var effectiveDateValidators:Dictionary = new Dictionary();
        private var depositFrequencyValidators:Dictionary = new Dictionary();

        [Bindable] public var dateOutOfOrder:Boolean = false;
        [Bindable] public var consecutiveDepositFrequencySame:Boolean = false;

        override protected function loadModelData():void {
            SAP.instance.taxService.getAllDepositFrequencies(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onDepositFrequenciesLoaded));
        }

        private function onDepositFrequenciesLoaded(e:ResultEvent):void {
            var depositFrequencyCollection:DepositFrequencyCollection = DepositFrequencyCollection(e.result);
            tempDepositFrequencies = depositFrequencyCollection.depositFrequencies;
            availableFrequencies = depositFrequencyCollection.availableFrequencies;
            availableFrequencies.addItemAt("", 0);
            defaultDepositFrequency = depositFrequencyCollection.defaultDepositFrequency;
        }

        override protected function initializeBackingProperties():void {
            validators.length = 0;
            effectiveDateValidators = new Dictionary();
            depositFrequencyValidators = new Dictionary();

            for each (var depositFrequency:DepositFrequency in tempDepositFrequencies) {
                depositFrequency.effectiveDateString = SAPDateFormatters.dateFormatShort.format(depositFrequency.effectiveDate);
                depositFrequency.newDepositFrequency = depositFrequency.depositFrequency;
                depositFrequency.canDelete = true;

                if (depositFrequency.obsoleteFrequency != null) {
                    depositFrequency.newDepositFrequency = "dummy";
                }
                createValidators(depositFrequency);

            }

            if (tempDepositFrequencies.length > 0) {
                DepositFrequency(tempDepositFrequencies.getItemAt(0)).canDelete = false;
            } else {
                insertAfter(-1);
            }


            depositFrequencies = tempDepositFrequencies;
        }

        private function createValidators(depositFrequency:DepositFrequency):void {
            effectiveDateValidators[depositFrequency] = SAPValidators.createDateValidator(depositFrequency, "effectiveDateString", true, -1, -1);
            validators.push(effectiveDateValidators[depositFrequency]);

            depositFrequencyValidators[depositFrequency] = SAPValidators.createRequiredFieldValidator(depositFrequency, "newDepositFrequency");
            validators.push(depositFrequencyValidators[depositFrequency]);
        }

        public function insertAfter(index:int):void {
            var newDepositFrequency:DepositFrequency = new DepositFrequency();
            newDepositFrequency.canDelete = true;
            newDepositFrequency.depositFrequency = "";
            newDepositFrequency.newDepositFrequency = "";
            newDepositFrequency.effectiveDate = null;
            newDepositFrequency.effectiveDateString = "";

            newDepositFrequency.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, function(e:Event):void {
                updateCanSave();
            });

            createValidators(newDepositFrequency);

            depositFrequencies.addItemAt(newDepositFrequency, index + 1);

            updateCanSave();
        }

        public function deleteAt(index:int):void {
            var deletedDepositFrequency:DepositFrequency = DepositFrequency(depositFrequencies.removeItemAt(index));
            //will leave these in the validator collection, but shouldn't cause any harm
            getEffectiveDateValidator(deletedDepositFrequency).enabled = false;
            getDepositFrequencyValidator(deletedDepositFrequency).enabled = false;

            updateCanSave();
        }

        public function getEffectiveDateValidator(depositFrequency:DepositFrequency):Validator {
            return Validator(effectiveDateValidators[depositFrequency]);
        }

        public function getDepositFrequencyValidator(depositFrequency:DepositFrequency):Validator {
            return Validator(depositFrequencyValidators[depositFrequency]);
        }

        override public function get hasChanged():Boolean {
            //stupid transient stuff, so rewriting this one by hand
            if (backingPropertiesSnapshot == null) {
                return false;
            }
            var snapShotFrequencies:ArrayCollection = backingPropertiesSnapshot["depositFrequencies"] as ArrayCollection;
            if (snapShotFrequencies == null) {
                return false;
            }
            if (snapShotFrequencies.length != depositFrequencies.length) {
                return true;
            }
            for (var i:int=0; i < depositFrequencies.length; i++) {
                var depositFrequency:DepositFrequency = DepositFrequency(depositFrequencies.getItemAt(i));
                var snapShotFrequency:DepositFrequency = DepositFrequency(snapShotFrequencies.getItemAt(i));
                if (depositFrequency.newEffectiveDateValue.fullYear != snapShotFrequency.effectiveDate.fullYear
                        || depositFrequency.newEffectiveDateValue.month != snapShotFrequency.effectiveDate.month
                        || depositFrequency.newEffectiveDateValue.date != snapShotFrequency.effectiveDate.date) {
                    return true;
                }
                if (depositFrequency.newDepositFrequency != snapShotFrequency.depositFrequency && depositFrequency.obsoleteFrequency == null) {
                    return true;
                }
            }

            return false;
        }

        override protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            dateOutOfOrder = false;
            consecutiveDepositFrequencySame = false;

            var validatorsValid:Boolean = super.evaluateIsValid(fireEvents);
            if (!validatorsValid) {
                return validatorsValid;
            }

            var lastDate:Date = null;
            var lastDepositFrequency:String = null;
            for each (var depositFrequency:DepositFrequency in depositFrequencies) {
                if (lastDate != null && depositFrequency.newEffectiveDateValue.time <= lastDate.time) {
                    dateOutOfOrder = true;
                    return false;
                }
                if (lastDepositFrequency == depositFrequency.newDepositFrequency) {
                    consecutiveDepositFrequencySame = true;
                    return false;
                }
                lastDate = depositFrequency.newEffectiveDateValue;
                lastDepositFrequency = depositFrequency.newDepositFrequency;
            }

            return true;
        }

        override protected function executeSave():void {
            for each (var depositFrequency:DepositFrequency in depositFrequencies) {
                depositFrequency.synchronizeTransients();
            }

            SAP.instance.taxService.updateDepositFrequencies(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, depositFrequencies, createSaveResponder());
        }
    }
}

/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 5:36 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;
    import flash.utils.Dictionary;

    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPRateFormatter;
    import psp.sap.model.LawQuarterRates;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.Quarter;
    import psp.sap.model.QuarterRate;
    import psp.sap.validators.SAPValidators;

    public class EditRatesMultipleViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditRatesMultipleViewModel() {
            reloadOnSave = true;
        }

        [ArrayElementType("String")]
        [Bindable] public var taxYears:ArrayCollection;

        [ArrayElementType("String")]
        [Bindable] public var taxQuarters:ArrayCollection = new ArrayCollection(["", "Q1", "Q2", "Q3", "Q4"]);

        [Bindable]
        [BackingProperty (recursive=true)]
        [ArrayElementType("psp.sap.model.LawQuarterRates")]
        public var lawQuarterRates:ArrayCollection;

        [ArrayElementType("psp.sap.model.LawQuarterRates")]
        private var tempLawQuarterRates:ArrayCollection;

        private var newPercentageTextValidators:Dictionary = new Dictionary();
        private var quarterYearValidators:Dictionary = new Dictionary();
        private var quarterQuarterValidators:Dictionary = new Dictionary();

        [Bindable] public var quarterOutOfOrder:Boolean = false;
        [Bindable] public var consecutiveRateSame:Boolean = false;

        [Bindable]
        [BackingProperty (hasChanged=false)]
        public var pushToQuickBooks:Boolean;

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.taxService.getFirstTaxYear(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onFirstTaxYearLoaded));
            SAP.instance.taxService.findAllEditableRates(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onLawQuarterRatesLoaded));
        }

        private function onFirstTaxYearLoaded(e:ResultEvent):void {
            taxYears = new ArrayCollection();
            taxYears.addItem("");
            var firstTaxYear:int = int(e.result);

            for (var i:int = firstTaxYear; i <= SAP.instance.PSPDate.fullYear + 1; i++) {
                taxYears.addItem(i.toString());
            }
        }

        private function onLawQuarterRatesLoaded(e:ResultEvent):void {
            tempLawQuarterRates = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            validators.length = 0;
            newPercentageTextValidators = new Dictionary();
            quarterYearValidators = new Dictionary();
            quarterQuarterValidators = new Dictionary();

            for each (var lawQuarterRate:LawQuarterRates in tempLawQuarterRates) {
                for each (var quarterRate:QuarterRate in lawQuarterRate.rates) {
                    quarterRate.newPercentage = quarterRate.currentPercentage;
                    quarterRate.newPercentageText = SAPRateFormatter.formatPercentageAsInput(quarterRate.newPercentage);
                    quarterRate.yearText = quarterRate.quarter.year.toString();
                    quarterRate.quarterText = "Q" + quarterRate.quarter.quarter;
                    quarterRate.canDelete = true;

                    createValidators(quarterRate);
                }
                if (lawQuarterRate.rates.length > 0) {
                    QuarterRate(lawQuarterRate.rates.getItemAt(0)).canDelete = false;
                }
            }

            lawQuarterRates = tempLawQuarterRates;

            var i:int = 0;
            for each (lawQuarterRate in lawQuarterRates) {
                if (lawQuarterRate.rates.length == 0) {
                    insertAfter(i, -1);
                }
                i++;
            }

            lawQuarterRates.addEventListener(CollectionEvent.COLLECTION_CHANGE, function(e:Event):void {
                dispatchEvent(e);
            });
            dispatchEvent(new Event(CollectionEvent.COLLECTION_CHANGE));

            pushToQuickBooks = true;
        }

        private function createValidators(quarterRate:QuarterRate):void {
            newPercentageTextValidators[quarterRate] = SAPValidators.createNumberValidator(quarterRate, "newPercentageText", true,  null,  null,  false, null);
            validators.push(newPercentageTextValidators[quarterRate]);

            quarterYearValidators[quarterRate] = SAPValidators.createRequiredFieldValidator(quarterRate, "yearText");
            validators.push(quarterYearValidators[quarterRate]);

            quarterQuarterValidators[quarterRate] = SAPValidators.createRequiredFieldValidator(quarterRate, "quarterText");
            validators.push(quarterQuarterValidators[quarterRate]);
        }

        public function editSingleRate():void {
            dispatchEvent(new Event(AgencyInfoViewModel.EDIT_SINGLE_RATE));
        }

        public function insertAfter(lawIndex:int,  quarterIndex:int):void {
            var newQuarterRate:QuarterRate = new QuarterRate();
            newQuarterRate.canDelete = true;
            newQuarterRate.currentPercentage = -1;
            newQuarterRate.newPercentage = 0;
            newQuarterRate.newPercentageText = "";
            newQuarterRate.yearText = "";
            newQuarterRate.quarterText = "";
            newQuarterRate.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, function(e:Event):void {
                updateCanSave();
            });

            createValidators(newQuarterRate);

            LawQuarterRates(lawQuarterRates.getItemAt(lawIndex)).rates.addItemAt(newQuarterRate, quarterIndex + 1);

            updateCanSave();

            dispatchEvent(new Event(CollectionEvent.COLLECTION_CHANGE));
        }

        public function deleteAt(lawIndex:int, quarterIndex:int):void {
            var deletedQuarterRate:QuarterRate = QuarterRate(LawQuarterRates(lawQuarterRates.getItemAt(lawIndex)).rates.removeItemAt(quarterIndex));
            //will leave these in the validator collection, but shouldn't cause any harm
            getNewPercentageTextValidator(deletedQuarterRate).enabled = false;
            getQuarterYearValidator(deletedQuarterRate).enabled = false;
            getQuarterQuarterValidator(deletedQuarterRate).enabled = false;

            updateCanSave();

            dispatchEvent(new Event(CollectionEvent.COLLECTION_CHANGE));
        }

        public function getNewPercentageTextValidator(quarterRate:QuarterRate):Validator {
            return Validator(newPercentageTextValidators[quarterRate]);
        }

        public function getQuarterYearValidator(quarterRate:QuarterRate):Validator {
            return Validator(quarterYearValidators[quarterRate]);
        }

        public function getQuarterQuarterValidator(quarterRate:QuarterRate):Validator {
            return Validator(quarterQuarterValidators[quarterRate]);
        }


        override protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            quarterOutOfOrder = false;
            consecutiveRateSame = false;

            var validatorsValid:Boolean = super.evaluateIsValid(fireEvents);
            if (!validatorsValid) {
                return validatorsValid;
            }

            for each (var lawQuarterRate:LawQuarterRates in lawQuarterRates) {
                var lastQuarter:Quarter = null;
                var lastRate:Number = NaN;
                for each (var quarterRate:QuarterRate in lawQuarterRate.rates) {
                    if (lastQuarter != null && !quarterRate.quarterValue.isAfter(lastQuarter)) {
                        quarterOutOfOrder = true;
                        return false;
                    }
                    if (!isNaN(lastRate) && lastRate == quarterRate.newPercentageTextValue) {
                        consecutiveRateSame = true;
                        return false;
                    }
                    lastQuarter = quarterRate.quarterValue;
                    lastRate = quarterRate.newPercentageTextValue;
                }

            }

            return true;
        }

        override protected function executeSave():void {
            for each (var lawQuarterRate:LawQuarterRates in lawQuarterRates) {
                for each (var quarterRate:QuarterRate in lawQuarterRate.rates) {
                    quarterRate.synchronizeTransients();
                }
            }

            SAP.instance.taxService.updateAllRates(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, lawQuarterRates, pushToQuickBooks, createSaveResponder());
        }
    }
}

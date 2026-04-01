/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:15 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
import mx.validators.RegExpValidator;
import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPRateFormatter;
    import psp.sap.model.LawRate;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.QuarterLawRates;
    import psp.sap.validators.BooleanValidator;
    import psp.sap.validators.SAPValidators;

    public class EditRatesViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditRatesViewModel() {
        }

        [Bindable]
        [BackingProperty (recursive=true)]
        [ArrayElementType("psp.sap.model.QuarterLawRates")]
        public var quarterLawRates:ArrayCollection;

        private var mSelectedQuarterLawRate:QuarterLawRates;

        [Bindable]
        [BackingProperty (hasChanged=false)]
        public var pushToQuickBooks:Boolean;

        private var mAllowUpdateDuringBlackout:Boolean;
        private var mAllowUpdateOutsideOfBoundaries:Boolean;

        [Bindable]
        public var userHasRateSuperPermissions:Boolean;

        [Bindable]
        public var userCanEditNonCurrentQuarter:Boolean;


        override protected function loadModelData():void {
            SAP.instance.taxService.findEditableQuarterRates(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onLawRateQuartersLoaded));
        }

        private function onLawRateQuartersLoaded(e:ResultEvent):void {
            quarterLawRates = ArrayCollection(e.result);
        }


        override protected function initializeBackingProperties():void {
            //Default quarter is current quarter
            for each (var quarterLawRate:QuarterLawRates in quarterLawRates) {
                if (quarterLawRate.quarter.isCurrentQuarter()) {
                    selectedQuarterLawRate = quarterLawRate;
                }

                for each (var lawRate:LawRate in quarterLawRate.lawRates) {
                    if (lawRate.hasCurrentRate) {
                        lawRate.newPercentage = lawRate.currentPercentage;
                        lawRate.newPercentageText = SAPRateFormatter.formatPercentageAsInput(lawRate.currentPercentage);
                    } else {
                        lawRate.newPercentage = 0;
                        lawRate.newPercentageText = "";
                    }
                }
            }

            pushToQuickBooks = true;
            allowUpdateDuringBlackout = false;
            allowUpdateOutsideOfBoundaries = false;

            userHasRateSuperPermissions = SAP.canPerformOperation(OperationsEnum.RATE_SUPER_USER);
            userCanEditNonCurrentQuarter = SAP.canPerformOperation(OperationsEnum.EDIT_RATES_IN_OTHER_QTRS);
        }

        [Bindable]
        public function get selectedQuarterLawRate():QuarterLawRates {
            return mSelectedQuarterLawRate;
        }

        public function set selectedQuarterLawRate(value:QuarterLawRates):void {
            mSelectedQuarterLawRate = value;

            //reset values for other quarters (s.t. hasChanged = false)
            for each (var quarterLawRate:QuarterLawRates in quarterLawRates) {
                for each (var allLawRate:LawRate in quarterLawRate.lawRates) {
                    if (allLawRate.hasCurrentRate) {
                        allLawRate.newPercentage = allLawRate.currentPercentage;
                        allLawRate.newPercentageText = SAPRateFormatter.formatPercentageAsInput(allLawRate.currentPercentage);
                    } else {
                        allLawRate.newPercentage = 0;
                        allLawRate.newPercentageText = "";
                    }
                }
            }

            recreateValidators();
        }


        [Bindable]
        [BackingProperty(hasChanged="false")]
        public function get allowUpdateDuringBlackout():Boolean {
            return mAllowUpdateDuringBlackout;
        }

        //noinspection JSUnusedGlobalSymbols
        public function set allowUpdateDuringBlackout(value:Boolean):void {
            mAllowUpdateDuringBlackout = value;
            updateValidators();
        }

        [Bindable]
        [BackingProperty(hasChanged="false")]
        public function get allowUpdateOutsideOfBoundaries():Boolean {
            return mAllowUpdateOutsideOfBoundaries;
        }

        //noinspection JSUnusedGlobalSymbols
        public function set allowUpdateOutsideOfBoundaries(value:Boolean):void {
            mAllowUpdateOutsideOfBoundaries = value;
            updateValidators();
        }

        private function recreateValidators():void {
            validators.length = 0;
            for each (var lawRate:LawRate in selectedQuarterLawRate.lawRates) {
                if(lawRate.hasValuesInsteadOfRanges){

                    validators.push(SAPValidators.createRegExValidator(
                            lawRate,
                            "newPercentageText",
                            true,
                            getRegexPattern(lawRate),
                            lawRate,
                            "The value matches none of the following expected values: \n"+lawRate.minPercentage.toString() + (lawRate.maxPercentage>=0 ? "\n"+lawRate.maxPercentage.toString() : "")));
                }
                else {
                    validators.push(SAPValidators.createNumberValidator(
                            lawRate,
                            "newPercentageText",
                            lawRate.hasCurrentRate,
                                    allowUpdateOutsideOfBoundaries || lawRate.minPercentage < 0 ? null : lawRate.minPercentage,
                                    allowUpdateOutsideOfBoundaries || lawRate.maxPercentage < 0 ? null : lawRate.maxPercentage,
                            false,
                                    lawRate.maxPrecision < 0 ? null : lawRate.maxPrecision));
                }
            }

            var booleanValidator:BooleanValidator = SAPValidators.createBooleanValidator(selectedQuarterLawRate, "underBlackout", true, false);
            booleanValidator.enabled = !allowUpdateDuringBlackout;
            validators.push(booleanValidator);

            updateCanSave();
        }

        private function getRegexPattern(lawRate:LawRate):String {
            var retStr:String="^0*";
            if(lawRate.minPercentage%1==0)
            {
                retStr+=lawRate.minPercentage.toString()+"(\\.0?)?0*$";
            }
            else{
                retStr+=lawRate.minPercentage.toString().replace(".", "\\.")+"0*$";
            }
            if(lawRate.maxPercentage>=0) {
                if (lawRate.maxPercentage % 1 == 0) {
                    retStr += "|^0*" + lawRate.maxPercentage.toString() + "(\\.0?)?0*$";
                }
                else {
                    retStr += "|^0*" + lawRate.maxPercentage.toString().replace(".", "\\.") + "0*$";
                }
            }
            return retStr;
        }

        private function updateValidators():void {
            var i:int = 0;
            for each (var lawRate:LawRate in selectedQuarterLawRate.lawRates) {
                if(lawRate.hasValuesInsteadOfRanges)
                {
                    if(allowUpdateOutsideOfBoundaries) {
                        RegExpValidator(validators[i]).expression = "\\d*(\\.\\d*)";
                    }
                    else {
                        RegExpValidator(validators[i]).expression = getRegexPattern(lawRate);
                    }
                }
                else {
                    NumberValidator(validators[i]).minValue = allowUpdateOutsideOfBoundaries || lawRate.minPercentage < 0 ? null : lawRate.minPercentage;
                    NumberValidator(validators[i]).maxValue = allowUpdateOutsideOfBoundaries || lawRate.maxPercentage < 0 ? null : lawRate.maxPercentage;
                }
                i++;
            }
            Validator(validators[i]).enabled = !allowUpdateDuringBlackout;

            updateCanSave();
        }

        public function editMultipleRates():void {
            dispatchEvent(new Event(AgencyInfoViewModel.EDIT_MULTIPLE_RATES));
        }

        override protected function executeSave():void {
            for each (var lawRate:LawRate in selectedQuarterLawRate.lawRates) {
                lawRate.synchronizeTransients();
            }

            SAP.instance.taxService.updateRates(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, selectedQuarterLawRate, pushToQuickBooks, createSaveResponder());
        }
    }
}

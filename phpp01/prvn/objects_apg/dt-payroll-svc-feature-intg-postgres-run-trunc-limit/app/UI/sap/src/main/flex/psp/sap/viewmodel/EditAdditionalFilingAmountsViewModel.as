/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:15 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.CompanyFilingAmount;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.QuarterCompanyFilingAmounts;
    import psp.sap.validators.SAPValidators;

    public class EditAdditionalFilingAmountsViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty (context=true)]
        public var paymentTemplate:PaymentTemplate;

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function EditAdditionalFilingAmountsViewModel() {
        }

        [Bindable]
        [BackingProperty (recursive=true)]
        [ArrayElementType("psp.sap.model.QuarterCompanyFilingAmounts")]
        public var quarterFilingAmounts:ArrayCollection;

        private var mSelectedQuarterCompanyFilingAmount:QuarterCompanyFilingAmounts;

        [Bindable]
        public var userCanEditNonCurrentQuarter:Boolean;

        override protected function loadModelData():void {
            SAP.instance.taxService.findEditableAdditionalFilingAmounts(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onFilingAmountQuartersLoaded));
        }

        private function onFilingAmountQuartersLoaded(e:ResultEvent):void {
            quarterFilingAmounts = ArrayCollection(e.result);
        }

        override protected function initializeBackingProperties():void {
            //Default quarter is current quarter
            for each (var quarterFilingAmount:QuarterCompanyFilingAmounts in quarterFilingAmounts) {
                if (quarterFilingAmount.quarter.isCurrentQuarter()) {
                    selectedQuarterCompanyFilingAmount = quarterFilingAmount;
                }

                for each (var companyFilingAmount:CompanyFilingAmount in quarterFilingAmount.amounts) {
                    if (companyFilingAmount.hasCurrentValue) {
                        companyFilingAmount.newValue = companyFilingAmount.value;
                    } else {
                        companyFilingAmount.value = "";
                    }
                }
            }

            userCanEditNonCurrentQuarter = SAP.canPerformOperation(OperationsEnum.EDIT_FILING_AMTS_OTHER_QTR);

        }

        [Bindable]
        public function get selectedQuarterCompanyFilingAmount():QuarterCompanyFilingAmounts {
            return mSelectedQuarterCompanyFilingAmount;
        }

        public function set selectedQuarterCompanyFilingAmount(value:QuarterCompanyFilingAmounts):void {
            mSelectedQuarterCompanyFilingAmount = value;

            //reset values for other quarters (s.t. hasChanged = false)
            for each (var quarterFilingAmount:QuarterCompanyFilingAmounts in quarterFilingAmounts) {
                for each (var companyFilingAmount:CompanyFilingAmount in quarterFilingAmount.amounts) {
                    if (companyFilingAmount.hasCurrentValue) {
                        companyFilingAmount.newValue = companyFilingAmount.value;
                    } else {
                        companyFilingAmount.value = "";
                    }
                }
            }

            recreateValidators();
        }

        private function recreateValidators():void {
            validators.length = 0;
            for each (var companyFilingAmount:CompanyFilingAmount in selectedQuarterCompanyFilingAmount.amounts) {
                validators.push(SAPValidators.createNumberValidator(
                        companyFilingAmount,
                        "newValue",
                        companyFilingAmount.hasCurrentValue,
                        null,
                        null,
                        false,
                        null));
            }

            updateCanSave();
        }


        override protected function executeSave():void {
            SAP.instance.taxService.updateAdditionalFilingAmounts(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, selectedQuarterCompanyFilingAmount, createSaveResponder());
        }
    }
}

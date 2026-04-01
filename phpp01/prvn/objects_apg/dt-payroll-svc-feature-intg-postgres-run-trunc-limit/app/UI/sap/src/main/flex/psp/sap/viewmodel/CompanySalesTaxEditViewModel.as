/**
 * User: dweinberg
 * Date: 3/14/12
 * Time: 1:00 PM
 */
package psp.sap.viewmodel {
    import mx.core.UIComponent;
    import mx.formatters.DateFormatter;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.TaxExemptInfo;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class CompanySalesTaxEditViewModel extends AbstractPartViewModel {

        public static const TAX_EXEMPT:String = "Exempt";
        public static const TAX_NON_EXEMPT:String = "NonExempt";

        private var exemptionInfo:TaxExemptInfo;

        private var mExemptionStatus:String;
        [Bindable] [BackingProperty] public var expirationDateText:String;

        [Bindable] public var isCurrentlyExempt:Boolean;
        [Bindable] public var currentExpirationDate:Date;
        [Bindable] public var displayStatus:String;

        [Bindable] public var expirationDateValidator:SAPDateValidator;

        public function CompanySalesTaxEditViewModel() {
            this.label = CompanyInspectorPageEnum.COMPANY_SALES_TAX;

            var fromValidationDate:Date = SAP.instance.PSPDate;
            fromValidationDate.setDate(fromValidationDate.getDate()+1);
            expirationDateValidator = SAPValidators.createDateValidator(this, "expirationDateText", true, 0, -1, fromValidationDate, "Exemption must expire in the future.", null);
            validators.push(expirationDateValidator);
        }

        override protected function loadModelData():void {
            SAP.instance.companyService.getTaxExemptStatus(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onTaxExemptStatusLoaded))
        }

        private function onTaxExemptStatusLoaded(e:ResultEvent):void {
            exemptionInfo = TaxExemptInfo(e.result);
        }

        override protected function initializeBackingProperties():void {
            isCurrentlyExempt = exemptionInfo.isCurrentlyExempt;
            if (isCurrentlyExempt) {
                displayStatus = "Exempt";
            } else if (exemptionInfo.exemptStatus == "Exempt") {
                displayStatus = "Expired";
            } else {
                displayStatus = "Not Exempt";
            }
            currentExpirationDate = exemptionInfo.expirationDate;

            var tmpExemptStatus:String = exemptionInfo.exemptStatus;
            if (tmpExemptStatus != TAX_EXEMPT) {
                tmpExemptStatus = TAX_NON_EXEMPT; //Want to treat "New" as "NonExempt"
            }
            exemptionStatus = tmpExemptStatus;
            expirationDateText = new DateFormatter().format(exemptionInfo.expirationDate);
        }

        [Bindable]
        [BackingProperty]
        public function get exemptionStatus():String {
            return mExemptionStatus;
        }

        public function set exemptionStatus(value:String):void {
            mExemptionStatus = value;
            expirationDateValidator.enabled = (value == TAX_EXEMPT);
            if (!expirationDateValidator.enabled && expirationDateValidator.listener != null) {
                UIComponent(expirationDateValidator.listener).errorString = "";
            }
        }

        public function get expirationDateValue():Date {
            return expirationDateValidator.isDateValid() ? new Date(expirationDateText) : null;
        }


        override protected function executeSave():void {
            var newInfo:TaxExemptInfo = new TaxExemptInfo();
            newInfo.exemptStatus = exemptionStatus;
            newInfo.expirationDate = (exemptionStatus != TAX_EXEMPT ? null : expirationDateValue);
            SAP.instance.companyService.updateTaxExemptStatus(companyKey.sourceSystemCd, companyKey.companyId, newInfo, createSaveResponder());
        }
    }
}

/**
 * User: dweinberg
 * Date: 2/7/13
 * Time: 1:31 PM
 */
package psp.sap.viewmodel {
    import mx.rpc.events.ResultEvent;
    import mx.validators.StringValidator;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyLegalInfo;
    import psp.sap.validators.SAPValidators;

    public class EFTPSManualEnrollmentViewModel extends AbstractPartViewModel {

        private var currentLegalInfo:CompanyLegalInfo;

        [Bindable] [BackingProperty] public var ein:String;
        [Bindable] [BackingProperty] public var legalName:String;
        [Bindable] [BackingProperty] public var legalZip:String;

        public function EFTPSManualEnrollmentViewModel() {
            validators.push(SAPValidators.createRequiredFieldValidator(this,  "legalName"));

            var zipValidator:StringValidator = SAPValidators.createStringValidator(this, "legalZip", true, 5, 5);
            zipValidator.tooShortError = "EFTPS enrollments must have a 5-digit zip code.";
            zipValidator.tooLongError = "EFTPS enrollments must have a 5-digit zip code.";
            validators.push(zipValidator);

            validators.push(SAPValidators.createEinValidator(this, "ein", true));
        }


        override protected function loadModelData():void {
            SAP.instance.companyService.getCompanyLegalInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onLegalInfoLoaded));
        }

        private function onLegalInfoLoaded(e:ResultEvent):void {
            currentLegalInfo = CompanyLegalInfo(e.result);
        }

        override protected function initializeBackingProperties():void {
            ein = currentLegalInfo.ein;
            legalName = currentLegalInfo.legalName;
            legalZip = currentLegalInfo.address.zipCode;
        }


        override protected function executeSave():void {
            SAP.instance.taxService.createManualEFTPSEnrollment(companyKey.sourceSystemCd, companyKey.companyId, ein, legalName, legalZip, createSaveResponder(onSaveSucceeded));
        }

        private function onSaveSucceeded(e:ResultEvent):void {
            cancel();
        }
    }
}

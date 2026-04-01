package psp.sap.viewmodel {
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.model.AgencyInfoDTO;
    import psp.sap.model.PaymentTemplate;

    public class AgencyInfoTabViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var agencyInfo:AgencyInfoDTO;

        [Bindable] public var agencyInfoViewModel:AgencyInfoViewModel; 

        public function AgencyInfoTabViewModel() {
            super();
        }

        public function viewEFTPSHistory():void {
           agencyInfoViewModel.viewEFTPSHistory();
        }

        public function viewRAFHistory():void {
            agencyInfoViewModel.viewRAFHistory();
        }

        public function viewLawRatesHistory(pTemplate:PaymentTemplate):void {
            agencyInfoViewModel.viewLawRatesHistory(pTemplate);
        }

        public function editLawRates(pTemplate:PaymentTemplate):void {
            agencyInfoViewModel.editRates(pTemplate);
        }

        public function viewDepositFrequencyHistory(template:PaymentTemplate):void {
            agencyInfoViewModel.viewDepositFrequencyHistory(template);
        }

        public function viewFilerTypeHistory(templateName:String):void {
            agencyInfoViewModel.viewFilerTypeHistory(templateName);
        }

        public function viewEntityChangeHistory():void {
            agencyInfoViewModel.viewEntityChangeHistory();
        }

        public function viewAgencyIdHistory(template:PaymentTemplate):void {
            agencyInfoViewModel.viewAgencyIdHistory(template);
        }

        public function viewErFicaDeferralHistory():void {
            agencyInfoViewModel.viewErFicaDeferralHistory();
        }

        public function showPaymentMethodsHistory(paymentTemplateCode:String):void {
            agencyInfoViewModel.showPaymentMethodsHistory(paymentTemplateCode);
        }
        public function showAdditionalIdHistory(paymentTemplateCode:String):void {
            agencyInfoViewModel.showAdditionalIdHistory(paymentTemplateCode);
        }

        public function viewACHRegistrationHistory(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.viewACHRegistrationHistory(paymentTemplate);
        }

        public function viewACHEnrollmentHistory():void {
            agencyInfoViewModel.viewACHEnrollmentHistory();
        }

        public function editAgencyIds(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.editAgencyIds(paymentTemplate);
        }

        public function editDepositFrequencies(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.editDepositFrequencies(paymentTemplate);
        }

        public function editAdditionalFilingAmounts(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.editAdditionalFilingAmounts(paymentTemplate);
        }

        public function editLawFlags(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.editLawFlags(paymentTemplate);
        }

        public function editFilerType():void {
            agencyInfoViewModel.editFilerType();
        }

        public function viewLawFlagHistory(paymentTemplate:PaymentTemplate):void {
            agencyInfoViewModel.viewLawFlagHistory(paymentTemplate);
        }

        public function updateAgentEnabled(paymentTemplate:PaymentTemplate, agentEnabled:Boolean):void {
            agencyInfoViewModel.updateAgentEnabled(paymentTemplate, agentEnabled);
        }

        public function updateErFicaDeferral(newErFicaDeferral:Boolean):void {
            agencyInfoViewModel.updateErFicaDeferral(newErFicaDeferral);
        }
    }
}
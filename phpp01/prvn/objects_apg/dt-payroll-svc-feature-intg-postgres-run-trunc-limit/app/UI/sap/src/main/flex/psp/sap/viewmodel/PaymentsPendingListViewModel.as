/**
 * User: dweinberg
 * Date: 11/21/11
 * Time: 9:09 AM
 */
package psp.sap.viewmodel {
    import mx.utils.ObjectUtil;

    import psp.sap.application.SAP;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentTemplate;

    public class PaymentsPendingListViewModel extends AbstractPaymentsListViewModel{

        [Bindable] public var holdsList:Array = defaultHoldsList;
        [Bindable] public var canFinalize:Boolean = false;
        [Bindable] public var canUpdateInitiationDates:Boolean = false;
        [Bindable] public var canUpdatePaymentMethods:Boolean = false;
        [Bindable] public var selectedTemplateCanBeFinalized:Boolean = false;
        public var clearMemo:Boolean = false;

        private const defaultHoldsList:Array = ["","Not on hold","On hold","On Enrollment hold","On Agent hold","On Company hold", "On Back Date hold"];
        //noinspection JSFieldCanBeLocalInspection
        private const finalizedHoldsList:Array = ["","Finalized", "Not Finalized", "NF - not on hold","On hold","On Enrollment hold","On Agent hold","On Company hold", "On Back Date hold"];

        private var action:String;
        private var paymentId:String;
        private var companyId:String;

        public function PaymentsPendingListViewModel() {
            super();
            searchTypeString = "Pending";
            statusFieldDescription = "Holds";
        }

        override public function set paymentTemplate(value:PaymentTemplate):void {
            super.paymentTemplate = value;
            selectedTemplateCanBeFinalized = value != null && (value.canBeFinalized || isToBeFinalizedNonSUIPaymentTemplate(value));
            holdsList = (value != null && (value.canBeFinalized || isToBeFinalizedNonSUIPaymentTemplate(value)) ) ? finalizedHoldsList : defaultHoldsList;
        }

        override protected function loadModelData():void {
            canFinalize = status == "NF - not on hold" && !quarter.isEmpty() && canEnableMultFinalizeForNonSUI();
            canUpdateInitiationDates = paymentMethod != null && paymentMethod != "" && !quarter.isEmpty() ;
            canUpdatePaymentMethods = agency != null && agency != "IRS" && !quarter.isEmpty() && canEnableMultUpdatePmtMethodForNonSUI();
            super.loadModelData();
        }


        override public function searchPayments():void {
            super.searchPayments();
            clearMemo = true;
        }

        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            canFinalize &&= payments.totalRecords > 0;
            canUpdateInitiationDates &&= payments.totalRecords > 0;
            canUpdatePaymentMethods &&= payments.totalRecords > 0;
        }

         public function canAddAgentHold(payment:Payment):Boolean {
             if (payment.status == "Finalized") {
                 return false;
             }
             for each (var hold:String in payment.holds) {
                 if (hold == "Agent") {
                     return false;
                 }
             }
             return true;
        }

        public function isLatePayment(settlementDate:Date, dueDate:Date):Boolean {
            return (ObjectUtil.dateCompare(dueDate, SAP.instance.PSPDate) != 1 || ObjectUtil.dateCompare(dueDate, settlementDate) == -1);
        }

        public function canRemoveHold(holdName:String):Boolean {
            return holdName == 'Agent' || holdName == 'BackDate';

        }

        public function canEditPaymentMethod(payment:Payment):Boolean {
            return (payment.paymentMethod != 'EFTPS' && payment.paymentMethod != 'EFTPSDirectDebit') && !(isFinalizedNonSUIPayment(payment));
        }

        public function canRefundPayment(payment:Payment):Boolean {
            return (payment.paymentMethod != 'ACHDirectDeposit' && payment.paymentMethod != 'EFTPSDirectDebit' && payment.status != "Finalized" && payment.amount > 0);
        }

        public function canEditPaymentAmount(payment:Payment):Boolean {
            return payment.status == "Finalized" && !isToBeFinalizedNonSUIPaymentTemplate(paymentTemplate);
        }

        public function canFinalizePayment(payment:Payment):Boolean {
            return (payment.status == "Pending" || payment.status == "Pending Re-initiation") && (payment.holds.length == 0 || (payment.holds.length > 0 && payment.paymentMethod == "ACHDebit")) && (paymentTemplate.canBeFinalized || (isToBeFinalizedNonSUIPayment(payment)));
        }

        public function canUnFinalizePayment(payment:Payment):Boolean {
            return payment.status == "Finalized";
        }

        public function canEditSettlementDate(payment:Payment):Boolean {
            return payment.status != "Finalized";
        }


        public function removeHold(data:Payment, holdName:String):void {
            paymentId = data.paymentId;
            action = holdName;
            companyId = data.companyId;
            forceSave();
        }

        public function addAgentHold(data:Payment):void {
            paymentId = data.paymentId;
            action = "AddAgent";
            companyId = data.companyId;
            forceSave();
        }

        public function finalizePayment(payment:Payment):void {
            paymentId = payment.paymentId;
            action = "finalize";
            companyId = payment.companyId;
            forceSave();
        }

        public function unFinalizePayment(payment:Payment):void {
            paymentId = payment.paymentId;
            action = "unFinalize";
            companyId = payment.companyId;
            forceSave();
        }

        override protected function executeSave():void {
            if (action == "AddAgent") {
                SAP.instance.taxService.addTaxPaymentAgentOnHoldReason(paymentId, companyId, createSaveResponder());
            } else if (action == "finalize") {
                SAP.instance.taxService.finalizePayment(paymentId, companyId, createSaveResponder());
            } else if (action == "unFinalize") {
                SAP.instance.taxService.unFinalizePayment(paymentId, companyId,  createSaveResponder());
            } else {
                SAP.instance.taxService.removePaymentOnHoldReason(paymentId, action, companyId, createSaveResponder());
            }
        }

        /**
         * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
         * Return true if the payment template needs to be finalized
         * @param value
         * @return
         */
        private function isToBeFinalizedNonSUIPaymentTemplate(value:PaymentTemplate):Boolean{
            return value.paymentTemplateCd=="NY-MTA305-PAYMENT";
        }

        /**
         * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
         * Return true if the payment needs to be finalized
         * @param payment
         * @return
         */
        private function isToBeFinalizedNonSUIPayment(payment:Payment):Boolean{
            return paymentTemplate.paymentTemplateCd=="NY-MTA305-PAYMENT" && payment.paymentMethod=="ACHDebit";
        }

        /**
         * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
         * Return true if the payment is finalized
         * @param payment
         * @return
         */
        private function isFinalizedNonSUIPayment(payment:Payment):Boolean{
            return paymentTemplate.paymentTemplateCd=="NY-MTA305-PAYMENT" && payment.paymentMethod=="ACHDebit" && payment.status == "Finalized" ;
        }

        /**
         * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
         * Return true if the payment template with selected payment method
         * should be allowed to be set to finalized status
         * (Here we should not see finalize option for payment Methods
         * other than ACHDebit for NY-MTA305)
         * @return
         */
        private function canEnableMultFinalizeForNonSUI():Boolean{
          return  !(paymentTemplate.paymentTemplateCd=="NY-MTA305-PAYMENT" && paymentMethod!="ACHDebit");
        }

        /**
         * PSP-14128: Add NY MTA305 PAYMENT to the list of payments that can be Finalized
         * Return true if the payment template with selected payment method and selected status
         * should be allowed to update payment method
         * (Here we should not see enable update payment method option for
         * ACHDebit payment method or if no payment method is selected
         * and if status is finalized or blank for NY-MTA305
         * )
         * @return
         */
        private function canEnableMultUpdatePmtMethodForNonSUI():Boolean{
          return !(paymentTemplate.paymentTemplateCd=="NY-MTA305-PAYMENT" && (paymentMethod=="ACHDebit" || paymentMethod=="" || paymentMethod==null) && (status=="Finalized" || status==null || status==""));
        }
    }
}

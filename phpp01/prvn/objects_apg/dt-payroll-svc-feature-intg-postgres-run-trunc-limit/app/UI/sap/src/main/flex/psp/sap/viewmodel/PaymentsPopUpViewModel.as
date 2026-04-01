/**
 * User: dweinberg
 * Date: 11/16/11
 * Time: 10:15 AM
 */
package psp.sap.viewmodel {
    import flash.events.Event;

    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.SearchResults;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class PaymentsPopUpViewModel extends CompositePartViewModel {

        private var mHoldsHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsHoldsHistoryViewModel:PaymentsHoldsHistoryViewModel;
        private var mPayDateEditPopUp:PopUpPartViewModel;
        private var mPaymentsPayDateViewModel:PaymentsPayDateEditViewModel;
        private var mStatusHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsStatusHistoryViewModel:PaymentsStatusHistoryViewModel;
        private var mPayDateHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsPayDateHistoryViewModel:PaymentsPayDateHistoryViewModel;
        private var mAmountDetailsPopUp:PopUpPartViewModel;
        private var mPaymentsAmountDetailsViewModel:PaymentsAmountDetailsViewModel;
        private var mPaymentMethodHistoryPopUp:PopUpPartViewModel;
        private var mPaymentMethodHistoryViewModel:PaymentMethodHistoryViewModel;
        private var mPaymentMethodEditPopUp:PopUpPartViewModel;
        private var mPaymentMethodViewModel:PaymentMethodEditViewModel;
        private var mRejectionPopUp:PopUpPartViewModel;
        private var mRejectionViewModel:PaymentRejectionViewModel;
        private var mPaymentRepaymentPopUp:PopUpPartViewModel;
        private var mPaymentRepaymentViewModel:PaymentRepaymentInitiateViewModel;
        private var mEditPaymentAmountPopUp:PopUpPartViewModel;
        private var mEditPaymentAmountViewModel:EditPaymentAmountViewModel;
        private var mFinalizePaymentsPopUp:PopUpPartViewModel;
        private var mFinalizePaymentsViewModel:FinalizePaymentsViewModel;
        private var mUpdateInitiationDatesPopUp:PopUpPartViewModel;
        private var mUpdateInitiationDatesViewModel:UpdateInitiationDatesViewModel;
        private var mUpdatePaymentMethodsPopUp:PopUpPartViewModel;
        private var mUpdatePaymentMethodsViewModel:UpdatePaymentMethodsViewModel;
        private var mRefundPaymentPopUp:PopUpPartViewModel;
        private var mRefundPaymentViewModel:RefundPaymentViewModel;


        public function PaymentsPopUpViewModel() {
            mHoldsHistoryPopUp = addPopUpPart(PaymentsPageEnum.HOLDS_POPUP);
            mPaymentsHoldsHistoryViewModel = mHoldsHistoryPopUp.addNewPart(PaymentsHoldsHistoryViewModel, PaymentsPageEnum.HOLDS_POPUP) as PaymentsHoldsHistoryViewModel;

            mPayDateEditPopUp = addPopUpPart(PaymentsPageEnum.EDIT_PAYDATE_POPUP);
            mPayDateEditPopUp.closeOnSave = true;
            mPaymentsPayDateViewModel = mPayDateEditPopUp.addNewPart(PaymentsPayDateEditViewModel, PaymentsPageEnum.EDIT_PAYDATE_POPUP) as PaymentsPayDateEditViewModel;
            mPaymentsPayDateViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mPaymentMethodEditPopUp = addPopUpPart(PaymentsPageEnum.EDIT_PAYMENT_METHOD_POPUP);
            mPaymentMethodEditPopUp.closeOnSave = true;
            mPaymentMethodViewModel = mPaymentMethodEditPopUp.addNewPart(PaymentMethodEditViewModel, PaymentsPageEnum.EDIT_PAYMENT_METHOD_POPUP) as PaymentMethodEditViewModel;
            mPaymentMethodViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mStatusHistoryPopUp = addPopUpPart(PaymentsPageEnum.STATUS_POPUP);
            mPaymentsStatusHistoryViewModel = mStatusHistoryPopUp.addNewPart(PaymentsStatusHistoryViewModel, PaymentsPageEnum.STATUS_POPUP) as PaymentsStatusHistoryViewModel;

            mPayDateHistoryPopUp = addPopUpPart(PaymentsPageEnum.PAYDATE_POPUP);
            mPaymentsPayDateHistoryViewModel = mPayDateHistoryPopUp.addNewPart(PaymentsPayDateHistoryViewModel, PaymentsPageEnum.PAYDATE_POPUP) as PaymentsPayDateHistoryViewModel;

            mAmountDetailsPopUp = addPopUpPart(PaymentsPageEnum.AMOUNT_POPUP);
            mPaymentsAmountDetailsViewModel = mAmountDetailsPopUp.addNewPart(PaymentsAmountDetailsViewModel, PaymentsPageEnum.AMOUNT_POPUP) as PaymentsAmountDetailsViewModel;

            mPaymentMethodHistoryPopUp = addPopUpPart(PaymentsPageEnum.PAYMENT_METHOD_POPUP);
            mPaymentMethodHistoryViewModel = mPaymentMethodHistoryPopUp.addNewPart(PaymentMethodHistoryViewModel, PaymentsPageEnum.PAYMENT_METHOD_POPUP) as PaymentMethodHistoryViewModel;

            mRejectionPopUp = addPopUpPart(PaymentsPageEnum.REJECTION_POPUP);
            mRejectionPopUp.closeOnSave = true;
            mRejectionViewModel = mRejectionPopUp.addNewPart(PaymentRejectionViewModel, PaymentsPageEnum.REJECTION_POPUP) as PaymentRejectionViewModel;
            mRejectionViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mPaymentRepaymentPopUp = addPopUpPart(PaymentsPageEnum.REPAYMENT_POPUP);
            mPaymentRepaymentPopUp.closeOnSave = true;
            mPaymentRepaymentViewModel = mPaymentRepaymentPopUp.addNewPart(PaymentRepaymentInitiateViewModel, PaymentsPageEnum.REPAYMENT_POPUP) as PaymentRepaymentInitiateViewModel;
            mPaymentRepaymentViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mEditPaymentAmountPopUp = addPopUpPart(PaymentsPageEnum.EDIT_PAYMENT_AMOUNT);
            mEditPaymentAmountPopUp.closeOnSave = true;
            mEditPaymentAmountViewModel = mEditPaymentAmountPopUp.addNewPart(EditPaymentAmountViewModel, PaymentsPageEnum.EDIT_PAYMENT_AMOUNT) as EditPaymentAmountViewModel;
            mEditPaymentAmountViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mFinalizePaymentsPopUp = addPopUpPart(PaymentsPageEnum.FINALIZE_PAYMENTS);
            mFinalizePaymentsPopUp.closeOnSave = true;
            mFinalizePaymentsViewModel = mFinalizePaymentsPopUp.addNewPart(FinalizePaymentsViewModel, PaymentsPageEnum.FINALIZE_PAYMENTS) as FinalizePaymentsViewModel;
            mFinalizePaymentsViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mUpdateInitiationDatesPopUp = addPopUpPart(PaymentsPageEnum.UPDATE_INITIATION_DATES);
            mUpdateInitiationDatesPopUp.closeOnSave = true;
            mUpdateInitiationDatesViewModel = mUpdateInitiationDatesPopUp.addNewPart(UpdateInitiationDatesViewModel, PaymentsPageEnum.UPDATE_INITIATION_DATES) as UpdateInitiationDatesViewModel;
            mUpdateInitiationDatesViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mUpdatePaymentMethodsPopUp = addPopUpPart(PaymentsPageEnum.UPDATE_PAYMENT_METHODS);
            mUpdatePaymentMethodsPopUp.closeOnSave = true;
            mUpdatePaymentMethodsViewModel = mUpdatePaymentMethodsPopUp.addNewPart(UpdatePaymentMethodsViewModel, PaymentsPageEnum.UPDATE_PAYMENT_METHODS) as UpdatePaymentMethodsViewModel;
            mUpdatePaymentMethodsViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mRefundPaymentPopUp = addPopUpPart(PaymentsPageEnum.REFUND_PAYMENT_POPUP);
            mRefundPaymentPopUp.closeOnSave = true;
            mRefundPaymentViewModel = mRefundPaymentPopUp.addNewPart(RefundPaymentViewModel, PaymentsPageEnum.REFUND_PAYMENT_POPUP) as RefundPaymentViewModel;
            mRefundPaymentViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);


        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this.host);
        }

        public function viewHoldsHistory(data:Payment):void {
            mPaymentsHoldsHistoryViewModel.payment = data;
            mHoldsHistoryPopUp.displayPopUp();
        }

        public function editPayDate(data:Payment):void {
            mPaymentsPayDateViewModel.payment = data;
            mPayDateEditPopUp.displayPopUp();
        }

        public function editPaymentMethod(data:Payment):void {
            mPaymentMethodViewModel.payment = data;
            mPaymentMethodEditPopUp.displayPopUp();
        }

        public function refundPayment(data:Payment):void {
            mRefundPaymentViewModel.payment = data;
            mRefundPaymentPopUp.displayPopUp();
        }

        public function viewStatusHistory(data:Payment):void {
            mPaymentsStatusHistoryViewModel.payment = data;
            mStatusHistoryPopUp.displayPopUp();
        }

        public function viewPaymentMethodHistory(data:Payment):void {
            mPaymentMethodHistoryViewModel.payment = data;
            mPaymentMethodHistoryPopUp.displayPopUp();
        }

        public function viewPayDateHistory(data:Payment):void {
            mPaymentsPayDateHistoryViewModel.payment = data;
            mPayDateHistoryPopUp.displayPopUp();
        }

        public function viewPaymentDetails(data:Payment):void {
            mPaymentsAmountDetailsViewModel.payment = data;
            mAmountDetailsPopUp.displayPopUp();
        }

        public function rejectPayment(data:Payment):void {
            mRejectionViewModel.payment = data;
            mRejectionPopUp.displayPopUp();
        }

        public function triggerRepaymentWithNewPaymentMethod(data:Payment):void {
            mPaymentRepaymentViewModel.payment = data;
            mPaymentRepaymentPopUp.displayPopUp();
        }

        public function editPaymentAmount(data:Payment, clearMemo:Boolean):void {
            mEditPaymentAmountViewModel.setActivator(EditPaymentAmountViewModel.createActivator(data, clearMemo));
            mEditPaymentAmountPopUp.displayPopUp();
        }

        public function finalizePayments(searchCriteria:PaymentSearch, searchResults:SearchResults):void {
            mFinalizePaymentsViewModel.searchCriteria = searchCriteria;
            mFinalizePaymentsViewModel.searchResults = searchResults;
            mFinalizePaymentsPopUp.displayPopUp();

        }

        public function updateInitiationDates(searchCriteria:PaymentSearch, searchResults:SearchResults):void {
            mUpdateInitiationDatesViewModel.searchCriteria = searchCriteria;
            mUpdateInitiationDatesViewModel.searchResults = searchResults;
            mUpdateInitiationDatesPopUp.displayPopUp();
        }

        public function updatePaymentMethods(searchCriteria:PaymentSearch, searchResults:SearchResults):void {
            mUpdatePaymentMethodsViewModel.searchCriteria = searchCriteria;
            mUpdatePaymentMethodsViewModel.searchResults = searchResults;
            mUpdatePaymentMethodsPopUp.displayPopUp();
        }

    }
}

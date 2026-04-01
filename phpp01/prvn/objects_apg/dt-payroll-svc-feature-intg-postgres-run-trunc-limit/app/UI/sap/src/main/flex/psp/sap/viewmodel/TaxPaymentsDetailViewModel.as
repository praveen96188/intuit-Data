package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentTemplateQuarterPayment;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class TaxPaymentsDetailViewModel extends CompositePartViewModel {
        private var mQuarterSort:Sort;
        private var mPaymentsSort:Sort;

        private var mStatusHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsStatusHistoryViewModel:PaymentsStatusHistoryViewModel;
        private var mPayDateHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsPayDateHistoryViewModel:PaymentsPayDateHistoryViewModel;
        private var mHoldsHistoryPopUp:PopUpPartViewModel;
        private var mPaymentsHoldsHistoryViewModel:PaymentsHoldsHistoryViewModel;
        private var mPayDateEditPopUp:PopUpPartViewModel;
        private var mPaymentsPayDateViewModel:PaymentsPayDateEditViewModel;
        private var mPaymentMethodHistoryPopUp:PopUpPartViewModel;
        private var mPaymentMethodHistoryViewModel:PaymentMethodHistoryViewModel;
        private var mPaymentMethodEditPopUp:PopUpPartViewModel;
        private var mPaymentMethodViewModel:PaymentMethodEditViewModel;
        private var mRejectionPopUp:PopUpPartViewModel;
        private var mRejectionViewModel:PaymentRejectionViewModel;

        private var holdAction:String;
        private var paymentId:String;
        private var companyId:String;

        [Bindable]
        public var dirty:Boolean = false;

        public function TaxPaymentsDetailViewModel() {
            super();
            bindSaveMessageWithChildren = true;
            reloadOnActivate = false;

            mStatusHistoryPopUp = addPopUpPart(PaymentsPageEnum.STATUS_POPUP);
            mPaymentsStatusHistoryViewModel = mStatusHistoryPopUp.addNewPart(PaymentsStatusHistoryViewModel, PaymentsPageEnum.STATUS_POPUP) as PaymentsStatusHistoryViewModel;

            mPayDateHistoryPopUp = addPopUpPart(PaymentsPageEnum.PAYDATE_POPUP);
            mPaymentsPayDateHistoryViewModel = mPayDateHistoryPopUp.addNewPart(PaymentsPayDateHistoryViewModel, PaymentsPageEnum.PAYDATE_POPUP) as PaymentsPayDateHistoryViewModel;

            mHoldsHistoryPopUp = addPopUpPart(PaymentsPageEnum.HOLDS_POPUP);
            mPaymentsHoldsHistoryViewModel = mHoldsHistoryPopUp.addNewPart(PaymentsHoldsHistoryViewModel, PaymentsPageEnum.HOLDS_POPUP) as PaymentsHoldsHistoryViewModel;

            mPayDateEditPopUp = addPopUpPart(PaymentsPageEnum.EDIT_PAYDATE_POPUP);
            mPayDateEditPopUp.closeOnSave = true;
            mPaymentsPayDateViewModel = mPayDateEditPopUp.addNewPart(PaymentsPayDateEditViewModel, PaymentsPageEnum.EDIT_PAYDATE_POPUP) as PaymentsPayDateEditViewModel;
            mPaymentsPayDateViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mPaymentMethodHistoryPopUp = addPopUpPart(PaymentsPageEnum.PAYMENT_METHOD_POPUP);
            mPaymentMethodHistoryViewModel = mPaymentMethodHistoryPopUp.addNewPart(PaymentMethodHistoryViewModel, PaymentsPageEnum.PAYMENT_METHOD_POPUP) as PaymentMethodHistoryViewModel;

            mPaymentMethodEditPopUp = addPopUpPart(PaymentsPageEnum.EDIT_PAYMENT_METHOD_POPUP);
            mPaymentMethodEditPopUp.closeOnSave = true;
            mPaymentMethodViewModel = mPaymentMethodEditPopUp.addNewPart(PaymentMethodEditViewModel, PaymentsPageEnum.EDIT_PAYMENT_METHOD_POPUP) as PaymentMethodEditViewModel;
            mPaymentMethodViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mRejectionPopUp = addPopUpPart(PaymentsPageEnum.REJECTION_POPUP);
            mRejectionPopUp.closeOnSave = true;
            mRejectionViewModel = mRejectionPopUp.addNewPart(PaymentRejectionViewModel, PaymentsPageEnum.REJECTION_POPUP) as PaymentRejectionViewModel;
            mRejectionViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);

            mQuarterSort = new Sort();

            var yearSortField:SortField = new SortField("year", false, true);
            yearSortField.compareFunction = compareStringYears;

            var quarterSortField:SortField = new SortField("quarter", false, true);

            var templateSortField:SortField = new SortField("paymentTemplateName", true);
            templateSortField.compareFunction = CompareUtils.comparePaymentTemplate;

            mQuarterSort.fields = [yearSortField, quarterSortField, templateSortField];

            mPaymentsSort = new Sort();
            mPaymentsSort.fields = [new SortField("settlementDate", false, true)];

        }

        private function compareStringYears(a:Object, b:Object):int {
            var numA:Number = a != null ? parseInt(String(a.year)) : NaN;
            var numB:Number = a != null ? parseInt(String(b.year)) : NaN;
            return ObjectUtil.numericCompare(numA, numB);
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }

        [Bindable]
        public var paymentQuarters:ArrayCollection;

        private var mTempSelectedQuarter:PaymentTemplateQuarterPayment;
        private var mSelectedQuarter:PaymentTemplateQuarterPayment;

        [Bindable]
        public function set selectedQuarter(value:PaymentTemplateQuarterPayment):void {
            if(value != null){
                mTempSelectedQuarter = value;   
            }
            if (paymentQuarters == null) {
                return;
            }
            if (paymentQuarters.contains(value)) {
                if (value != mSelectedQuarter) {
                    selectedPayment = null;
                }
                mSelectedQuarter = value;
            }
            else if (value != null) {
                for each(var quarter:PaymentTemplateQuarterPayment in paymentQuarters) {
                    if (quarter.quarter == value.quarter && quarter.year == value.year && quarter.paymentTemplateCd == value.paymentTemplateCd) {
                        if (quarter != mSelectedQuarter) {
                            selectedPayment = null;
                        }
                        mSelectedQuarter = quarter;
                        break;
                    }
                }
            }
            else {
                mSelectedQuarter = null;
            }            
            if (mSelectedQuarter != null) {
                saveMsg = "";
                SAP.instance.taxService.getPaymentTemplateQuarterPayment(company.sourceSystemCd,
                        company.companyId,
                        mSelectedQuarter.paymentTemplateCd,
                        mSelectedQuarter.year,
                        mSelectedQuarter.quarter,
                        new Responder(onQuarterPaymentLoaded, onSaveFaulted_internal));
            }

        }

        public function get selectedQuarter():PaymentTemplateQuarterPayment {
            return mSelectedQuarter;
        }

        private var mSelectedPayment:Payment;

        [Bindable]
        public function set selectedPayment(value:Payment):void {
            mSelectedPayment = value;
            if (mSelectedPayment != null) {
                saveMsg = "";
                paymentDetails = null;
                SAP.instance.taxService.findPaymentDetailTransactions(mSelectedPayment.paymentId, mSelectedPayment.companyId,
                        new Responder(onSelectedPaymentLoaded, onSaveFaulted_internal));
            }
        }

        public function get selectedPayment():Payment {
            return mSelectedPayment;
        }

        [ArrayElementType("psp.sap.model.TaxPaymentCheckDateSet")]
        [Bindable]
        public var paymentDetails:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getPaymentTemplateQuarters(company.sourceSystemCd,
                    company.companyId,
                    SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_POSSIBLE_BACKDATE_YEARS),
                    createLoadModelDataResponder(onQuartersLoaded));
        }

        private function onQuartersLoaded(e:ResultEvent):void {
            paymentQuarters = e.result as ArrayCollection;
            paymentQuarters.sort = mQuarterSort;
            paymentQuarters.refresh();

            if (mTempSelectedQuarter != null) {
                selectedQuarter = mTempSelectedQuarter;
            } else if (selectedQuarter == null && paymentQuarters != null && paymentQuarters.length > 0) {
                selectedQuarter = paymentQuarters.getItemAt(0) as PaymentTemplateQuarterPayment;
            }
        }


        private function onQuarterPaymentLoaded(e:ResultEvent):void {
            var result:PaymentTemplateQuarterPayment = e.result as PaymentTemplateQuarterPayment;

            // sort
            result.paymentsMade.sort = mPaymentsSort;
            result.paymentsMade.refresh();
            result.pendingPayments.sort = mPaymentsSort;
            result.pendingPayments.refresh();

            // add to selected quarter
            for each(var quarter:PaymentTemplateQuarterPayment in paymentQuarters) {
                if (quarter.quarter == result.quarter && quarter.year == result.year && quarter.paymentTemplateCd == result.paymentTemplateCd) {
                    if (quarter != mSelectedQuarter) {
                        selectedPayment = null;
                    }
                    mSelectedQuarter = quarter;
                    break;
                }
            }
            mSelectedQuarter.paymentsMade = result.paymentsMade;
            mSelectedQuarter.paymentsMadeTotal = result.paymentsMadeTotal;
            mSelectedQuarter.pendingPayments = result.pendingPayments;
            mSelectedQuarter.pendingPaymentsTotal = result.pendingPaymentsTotal;
            mSelectedQuarter.quarterPaymentsTotal = result.quarterPaymentsTotal;
        }

        private function onSelectedPaymentLoaded(e:ResultEvent):void {
            paymentDetails = e.result as ArrayCollection;
        }

        public function viewStatusHistory(data:Payment):void {
            mPaymentsStatusHistoryViewModel.payment = data;
            mStatusHistoryPopUp.displayPopUp();
        }

        public function viewPayDateHistory(data:Payment):void {
            mPaymentsPayDateHistoryViewModel.payment = data;
            mPayDateHistoryPopUp.displayPopUp();
        }

        public function viewHoldsHistory(data:Payment):void {
            mPaymentsHoldsHistoryViewModel.payment = data;
            mHoldsHistoryPopUp.displayPopUp();
        }

        override public function refresh(resetSaveMessage:Boolean = true):void {
            selectedQuarter = null;
            selectedPayment = null;
            super.refresh(resetSaveMessage);
        }

        public function removeHold(data:Payment, holdName:String):void {
            paymentId = data.paymentId;
            holdAction = holdName;
            companyId = data.companyId;
            forceSave();
        }

        public function addAgentHold(data:Payment):void {
            paymentId = data.paymentId;
            holdAction = "AddAgent";
            companyId = data.companyId;
            forceSave();
        }

        override protected function executeSave():void {
            if (holdAction == "AddAgent") {
                SAP.instance.taxService.addTaxPaymentAgentOnHoldReason(paymentId, companyId,createSaveResponder());
            } else {
                SAP.instance.taxService.removePaymentOnHoldReason(paymentId, holdAction, companyId, createSaveResponder());
            }
            refresh();
        }

        public function RejectPayment(data:Payment):void {
            mRejectionViewModel.payment = data;
            mRejectionPopUp.displayPopUp();
        }

        public function editPayDate(data:Payment):void {
            mPaymentsPayDateViewModel.payment = data;
            mPayDateEditPopUp.displayPopUp();
        }

        public function viewPaymentMethodHistory(data:Payment):void {
            mPaymentMethodHistoryViewModel.payment = data;
            mPaymentMethodHistoryPopUp.displayPopUp();
        }

        public function editPaymentMethod(data:Payment):void {
            mPaymentMethodViewModel.payment = data;
            mPaymentMethodEditPopUp.displayPopUp();
        }
    }
}
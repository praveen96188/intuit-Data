package psp.sap.viewmodel {

    import flash.display.DisplayObject;
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;
    import mx.validators.DateValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.PaginationCollection;
    import psp.sap.application.enums.PaymentsPageEnum;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.Agency;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.Payment;
    import psp.sap.model.PaymentSearch;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.Quarter;
    import psp.sap.model.SearchResults;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;
    import mx.controls.Alert;

    public class AbstractPaymentsListViewModel extends CompositePartViewModel {

        protected const EMPTY_STRING:String = "";
        private const DEFAULT_PAGE_SIZE:int = 15;

        protected var searchTypeString:String;
        protected var statusFieldDescription:String;

        [Bindable]
        [ArrayElementType("String")]
        public var agencyList:ArrayCollection = new ArrayCollection();
        [Bindable]
        [ArrayElementType("psp.sap.model.PaymentTemplate")]
        public var paymentTemplateList:ArrayCollection = new ArrayCollection();
        [Bindable]
        [ArrayElementType("psp.sap.model.Quarter")]
        public var quarterList:ArrayCollection;

        public const paymentMethodList:Array = [EMPTY_STRING,"EFTPS","EFTPSDirectDebit", "ACHCredit", "ACHDebit", "CheckPayment", "SuperCheck", "EDI", "None"];

        private var mAgency:String;
        private var mPaymentTemplate:PaymentTemplate = PaymentTemplate.EMPTY_TEMPLATE;

        [Bindable] [BackingProperty] public var settlementStartDate:String = EMPTY_STRING;
        [Bindable] [BackingProperty] public var settlementEndDate:String = EMPTY_STRING;
        [Bindable] [BackingProperty] public var initiationStartDate:String = EMPTY_STRING;
        [Bindable] [BackingProperty] public var initiationEndDate:String = EMPTY_STRING;
        [Bindable] [BackingProperty] public var quarter:Quarter;

        [Bindable] [BackingProperty] public var paymentMethod:String;
        [Bindable] [BackingProperty] public var companyIds:String;
        [Bindable] [BackingProperty] public var status:String;
        [Bindable] [BackingProperty] public var overdueOnly:Boolean;

        [Bindable] public var agencyRequiredValidator:Validator;
        [Bindable] public var paymentTemplateRequiredValidator:Validator;
        [Bindable] public var settlementStartDateValidator:DateValidator;
        [Bindable] public var settlementEndDateValidator:DateValidator;
        [Bindable] public var settlementDateRangeValidator:SAPStartEndDateValidator;
        [Bindable] public var initiationStartDateValidator:DateValidator;
        [Bindable] public var initiationEndDateValidator:DateValidator;
        [Bindable] public var initiationDateRangeValidator:SAPStartEndDateValidator;
        [Bindable] public var canExportResults:Boolean = false;
        
        public var lastSearched:PaymentSearch;

        private var searchButtonClicked:Boolean = false;
        public var popUps:PaymentsPopUpViewModel;

        public function AbstractPaymentsListViewModel() {
            super();

            reloadOnActivate = false;
            reloadOnSave = true;

            popUps = PaymentsPopUpViewModel(addNewPart(PaymentsPopUpViewModel, PaymentsPageEnum.POP_UPS));

            settlementStartDateValidator = new DateValidator();
            settlementStartDateValidator.source = this;
            settlementStartDateValidator.property = "settlementStartDate";
            settlementStartDateValidator.required = false;
            settlementStartDateValidator.trigger = this;
            validators.push(settlementStartDateValidator);

            initiationStartDateValidator = new DateValidator();
            initiationStartDateValidator.source = this;
            initiationStartDateValidator.property = "initiationStartDate";
            initiationStartDateValidator.required = false;
            initiationStartDateValidator.trigger = this;
            validators.push(initiationStartDateValidator);

            settlementEndDateValidator = new DateValidator();
            settlementEndDateValidator.source = this;
            settlementEndDateValidator.property = "settlementEndDate";
            settlementEndDateValidator.required = false;
            settlementEndDateValidator.trigger = this;
            validators.push(settlementEndDateValidator);

            initiationEndDateValidator = new DateValidator();
            initiationEndDateValidator.source = this;
            initiationEndDateValidator.property = "initiationEndDate";
            initiationEndDateValidator.required = false;
            initiationEndDateValidator.trigger = this;
            validators.push(initiationEndDateValidator);

            settlementDateRangeValidator = new SAPStartEndDateValidator();
            settlementDateRangeValidator.source = this;
            settlementDateRangeValidator.trigger = this;
            settlementDateRangeValidator.startDateProperty = "settlementStartDate";
            settlementDateRangeValidator.endDateProperty = "settlementEndDate";
            settlementDateRangeValidator.required = false;
            validators.push(settlementDateRangeValidator);

            initiationDateRangeValidator = new SAPStartEndDateValidator();
            initiationDateRangeValidator.source = this;
            initiationDateRangeValidator.trigger = this;
            initiationDateRangeValidator.startDateProperty = "initiationStartDate";
            initiationDateRangeValidator.endDateProperty = "initiationEndDate";
            initiationDateRangeValidator.required = false;
            validators.push(initiationDateRangeValidator);

            agencyRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "agency");
            validators.push(agencyRequiredValidator);

            paymentTemplateRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "paymentTemplate", true);
            validators.push(paymentTemplateRequiredValidator);
        }

        override protected function preActivation():void {
            if (SAP.instance.lookupService.agencyList == null) {
                SAP.instance.lookupService.addEventListener(SAPEvent.DATA_LOAD_COMPLETED, function(e:Event):void {
                    setAgencyListPreActivation();
                });
            } else {
                setAgencyListPreActivation();
            }
        }

        private function setAgencyListPreActivation():void {
            agencyList.removeAll();
            agencyList.addItem(EMPTY_STRING);
            for each (var taxAgency:Agency in SAP.instance.lookupService.agencyList){
                agencyList.addItem(taxAgency.agencyAbbrev);
            }
            agencyList.refresh();
            preActivationComplete();
        }

        private var mPayments:PaginationCollection = new PaginationCollection(null, DEFAULT_PAGE_SIZE);


        [Bindable]
        public function get payments():PaginationCollection {
            return mPayments;
        }

        public function set payments(value:PaginationCollection):void {
            if (value != null) {
                mPayments = value;
                mPayments.refresh();
            }
        }

        override protected function loadModelData():void {
            if (!searchButtonClicked) {
                loadSearch();
            } else {
                loadPayments();
            }
        }
        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            canExportResults = payments.totalRecords > 0 && lastSearched != null;
        }
        protected function loadSearch():void {
            quarterList = Quarter.validTaxQuarters();
            quarter = quarterList.length >= 3 ? Quarter(quarterList.getItemAt(2)) : Quarter.EMPTY_QUARTER;
            modelDataLoaded();
        }

        protected function loadPayments():void {
            lastSearched = new PaymentSearch();
            lastSearched.searchType = searchTypeString;
            lastSearched.status = status;
            lastSearched.agencyAbbrev = agency;
            lastSearched.paymentTemplate = paymentTemplate == null ? null : paymentTemplate.paymentTemplateCd;
            lastSearched.paymentMethod = paymentMethod;
            lastSearched.companyIds = companyIds;
            lastSearched.settlementStartDate = settlementStartDateValue;
            lastSearched.settlementEndDate = settlementEndDateValue;
            lastSearched.initiationStartDate = initiationStartDateValue;
            lastSearched.initiationEndDate = initiationEndDateValue;
            lastSearched.quarter = selectedQuarter;
            lastSearched.overduePaymentsOnly = overdueOnly;
            SAP.instance.taxService.findTaxPayments(lastSearched, payments.startIndex, payments.pageSize, payments.sortBy, payments.sortDesc, createLoadModelDataResponder(onPaymentResults));
        }

        public function searchPayments():void {
            payments.reset();
            searchButtonClicked = true;
            refresh();
        }

        public function onPaymentResults(e:ResultEvent):void {
            payments.searchResults = SearchResults(e.result);
        }
        public function getFilterString():String {
            var filterString:String = EMPTY_STRING;
            if (StringUtil.trim(agency).length > 0) {
                filterString += "Agency: " + agency + ", ";
            }
            if (paymentTemplate != null && StringUtil.trim(paymentTemplate.paymentTemplateName).length > 0) {
                filterString += "Payment Type: " + paymentTemplate + ", ";
            }

            if (StringUtil.trim(paymentMethod).length > 0) {
                filterString += "Payment Method: " + paymentMethod + ", ";
            }

            if (StringUtil.trim(status).length > 0) {
                filterString += statusFieldDescription + ": " + status + ", ";
            }

            if (StringUtil.trim(companyIds).length > 0)  {
                filterString += "Companies: (" + StringUtil.trim(companyIds).replace(/\s+/g, ", ") + "), ";
            }


            if (StringUtil.trim(settlementStartDate).length > 0) {
                filterString += "Settlement Date from: " + settlementStartDate + ", ";
            }

            if (StringUtil.trim(settlementEndDate).length > 0) {
                filterString += "Settlement Date to: " + settlementEndDate + ", ";
            }

            if (StringUtil.trim(initiationStartDate).length > 0) {
                filterString += "Initiation Date from: " + initiationStartDate + ", ";
            }

            if (StringUtil.trim(initiationEndDate).length > 0) {
                filterString += "Initiation Date to: " + initiationEndDate + ", ";
            }

            if (selectedQuarter != null) {
                filterString += "Quarter: " + quarter.label + ", ";
            }

            if (overdueOnly) {
                filterString += "Overdue or scheduled as overdue payments only, ";
            }
            filterString = filterString.substr(0, filterString.length - 2);
            return filterString;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        public function goToCompanyInfo(payment:Payment):void {
            var companyKey:CompanyKey = payment.companyKey;
            companyKey.display();
        }

        public function get settlementStartDateValue():Date {
            if (settlementStartDate == EMPTY_STRING) {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(settlementStartDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get settlementEndDateValue():Date {
            if (settlementEndDate == EMPTY_STRING) {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(settlementEndDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get initiationStartDateValue():Date {
            if (initiationStartDate == EMPTY_STRING) {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(initiationStartDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get initiationEndDateValue():Date {
            if (initiationEndDate == EMPTY_STRING) {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(initiationEndDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get selectedQuarter():Quarter {
            if (quarter == null || quarter == Quarter.EMPTY_QUARTER) {
                return null;
            }
            return quarter;
        }

        [Bindable]
        public function set agency(value:String):void {
            mAgency = value;
            paymentTemplateList.removeAll();
            paymentTemplateList.addItem(PaymentTemplate.EMPTY_TEMPLATE);
            var lAgency:Agency = SAP.instance.lookupService.agencyList.getItemByKey(mAgency) as Agency;
            if (lAgency != null) {
                for each (var paymentTemplate:PaymentTemplate in lAgency.paymentTemplates) {
                    paymentTemplateList.addItem(paymentTemplate);
                }
            }
        }

        public function get agency():String {
            return mAgency;
        }
        [Bindable] [BackingProperty]
        public function get paymentTemplate():PaymentTemplate {
            return mPaymentTemplate;
        }

        public function set paymentTemplate(value:PaymentTemplate):void {
            mPaymentTemplate = value;
        }

        public function updatePageSizeToFitScreen(view:DisplayObject):void {
            try {
                var availableHeight:Number = view.parent.parent.parent.parent.parent.parent.height - 225;
                var numberOfRows:Number = availableHeight / 24;
                payments.pageSize = Math.max(10, numberOfRows - (numberOfRows % 5));
            } catch (e:Error) {
                //just in case something goes wrong with this. I mean I don't see how anything could wrong with this technique, though.
                payments.pageSize = 10;
            }
        }


    }
}

/**
 * User: dweinberg
 * Date: 1/29/11
 * Time: 7:48 PM
 */
package psp.sap.viewmodel {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.rpc.events.ResultEvent;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.model.Agency;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.LawItem;
    import psp.sap.model.ManualLedgerTaxLine;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.ManualLedgerLimit;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class ManualLedgerViewModel extends AbstractPartViewModel {

        [Bindable] public var entryTypes:Array = ["", "Liabilities/Wages", "Reconciling Adjustment", "Record Customer Payment"];

        private var mEntryType:String="";

        [Bindable] [BackingProperty]
        public function get entryType():String {
            return mEntryType;
        }

        public function set entryType(value:String):void {
            mEntryType = value;
            layout1 = entryType == entryTypes[1];
            layout2 = entryType == entryTypes[2];
            layout3 = entryType == entryTypes[3];

            checkDateValidator.enabled = !layout3;
            yearValidator.enabled = layout3;
            quarterValidator.enabled = layout3;
            datePaidValidator.enabled = layout3;
            updateRecordingOptionValidator();

            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "showYearQuarter", null, null));
        }

        [Bindable] public var layout1:Boolean = false;
        [Bindable] public var layout2:Boolean = false;
        [Bindable] public var layout3:Boolean = false;


        [ArrayElementType("psp.sap.model.PaymentTemplate")]
        [Bindable] [BackingProperty] public var paymentTemplates:ArrayCollection = new ArrayCollection();

        private var mSelectedPaymentTemplate:PaymentTemplate=null;

        [Bindable] [BackingProperty]
        public function get selectedPaymentTemplate():PaymentTemplate {
            return mSelectedPaymentTemplate;
        }

        public function set selectedPaymentTemplate(value:PaymentTemplate):void {
            mSelectedPaymentTemplate = value;
            laws = value.lawItems;
            onSearchFieldsChange();
        }

        [ArrayElementType("psp.sap.model.LawItem")]
        [Bindable] public var laws:ArrayCollection = new ArrayCollection();

        private var mSelectedLaw:LawItem;
        [Bindable] [BackingProperty]
        public function get selectedLaw():LawItem {
            return mSelectedLaw;
        }

        public function set selectedLaw(value:LawItem):void {
            mSelectedLaw = value;
            onSearchFieldsChange();
        }

        private var mCheckDate:String="";

        [Bindable] [BackingProperty]
        public function get checkDate():String {
            return mCheckDate;
        }

        public function set checkDate(value:String):void {
            mCheckDate = value;
            if (checkDateValue != null) {
                selectedYear = checkDateValue.fullYear.toString();
                if (checkDateValue.month <= 2) {
                    selectedQuarter = "Q1";
                } else if (checkDateValue.month <= 5) {
                    selectedQuarter = "Q2";
                } else if (checkDateValue.month <= 8) {
                    selectedQuarter = "Q3";
                } else {
                    selectedQuarter = "Q4";
                }
            } else {
                selectedYear = "";
                selectedQuarter = "";
            }
            onSearchFieldsChange();
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "checkDateValue", null, null));
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "showYearQuarter", null, null));
        }

        [Bindable("propetyChange")]
        public function get checkDateValue():Date {
            return checkDateValidator.isDateValid() ? new Date(checkDate) : null;
        }

        [Bindable("propertyChange")]
        public function get showYearQuarter():Boolean {
            return layout3 || checkDateValue != null;
        }

        private var mDatePaid:String="";

        [Bindable] [BackingProperty]
        public function get datePaid():String {
            return mDatePaid;
        }

        public function set datePaid(value:String):void {
            mDatePaid = value;
        }

        public function get datePaidValue():Date {
            return datePaidValidator.isDateValid() ? new Date(datePaid) : null;
        }

        private var mSelectedYear:String="";

        [Bindable] [BackingProperty]
        public function get selectedYear():String {
            return mSelectedYear;
        }

        public function set selectedYear(value:String):void {
            mSelectedYear = value;
            onSearchFieldsChange();
        }

        [Bindable] public var quarters:Array = ["", "Q1", "Q2", "Q3", "Q4"];
        private var mSelectedQuarter:String="";

        [Bindable] [BackingProperty]
        public function get selectedQuarter():String {
            return mSelectedQuarter;
        }

        public function set selectedQuarter(value:String):void {
            mSelectedQuarter = value;
            onSearchFieldsChange();
        }

        private function get checkDateFromYearQuarter():Date {
            if (isValidatorValid(yearValidator) && isValidatorValid(quarterValidator)) {
                //month is zero-indexed but day is one-indexed, so this is the last day of the quarter (being mindful of UTC/PST differences)
                return new Date(selectedYear, quarters.indexOf(selectedQuarter) * 3 , 0,  0, 0, 0, 0);
            } else {
                return null;
            }
        }

        //flex components don't like to follow spec and return null instead of []
        private function isValidatorValid(validator:Validator):Boolean {
            var results:Array = validator.validate().results;
            return results == null || results.length == 0;
        }

        [Bindable] [BackingProperty] public var memo:String="";

        [Bindable] [BackingProperty]
        [ArrayElementType("psp.sap.model.ManualLedgerTaxLine")]
        public var taxLines:ArrayCollection = null;

        [Bindable] [BackingProperty]
        public var summaryLine:ManualLedgerTaxLine = null;

        private var anyBalance:Boolean = false;
        private var isWarningMessage:Boolean = true;
        private var defaultWarningMessage:String = "Are you sure you want to create this entry?";
        public var warningMessage:String = defaultWarningMessage;
        public var warningLimit:Number =10000;
        private var mNetBalance:Number=0;


        private var mSelectedRecordingOption:String;

        [Bindable]
        public var actionSelectionRequiredValidator:Validator;

        public var checkDateValidator:SAPDateValidator;
        public var memoRequiredValidator:Validator;
        public var yearValidator:NumberValidator;
        public var quarterValidator:Validator;
        public var datePaidValidator:SAPDateValidator;
        private var mAllowLimit_OutsideOfBoundaries:Boolean = false;

        public var taxValidators:ArrayCollection;
        public var wageValidators:ArrayCollection;
        public var qtdTaxValidators:ArrayCollection;
        public var ytdTaxValidators:ArrayCollection;
        public var qtdWageValidators:ArrayCollection;
        public var ytdWageValidators:ArrayCollection;

        public var qtdTaxTotalValidator:NumberValidator;
        public var ytdTaxTotalValidator:NumberValidator;

        public function updateWarningMessage():void {
            if(!isWarningMessage) {
                warningMessage = defaultWarningMessage;
                return;
            }
            for each (var taxLine:ManualLedgerTaxLine in taxLines) {
                if (taxLine.amount != 0 && taxLine.amount > warningLimit) {
                    warningMessage = "The liability amount you’ve entered is very high."
                        +"\n"+defaultWarningMessage;
                    return;
                }
                warningMessage = defaultWarningMessage;
            }
        }

        [Bindable] [BackingProperty]
        public function get selectedRecordingOption():String {
            return mSelectedRecordingOption;
        }

        public function set selectedRecordingOption(val:String):void {
            //workaround for RadioButtonGroup binding "null" instead of null
            if (val != "null") {
                mSelectedRecordingOption = val;
            } else {
                mSelectedRecordingOption = null;
            }
        }

        public function ManualLedgerViewModel() {
            super();
            reloadOnSave = true;

            checkDateValidator = SAPValidators.createDateValidator(this, "checkDate", true, -1, -1, null, null, null);
            validators.push(checkDateValidator);
            actionSelectionRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "selectedRecordingOption", true);
            validators.push(actionSelectionRequiredValidator);
            memoRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "memo", true);
            validators.push(memoRequiredValidator);
            yearValidator = SAPValidators.createNumberValidator(this, "selectedYear", true, 1990, 2100, false);
            validators.push(yearValidator);
            quarterValidator = SAPValidators.createRequiredFieldValidator(this, "selectedQuarter");
            validators.push(quarterValidator);
            datePaidValidator = SAPValidators.createDateValidator(this, "datePaid", true, -1, -1, null, null, null);
            validators.push(datePaidValidator);
        }

        override protected function onActivating():void {
            cancel();
        }

        override protected function loadModelData():void {
            if (!canLoadTransactions) {
                SAP.instance.taxService.findCompanyAgencies(company.sourceSystemCd, company.companyId, createLoadModelDataResponder(onCompanyAgenciesLoaded));
            } else {
                SAP.instance.taxService.getManualLedgerLines(company.sourceSystemCd,
                        company.companyId,
                        selectedPaymentTemplate.paymentTemplateCd,
                        selectedLaw.lawId,
                        layout3 ? checkDateFromYearQuarter : checkDateValue,
                        createLoadModelDataResponder(onLedgerLinesLoaded));
            }
            SAP.instance.taxService.getManualLedgerLimit(companyKey.sourceSystemCd,
                companyKey.companyId,createLoadModelDataResponder(onManualLedgerLimit));
        }

        [Bindable("propertyChange")]
        public function get canLoadTransactions():Boolean {
            return ((selectedPaymentTemplate != null && selectedPaymentTemplate.paymentTemplateName != "") || (selectedLaw != null && selectedLaw.name != ""))
                    && (layout3 ? checkDateFromYearQuarter != null : checkDateValue != null);
        }


        private function  onManualLedgerLimit(e:ResultEvent):void {
            var mll:ManualLedgerLimit = e.result as ManualLedgerLimit;
            warningLimit = mll.warningLimit;
            isWarningMessage = mll.limitEnabled;
        }

        private function  onCompanyAgenciesLoaded(e:ResultEvent):void {
            var agencies:ArrayCollection = ArrayCollection(e.result);

            var newTemplates:ArrayCollection = new ArrayCollection();
            var blankPaymentTemplate:PaymentTemplate = new PaymentTemplate();
            blankPaymentTemplate.paymentTemplateName = "";
            blankPaymentTemplate.lawItems = new ArrayCollection();
            newTemplates.addItem(blankPaymentTemplate);

            var blankLawItem:LawItem = new LawItem();
            blankLawItem.name =  "";
            blankPaymentTemplate.lawItems.addItem(blankLawItem);

            for each (var agency:Agency in agencies) {
                for each (var template:PaymentTemplate in agency.paymentTemplates) {
                    for each (var law:LawItem in template.lawItems) {
                        blankPaymentTemplate.lawItems.addItem(law);
                    }
                    template.lawItems.addItemAt(blankLawItem, 0);
                    newTemplates.addItem(template);
                }
            }

            var paymentTemplateSort:Sort = new Sort();
            var paymentTemplateSortField:SortField = new SortField("paymentTemplateName");
            paymentTemplateSortField.compareFunction = CompareUtils.comparePaymentTemplate;
            paymentTemplateSort.fields = [paymentTemplateSortField];
            newTemplates.sort = paymentTemplateSort;
            newTemplates.refresh();
            paymentTemplates = newTemplates;

            if (selectedPaymentTemplate == null) {
                selectedPaymentTemplate = blankPaymentTemplate;
            }
        }

        private function onLedgerLinesLoaded(e:ResultEvent):void {
            var newTaxLines:ArrayCollection = ArrayCollection(e.result);
            if (newTaxLines.length > 1) {
                summaryLine = ManualLedgerTaxLine(newTaxLines.removeItemAt(newTaxLines.length-1));
            }
            newTaxLines.addEventListener(CollectionEvent.COLLECTION_CHANGE, onTaxItemChange, false, 0, true);

            taxLines = newTaxLines;
        }

        private function onTaxItemChange(e:Event):void {
            anyBalance = false;
            var newAmount:Number = 0;
            for each (var taxLine:ManualLedgerTaxLine in taxLines) {
                if (taxLine.amount != 0 || taxLine.wageAmount != 0) {
                    anyBalance = true;
                }
                newAmount += taxLine.amount;
            }
            netBalance = newAmount;
            if (summaryLine != null) {
                summaryLine.amount = newAmount;
            }
            updateCanSave();
        }

        override public function get hasChanged():Boolean {
            return anyBalance;
        }

        private function onSearchFieldsChange():void {
            taxLines = null;
            summaryLine = null;
            if (canLoadTransactions) {
                refresh();
            }
        }

        [Bindable]
        public function get allowLimitOutsideOfBoundaries():Boolean {
            return mAllowLimit_OutsideOfBoundaries;
        }

        public function set allowLimitOutsideOfBoundaries(value:Boolean):void {
            mAllowLimit_OutsideOfBoundaries = value;
        }

        override protected function initializeBackingProperties():void {
            validators.length = 5;

            taxValidators = new ArrayCollection();
            wageValidators = new ArrayCollection();

            qtdTaxValidators = new ArrayCollection();
            ytdTaxValidators = new ArrayCollection();
            qtdWageValidators = new ArrayCollection();
            ytdWageValidators = new ArrayCollection();

            if (taxLines != null) {
                for each (var taxLine:ManualLedgerTaxLine in taxLines) {

                    var negativeOnly:Boolean = layout3 && taxLine.law.negativeLiability;

                    var taxValidator:Validator = SAPValidators.createNumberValidator(taxLine, "amountText", false, null, negativeOnly && !layout3 ? 0 : null, true);
                    taxValidators.addItem(taxValidator);
                    validators.push(taxValidator);


                    var wageValidator:Validator = SAPValidators.createNumberValidator(taxLine, "wageAmountText", false, null, null, true);
                    wageValidators.addItem(wageValidator);
                    validators.push(wageValidator);

                    var qtdTaxValidator:NumberValidator = SAPValidators.createNumberValidator(
                            taxLine.adjustedQTDYTD,
                            "qtdLiability",
                            false,
                            null,
                            taxLine.law.negativeLiability ? 0 : null,
                            taxLine.law.negativeLiability);
                    qtdTaxValidator.enabled = layout1;
                    qtdTaxValidators.addItem(qtdTaxValidator);
                    validators.push(qtdTaxValidator);
                    var ytdTaxValidator:NumberValidator = SAPValidators.createNumberValidator(
                            taxLine.adjustedQTDYTD,
                            "ytdLiability",
                            false,
                            null,
                            taxLine.law.negativeLiability ? 0 : null,
                            taxLine.law.negativeLiability);
                    ytdTaxValidator.enabled = layout1;
                    ytdTaxValidators.addItem(ytdTaxValidator);
                    validators.push(ytdTaxValidator);
                    var qtdWageValidator:NumberValidator = SAPValidators.createNumberValidator(
                            taxLine.adjustedQTDYTD,
                            "qtdWages",
                            false,
                            null,
                            null,
                            false);
                    qtdWageValidator.enabled = layout1;
                    qtdWageValidators.addItem(qtdWageValidator);
                    validators.push(qtdWageValidator);
                    var ytdWageValidator:NumberValidator = SAPValidators.createNumberValidator(
                            taxLine.adjustedQTDYTD,
                            "ytdWages",
                            false,
                            null,
                            null,
                            false);
                    ytdWageValidator.enabled = layout1;
                    ytdWageValidators.addItem(ytdWageValidator);
                    validators.push(ytdWageValidator);

                }


                if (summaryLine != null) {
                    qtdTaxTotalValidator = SAPValidators.createNumberValidator(
                            summaryLine.adjustedQTDYTD,
                            "qtdLiability",
                            false,
                            null,
                            null,
                            true);
                    qtdTaxTotalValidator.enabled = layout1;
                    validators.push(qtdTaxTotalValidator);

                    ytdTaxTotalValidator = SAPValidators.createNumberValidator(
                            summaryLine.adjustedQTDYTD,
                            "ytdLiability",
                            false,
                            null,
                            null,
                            true);
                    ytdTaxTotalValidator.enabled = layout1;
                    validators.push(ytdTaxTotalValidator);

                }
            }

            netBalance=0;
            anyBalance = false;

        }

        [Bindable]
        public function get netBalance():Number {
            return mNetBalance;
        }

        public function set netBalance(value:Number):void {
            mNetBalance = value;
            updateRecordingOptionValidator();
        }

        private function updateRecordingOptionValidator():void {
            actionSelectionRequiredValidator.enabled = (layout1 || layout3) && netBalance != 0;
        }

        override protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            return super.evaluateIsValid(fireEvents) && taxLines != null;
        }

        override public function cancel():void {
            super.cancel();
            entryType = "";
            selectedPaymentTemplate = PaymentTemplate(paymentTemplates.length > 0 ? paymentTemplates.getItemAt(0) : null);
            selectedLaw = laws.length > 0 ? LawItem(laws.getItemAt(0)) : null;
            checkDate="";
            selectedYear="";
            selectedQuarter="";
            datePaid="";
            selectedRecordingOption=null;
            netBalance=0;
            anyBalance = false;
            warningMessage = defaultWarningMessage;
            memo="";
        }

        override protected function executeSave():void {
            SAP.instance.taxService.createManualLedgerEntry(companyKey.sourceSystemCd,
                    companyKey.companyId,
                    entryType,
                    taxLines,
                    layout3 ? checkDateFromYearQuarter : checkDateValue,
                    memo,
                    parseInt(selectedRecordingOption),
                    datePaidValue,
                    allowLimitOutsideOfBoundaries,
                    createSaveResponder())
        }


    }
}

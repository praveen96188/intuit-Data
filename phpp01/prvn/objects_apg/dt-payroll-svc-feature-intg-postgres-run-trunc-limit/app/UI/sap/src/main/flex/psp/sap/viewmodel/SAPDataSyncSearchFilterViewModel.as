package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.events.ValidationResultEvent;
    import mx.utils.StringUtil;
    import mx.validators.DateValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.validators.SAPFromAndToValueValidator;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class SAPDataSyncSearchFilterViewModel extends AbstractPartViewModel {

        [Bindable]
        public var idSearchStrings:ArrayCollection = new ArrayCollection(["Transaction ID", "Paycheck ID", "Employee ID","Payroll Item ID", "Token"]);

        [BackingProperty]
        [Bindable]
        public var selectedIdSearchString:String;

        [Bindable]
        public var typeSearchStrings:ArrayCollection = new ArrayCollection(["Payroll Txns", "Paychecks", "Payroll Items", "Items Stopped", "Employees"]);

        private var mSelectedTypeSearchString:String;

        [Bindable]
        public var informationSearchStrings:ArrayCollection = new ArrayCollection(['Check #','Amount', 'PItem Name']);

        [Bindable]
        public var selectedInformationSearchString:String;

        [BackingProperty]
        [Bindable]
        public var informationSearchQueryString:String;

        [Bindable]
        [BackingProperty]
        public var fromId:String = "";

        [Bindable]
        [BackingProperty]
        public var toId:String = "";

        [Bindable]
        [BackingProperty]
        public var fromDateString:String = "";

        [Bindable]
        [BackingProperty]
        public var toDateString:String = "";

        [Bindable]
        public var dateLabel:String = "";

        [BackingProperty]
        [Bindable]
        public function get selectedTypeSearchString():String {
            return mSelectedTypeSearchString;
        }

        public function set selectedTypeSearchString(value:String):void {
            mSelectedTypeSearchString = value;
            switch (value) {
                case "Payroll Items":
                case "Items Stopped":
                    dateLabel = "Modified Date";
                    break;
                case "Paychecks":
                    dateLabel = "Check Date";
                    break;
                case "Payroll Txns":
                    dateLabel = "Created Date";
                    break;
                default:
                    dateLabel = "";
            }
        }


        public function get fromDateValue():Date {
            if (fromDateString == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(fromDateString);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get toDateValue():Date {
            if (toDateString == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(toDateString);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get checkNumber():String {
            return selectedInformationSearchString == "Check #" ? informationSearchQueryString : null;
        }

        public function get amount():String {
            return selectedInformationSearchString == "Amount" ? informationSearchQueryString : null;
        }

        public function get pItemName():String {
            return selectedInformationSearchString == "PItem Name" ? informationSearchQueryString : null;
        }

        [Bindable]
        public var fromAndToIdRequiredValidator:SAPFromAndToValueValidator;

        [Bindable]
        public var startEndDateValidator:SAPStartEndDateValidator;

        [Bindable]
        public var fromDateValidator:DateValidator;
        [Bindable]
        public var toDateValidator:DateValidator;

        [Bindable]
        public var fromIdRequiredValidator:Validator;

        [Bindable]
        public var toIdRequiredValidator:Validator;

        public function SAPDataSyncSearchFilterViewModel() {
            super();
            this.reloadOnActivate = false;

            fromIdRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "fromId", true);
            validators.push(fromIdRequiredValidator);

            toIdRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "toId", true);
            validators.push(toIdRequiredValidator);

            fromAndToIdRequiredValidator = SAPValidators.createSAPFromAndToIdValidator(this, this, "fromId", "toId", false);
            validators.push(fromAndToIdRequiredValidator);

            startEndDateValidator = SAPValidators.createSAPStartEndDateValidator(this, this, "fromDateString", "toDateString", false);
            validators.push(startEndDateValidator);

            fromDateValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "fromDateString", false);
            this.validators.push(fromDateValidator);

            toDateValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "toDateString", false);
            this.validators.push(toDateValidator);

            selectedIdSearchString = idSearchStrings[0];
            selectedTypeSearchString = typeSearchStrings[0];
            selectedInformationSearchString = informationSearchStrings[0];

            selectedSearchType = 0;
        }

        public function getFilterLabelString():String {
            var filterLabelString:String = "";

            if (selectedSearchType == 0) {
                filterLabelString += selectedIdSearchString;
                if (fromId != null && toId != null && StringUtil.trim(fromId).length > 0 && StringUtil.trim(toId).length > 0) {
                    filterLabelString += " from: " + fromId + " to: " + toId + ", ";
                }
            } else {
                filterLabelString += selectedTypeSearchString + ", ";
            }
            if (dateLabel != "") {
                if (StringUtil.trim(fromDateString).length > 0 || StringUtil.trim(toDateString).length > 0) {
                    filterLabelString += dateLabel + ": ";
                }
                if (StringUtil.trim(fromDateString).length > 0) {
                    filterLabelString += "From : " + fromDateString + ", ";
                }
                if (StringUtil.trim(toDateString).length > 0) {
                    filterLabelString += "To : " + toDateString + ", ";
                }
            }
            if (StringUtil.trim(informationSearchQueryString).length > 0) {
                filterLabelString += selectedInformationSearchString + ": " + informationSearchQueryString + ", ";
            }

            if (filterLabelString.length > 2) {
                filterLabelString = filterLabelString.substr(0, filterLabelString.length - 2);
            }
            return filterLabelString;
        }

        private var mSelectedSearchType:int;

        [Bindable]
        public function get selectedSearchType():int {
            return mSelectedSearchType;
        }

        public function set selectedSearchType(val:int):void {
            mSelectedSearchType = val;
            if (mSelectedSearchType == 0) {
                fromAndToIdRequiredValidator.enabled = true;
                startEndDateValidator.enabled = false;                                
                startEndDateValidator.dispatchEvent(new ValidationResultEvent(ValidationResultEvent.VALID)); //validation events not generated when not enabled and not required                
                fromDateValidator.enabled = false;
                toDateValidator.enabled = false;
                fromIdRequiredValidator.required = true;
                toIdRequiredValidator.required = true;
            }
            else {
                fromAndToIdRequiredValidator.enabled = false;
                startEndDateValidator.enabled = true;
                fromDateValidator.enabled = true;
                toDateValidator.enabled = true;
                fromIdRequiredValidator.required = false;
                toIdRequiredValidator.required = false;
            }
            updateCanSave();
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        public function doSearch():void {
            dispatchEvent(new SAPEvent(DataSyncSearchViewModel.SEARCH_REQUESTED_EVENT));
        }
    }
}
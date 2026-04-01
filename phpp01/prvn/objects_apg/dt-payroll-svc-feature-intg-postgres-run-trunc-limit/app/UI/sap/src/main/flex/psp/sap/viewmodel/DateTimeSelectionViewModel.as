package psp.sap.viewmodel {
    import mx.validators.RegExpValidator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.validators.SAPValidators;

    public class DateTimeSelectionViewModel extends DateSelectionViewModel {
        private var mStartTime:String = "00:00";
        private var mEndTime:String = "23:59";
        private var lastStartTime:String = "00:00";
        private var lastEndTime:String = "23:59";
        public var startTimeValidator:RegExpValidator;
        public var endTimeValidator:RegExpValidator;

        [Bindable]
        public var useOldValue:Boolean = false;

        [Bindable]
        public function get startTime():String {
            return mStartTime;
        }

        public function set startTime(value:String):void {
            mStartTime = value;
            inspectorPage.updateCanSave();
        }

        [Bindable]
        public function get endTime():String {
            return mEndTime;
        }

        public function set endTime(value:String):void {
            mEndTime = value;
            inspectorPage.updateCanSave();
        }

        public function DateTimeSelectionViewModel(inspectorPage:AbstractPartViewModel) {
            super(inspectorPage);
            this.inspectorPage = inspectorPage;

            this.startDate = SAPDateFormatters.dateFormatShort.format(SAP.instance.PSPDate);
            this.endDate = SAPDateFormatters.dateFormatShort.format(SAP.instance.PSPDate);
            this.startTimeValidator = SAPValidators.createTimeValidator(this, "startTime", true, this);
            this.endTimeValidator = SAPValidators.createTimeValidator(this, "endTime", true, this);

            inspectorPage.validators.push(startTimeValidator);
            inspectorPage.validators.push(endTimeValidator);

        }


        override public function get startDateValue():Date {
            if (startDate == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(startDate);
            var txDate:Date = SAP.instance.PSPDate;
            this.startTime = (isValidTimeString(this.startTime)) ? this.startTime : this.lastStartTime;
            this.lastStartTime = this.startTime;
            formattedDate += (" " + this.startTime);
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            txDate.setHours(this.startTime.split(":")[0]);
            txDate.setMinutes(this.startTime.split(":")[1]);
            return txDate;
        }

        override public function get endDateValue():Date {
            if (endDate == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(endDate);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            this.endTime = (isValidTimeString(this.endTime)) ? this.endTime : this.lastEndTime;
            this.lastEndTime = this.endTime;
            txDate.setTime(time);
            txDate.setHours(this.endTime.split(":")[0]);
            txDate.setMinutes(this.endTime.split(":")[1]);
            return txDate;
        }


        protected function isValidTimeString(input:String):Boolean {
            if (input == null) {
                return false;
            }
            var output:Array = input.match(SAPValidators.TIME_REGEX);
            return (output.length > 0) ? (output[0] == input) : false;
        }
    }

}
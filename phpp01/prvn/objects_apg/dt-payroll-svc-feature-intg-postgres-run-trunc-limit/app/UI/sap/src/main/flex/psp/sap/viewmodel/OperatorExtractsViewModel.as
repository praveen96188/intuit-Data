/**
 * User: dweinberg
 * Date: 10/25/12
 * Time: 1:12 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;

    import psp.sap.validators.SAPValidators;

    public class OperatorExtractsViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty] public var selectedYear:String;
        [Bindable] [BackingProperty] public var selectedQuarter:String;
        [Bindable] [BackingProperty] public var scheduledDateText:String="";
        [Bindable] [BackingProperty] public var scheduledTimeText:String="00:00";

        [Bindable]
        [ArrayElementType("String")]
        public var quarters:ArrayCollection;

        [Bindable]
        [ArrayElementType("String")]
        public var times:ArrayCollection;

        [Bindable] public var yearValidator:NumberValidator;
        [Bindable] public var quarterValidator:Validator;
        [Bindable] public var scheduleDateValidator:Validator;

        public function OperatorExtractsViewModel() {
            quarters = new ArrayCollection();
            quarters.addItem(" ");
            quarters.addItem("Q1");
            quarters.addItem("Q2");
            quarters.addItem("Q3");
            quarters.addItem("Q4");

            times = new ArrayCollection();
            for (var i:int = 0; i < 24; i++) {
                for (var j:int = 0; j < 60; j+=15) {
                    times.addItem(leadingZero(i) + ":" + leadingZero(j));
                }
            }

            yearValidator = SAPValidators.createNumberValidator(this, "selectedYear", true, 1900, 2100, false, 1);
            validators.push(yearValidator);
            quarterValidator = SAPValidators.createRequiredFieldValidator(this, "selectedQuarter", true);
            validators.push(quarterValidator);
            scheduleDateValidator = SAPValidators.createDateValidator(this, "scheduledDateText", false, 0, 7);
            validators.push(scheduleDateValidator);
        }

        private static function leadingZero(num:Number):String {
            if(num < 10) {
                return "0" + num;
            }
            return num.toString();
        }


        override protected function executeSave():void {
            SAP.instance.administrationService.scheduleATFExtract(selectedYear, parseInt(selectedQuarter.charAt(1)), scheduledDateTime, createSaveResponder());
        }

        public function get scheduledDateTime():Date {
            if (scheduledDateText == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(scheduledDateText);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            txDate.setHours(scheduledTimeText.substr(0, 2));
            txDate.setMinutes(scheduledTimeText.substr(3, 2));
            return txDate;
        }
    }
}

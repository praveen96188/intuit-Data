package psp.taxcredits.model {
    import flash.events.Event;
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;

    import mx.formatters.CurrencyFormatter;
    import mx.formatters.DateFormatter;

    import mx.formatters.NumberFormatter;

    import psp.taxcredits.dto.WOTCCategory;

    /**
     * estimates credit amounts and date of max amount
     * ignores leap years/edgess since it's just an estimation
     */
    public class EstimatorModel extends EventDispatcher {

        private const HOURS_PER_YEAR:Number = 2080;

        public var wotcCategory:WOTCCategory;
        public var wotcCategorySecondYear:WOTCCategory;

        private var mWageString:String;
        private var mSalaryString:String;
        private var mHoursPerWeekString:String;

        private var mHoursPerWeek:Number;
        private var mWage:Number;        
        private var mHourly:Boolean=true;
        private var mEmployedUntil:Date;
        private var mUseMaximumDate:Boolean=true;
        [Bindable] public var useMaximumDateSet:Boolean=true;

        [Bindable] public var estimate:Number;
        private var mMaxCreditDate:Date;

        [Bindable] public var numberFormatter:NumberFormatter;
        private var dateFormatter:DateFormatter;

        public function EstimatorModel(model:TaxCreditsModel) {
            dateFormatter = new DateFormatter();
            dateFormatter.formatString = "MMMM D, YYYY";
            numberFormatter = new NumberFormatter();
            numberFormatter.precision=0;
            
            model.addEventListener(TaxCreditsEvent.CATEGORIES_LOADED, onCategoriesLoaded);
        }

        private function onCategoriesLoaded(e:Event):void {
            var categories:ArrayCollection = TaxCreditsModel.instance.eligibleCategories;

            //test for multiple years (will always produce largest credit)
            var multipleYears1:WOTCCategory = null;
            var multipleYears2:WOTCCategory = null;
            for each (var multipleCategory:WOTCCategory in categories) {
                if (multipleCategory.category.match("Year1")) {
                    multipleYears1 = multipleCategory;
                } else if (multipleCategory.category.match("Year2")) {
                    multipleYears2 = multipleCategory;
                }
            }

            wotcCategory = multipleYears1;
            wotcCategorySecondYear = multipleYears2;

            if (multipleYears1 == null) {
                var maxCategory:WOTCCategory = null;
                for each (var category:WOTCCategory in categories) {
                    if (maxCategory == null || category.maxCredit > maxCategory.maxCredit) {
                        maxCategory = category;
                    }
                }
                wotcCategory = maxCategory;
                wotcCategorySecondYear = null;
            }
        }




        private function set salary(value:Number):void {
            wage = value / HOURS_PER_YEAR;
            hoursPerWeek = 40;
        }

        [Bindable]
        public function get salaryString():String {
            return mSalaryString;
        }

        public function set salaryString(value:String):void {
            mSalaryString = value;
            setSalaryFromString();
        }

        private function setSalaryFromString():void {
            var number:Number = parseFloat(salaryString);
            if (isNaN(number)) {
                salary = 0;
            } else {
                salary = number;
            }
        }


        private function get hoursPerWeek():Number {
            return mHoursPerWeek;
        }

        private function set hoursPerWeek(value:Number):void {
            mHoursPerWeek = value;
            calculateMaxCreditDate();
            calculateEstimate();
        }

        [Bindable]
        public function get hoursPerWeekString():String {
            return mHoursPerWeekString;            
        }

        public function set hoursPerWeekString(value:String):void {
            mHoursPerWeekString = value;
            setHoursPerWeekFromString();
        }

        private function setHoursPerWeekFromString():void {
            var number:Number = parseFloat(hoursPerWeekString);
            if (isNaN(number)) {
                hoursPerWeek = 0;
            } else {
                hoursPerWeek = number;
            }

        }

        private function get wage():Number {
            return mWage;
        }

        private function set wage(value:Number):void {
            mWage = value;
            calculateMaxCreditDate();
            calculateEstimate();
        }

        [Bindable]
        public function get wageString():String {
            return mWageString;
        }

        public function set wageString(value:String):void {
            mWageString = value;
            setWageFromString();
        }

        private function setWageFromString():void {
            var number:Number = parseFloat(wageString);
            if (isNaN(number)) {
                wage = 0;
            } else {
                wage = number;
            }

        }

        [Bindable]
        public function get employedUntil():Date {
            return mEmployedUntil;
        }

        public function set employedUntil(value:Date):void {
            mEmployedUntil = value;
            calculateEstimate();            
        }

        [Bindable]
        public function get useMaximumDate():Boolean {
            return mUseMaximumDate;
        }

        public function set useMaximumDate(value:Boolean):void {
            mUseMaximumDate = value;            
            calculateEstimate();
        }

        public function get maxCreditDate():Date {
            return mMaxCreditDate;
        }

        public function set maxCreditDate(value:Date):void {
            mMaxCreditDate = value;
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "maxCreditDateString",null,null))
        }

        [Bindable("propertyChange")]
        public function get maxCreditDateString():String {
            return dateFormatter.format(maxCreditDate);
        }

        public function formatLongDate(date:Date):String {
            if (dateFormatter == null || date == null) {
                return "";
            }
            return dateFormatter.format(date);
        }

        [Bindable]
        public function get hourly():Boolean {
            return mHourly;
        }

        public function set hourly(value:Boolean):void {
            mHourly = value;
            if (value) {
                setHoursPerWeekFromString();
                setWageFromString();
            } else {
                setSalaryFromString();
            }

        }

        public function calculateEstimate():void {
            var effectiveEmployedUntilDate:Date = useMaximumDate ? maxCreditDate : employedUntil;


            if (wotcCategory == null || hoursPerWeek == 0 || wage == 0 || effectiveEmployedUntilDate == null) {
                estimate = NaN;
                return;
            }

            //ie if hired today and fired tomorrow, 2 days of work
            var days:Number = ModelUtils.dayDifference(TaxCreditsModel.instance.employee.startDate, effectiveEmployedUntilDate) + 1;


            if (days > 365 && wotcCategorySecondYear != null) {
                estimate = calculate(365, wotcCategory) + calculate(days - 365, wotcCategorySecondYear);
            } else {
                estimate = calculate(days, wotcCategory);
            }

            if (isEmployeeHireRetentionEligible && days >= 365) {
                estimate += 1000;
            }

            if (! isNaN(estimate)) {
                TaxCreditsModel.instance.tracker.trackEvent("Estimate", "Estimate savings", "estimate", estimate);
            }
        }

        private function calculate(days:Number, wotcCategory:WOTCCategory):Number {
            var newEstimate:Number;

            var weeks:Number = days / 7.;
            var hoursWorked:Number = mHoursPerWeek * weeks;

            var wageBase:Number = hoursWorked * mWage;

            wageBase = Math.min(wageBase, wotcCategory.wageBase);

            if (hoursWorked < 120) {
                newEstimate = wageBase * wotcCategory.taxRate0;
            } else if (hoursWorked >= 120 && hoursWorked < 400) {
                newEstimate = wageBase * wotcCategory.taxRate1;
            } else if (hoursWorked >= 400) {
                newEstimate = wageBase * wotcCategory.taxRate2;
            }

            newEstimate = Math.min(newEstimate, wotcCategory.maxCredit);

            return newEstimate;
        }

        public function calculateMaxCreditDate():void {
            if (wotcCategory == null || hoursPerWeek == 0 || wage == 0) {
                maxCreditDate = null;
                return;
            }

            if (wotcCategorySecondYear != null) {
                var secondYearDays:Number = getMaxCreditDays(wotcCategorySecondYear);
                maxCreditDate = ModelUtils.addDays(TaxCreditsModel.instance.employee.startDate, 365 + secondYearDays + 1);
            } else {
                var days:Number = Math.ceil(getMaxCreditDays(wotcCategory));
                //because of rounding, the max might be a day before if selected and want to keep it consistent
                while (calculate(days-1, wotcCategory) == wotcCategory.maxCredit) {
                    days--;
                }

                if (isEmployeeHireRetentionEligible && days < 365) {
                    days = 365;
                }

                maxCreditDate = ModelUtils.addDays(TaxCreditsModel.instance.employee.startDate, days);
            }

        }

        private function getMaxCreditHours(wotcCategory:WOTCCategory):Number {
            //max tax credit = max wage base * max tax rate; since max wage base is defined, must maximize tax rate;
            var maxRate:Number = Math.max(wotcCategory.taxRate0, wotcCategory.taxRate1, wotcCategory.taxRate2);
            if (wotcCategory.taxRate0 == maxRate) {
                return wotcCategory.maxCredit / wotcCategory.taxRate0 / mWage;
            } else if (wotcCategory.taxRate1 == maxRate) {
                return Math.max(120, wotcCategory.maxCredit / wotcCategory.taxRate1 / mWage );
            } else {
                return Math.max(400, wotcCategory.maxCredit / wotcCategory.taxRate2 / mWage );
            }
        }

        private function getMaxCreditDays(wotcCategory:WOTCCategory):Number {
            var hours:Number = getMaxCreditHours(wotcCategory);
            var weeks:Number = hours / mHoursPerWeek;
            return weeks * 7;
        }

        private function get isEmployeeHireRetentionEligible():Boolean {
            return TaxCreditsModel.instance.employee.unemployed60Set && TaxCreditsModel.instance.employee.unemployed60;
        }

    }
}
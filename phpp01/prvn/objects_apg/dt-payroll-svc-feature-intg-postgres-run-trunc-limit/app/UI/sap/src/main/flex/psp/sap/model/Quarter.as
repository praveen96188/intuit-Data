/**
  * User: dweinberg
 * Date: 10/30/11
 * Time: 11:38 PM
 */
package psp.sap.model {
    import mx.collections.ArrayCollection;

    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarter")]
    public class Quarter {

        public static var EMPTY_QUARTER:Quarter = new Quarter(0, 0);

        public var year:int;
        public var quarter:int;

        public function Quarter(year:int=0, quarter:int=0) {
            this.year = year;
            this.quarter = quarter;
        }

        [Bindable("propertyChange")]
        public function get label():String {
            if (year == 0 || quarter == 0) {
                return "";
            }
            return year + " Q" + quarter;
        }

        //2011 Q2 through next quarter
        [ArrayElementType("psp.sap.model.Quarter")]
        public static function validTaxQuarters():ArrayCollection {
            var maxYear:int = SAP.instance.PSPDate.fullYear;
            var maxQuarter:int = Math.ceil((SAP.instance.PSPDate.month + 1)/ 3.) + 1;
            if (maxQuarter == 5) {
                maxYear++;
                maxQuarter = 1;
            }

            var quarters:ArrayCollection = new ArrayCollection();
            quarters.addItem(EMPTY_QUARTER);

            for (var currentYear:int = maxYear; currentYear >= 2011; currentYear--) {
                for (var currentQuarter:int = currentYear == maxYear ? maxQuarter : 4; currentQuarter >= (currentYear == 2011 ? 2 : 1); currentQuarter--) {
                    quarters.addItem(new Quarter(currentYear, currentQuarter));
                }
            }

            return quarters;


        }

        public function isEmpty():Boolean {
            return this == EMPTY_QUARTER;
        }

        public function isCurrentQuarter():Boolean {
            var currentYear:int = SAP.instance.PSPDate.fullYear;
            var currentQuarter:int = Math.ceil((SAP.instance.PSPDate.month + 1)/ 3.);
            return year == currentYear && quarter == currentQuarter;
        }

        public function isAfter(other:Quarter):Boolean {
            return year > other.year || (this.year == other.year && this.quarter > other.quarter);
        }

        //noinspection JSUnusedLocalSymbols
        public static function sortCompareFunction(a:Quarter, b:Quarter, fields:Array = null):int {
            if (a.isEmpty()) {
                return -1;
            }
            if (b.isEmpty()) {
                return 1;
            }

            if (a.year < b.year) {
                return -1;
            }
            if (a.year > b.year) {
                return 1;
            }
            if (a.quarter < b.quarter) {
                return -1;
            }
            if (a.quarter > b.quarter) {
                return 1;
            }
            return 0;
        }
    }


}

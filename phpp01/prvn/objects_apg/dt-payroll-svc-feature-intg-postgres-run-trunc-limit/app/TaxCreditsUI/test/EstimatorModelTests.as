package {
    import flexunit.framework.TestCase;

    import mx.formatters.CurrencyFormatter;

    import mx.utils.ObjectUtil;

    import psp.taxcredits.dto.WOTCCategory;
    import psp.taxcredits.model.EstimatorModel;
    import psp.taxcredits.model.ModelUtils;
    import psp.taxcredits.model.TaxCreditsModel;

    public class EstimatorModelTests extends TestCase {

        private static var currencyFormatter:CurrencyFormatter = new CurrencyFormatter();

        override public function setUp():void {
            super.setUp();
            currencyFormatter.precision = 2;
            TaxCreditsModel.instance.pspDate = new Date(2010, 1, 15);
            TaxCreditsModel.instance.initialized = true;
		}

        public function testDateMath():void {

            var d:Date = new Date(2010, 1, 1);
            var d2:Date;
            var days:int;
            var i:int;

            for (i=1; i < 27; i++) {
                d2 = ModelUtils.addDays(d, i);
                assertEquals(2010, d2.fullYear);
                assertEquals(1, d2.month);
                assertEquals(1+i, d2.date);

                days = ModelUtils.dayDifference(d, d2);
                assertEquals(i, days);
            }

            for (i=28; i < 58; i++) {
                d2 = ModelUtils.addDays(d, i);
                assertEquals(2010, d2.fullYear);
                assertEquals(2, d2.month);
                assertEquals(1+i-28, d2.date);

                days = ModelUtils.dayDifference(d, d2);
                assertEquals(i, days);
            }
        }

        public function testMaxDateEarliestToGetMaxCredit():void {
            TaxCreditsModel.instance.employee.startDate = new Date(2010, 1, 1);

            var estimatorModel:EstimatorModel = new EstimatorModel(TaxCreditsModel.instance);
            estimatorModel.wotcCategory = getCategory1();
            for (var i:int = 1; i<100; i++) {
                for (var j:int =1; j<100; j++) {
                    estimatorModel.hoursPerWeekString = i.toString();
                    estimatorModel.wageString = j.toString();
                    estimatorModel.useMaximumDate = true;

                    assertEquals(2400, estimatorModel.estimate);

                    estimatorModel.useMaximumDate = false;
                    estimatorModel.employedUntil = estimatorModel.maxCreditDate;

                    assertEquals(2400, estimatorModel.estimate);

                    estimatorModel.employedUntil = ModelUtils.addDays(estimatorModel.maxCreditDate, -1);

                    assertTrue(estimatorModel.estimate < 2400);

                    estimatorModel.useMaximumDate = true;

                    assertEquals(2400, estimatorModel.estimate);
                }
            }
            

        }

        public function test1YrEstimates():void {
            TaxCreditsModel.instance.employee.startDate = new Date(2010, 1, 10);

            var estimatorModel:EstimatorModel = new EstimatorModel(TaxCreditsModel.instance);
            estimatorModel.wotcCategory = getCategory1();
            estimatorModel.hoursPerWeekString = "30";

            estimatorModel.wageString = "18";
            estimatorModel.employedUntil = new Date(2010,1,11);
            estimatorModel.calculateEstimate();
            assertEquals("<120hrs so no credit",0, estimatorModel.estimate);

            estimatorModel.wageString = "18";
            estimatorModel.employedUntil = new Date(2010,2,9);
            estimatorModel.calculateEstimate();
            assertWithinDollar(540.00, estimatorModel.estimate);

            estimatorModel.wageString = "10";
            estimatorModel.employedUntil = new Date(2010,5,2);
            estimatorModel.calculateEstimate();
            assertWithinDollar(1937.14, estimatorModel.estimate);

            estimatorModel.wageString = "18";
            estimatorModel.employedUntil = new Date(2010,5,2);
            estimatorModel.calculateEstimate();
            assertEquals("Maximum credit", 2400, estimatorModel.estimate);
        }
//
//        public function test2YrEstimates():void {
//            TaxCreditsModel.instance.employee.hireDate = new Date(2010, 1, 10);
//
//            var estimatorModel:EstimatorModel = new EstimatorModel();
//            estimatorModel.wotcCategory = getCategory1();
//            estimatorModel.wotcCategorySecondYear = getCategory2();
//            estimatorModel.mHoursPerWeek = 30;
//
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2010,1,11);
//            estimatorModel.calculateEstimate();
//            assertEquals("<120hrs so no credit",0, estimatorModel.estimate);
//
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2010,2,9);
//            estimatorModel.calculateEstimate();
//            assertWithinDollar(540.00, estimatorModel.estimate);
//
//            estimatorModel.mWage = 10;
//            estimatorModel.mEmployedUntil = new Date(2010,5,2);
//            estimatorModel.calculateEstimate();
//            assertWithinDollar(1937.14, estimatorModel.estimate);
//
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2010,5,2);
//            estimatorModel.calculateEstimate();
//            assertEquals("Maximum credit", 2400, estimatorModel.estimate);
//
//            //add a year
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2011,1,11);
//            estimatorModel.calculateEstimate();
//            assertWithinDollar(2477.14, estimatorModel.estimate);
//
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2011,2,9);
//            estimatorModel.calculateEstimate();
//            assertWithinDollar(3480.00, estimatorModel.estimate);
//
//            estimatorModel.mWage = 18;
//            estimatorModel.mEmployedUntil = new Date(2011,6,9);
//            estimatorModel.calculateEstimate();
//            assertEquals("Maximum 2 year credit", 7400, estimatorModel.estimate);
//
//        }
//
//        public function test1YrMaximumDate():void {
//            TaxCreditsModel.instance.employee.hireDate = new Date(2010, 0, 1);
//
//            var estimatorModel:EstimatorModel = new EstimatorModel();
//            estimatorModel.wotcCategory = getCategory1();
//            estimatorModel.mHoursPerWeek = 40;
//
//            estimatorModel.mWage = 59.9;
//            estimatorModel.calculateMaxCreditDate();
//            assertObjectEquals(new Date(2010,0,30), estimatorModel.maxCreditDate);
//
//            estimatorModel.mWage = 8;
//            estimatorModel.calculateMaxCreditDate();
//
//            assertObjectEquals(new Date(2010,4,13), estimatorModel.maxCreditDate);
//        }
//
//        public function test2YrMaximumDate():void {
//            TaxCreditsModel.instance.employee.hireDate = new Date(2010, 0, 1);
//
//            var estimatorModel:EstimatorModel = new EstimatorModel();
//            estimatorModel.wotcCategory = getCategory1();
//            estimatorModel.wotcCategorySecondYear = getCategory2();
//            estimatorModel.mHoursPerWeek = 40;
//
//            estimatorModel.mWage = 20;
//            estimatorModel.calculateMaxCreditDate();
//            assertObjectEquals(new Date(2011,2,30), estimatorModel.maxCreditDate);
//
//
//        }

        private function assertWithinDollar(expectedValue:Number,  actualValue:Number):void {
            assertTrue("expected "+  currencyFormatter.format(expectedValue - 1) + " < value > " + currencyFormatter.format(expectedValue + 1) + " but was " + currencyFormatter.format(actualValue)
                    , Math.abs(expectedValue - actualValue) < 1.00);
        } 

        private function assertObjectEquals(expected:Object, actual:Object):void {
            if (ObjectUtil.compare(expected, actual) != 0) {
                assertEquals(expected, actual);
            }

        }

        private function getCategory1():WOTCCategory {
            var category:WOTCCategory = new WOTCCategory();
            category.category = "Category1";
            category.maxCredit = 2400;
            category.wageBase = 6000;
            category.taxRate0 = 0;
            category.taxRate1 = .25;
            category.taxRate2 = .40;
            return category;
        }

        private function getCategory2():WOTCCategory {
            var category:WOTCCategory = new WOTCCategory();
            category.category = "Category2";
            category.maxCredit = 5000;
            category.wageBase = 10000;
            category.taxRate0 = .5;
            category.taxRate1 = .5;
            category.taxRate2 = .5;
            return category;
        }

    }
}
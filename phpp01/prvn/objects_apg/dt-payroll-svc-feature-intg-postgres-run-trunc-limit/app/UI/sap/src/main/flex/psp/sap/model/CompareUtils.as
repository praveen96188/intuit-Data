package psp.sap.model {
    import mx.utils.ObjectUtil;

    public class CompareUtils {
        public static function comparePaymentTemplate(a:Object, b:Object):int {
            if (String(a) == ("")) {
                return -1;
            }
            if (String(b) == "") {
                return 1;
            }
            if (String(a.paymentTemplateName).indexOf("IRS") > -1 && String(b.paymentTemplateName).indexOf("IRS") > -1) {
                if (String(a.paymentTemplateName).indexOf("941") > -1) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
            return compareIRSOnTop(a.paymentTemplateCd, b.paymentTemplateCd);
        }

        public static function compareAgencies(a:Agency, b:Agency):int {
            return compareIRSOnTop(a.agencyId, b.agencyId);
        }

        public static function compareAgencyInfos(a:AgencyInfoDTO, b:AgencyInfoDTO):int {
            return compareAgencies(a.agency, b.agency);
        }

        public static function compareAgenciesTransactions(a:Object, b:Object):int {
            return compareIRSOnTop(a.agencyAbbreviation, b.agencyAbbreviation);
        }

        private static function compareIRSOnTop(a:String, b:String):int {
            if (a == ("")) {
                return -1;
            }
            if (b == "") {
                return 1;
            }
            if (a.indexOf("ALL") > -1) {
                return -1;
            }
            if (b.indexOf("ALL") > -1) {
                return 1;
            }
            if (a.indexOf("IRS") > -1) {
                return -1;
            }
            if (b.indexOf("IRS") > -1) {
                return 1;
            }
            return ObjectUtil.stringCompare(a, b, true);
        }

        public static function compareStringYear(a:Object, b:Object):int {
            var numA:Number = parseInt(String(a.year));
            var numB:Number = parseInt(String(b.year));
            return ObjectUtil.numericCompare(numA, numB);
        }

    }
}
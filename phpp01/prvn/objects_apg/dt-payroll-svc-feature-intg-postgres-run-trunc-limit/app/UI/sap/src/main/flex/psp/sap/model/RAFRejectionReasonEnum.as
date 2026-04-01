package psp.sap.model {
    import mx.collections.ArrayCollection;

    [Bindable]
    public class RAFRejectionReasonEnum {
        public static const MISSING_SIGNATURE:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("MISSING_SIGNATURE", "Missing Signature");
        public static const UNREADABLE_8655:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("UNREADABLE_8655", "Unreadable 8655");
        public static const MISSING_DATES:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("MISSING_DATES", "Missing Dates");
        public static const MISSING_8655:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("MISSING_8655", "Missing 8655");

        public static const OTHER:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("OTHER", "Other");
        public static const BLANK:RAFRejectionReasonEnum = new RAFRejectionReasonEnum("BLANK", "");

        public static const values:ArrayCollection = new ArrayCollection([MISSING_SIGNATURE, UNREADABLE_8655, MISSING_DATES, MISSING_8655, OTHER]);
        public static const valuesWithBlank:ArrayCollection = new ArrayCollection([BLANK, MISSING_SIGNATURE, UNREADABLE_8655, MISSING_DATES, MISSING_8655, OTHER]);

        private var mCode:String;
        private var mLabel:String;

        public function RAFRejectionReasonEnum(code:String, label:String = null) {
            mCode = code;
            mLabel = (label != null ? label : code);
        }

        public function get code():String {
            return mCode;
        }

        public function get label():String {
            return mLabel;
        }

        public function toString():String {
            return label;
        }

        public static function fromCode(code:String):RAFRejectionReasonEnum {
            for each (var enum:RAFRejectionReasonEnum   in values) {
                if (enum.code == code) {
                    return enum;
                }
            }

            return null;
        }

    }
}
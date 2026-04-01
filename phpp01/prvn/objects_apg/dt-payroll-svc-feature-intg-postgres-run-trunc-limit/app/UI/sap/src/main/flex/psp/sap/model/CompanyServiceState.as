/**
 * User: dweinberg
 * Date: 9/7/11
 * Time: 3:29 PM
 */
package psp.sap.model {
    public class CompanyServiceState {

        public static const DIYOnly:CompanyServiceState = new CompanyServiceState("DIYOnly");
        public static const DIYDD:CompanyServiceState = new CompanyServiceState("DIYDD");
        public static const AssistedPending:CompanyServiceState = new CompanyServiceState("AssistedPending");
        public static const AssistedActive:CompanyServiceState = new CompanyServiceState("AssistedActive");

        public static const values:Array = [DIYOnly, DIYDD, AssistedPending, AssistedActive];

        private var mCode:String;
        private var mLabel:String;

        public function CompanyServiceState(code:String, label:String = null)
        {
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

        public static function fromLabel(label:String):CompanyServiceState {
              for each (var enum:CompanyServiceState in values) {
                    if (enum.label == label)
                          return enum;
              }

              return null;
        }

        public static function valueOf(value:String):CompanyServiceState {
            for each (var enum:CompanyServiceState in values) {
                if (enum.code == value)
                    return enum;
            }

            return null;
        }

    }
}

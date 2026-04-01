package psp.taxcredits.dto {
    import mx.collections.ArrayCollection;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.taxcredits.dto.ApplicationInfo")]
    public class ApplicationInfo {

        private var numbers:Array=["", "", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen"];

        [ArrayElementType("mx.collections.ArrayCollection")]
        public var proofDocumentGroups:ArrayCollection;
        public var password:String;

        [Transient]
        public function get numberRequiredText():String {
            return proofDocumentGroups.length == 1 ? "" : "<B>" + String(numbers[proofDocumentGroups.length]).toUpperCase() + "</B> ";
        }

        [Transient]
        public function get proofDocsText():String {
            var text:String = "";

            for each (var group:ArrayCollection in proofDocumentGroups) {
                text+= "<img src='psp/taxcredits/view/assets/longArrowRight.png'>One of the following:" + "<br>";
                for each (var option:String in group) {
                    text+= "\t<img src='psp/taxcredits/view/assets/t_arrow_right.png'>" + option + "<br>";
                }
            }

            return text;
        }
    }
}
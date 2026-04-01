package psp.taxcredits.model {
    [Bindable]
    public class Benefits {

        public var name:String;
        public var city:String;
        public var state:String;

        public function get cityState():String {
            return city + ", " + state;
        }

    }
}
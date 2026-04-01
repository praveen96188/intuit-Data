package psp.taxcredits.view.controls {
    import mx.controls.Image;

    public class CheckImage extends Image {

        [Bindable]
        [Embed("../assets/Grey_check_24x24.png")]
        private var incomplete:Class;

        [Bindable]
        [Embed("../assets/GreenCheck_24x24.png")]
        private var complete:Class;

        [Bindable]
        [Embed("../assets/x_Error_Red_24x24.png")]
        private var error:Class;



        public function CheckImage() {
            super();
            this.source = incomplete;
        }

        public function checkIncomplete():void {
            this.source = incomplete;
        }

        public function checkComplete():void {
            this.source = complete;
        }

        public function checkError():void {
            this.source = error;   
        }

        public function set checkType(value:String):void {
            if (value == "incomplete") {
                checkIncomplete();
            } else if (value == "complete") {
                checkComplete();
            } else if (value == "error") {
                checkError();
            }
        }

    }

}
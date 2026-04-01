/**
 * Created by IntelliJ IDEA.
 * User: dweinberg
 * Date: 1/31/11
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.view {
    import mx.controls.Label;

    public class ErrorLabel extends Label
    {
        public function ErrorLabel() {
            super();
        }

        private var lastColor:String;
        private var lastWeight:String;
        override public function set errorString(value:String):void {
            if (value == "") {
                this.setStyle("color", lastColor);
                this.setStyle("fontWeight", lastWeight);
            } else {
                if (errorString == "") {
                    lastColor = this.getStyle("color");
                    lastWeight = this.getStyle("fontWeight");
                }
                this.setStyle("color", "red");
                this.setStyle("fontWeight", "bold");
            }
            super.errorString = value;
        }
    }
}

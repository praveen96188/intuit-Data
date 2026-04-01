/**
 * User: dweinberg
 * Date: 3/19/12
 * Time: 2:29 PM
 */
package psp.sap.validators {
    import mx.core.UIComponent;
    import mx.validators.StringValidator;

    public class SAPStringValidator extends StringValidator {
        override public function set enabled(value:Boolean):void {
            super.enabled = value;
            clearListener();
        }

        public function clearListener():void {
            if (!required || !enabled) {
                if (listener != null && listener is UIComponent) {
                    UIComponent(listener).errorString = "";
                }
            } else {
                validate();
            }
        }
    }
}

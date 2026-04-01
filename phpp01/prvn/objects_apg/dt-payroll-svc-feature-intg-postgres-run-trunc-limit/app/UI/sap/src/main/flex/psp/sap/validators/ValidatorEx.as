/**
 * User: dweinberg
 * Date: 3/19/12
 * Time: 2:44 PM
 */
package psp.sap.validators {
    import mx.core.UIComponent;
    import mx.validators.Validator;

    public class ValidatorEx extends Validator {
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

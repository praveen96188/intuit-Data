/**
 * User: dweinberg
 * Date: 2/2/12
 * Time: 12:35 PM
 */
package psp.sap.view.controls {
    import mx.controls.RadioButtonGroup;

    public class SAPRadioButtonGroup extends RadioButtonGroup{

        override public function set selectedValue(value:Object):void {
            super.selectedValue = value;
            if (value == null) {
                selection = null;
            }
        }

    }
}

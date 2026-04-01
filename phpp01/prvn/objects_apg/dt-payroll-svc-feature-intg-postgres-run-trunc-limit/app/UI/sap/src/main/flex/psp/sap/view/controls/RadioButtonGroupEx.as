/**
 * Created by IntelliJ IDEA.
 * User: kimberlyp876
 * Date: 8/3/12
 * Time: 4:24 PM
  */
package psp.sap.view.controls {
    import mx.controls.RadioButtonGroup;

    public class RadioButtonGroupEx extends RadioButtonGroup {

    //setting selectedValue to null does not reset the selected radio button
    override public function set selectedValue(value:Object):void
    {
        super.selectedValue = value;

        if(value == null)
        {
            selection = null;
        }

    }

    }
}

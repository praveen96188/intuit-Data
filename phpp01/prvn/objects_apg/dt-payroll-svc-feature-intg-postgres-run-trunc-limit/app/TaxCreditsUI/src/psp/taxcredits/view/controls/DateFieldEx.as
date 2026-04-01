package psp.taxcredits.view.controls {
    import flash.events.FocusEvent;

    import mx.controls.DateField;

    public class DateFieldEx extends DateField {

        override protected function focusOutHandler(event:FocusEvent):void
        {
            //flex decides that when you focus out it should parse the text, which is stupid when it's not editable
            //and in fact it can't parse the text if you are using a custom label function
            if (editable) {
                super.focusOutHandler(event);
            }


        }

    }
}
package psp.taxcredits.view.controls {
    import mx.controls.ComboBox;

    public class ComboBoxExt extends ComboBox
    {
        public function ComboBoxExt()
        {
            super();
        }

        override public function set dataProvider(value:Object):void
        {
            var tempObject:Object = selectedItem;

            super.dataProvider = value;

            selectedItem = tempObject;
        }

    }



}
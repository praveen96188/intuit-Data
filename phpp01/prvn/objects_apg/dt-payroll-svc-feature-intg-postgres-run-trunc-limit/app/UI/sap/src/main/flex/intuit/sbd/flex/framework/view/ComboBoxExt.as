
package intuit.sbd.flex.framework.view
{
    import mx.collections.IList;
    import mx.controls.ComboBox;
    import mx.utils.ObjectUtil;

    public class ComboBoxExt extends ComboBox
    {

        private var waitingDP:Object = null;

        public function ComboBoxExt()
        {
            super();
        }

        override public function set selectedItem(value:Object):void {
            if ((!dataProvider || (dataProvider is IList && (dataProvider as IList).length == 0)) && value) {
                waitingDP = value;
            } else {
                super.selectedItem = value;
            }
        }

        override public function set dataProvider(value:Object):void
        {
            var tempObject:Object = selectedItem;

            super.dataProvider = value;

            if (waitingDP != null) {
                selectedItem = waitingDP;
                waitingDP = null;
            } else {
                if (tempObject) {
                    selectedItem = tempObject;
                }                    
            }

        }
    }
}



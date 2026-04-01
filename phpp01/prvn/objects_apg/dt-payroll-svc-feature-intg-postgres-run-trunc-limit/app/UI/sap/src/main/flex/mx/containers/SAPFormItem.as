package mx.containers {
    public class SAPFormItem extends FormItem {
        public function SAPFormItem() {
            super();
        }

        // This is a hack. The form items keep the space of all labels even though a label may not be showing.
        override internal function getPreferredLabelWidth():Number
        {
            if(visible) {
                return super.getPreferredLabelWidth();
            }

            return 0;
        }
    }
}
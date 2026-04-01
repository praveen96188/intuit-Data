package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    public class BackingPropertyCollection extends ArrayCollection {
        /**
         * override to allow searching based on view model property name;
         * if item is a String assume it represents the ViewModel PropertyName
         */
        override public function getItemIndex(item:Object):int {
            //
            if (item is String) {
                for (var i:int = 0; i < this.length; i++) {
                    var backingProperty:BackingPropertyMetaData = getItemAt(i) as BackingPropertyMetaData;
                    if (backingProperty.viewModelPropertyName == String(item))
                        return i;
                }
                return -1;
            }

            return super.getItemIndex(item);
        }

        public function getBackingProperty(viewModelPropertyName:String):BackingPropertyMetaData {
            var i:int = getItemIndex(viewModelPropertyName);
            if (i != -1)
                return getItemAt(i) as BackingPropertyMetaData;

            return null;
        }
    }

}
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    public class BackingPropertyMetaData {
        public var viewModelPropertyName:String;
        public var viewModelPropertyType:String;
        public var trackHasChanged:Boolean;
        public var contextProperty:Boolean;
        public var requiredProperty:Boolean;
        public var linkableProperty:Boolean;
        public var recursiveProperty:Boolean;
        [ArrayElementType("String")]
        public var recursiveNoHasChangeTrackedProperties:ArrayCollection;
    }
}
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    import org.as3commons.reflect.IMember;
    import org.as3commons.reflect.MetaData;
    import org.as3commons.reflect.MetaDataArgument;
    import org.as3commons.reflect.Type;

    public class ViewModelMetaData {
        private var mUpdateFunction:Function;
        /**
         * Map<FullyQualifiedTypeName,ViewModelMetaData>
         */
        public static var MetaDataCache:Object = new Object();

        /**
         * Fully qualified type name of the ViewModel
         */
        public var fullyQualifiedTypeName:String;

        /**
         * All vars and properties in the types that are attributed with the [BackingProperty] metatag.
         */
        [ArrayElementType("psp.sap.viewmodel.BackingPropertyMetaData")]
        public var backingProperties:BackingPropertyCollection = new BackingPropertyCollection();



        public function ViewModelMetaData(viewModel:AbstractPartViewModel, updateFunction:Function) {
            mUpdateFunction = updateFunction;
            var type:Type = Type.forInstance(viewModel);

            if (MetaDataCache[type.name] != null) {
                this.backingProperties = (MetaDataCache[fullyQualifiedTypeName] as ViewModelMetaData).backingProperties;
                return;
            }

            addBackingProperties(viewModel, type.accessors);
            addBackingProperties(viewModel, type.variables);

            MetaDataCache[fullyQualifiedTypeName] = this;
        }

        private function addBackingProperties(viewModel:AbstractPartViewModel, attributedProperties:Array):void {
            for (var i:int = 0; i < attributedProperties.length; i++) {
                var metadataTags:Array = attributedProperties[i].metaData as Array;
                if (metadataTags != null) {
                    for(var j:int = 0; j < metadataTags.length; j++) {
                        if(metadataTags[j].name == "BackingProperty"){
                            var backingProperty:BackingPropertyMetaData = new BackingPropertyMetaData();

                            backingProperty.viewModelPropertyName = attributedProperties[i].name;
                            backingProperty.viewModelPropertyType = Type(attributedProperties[i].type).fullName;
                            backingProperty.trackHasChanged = isChangeTracked(metadataTags[j]);
                            backingProperty.contextProperty = isContext(metadataTags[j]);
                            backingProperty.requiredProperty = isRequired(metadataTags[j]);
                            backingProperty.linkableProperty = isLinkable(metadataTags[j]);
                            backingProperty.recursiveProperty = isRecursive(metadataTags[j]);
                            if (backingProperty.recursiveProperty) {
                                backingProperty.recursiveNoHasChangeTrackedProperties = new ArrayCollection();
                                var arrayElementType:String = getArrayElementType(metadataTags);
                                if (arrayElementType != null) {
                                    addRecursiveBackingProperties(backingProperty, Type.forName(arrayElementType).accessors);
                                    addRecursiveBackingProperties(backingProperty, Type.forName(arrayElementType).variables);
                                } //todo else if not a collection, etc.
                            }
                            backingProperties.addItem(backingProperty);
                        }
                    }
                }
            }
        }

        private function getArrayElementType(metadataTags:Array):String {
            for(var j:int = 0; j < metadataTags.length; j++) {
                if (metadataTags[j].name == "ArrayElementType") {
                    return MetaData(metadataTags[j]).getArgument("").value;
                }
            }
            return null;
        }

        private function addRecursiveBackingProperties(backingProperty:BackingPropertyMetaData, attributedProperties:Array):void {
            for (var i:int = 0; i < attributedProperties.length; i++) {
                var metadataTags:Array = attributedProperties[i].metaData as Array;
                if (metadataTags != null) {
                    for(var j:int = 0; j < metadataTags.length; j++)
                        if(metadataTags[j].name == "BackingProperty"){
                            if (!isChangeTracked(metadataTags[i])) {
                                backingProperty.recursiveNoHasChangeTrackedProperties.addItem(IMember(attributedProperties[i]).name);
                            }
                        }
                }
            }
        }

        private function isChangeTracked(backingPropertyMetaTag:MetaData):Boolean {
            if(backingPropertyMetaTag == null){
                return false;
            }

            if(backingPropertyMetaTag.arguments.length > 0) {
                var metadataArguments:Array = backingPropertyMetaTag.arguments;
                for(var i:int = 0; i < metadataArguments.length; i++){
                    if(metadataArguments[i].key == "hasChanged" && metadataArguments[i].value == "false"){
                        return false;
                    }
                }
            }
            return true;
        }

        private function isContext(backingPropertyMetaTag:MetaData):Boolean {
            if(backingPropertyMetaTag == null){
                return false;
            }

            if(backingPropertyMetaTag.arguments.length > 0) {
                var metadataArguments:Array = backingPropertyMetaTag.arguments;
                for(var i:int = 0; i < metadataArguments.length; i++){
                    if(metadataArguments[i].key == "context" && metadataArguments[i].value == "true"){
                        return true;
                    }
                }
            }
            return false;
        }

        private function isRequired(backingPropertyMetaTag:MetaData):Boolean {
            if(backingPropertyMetaTag == null){
                return false;
            }

            if(backingPropertyMetaTag.arguments.length > 0) {
                var metadataArguments:Array = backingPropertyMetaTag.arguments;
                for(var i:int = 0; i < metadataArguments.length; i++){
                    if(metadataArguments[i].key == "required" && metadataArguments[i].value == "false"){
                        return false;
                    }
                }
            }
            return true;
        }

        private function isLinkable(backingPropertyMetaTag:MetaData):Boolean {
            if (backingPropertyMetaTag == null) {
                return true;
            }

            var argument:MetaDataArgument = backingPropertyMetaTag.getArgument("linkable");
            if (argument == null) {
                return true;
            }
            return argument.value != "false";
        }

        private function isRecursive(backingPropertyMetaTag:MetaData):Boolean {
            if (backingPropertyMetaTag == null) {
                return false;
            }

            var argument:MetaDataArgument = backingPropertyMetaTag.getArgument("recursive");
            if (argument == null) {
                return false;
            }
            return argument.value == "true";
        }
    }
}
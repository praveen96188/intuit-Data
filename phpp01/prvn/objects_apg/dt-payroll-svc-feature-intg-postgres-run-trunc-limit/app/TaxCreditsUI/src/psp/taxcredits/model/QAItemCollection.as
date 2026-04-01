package psp.taxcredits.model {
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.resources.ResourceManager;

    public class QAItemCollection extends ArrayCollection{


        public function QAItemCollection(source:Array = null) {
            super(source);
        }

        public static function fromXML(xml:XML):QAItemCollection {
            var collection:QAItemCollection = new QAItemCollection();

            for each (var xmlQaItem:XML in xml.QAItem) {
                var newQaItem:QAItem = new QAItem();
                newQaItem.question = xmlQaItem.Question;
                newQaItem.answer = modelReplace(localeReplace(xmlQaItem.Answer, newQaItem));

                collection.addItem(newQaItem);
            }

            return collection;
        }

        //will only do first one
        private static function localeReplace(text:String, item:QAItem):String {
            var regexp:RegExp = new RegExp(/\$\{(\w+),(\w+)\}/);
            var groups:Array = text.match(regexp);
            if (groups) {
                var resourcePackage:String = groups[1];
                var resourceName:String = groups[2];
                var replacementText:String = ResourceManager.getInstance().getString(resourcePackage, resourceName);
                ResourceManager.getInstance().addEventListener(Event.CHANGE, function(e:Event):void {
                    var newReplacement:String = ResourceManager.getInstance().getString(resourcePackage, resourceName);
                    item.answer = text.replace(/\$\{\w+,\w+\}/, newReplacement);
                });
                return text.replace(/\$\{\w+,\w+\}/, replacementText);
            }
            return text;
        }

        private static function modelReplace(text:String):String {
            var regexp:RegExp = new RegExp(/\$\{(\w+)\}/);
            var groups:Array = text.match(regexp);
            if (groups) {
                var modelProperty:String = groups[1];
                var replacementText:String = TaxCreditsModel.instance[modelProperty];
                return text.replace(/\$\{\w+\}/, replacementText);
            }
            return text;
        }

    }
}
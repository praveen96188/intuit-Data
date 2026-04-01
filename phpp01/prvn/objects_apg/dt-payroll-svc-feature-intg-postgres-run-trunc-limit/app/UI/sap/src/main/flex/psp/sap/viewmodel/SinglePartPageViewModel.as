package psp.sap.viewmodel {
    import mx.binding.utils.BindingUtils;

    import mx.controls.Label;

    import mx.formatters.DateFormatter;

    import org.as3commons.reflect.Method;
    import org.as3commons.reflect.Type;

    import psp.sap.application.SAP;
    import psp.sap.swfaddress.SWFAddress;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class SinglePartPageViewModel extends CompositePartViewModel {

        [Bindable] public var pageLabel:String;
        [Bindable] public var breadCrumbLabel:String=null; //default is part's label, though pageLabel might be better in future
        [Bindable] public var maintainCrossTopicHistory:Boolean = false;        

        public var respondingToFragmentChange:Boolean = false;

        public function SinglePartPageViewModel() {
            super();
            bindSaveMessageWithChildren = true;
        }

        override public function createNewChildPartViewModel(partClass:Class, label:String=null):AbstractPartViewModel {
            var vm:AbstractPartViewModel = super.createNewChildPartViewModel(partClass, label);
            vm.addEventListener(ViewModelEvent.CLOSE, function(e:ViewModelEvent):void {
                cancel();
            }, false, 0, false);

            if ("pageLabel" in vm) {
                BindingUtils.bindProperty(this, "pageLabel", vm, "pageLabel");
            }            

            return vm;
        }

        override protected function onActivating():void {
            //it is possible to get to pages without going through the topic (recent event, deep link, etc.)
            //make the breadcrumb display as if the user came from the default topic so that they can get back there
            //otherwise they can't click the topic and get back

            if (inspector && topic && ! inspector.pageHistory.containsLabel(topic.defaultPage.label)) {
                var defaultPageViewModel:SinglePartPageViewModel = SinglePartPageViewModel(inspector.findPart(topic.defaultPage.label));
                if (defaultPageViewModel != this) {
                    inspector.pageHistory.addItemAt(defaultPageViewModel, inspector.pageHistory.length - 1);
                }
            }
        }

        public function get part():AbstractPartViewModel {
            return AbstractPartViewModel(partViewModels.getItemAt(0));
        }

        public function activatePage(activator:Object=null):void {
            part.setActivator(activator);
            this.activate();
        }

        override protected function onSubPartsActivated():void {
            SWFAddress.setTitle("SAP - " + this.part.label);

            inspector.pageHistory.addItem(this);

            //set swf address
            if (respondingToFragmentChange) {
                //if user hits back, don't change URL when activating or forward won't work
                respondingToFragmentChange = false;
            } else {
                SWFAddress.setValue(generatePageFragment());
            }

        }

        public function generatePageFragment():String {
            var explorerLabel:String = formatAsURL(this.inspector.explorer.label);
            var inspectorLabel:String = formatAsURL(this.inspector.persistentLabel);
            var topicLabel:String = formatAsURL(this.topic.label);
            var pageLabel:String = formatAsURL(this.part.label);

            var activators:String = createCurrentActivatorString();

            return "/" + explorerLabel + "/" + inspectorLabel + "/" + topicLabel + "/" + pageLabel + "/" + activators + "/";
        }

        public function createCurrentActivatorString():String {
            var activators:String = "";
            for each (var backingProperty:BackingPropertyMetaData in  part.viewModelMetaData.backingProperties) {
                if (backingProperty.contextProperty && backingProperty.linkableProperty) {
                    activators += backingProperty.viewModelPropertyName + "=" + formatAsURL(part.serializeActivatorValue(part[backingProperty.viewModelPropertyName])) + "&";
                }
            }
            return activators.substr(0, activators.length-1);
        }

        private function formatAsURL(t:String):String {
            return t.replace(/-/g,"+").replace(/ /g,"-").replace(/\//g,"*");
        }
    }
}
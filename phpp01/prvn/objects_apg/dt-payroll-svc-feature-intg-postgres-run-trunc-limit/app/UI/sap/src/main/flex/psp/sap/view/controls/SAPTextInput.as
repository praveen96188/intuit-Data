package psp.sap.view.controls {
    import flash.events.FocusEvent;

    import mx.binding.utils.BindingUtils;
    import mx.controls.TextInput;
    import mx.events.FlexEvent;
    import mx.formatters.NumberFormatter;
    import mx.validators.Validator;

    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class SAPTextInput extends TextInput {
        private var mViewModel:AbstractPartViewModel;
        private var mDataField:String;
        private var mCrawlParents:Boolean = true;
        private var mNumberFormatter:NumberFormatter;

        [Bindable] public var isCurrency:Boolean = false;

        public function SAPTextInput() {
            super();
            addEventListener(FlexEvent.CREATION_COMPLETE, onCreationComplete, false, 0, true);
        }

        private function onCreationComplete(event:FlexEvent):void {
            if(mCrawlParents){
                bindViewModel(this.parent);
            }
            if(isCurrency) {
                mNumberFormatter = new NumberFormatter();
                mNumberFormatter.useThousandsSeparator = false;
                mNumberFormatter.precision = 2;
                addEventListener(FocusEvent.FOCUS_OUT, onFocusOut, false, 0, true);
            }
        }

        private function bindViewModel(parent:Object):void {
            if(parent != null && dataField != null) {
                if("viewModel" in parent){
                    setInternalViewModel(parent.viewModel);
                    BindingUtils.bindSetter(setInternalViewModel, parent, "viewModel");
                }
                else {
                    bindViewModel(parent.parent);
                }
            }
        }

        private function onFocusOut(e:FocusEvent):void {
            if (text != "" && errorString == "") {
                text = mNumberFormatter.format(text);
            }            
        }

        private function setInternalViewModel(value:AbstractPartViewModel):void {
            if(mViewModel != null){
                mViewModel.removeEventListener(ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, bindControlToTarget);
            }

            mViewModel = value;

            if(mViewModel != null){
                mViewModel.addEventListener(ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, bindControlToTarget, false, 0, true);
                // just in case we already missed the MODEL_DATA_SETUP_COMPLETED event
                if(mViewModel.activationState == ViewModelActivationStateEnum.ACTIVATED){
                    bindControlToTarget(null);
                }
            }

            setupBindings();
        }

        [Bindable]
        public function get viewModel():AbstractPartViewModel {
            return mViewModel;
        }

        public function set viewModel(value:AbstractPartViewModel):void {
            mCrawlParents = false;
            setInternalViewModel(value);
        }

        [Bindable]
        public function get dataField():String {
            return mDataField;
        }

        public function set dataField(value:String):void {
            mDataField = value;
            setupBindings();
        }

        private function setupBindings():void {
            if(viewModel != null && dataField != null){
                setupViewModelBindings();
            }
        }

        private function setupViewModelBindings():void {
            var paths:Array = dataField.split(".");
            BindingUtils.bindProperty(this, "text", viewModel, paths);
        }

        private function bindControlToTarget(event:ViewModelEvent):void {
            var paths:Array = dataField.split(".");
            var targeProperty:String = paths.pop() as String;
            var source:Object = viewModel;
            for each(var path:String in paths){
                source = source[path];
            }

            if(source != null){
                this.text = source[targeProperty];
                BindingUtils.bindProperty(source, targeProperty, this, "text");

                for(var i:int = 0; i < viewModel.validators.length; i++) {
                    var validator:Validator = viewModel.validators[i] as Validator;
                    if(validator.property == targeProperty) {
                        validator.listener = this;
                        validator.validate(source[targeProperty]);
                    }
                }
            }
        }

        public function set validator(validator:Validator):void {
            validator.listener = this;
            validator.validate();
        }

    }
}
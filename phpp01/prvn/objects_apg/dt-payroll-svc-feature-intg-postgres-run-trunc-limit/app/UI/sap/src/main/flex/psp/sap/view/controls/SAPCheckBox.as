package psp.sap.view.controls {
    import mx.binding.utils.BindingUtils;
    import mx.controls.CheckBox;

    import mx.events.FlexEvent;

    import psp.sap.application.enums.ViewModelActivationStateEnum;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class SAPCheckBox extends CheckBox {        
        private var mViewModel:AbstractPartViewModel;
        private var mDataField:String;
        private var mCrawlParents:Boolean = true;

        public function SAPCheckBox() {
            super();
            addEventListener(FlexEvent.CREATION_COMPLETE, onCreationComplete, false, 0, true);
        }

        private function onCreationComplete(event:FlexEvent):void {
            if(mCrawlParents){
                bindViewModel(this.parent);
            }
        }

        private function bindViewModel(parent:Object):void {
            if(parent != null) {
                if("viewModel" in parent){
                    setInternalViewModel(parent.viewModel);
                    BindingUtils.bindSetter(setInternalViewModel, parent, "viewModel");
                }
                else {
                    bindViewModel(parent.parent);
                }
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
            BindingUtils.bindProperty(this, "selected", viewModel, paths);
        }

        private function bindControlToTarget(event:ViewModelEvent):void {
            var paths:Array = dataField.split(".");
            var targeProperty:String = paths.pop() as String;
            var source:Object = viewModel;
            for each(var path:String in paths){
                source = source[path];
            }

            if(source != null){
                this.selected = source[targeProperty];
                BindingUtils.bindProperty(source, targeProperty, this, "selected");
            }
        }
    }
}
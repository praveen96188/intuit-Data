package psp.sap.view.controls
{
    import mx.binding.utils.BindingUtils;
    import mx.containers.HBox;
    import mx.controls.Image;
    import mx.controls.Label;
    import mx.events.FlexEvent;
    import mx.events.ValidationResultEvent;    
    import mx.validators.ValidationResult;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class RequiredLabel extends HBox
    {
        private var mBlackAstrisk:Image;
        private var mRedAstrisk:Image;
        private var mValidator:Validator;
        private var mViewModel:AbstractPartViewModel;
        private var mDataField:String;
        private var mCrawlParents:Boolean = true;

        [Bindable] public var text:String = "";

        public function RequiredLabel()
        {
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
                mViewModel.removeEventListener(ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, setupValidator);
            }

            mViewModel = value;

            if(mViewModel != null){
                mViewModel.addEventListener(ViewModelEvent.MODEL_DATA_SETUP_COMPLETED, setupValidator, false, 0, true);
            }

            setupValidator(null);
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
            setupValidator(null);
        }

        private function internalValidator(value:Validator):void {
            removeValidatorListeners();
            mValidator = value;
            addValidatorListeners();
            if(mValidator != null){
				mValidator.validate(null, false);
			}
        }

        public function set validator(value:Validator):void {
            mCrawlParents = false;
			internalValidator(value);
		}

		private function addValidatorListeners():void {
			if(mValidator != null){
				mValidator.addEventListener(ValidationResultEvent.VALID,
	                                							validationResultHandler, false, 0, true);

	            mValidator.addEventListener(ValidationResultEvent.INVALID,
	                                 							validationResultHandler, false, 0, true);
   			}
		}

		private function removeValidatorListeners():void {
			if(mValidator != null){
				mValidator.removeEventListener(ValidationResultEvent.VALID,
	                                							validationResultHandler);

	            mValidator.removeEventListener(ValidationResultEvent.INVALID,
	                                 							validationResultHandler);
   			}
		}

        private function setupValidator(e:ViewModelEvent):void {
            if(viewModel != null && dataField != null){
                bindValidatorListener();
            }
        }

        private function bindValidatorListener():void {
            for(var i:int = 0; i < viewModel.validators.length; i++) {
                var validator:Validator = viewModel.validators[i] as Validator;
                if(validator.property == dataField) {
                    internalValidator(validator);
                }
            }
        }

        [Bindable] public var errorCode:String = null;
        override public function validationResultHandler(event:ValidationResultEvent):void {
            for each(var result:ValidationResult in event.results){
                if(result.isError && result.errorCode == "requiredField" || result.errorCode == "lowerThanMin" || (errorCode != null && result.errorCode == errorCode) ){
                    // display red
                    mBlackAstrisk.visible = false;
                    mBlackAstrisk.includeInLayout = false;
                    mRedAstrisk.visible = true;
                    mRedAstrisk.includeInLayout = true;
                    // only want the first one
                    return;
                }
            }
            // display black
            mRedAstrisk.visible = false;
            mRedAstrisk.includeInLayout = false;
            mBlackAstrisk.visible = true;
            mBlackAstrisk.includeInLayout = true;
        }

        override protected function  createChildren():void {
            super.createChildren();
            setStyle("horizontalGap", "0");
            var label:Label = new Label();

            // add the label
            label.text = text;
            addChild(label);

            // add the astrisk
            mBlackAstrisk = new Image();
            mBlackAstrisk.setStyle("horizontalAlign", "left");
            mBlackAstrisk.setStyle("verticalAlign", "top");
            mBlackAstrisk.source = SAP.instance.icons.blackAsterisk;
            addChild(mBlackAstrisk);

            // add the astrisk
            mRedAstrisk = new Image();
            mRedAstrisk.toolTip = "This field is required";
            mRedAstrisk.setStyle("horizontalAlign", "left");
            mRedAstrisk.setStyle("verticalAlign", "top");
            mRedAstrisk.source = SAP.instance.icons.redAsterisk;
            mRedAstrisk.visible = false;
            mRedAstrisk.includeInLayout = false;
            addChild(mRedAstrisk);
        }

    }


}
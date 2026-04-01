package psp.taxcredits.view.controls {
    import flash.events.Event;
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.containers.VBox;
    import mx.core.ScrollPolicy;
    import mx.events.FlexEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.ValidationResultEvent;
    import mx.validators.DateValidator;
    import mx.validators.Validator;

    import psp.taxcredits.model.TaxCreditsModel;

    public class InterviewPage extends VBox {

        [Bindable] public var model:TaxCreditsModel = TaxCreditsModel.instance;

        [Bindable] public var titleText:String=null;
        [Bindable] public var tagText:String;
        [Bindable] public var showTag:Boolean=true;

        [Bindable] public var backOffset:int=-1;    //0 == no button
        [Bindable] public var continueOffset:int=1;


        public function InterviewPage() {
            this.horizontalScrollPolicy = ScrollPolicy.OFF;

            addEventListener(FlexEvent.CREATION_COMPLETE, function(e:Event):void {
                readFromModel();
                model.employee.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChange,false);
            }, false, -100);
        }

        public function tearDown():void {
            model.employee.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChange);
        }

        public function onPropertyChange(e:Event):void {
            readFromModel();
        }

        public function readFromModel():void {

        }

        protected var validators:ArrayCollection = new ArrayCollection();

        //validate; show errors if any or else return true
        //override if needed
        public function canContinue():Boolean {
            var invalid:Boolean = false;
            for each (var validator:Validator in validators) {
                if (validator.enabled) {
                    var resultEvent:ValidationResultEvent = validator.validate();
                    if (resultEvent.type != ValidationResultEvent.VALID) {
                        invalid = true;
                    }
                }
            }
            return !invalid;
        }

        //disable them if their containers are disabled
        public function syncValidators():void {
            for each (var validator:Validator in validators) {
                testEnabled(validator);
            }
        }

        private function testEnabled(validator:Validator):void {
            if (validator is DateValidator) {
                var dateValidator:DateValidator = validator as DateValidator;
                dateValidator.enabled = dateValidator.dayListener ? isEnabled(dateValidator.dayListener) : isEnabled(dateValidator.daySource);
                if (dateValidator.dayListener) {
                    (dateValidator.dayListener as EventDispatcher).dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.dayListener["errorString"] = "";
                    (dateValidator.monthListener as EventDispatcher).dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.monthListener["errorString"] = "";
                    (dateValidator.yearListener as EventDispatcher).dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.yearListener["errorString"] = "";
                } else {
                    dateValidator.daySource.dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.daySource["errorString"] = "";
                    dateValidator.monthSource.dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.monthSource["errorString"] = "";
                    dateValidator.yearSource.dispatchEvent(new FlexEvent(FlexEvent.VALID));
                    dateValidator.yearSource["errorString"] = "";                    
                }
            } else {
                validator.enabled = validator.listener ? isEnabled(validator.listener) : isEnabled(validator.source);
                if (!validator.enabled) {
                    if (validator.listener) {
                        validator.listener.dispatchEvent(new FlexEvent(FlexEvent.VALID));
                        validator.listener["errorString"] = "";
                    } else {
                        validator.source.dispatchEvent(new FlexEvent(FlexEvent.VALID));
                        validator.source["errorString"] = "";
                    }
                }
            }
        }

        private function isEnabled(target:Object):Boolean {
            if (target == null) {
                return true;
            } else if ("enabled" in target && ! target["enabled"]) {
                return false;
            } else if ("parent" in target) {
                return isEnabled(target.parent);
            } else {
                return true;
            }
        }

        protected function recurseTest(tempValues:Object, tree:Object):void {
            for each (var obj:Object in tree) {
                if (!tempValues[obj["label"]]) {
                    recurseTrue(tempValues, obj["children"]);
                }
                recurseTest(tempValues, obj["children"]);
            }
        }

        protected function recurseTrue(tempValues:Object, tree:Object):void {
            for each (var obj:Object in tree) {
                tempValues[obj["label"]] = true;
                recurseTrue(tempValues, obj["children"])
            }
        }

        protected function writeYesNoToModel(property:String):void {
            if (this[property].selectedValue == null) {
                model.employee[property+"Set"] = false;
            } else {
                model.employee[property] = this[property].selectedValue as Boolean;
                model.employee[property+"Set"] = true;
            }
        }
        

        protected function readYesNoFromModel(property:String):void {
            if (! model.employee[property+"Set"]) {
                this[property].selectedValue = null;
            } else {
                this[property].selectedValue = model.employee[property];
            }

        }


    }
}
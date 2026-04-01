package psp.sap.validators {
    import mx.utils.StringUtil;
    import mx.validators.IValidatorListener;
    import mx.validators.ValidationResult;
    import mx.validators.Validator;

    public class SAPFromAndToValueValidator extends Validator {


        //View model properties
        public var fromValueProperty:String;
        public var toValueProperty:String;


        public function SAPFromAndToValueValidator() {
            super();
            subFields = ["fromValue", "toValue"];
        }


        /**
         *  @private
         *  Storage for the fromValueListener property.
         */
        private var _fromValueListener:IValidatorListener;

        /**
         *  The component that listens for the validation result
         *  for the from value sub-field.
         *  If none is specified, use the value specified
         *  to the <code>source</code> property.
         */
        public function get fromValueListener():IValidatorListener {
            return _fromValueListener;
        }

        /**
         *  @private
         */
        public function set fromValueListener(value:IValidatorListener):void {
            if (_fromValueListener == value) {
                return;
            }

            removeListenerHandler();

            _fromValueListener = value;

            addListenerHandler();
        }


        private var _toValueListener:IValidatorListener;


        public function get toValueListener():IValidatorListener {
            return _toValueListener;
        }


        public function set toValueListener(value:IValidatorListener):void {
            if (_toValueListener == value) {
                return;
            }

            removeListenerHandler();

            _toValueListener = value;

            addListenerHandler();
        }


        /*
         When the validator tries to get a list of listeners, return both.
         */

        override protected function get actualListeners():Array {
            var results:Array = [];

            var fromValueResult:Object = _fromValueListener;
            results.push(fromValueResult);

            if (fromValueResult is IValidatorListener) {
                IValidatorListener(fromValueResult).validationSubField = "fromValue";
            }

            var toValueResult:Object = _toValueListener;
            results.push(toValueResult);

            if (toValueResult is IValidatorListener) {
                IValidatorListener(toValueResult).validationSubField = "toValue";
            }

            if (results.length > 0 && listener) {
                results.push(listener);
            }

            return results;
        }


        /*
         Create custom value object with both values on it.
         */
        override protected function getValueFromSource():Object {
            var useValue:Boolean = false;

            var value:Object = {};

            if (source && fromValueProperty) {
                value.fromValue = source[fromValueProperty];
                useValue = true;
            }

            if (source && toValueProperty) {
                value.toValue = source[toValueProperty];
                useValue = true;
            }

            if (useValue) {
                return value;
            }
            else {
                return super.getValueFromSource();
            }
        }


        override protected function doValidation(value:Object):Array {
            var results:Array = super.doValidation(value);

            // Return if there are errors
            // or if the required property is set to false and length is 0.
            var val:String = value ? String(value) : "";
            if (results.length > 0 || ((val.length == 0) && !required)) {
                return results;
            }
            else {
                return doFromAndToValidation(value);
            }
        }


        /*
         Custom validation to make sure that toValue is not less than fromValue
         */
        protected function doFromAndToValidation(value:Object):Array {
            var results:Array = [];

            var fromValueString:String = "";
            var toValueString:String = "";

            try {
                fromValueString = (value.fromValue);
            }
            catch(e:Error) {
                // Error out cause the value object was not rendered correctly (see getValueFromSource())
                throw new Error("Missing fromValue on object.");
            }

            try {
                toValueString = (value.toValue);
            }
            catch(f:Error) {
                // Error out cause the value object was not rendered correctly (see getValueFromSource())
                throw new Error("Missing toValue on object.");
            }

            if (required && StringUtil.trim(fromValueString).length <= 0) {
                results.push(new ValidationResult(
                        true,
                        "fromValue",
                        "fromValueRequired",
                        "The From value is required."));
            }

            if (required && StringUtil.trim(toValueString).length <= 0) {
                results.push(new ValidationResult(
                        true,
                        "toValue",
                        "toValueRequired",
                        "The To value is required."));
            }

            if (results.length > 0) {
                return results;
            }

            var fromValue:int = parseInt(fromValueString);
            var toValue:int = parseInt(toValueString);


            if (fromValue > toValue) {
                results.push(new ValidationResult(
                        true,
                        "fromValue",
                        "fromValueGreaterThantoValue",
                        "The From value cannot be greater than the To value."));
                results.push(new ValidationResult(
                        true,
                        "toValue",
                        "fromValueLessThantoValue",
                        "The To value cannot be less than the From value."));
            }

            return results;
        }

    }
}
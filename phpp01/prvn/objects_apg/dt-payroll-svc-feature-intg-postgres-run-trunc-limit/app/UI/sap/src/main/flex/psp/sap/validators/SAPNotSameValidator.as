package psp.sap.validators {
    import mx.utils.StringUtil;
    import mx.validators.IValidatorListener;
    import mx.validators.ValidationResult;
    import mx.validators.Validator;

    public class SAPNotSameValidator extends Validator {


        //View model properties
        public var property1:String;
        public var property2:String;


        public function SAPNotSameValidator() {
            super();
            subFields = ["property1", "property2"];
        }


        /**
         *  @private
         *  Storage for the fromValueListener property.
         */
        private var _property1Listener:IValidatorListener;

        /**
         *  The component that listens for the validation result
         *  for the from value sub-field.
         *  If none is specified, use the value specified
         *  to the <code>source</code> property.
         */
        public function get property1Listener():IValidatorListener {
            return _property1Listener;
        }

        /**
         *  @private
         */
        public function set property1Listener(value:IValidatorListener):void {
            if (_property1Listener == value) {
                return;
            }

            removeListenerHandler();

            _property1Listener = value;

            addListenerHandler();
        }


        private var _property2Listener:IValidatorListener;


        public function get property2Listener():IValidatorListener {
            return _property2Listener;
        }


        public function set property2Listener(value:IValidatorListener):void {
            if (_property2Listener == value) {
                return;
            }

            removeListenerHandler();

            _property2Listener = value;

            addListenerHandler();
        }


        /*
         When the validator tries to get a list of listeners, return both.
         */

        override protected function get actualListeners():Array {
            var results:Array = [];

            var property1Result:Object = _property1Listener;
            results.push(  property1Result);

            if (property1Result is IValidatorListener) {
                IValidatorListener(property1Result).validationSubField = "property1";
            }

            var property2Result:Object = _property2Listener;
            results.push(property2Result);

            if (property2Result is IValidatorListener) {
                IValidatorListener(property2Result).validationSubField = "property2";
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

            if (source && property1) {
                value.property1 = source[property1];
                useValue = true;
            }

            if (source && property2) {
                value.property2 = source[property2];
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
                return doNotSameValidation(value);
            }
        }


        /*
         Custom validation to make sure that toValue is not less than fromValue
         */
        protected function doNotSameValidation(value:Object):Array {
            var results:Array = [];

            var property1String:String = "";
            var property2String:String = "";

            try {
                property1String = (value.property1);
            }
            catch(e:Error) {
                // Error out cause the value object was not rendered correctly (see getValueFromSource())
                throw new Error("Missing Debit Account on object.");
            }

            try {
                property2String = (value.property2);
            }
            catch(f:Error) {
                // Error out cause the value object was not rendered correctly (see getValueFromSource())
                throw new Error("Missing credit Account on object.");
            }

            if (required && StringUtil.trim( property1String).length <= 0) {
                results.push(new ValidationResult(
                        true,
                        "property1",
                        "property1Required",
                        "Debit Account is required."));
            }

            if (required && StringUtil.trim(property2String).length <= 0) {
                results.push(new ValidationResult(
                        true,
                        "property2",
                        "property2tRequired",
                        "Credit Account is required."));
            }

            if (results.length > 0) {
                return results;
            }



            if (property1String!="" && property1String == property2String) {
                results.push(new ValidationResult(
                        true,
                        "property2",
                        "property1AndProperty2areSame",
                        "Debit and Credit accounts cannot be same."));

            }

            return results;
        }

    }
}
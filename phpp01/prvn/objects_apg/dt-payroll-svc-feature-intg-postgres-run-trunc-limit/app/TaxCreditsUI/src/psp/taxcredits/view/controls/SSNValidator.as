package psp.taxcredits.view.controls {
    import mx.validators.StringValidator;
    import mx.validators.ValidationResult;

    public class SSNValidator extends StringValidator {

        //The overriden validation function
        override protected function doValidation(value:Object):Array
        {
            var results:Array = super.doValidation(value);

            // Return if there are errors
            // or if the required property is set to false and length is 0.
            var val:String = value ? String(value) : "";
            if (results.length > 0 || ((val.length == 0) && !required))
                return results;
            else
                return isSSN(value as String);
        }

        private function isEmptyText(str:String):Boolean {
            return str == null || str.replace(" ", "").length == 0;
        }

        private function isSSN(str:String):Array {
            var results:Array = [];
            if(!(isEmptyText(str))) {
                var ssnReg:RegExp = /^\d{9}$/;
                var result:Object = ssnReg.exec(str);
                if(result == null){
                    var ssnRegWithDashes:RegExp = /^\d{3}-\d{2}-\d{4}$/;
                    result = ssnRegWithDashes.exec(str);
                    if (result == null) {
                        results.push(new ValidationResult(
                                true,
                                "",
                                "notSSN",
                                "SSN must be 9 digits"));
                    }
                }
            }
            return results;
        }

    }
}
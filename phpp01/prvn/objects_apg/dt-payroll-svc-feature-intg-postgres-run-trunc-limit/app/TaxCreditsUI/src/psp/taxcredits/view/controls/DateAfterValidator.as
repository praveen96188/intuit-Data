package psp.taxcredits.view.controls {
    import mx.validators.DateValidator;
    import mx.validators.ValidationResult;
    import mx.validators.Validator;

    public class DateAfterValidator extends DateValidator {

        [Bindable] public var dateAfter:Date;
        [Bindable] public var dateNotAfter:String;

        //The overriden validation function
        override protected function doValidation(value:Object):Array
        {
            var results:Array = super.doValidation(value);
            var date:Date = super.getValueFromSource() as Date;
            if (date != null) {
                if (date.getTime() < dateAfter.getTime()) {
                        results.push(new ValidationResult(
                                true,
                                "",
                                "notAfter",
                                dateNotAfter));
                }
            }

            return results;

        }

 

    }
}
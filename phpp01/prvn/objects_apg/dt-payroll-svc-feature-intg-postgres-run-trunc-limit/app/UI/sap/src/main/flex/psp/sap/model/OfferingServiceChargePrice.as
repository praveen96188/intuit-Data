/**
 * Created by IntelliJ IDEA.
 * User: dweinberg
 * Date: 2/10/11
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    import mx.validators.NumberValidator;
    import mx.validators.Validator;

    import psp.sap.formatters.SAPCurrencyFormatters;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPOfferingServiceChargePrice")]
    public class OfferingServiceChargePrice {
        public var serviceChargeTypeCode:String;
        public var price:Number;
        public var unitPrice:Number;
        public var displayName:String;
        public var memo:String;

        public var chargedPrice:Number;

        private var mChargedPriceString:String;
        [Transient]
        public function get chargedPriceString():String {
            return mChargedPriceString;
        }

        public function set chargedPriceString(value:String):void {
            mChargedPriceString = value;
            if (!isNaN(parseFloat(value))) {
                chargedPrice = parseFloat(value);
            } else {
                chargedPrice = 0;
            }
        }


        private var mChecked:Boolean = true;

        public function get checked():Boolean {
            return mChecked;
        }
        public function set checked(value:Boolean):void {
            mChecked = value;
            if (!value) {
                chargedPriceString = SAPCurrencyFormatters.currencyFormatterNoSymbolNoComma.format(price);
            }
            if (validator != null) {
                validator.enabled = value;
            }
            if (memoValidator != null) {
                memoValidator.enabled = value;
            }
        }

        [Transient] public var tempId:Number = 0;

        [Transient] public var validator:NumberValidator;
        [Transient] public var memoValidator:Validator;

        [Bindable("propertyChange")]
        public function get requiresMemo():Boolean {
            return isOtherFee;
        }

        public function get canBeRepeated():Boolean {
            return isOtherFee;
        }

        public function get isOtherFee():Boolean {
            return displayName == "Other Fee";
        }
    }
}

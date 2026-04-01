/**
 * Created by IntelliJ IDEA.
 * User: dweinberg
 * Date: 1/29/11
 * Time: 8:04 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    import flash.events.EventDispatcher;

    import mx.events.PropertyChangeEvent;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPManualLedgerTaxLine")]
    public class ManualLedgerTaxLine extends EventDispatcher {

        public var law:LawItem;

        private var mAmountText:String = "";

        [Transient]
        public function get amountText():String {
            return mAmountText;
        }

        public function set amountText(value:String):void {
            mAmountText = value;
            var newAmount:Number = parseFloat(amountText);
            amount = isNaN(newAmount) ? 0 : newAmount;
        }

        private var mAmount:Number;

        public function get amount():Number {
            return mAmount;
        }

        public function set amount(value:Number):void {
            mAmount = value;
            doCalc();
        }


        private var mWageAmountText:String="";

        [Transient]
        public function get wageAmountText():String {
            return mWageAmountText;
        }

        public function set wageAmountText(value:String):void {
            mWageAmountText = value;
            var newWageAmount:Number = parseFloat(wageAmountText);
            wageAmount = isNaN(newWageAmount) ? 0 : newWageAmount;
        }

        private var mWageAmount:Number;

        public function get wageAmount():Number {
            return mWageAmount;
        }

        public function set wageAmount(value:Number):void {
            mWageAmount = value;
            doCalc();
        }

        private function doCalc():void {
            adjustedQTDYTD.qtdLiability = originalQTDYTD.qtdLiability + amount;
            adjustedQTDYTD.ytdLiability = originalQTDYTD.ytdLiability + amount;
            adjustedQTDYTD.taxBalance = originalQTDYTD.taxBalance + amount;
            adjustedQTDYTD.qtdWages = originalQTDYTD.qtdWages + wageAmount;
            adjustedQTDYTD.ytdWages = originalQTDYTD.ytdWages + wageAmount;
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "adjustedQTDYTD", null, null));
        }

        private var mOriginalQTDYTD:QTDYTDs = new QTDYTDs();

        public function get originalQTDYTD():QTDYTDs {
            return mOriginalQTDYTD;
        }

        public function set originalQTDYTD(value:QTDYTDs):void {
            mOriginalQTDYTD = value;
            amount=0;
            wageAmount=0;
            doCalc();
        }

        [Transient] public var adjustedQTDYTD:QTDYTDs = new QTDYTDs();

        private var mCompanyLawExists:Boolean;

        public function get companyLawExists():Boolean {
            return mCompanyLawExists;
        }

        public function set companyLawExists(value:Boolean):void {
            mCompanyLawExists = value;
        }

        public function ManualLedgerTaxLine() {

        }
    }
}

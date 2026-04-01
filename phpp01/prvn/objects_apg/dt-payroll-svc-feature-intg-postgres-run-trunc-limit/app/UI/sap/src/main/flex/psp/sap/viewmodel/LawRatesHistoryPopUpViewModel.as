package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.model.CompanyLawRateDetail;
import psp.sap.model.CompanyLawRatesHistory;
import psp.sap.model.PaymentTemplate;

public class LawRatesHistoryPopUpViewModel extends AbstractPartViewModel {

        public static function createActivator(paymentTemplate:PaymentTemplate):Object {
            return {"paymentTemplate":paymentTemplate};
        }

        public function LawRatesHistoryPopUpViewModel() {
            super();
        }

        [Bindable]
        [BackingProperty(context=true)]
        public var paymentTemplate:PaymentTemplate;

        [Bindable]
        [ArrayElementType("String")]
        public var companyLaws:Array;

        private var mCompanyLaw:String;

        [Bindable]
        public function get companyLaw():String {
            return mCompanyLaw;
        }

        public function set companyLaw(value:String):void {
            mCompanyLaw = value;
            companyLawRateHistory.refresh();
        }

        [Bindable]
        [BackingProperty]
        public function get hideInvalidRates():Boolean {
            return mHideInvalidRates;
        }

        //noinspection JSUnusedGlobalSymbols
        public function set hideInvalidRates(value:Boolean):void {
            mHideInvalidRates = value;
            companyLawRateHistory.refresh();
        }

        private var mHideInvalidRates:Boolean = false;

        [ArrayElementType("psp.sap.model.CompanyLawRateDetail")]
        [Bindable]
        public var companyLawRateHistory:ArrayCollection;

        override protected function loadModelData():void {
            SAP.instance.taxService.getCompanyLawRatesHistory(companyKey.sourceSystemCd, companyKey.companyId, paymentTemplate.paymentTemplateCd, createLoadModelDataResponder(onSearchCompleted));
        }

        private function onSearchCompleted(e:ResultEvent):void {
            companyLawRateHistory = CompanyLawRatesHistory(e.result).companyLawRateDetails as ArrayCollection;
            companyLaws = CompanyLawRatesHistory(e.result).companyLawNames.source;
            companyLaws.push("");
            if (companyLaws.length > 1) {
                companyLaws.sort();
            }
            companyLaw = companyLaws[0];
            companyLawRateHistory.filterFunction = processFilter;
        }

        private function processFilter(item:CompanyLawRateDetail):Boolean {
            if (hideInvalidRates && item.invalidDate != null) {
                return false;
            }

            if (item == null || item.lawName == null || item.lawName == "") {
                return true;
            }

            return item.lawName.toUpperCase().indexOf(this.companyLaw.toUpperCase()) >= 0;
        }

    }
}

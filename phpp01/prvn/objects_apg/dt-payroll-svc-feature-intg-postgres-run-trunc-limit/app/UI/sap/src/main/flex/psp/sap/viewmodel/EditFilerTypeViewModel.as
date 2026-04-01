/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 1:15 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.FilerType;

    public class EditFilerTypeViewModel extends AbstractPartViewModel {

        [Bindable]
        [BackingProperty(recursive=true)]
        public var filerType:FilerType;

        [ArrayElementType("String")]
        [Bindable] public var filerTypes:ArrayCollection = new ArrayCollection(["941", "944"]);

        [ArrayElementType("String")]
        [Bindable] public var taxYears:ArrayCollection;

        [ArrayElementType("String")]
        [Bindable] public var taxQuarters:ArrayCollection = new ArrayCollection(["Q1", "Q2", "Q3", "Q4"]);

        [Bindable]
        [BackingProperty]
        public var selectedQuarter:String;

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.taxService.getFirstTaxYear(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onFirstTaxYearLoaded));
            SAP.instance.taxService.getFilerType(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onFilerTypeLoaded));
        }

        private function onFirstTaxYearLoaded(e:ResultEvent):void {
            taxYears = new ArrayCollection();
            var firstTaxYear:int = int(e.result);

            for (var i:int = firstTaxYear; i <= SAP.instance.PSPDate.fullYear + 1; i++) {
                taxYears.addItem(i.toString());
            }
        }

        private function onFilerTypeLoaded(e:ResultEvent):void {
            filerType = FilerType(e.result);
        }

        override protected function initializeBackingProperties():void {
            selectedQuarter = "Q" + filerType.effectiveQuarter.quarter;
        }

        override protected function executeSave():void {
            filerType.effectiveQuarter.quarter = parseInt(selectedQuarter.charAt(1));
            SAP.instance.taxService.updateFilerType(companyKey.sourceSystemCd, companyKey.companyId, filerType, createSaveResponder());
        }
    }
}

/**
 * User: dweinberg
 * Date: 2/6/11
 * Time: 6:12 PM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.TemplateQuarterAmount;

    public class TaxOverpaymentViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.CompanyLedgerAccount")]
        [Bindable] public var ledgerAccounts:ArrayCollection;

        [ArrayElementType("psp.sap.model.TemplateQuarterAmount")]
        [Bindable] public var atrBreakdown:ArrayCollection;

        public function TaxOverpaymentViewModel() {
            reloadOnSave = true;
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.payrollRunService.findLedgerAccounts(
                    company.companyId,
                    company.sourceSystemCd,
                    createLoadModelDataResponder(onLedgerAccountsLoaded));

            SAP.instance.taxService.getAgencyTaxRefundBreakdown(
                    company.sourceSystemCd,
                    company.companyId,
                    createLoadModelDataResponder(onATRBreakdownLoaded))
        }

        public function onLedgerAccountsLoaded(e:ResultEvent):void {
            var newLedgerAccounts:ArrayCollection = ArrayCollection(e.result);

            newLedgerAccounts.filterFunction = function(obj:Object):Boolean {
                var accountCode:String = CompanyLedgerAccount(obj).ledgerAccountCode;
                return accountCode == "ERPayable" || accountCode == "AgencyTaxRefund";
            };

            var sort:Sort = new Sort();
			sort.fields = [new SortField("name", true)];
		    newLedgerAccounts.sort = sort;

            newLedgerAccounts.refresh();

            ledgerAccounts = newLedgerAccounts;

        }

        public function onATRBreakdownLoaded(e:ResultEvent):void {
            var newBreakdown:ArrayCollection = ArrayCollection(e.result);

            var sort:Sort = new Sort();
            sort.fields = [new SortField("paymentTemplateCd"), new SortField("yearNumber", false, true, true), new SortField("quarterNumber", false, true, true)];
            newBreakdown.sort = sort;
            newBreakdown.refresh();

            atrBreakdown = newBreakdown;
        }

        private var selectedTemplateQuarter:TemplateQuarterAmount;
        public function createTOR(templateQuarter:TemplateQuarterAmount):void {
            selectedTemplateQuarter = templateQuarter;
            forceSave();
        }

        override protected function executeSave():void {
            SAP.instance.taxService.createTORTransactions(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    selectedTemplateQuarter.paymentTemplateCd,
                    selectedTemplateQuarter.quarter,
                    createSaveResponder());
        }
    }
}

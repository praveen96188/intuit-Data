/**
 * User: dweinberg
 * Date: 4/27/11
 * Time: 9:29 AM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.BillingTransaction;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.PayrollBillingTransactions;
    import psp.sap.model.PayrollRun;
    import psp.sap.validators.SAPValidators;

    public class ApplyERPayableToBalanceDueViewModel extends AbstractPartViewModel {

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;

        [Bindable] public var taxAmount:Number;
        [Bindable] public var erPayableAmount:Number;
        [Bindable] [BackingProperty] public var appliedAmountText:String;

        public static function createActivator(payrollRun:PayrollRun):Object {
            return {"payrollRun":payrollRun};
        }

        public function ApplyERPayableToBalanceDueViewModel() {
            this.reloadOnSave = true;
        }

        override protected function loadModelData():void {
            loadCount = 2;
            SAP.instance.payrollRunService.findPayrollUncollectedBalances(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    payrollRun.sourcePayRunId,
                    createLoadModelDataResponder(onUncollectedBalancesLoaded));
            SAP.instance.payrollRunService.findLedgerAccounts(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    createLoadModelDataResponder(onLedgerBalancesLoaded));
        }


        private function onUncollectedBalancesLoaded(e:ResultEvent):void {
            [ArrayElementType("psp.sap.model.PayrollBillingTransactions")]
            var billingTransactions:ArrayCollection = ArrayCollection(e.result);

            taxAmount = 0;

            for each (var payrollBillingTransactions:PayrollBillingTransactions in billingTransactions) {
                var billingTransaction:BillingTransaction = payrollBillingTransactions.taxTransaction;
                if (billingTransaction && billingTransaction.financialTxnType == "ER Tax Debit") {
                    taxAmount += billingTransaction.financialAmount;
                }
            }
        }

        private function onLedgerBalancesLoaded(e:ResultEvent):void {
            [ArrayElementType("psp.sap.model.CompanyLedgerAccount")]
            var ledgerAccounts:ArrayCollection = ArrayCollection(e.result);

            for each (var ledgerAccount:CompanyLedgerAccount in ledgerAccounts) {
                if (ledgerAccount.ledgerAccountCode == "ERPayable") {
                    erPayableAmount = ledgerAccount.balance;
                    break;
                }
            }

        }

        override protected function initializeBackingProperties():void {
            validators.length = 0;
            validators.push(SAPValidators.createNumberValidator(this, "appliedAmountText", true, 0.01, Math.min(taxAmount, erPayableAmount), false));
        }


        override protected function executeSave():void {
            SAP.instance.payrollRunService.applyERPayableToBalanceDue(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    payrollRun.id.toString(),
                    parseFloat(appliedAmountText),
                    createSaveResponder());
        }
    }
}

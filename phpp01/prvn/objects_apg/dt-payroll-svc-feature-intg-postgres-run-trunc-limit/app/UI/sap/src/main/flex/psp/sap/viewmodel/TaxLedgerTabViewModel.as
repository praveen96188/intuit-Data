package psp.sap.viewmodel {
    import psp.sap.model.LawTransactions;

    public class TaxLedgerTabViewModel {

        [Bindable] [BackingProperty] public var lawTransactions:LawTransactions;

        [Bindable] public var taxLedgerViewModel:TaxLedgerViewModel;

        public function TaxLedgerTabViewModel() {            
        }
    }
}
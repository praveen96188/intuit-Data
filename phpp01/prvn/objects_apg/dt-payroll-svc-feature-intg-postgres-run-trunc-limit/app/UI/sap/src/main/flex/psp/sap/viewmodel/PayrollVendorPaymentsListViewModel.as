package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;

    public class PayrollVendorPaymentsListViewModel extends PayrollsListViewModel {
        override protected function get payrollTypes():ArrayCollection {
            var defaultPayrollTypes:ArrayCollection = new ArrayCollection;
            defaultPayrollTypes.addItem("BillPayment");
            return defaultPayrollTypes;
        }
    }
}
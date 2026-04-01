package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;
    import psp.sap.model.CompanyLedgerAccount;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;
    import psp.sap.view.SinglePartPageView;
    import psp.sap.viewmodel.PayrollPItemsTaxabilityViewModel;


    public class CompanyPayrollsTopicViewModel extends CompanyInspectorTopicViewModel
    {

        public function CompanyPayrollsTopicViewModel(companyInspector:CompanyInspectorViewModel)
        {
            super(companyInspector, CompanyInspectorTopicEnum.PAYROLLS);

            addSinglePart(CompanyInspectorPageEnum.PAYROLL, PayrollViewModel,"");
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST, PayrollTransactionsListViewModel);

            var vendorPayments:PayrollTransactionsListViewModel = PayrollTransactionsListViewModel(addSinglePart(CompanyInspectorPageEnum.PAYROLL_VENDOR_TRANSACTION_LIST, PayrollTransactionsListViewModel).part);
            vendorPayments.payrollType = "Vendor";

            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_CANCEL, PayrollTransactionCancelViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_REVERSE, PayrollTransactionReverseViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_ADD_REDEBIT, PayrollAddRedebitViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_NONACH_ADD_REDEBIT, PayrollAddNonACHRedebitViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_CHANGE_REDEBIT, PayrollChangeRedebitViewModel);
            addSinglePart(CompanyInspectorPageEnum.COMPANY_PAYROLL_LEDGER, PayrollLedgerViewModel);
            addSinglePart(CompanyInspectorPageEnum.COMPANY_PAYROLL_LEDGER_ACCOUNT_ENTRIES, PayrollLedgerEntriesViewModel);
            addSinglePart(CompanyInspectorPageEnum.COMPANY_PAYROLL_ADD_ESCALATION, PayrollLedgerAddEscalationViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_HISTORY, PayrollTransactionHistoryViewModel, "Transaction State Change History");
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_ISSUE_REISSUE_REFUND, PayrollTransactionIssueReissueRefundPageViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_CREATE_FEE, PayrollTransactionCreateFeeViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_REFUND_REBILL, PayrollRefundRebillViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_LEDGER_RECOVER_BAD_DEBT, PayrollLedgerRecoverBadDebtViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_REFUND_FRAUD_ESCALATION, PayrollRefundFraudEscalationViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_LEDGER_FEE_TRANSFER, PayrollLedgerFeeTransferViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_LEDGER_DD_REFUND, PayrollLedgerDDRefundTransactionViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_LEDGER_EE_RETURN_REFUND, PayrollLedgerEmployeeReturnRefundViewModel);
            var erReturnViewModel:PayrollLedgerEmployerReturnRefundViewModel = PayrollLedgerEmployerReturnRefundViewModel(addSinglePart(CompanyInspectorPageEnum.PAYROLL_LEDGER_ER_RETURN_REFUND, PayrollLedgerEmployerReturnRefundViewModel).part);
            erReturnViewModel.canDdTaxSplit = true;
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_CREATE_EXPECTED_WIRE_DATE, PayrollExpectedWireViewModel, "Add / Edit Wire Expectation Dates for this Transaction");
            addSinglePart(CompanyInspectorPageEnum.PAYCHECK_LINE_ITEMS, EmployeeLineItemsViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLL_PREFUNDING, PayrollPrefundingViewModel);
			addSinglePart(CompanyInspectorPageEnum.PAYROLL_APPLY_ER_PAYABLE,ApplyERPayableToBalanceDueViewModel);
 			addSinglePart(CompanyInspectorPageEnum.FINANCIAL_LEDGER_ADJUSTMENT_PAYROLL, FinancialLedgerAdjustmentViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLLS_PITEMS, PayrollPItemsViewModel);
            addSinglePart(CompanyInspectorPageEnum.PAYROLLS_PITEM_TAXABILITY, PayrollPItemsTaxabilityViewModel);
        }


    }
}

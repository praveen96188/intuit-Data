package psp.sap.model {
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.viewmodel.AbstractInspectorViewModel;
    import psp.sap.viewmodel.ApplyERPayableToBalanceDueViewModel;
    import psp.sap.viewmodel.PayrollExpectedWireViewModel;
    import psp.sap.viewmodel.PayrollLedgerViewModel;
    import psp.sap.viewmodel.PayrollMultiItemEnteringPageViewModel;
    import psp.sap.viewmodel.PayrollRefundRebillViewModel;
    import psp.sap.viewmodel.PayrollSettlementViewModel;
    import psp.sap.viewmodel.PayrollTransactionCancelViewModel;
    import psp.sap.viewmodel.PayrollTransactionCreateFeeViewModel;
    import psp.sap.viewmodel.PayrollTransactionHistoryViewModel;
    import psp.sap.viewmodel.PayrollTransactionIssueReissueRefundPageViewModel;
    import psp.sap.viewmodel.PayrollTransactionReverseViewModel;
    import psp.sap.viewmodel.PayrollTransactionsListViewModel;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPActionEvent")]
    public class ActionEvent {
        public var code:ActionEventCode;
        public var description:String;

        public function ActionEvent(aCode:ActionEventCode = null, aDescription:String = null) {
            code = aCode;
            description = aDescription;
        }

        public function set actionEventCd(value:String):void {
            code = ActionEventCode.fromLabel(value);
        }


        [Transient]
        public function canPreformAction():Boolean {
            if (code == ActionEventCode.DD_TRANSACTION_CANCEL) {
                // Cancel Paychecks
                return SAP.canPerformOperation(OperationsEnum.CANCEL_DD_TRANSACTION);
            } else if (code == ActionEventCode.DD_TRANSACTION_REVERSE) {
                // Create Reversals
                return SAP.canPerformOperation(OperationsEnum.CREATE_REVERSAL_TRANSACTION);
            } else if (code == ActionEventCode.DD_REDEBIT_ADD || code == ActionEventCode.DD_REDEBIT_EDIT) {
                // Issue ACH Redebit
                return SAP.canPerformOperation(OperationsEnum.ISSUE_REDEBIT_TRANSACTION);
            } else if (code == ActionEventCode.DD_REDEBIT_RECORD) {
                // Record Non-ACH Payments
                return SAP.canPerformOperation(OperationsEnum.RECORD_NON_ACH_REDEBIT_TRANSACTION);
            } else if (code == ActionEventCode.ER_FEE_ADD) {
                // Create Fees
                return SAP.canPerformOperation(OperationsEnum.CREATE_FEE_TRANSACTION);
            } else if (code == ActionEventCode.ISSUE_REISSUE_REFUND_ER) {
                // Create Refunds
                return SAP.canPerformOperation(OperationsEnum.CREATE_REFUND_TRANSACTION);
            } else if (code == ActionEventCode.BAD_DEBT_WRITE_OFF) {
                return SAP.canPerformOperation(OperationsEnum.WRITE_OFF_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.VOID_PAYROLL_TAX_PAYMENT) {
                return SAP.canPerformOperation(OperationsEnum.WRITE_OFF_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.BAD_DEBT_WRITE_OFF_EE_RETURN) {
                return SAP.canPerformOperation(OperationsEnum.WRITE_OFF_EMPLOYEE_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.FEE_TRANSFER) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_FEE_TRANSACTION); //Double check this action
            } else if (code == ActionEventCode.TX_STATE_HISTORY) {
                return true;
            } else if (code == ActionEventCode.FINANCIAL_TRANSACTION_CANCEL) {
                return SAP.canPerformOperation(OperationsEnum.CANCEL_TRANSACTION);
            } else if (code == ActionEventCode.ER_FEE_CANCEL) {
                return SAP.canPerformOperation(OperationsEnum.EMPLOYER_FEE_DEBIT_CANCEL) || SAP.canPerformOperation(OperationsEnum.CANCEL_TRANSACTION);
            } else if (code == ActionEventCode.ER_PAYABLE_CANCEL) {
                return SAP.canPerformOperation(OperationsEnum.REFUND_ER_PAYABLE) || SAP.canPerformOperation(OperationsEnum.CANCEL_TRANSACTION);
            } else if (code == ActionEventCode.FINANCIAL_TRANSACTION_VOID_TX) {
                return SAP.canPerformOperation(OperationsEnum.VOID_TRANSACTION);
            } else if (code == ActionEventCode.VOID_TOR) {
                return SAP.canPerformOperation(OperationsEnum.VOID_TOR) || SAP.canPerformOperation(OperationsEnum.VOID_TRANSACTION);
            } else if (code == ActionEventCode.EE_RETURN_REFUND) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_REFUND_TRANSACTION);
            } else if (code == ActionEventCode.EE_RETURN_TRANSFER) {
                return SAP.canPerformOperation(OperationsEnum.BOOK_TRANSFER_TRANSACTION);
            } else if (code == ActionEventCode.DD_REFUND) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_REFUND_TRANSACTION);
            } else if (code == ActionEventCode.INTUIT_5_DAY_RETURN_TRANSFER) {
                return SAP.canPerformOperation(OperationsEnum.BOOK_TRANSFER_TRANSACTION);
            } else if (code == ActionEventCode.BAD_DEBT_RECOVER) {
                return SAP.canPerformOperation(OperationsEnum.RECOVER_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.ER_WIRE_EXPECTED) {
                return SAP.canPerformOperation(OperationsEnum.ENTER_WIRE_EXPECTED_DATE);
            } else if (code == ActionEventCode.ER_RETURN_REFUND) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_REFUND_TRANSACTION);
            } else if (code == ActionEventCode.REFUND_REBILL_FEE) {
                return SAP.canPerformOperation(OperationsEnum.AGENT_INITATES_REFUND_REBILL);
            } else if (code == ActionEventCode.ER_FRAUD_OR_ESCALATION_REFUND) {
                return SAP.canPerformOperation(OperationsEnum.REFUND_EMPLOYER_FRAUD_ESCALATION);
            } else if (code == ActionEventCode.VIEW_LEDGER) {
                return SAP.canPerformOperation(OperationsEnum.VIEW_LEDGER);
            } else if (code == ActionEventCode.RECORD_PREFUNDING_WIRE) {
                return SAP.canPerformOperation(OperationsEnum.RECORD_PREFUNDING_WIRE);
            } else if (code == ActionEventCode.CANCEL_ADJUSTMENT) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_MANUAL_LEDGER_ENTRY);
            } else if (code == ActionEventCode.REISSUE_PAYROLL_TAX_PAYMENT) {
                return SAP.canPerformOperation(OperationsEnum.WRITE_OFF_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.APPLY_ER_PAYABLE_TO_BALANCE_DUE) {
                return SAP.canPerformOperation(OperationsEnum.WRITE_OFF_BAD_DEBT_TRANSACTION);
            } else if (code == ActionEventCode.VIEW_TRANSACTION) {
                return true;
            } else if (code == ActionEventCode.REFUND_DEBIT) {
                return SAP.canPerformOperation(OperationsEnum.CREATE_ER_PENALTIES_AND_INTEREST_REFUNDS);
            }


            // default
            return false;
        }

        public function label():String {
            return description;
        }

        public function performPayrollAction(inspector:AbstractInspectorViewModel, payrollRun:PayrollRun):void {
            switch (code) {
                case ActionEventCode.VIEW_TRANSACTION:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(payrollRun.sourcePayRunId, payrollRun.paycheckDate));
                    break;
                case ActionEventCode.VIEW_LEDGER:
                    inspector.getPage(CompanyInspectorPageEnum.COMPANY_PAYROLL_LEDGER).activatePage(PayrollLedgerViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.DD_TRANSACTION_CANCEL:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_CANCEL).activatePage(PayrollTransactionCancelViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.DD_TRANSACTION_REVERSE:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_REVERSE).activatePage(PayrollTransactionReverseViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.DD_REDEBIT_ADD:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_ADD_REDEBIT).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.DD_REDEBIT_RECORD:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_NONACH_ADD_REDEBIT).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.DD_REDEBIT_EDIT:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_CHANGE_REDEBIT).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.ER_FEE_ADD:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_CREATE_FEE).activatePage(PayrollTransactionCreateFeeViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.ER_WIRE_EXPECTED:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_CREATE_EXPECTED_WIRE_DATE).activatePage(PayrollExpectedWireViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.ER_FRAUD_OR_ESCALATION_REFUND:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_REFUND_FRAUD_ESCALATION).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.RECORD_PREFUNDING_WIRE:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_PREFUNDING).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
            }
        }

        public function performPayrollLedgerAction(inspector:AbstractInspectorViewModel, payrollRun:PayrollRun, ledgerAccount:CompanyLedgerAccount):void {
            switch (code) {
                case ActionEventCode.BAD_DEBT_RECOVER:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_LEDGER_RECOVER_BAD_DEBT).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, this));
                    break;
                case ActionEventCode.FEE_TRANSFER:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_LEDGER_FEE_TRANSFER).activatePage(PayrollSettlementViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.DD_REFUND:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_LEDGER_DD_REFUND).activatePage(PayrollSettlementViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.EE_RETURN_REFUND:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_LEDGER_EE_RETURN_REFUND).activatePage(PayrollSettlementViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.ER_RETURN_REFUND:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_LEDGER_ER_RETURN_REFUND).activatePage(
                            PayrollSettlementViewModel.createActivator(payrollRun));
                    break;
                case ActionEventCode.APPLY_ER_PAYABLE_TO_BALANCE_DUE:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_APPLY_ER_PAYABLE).activatePage(ApplyERPayableToBalanceDueViewModel.createActivator(payrollRun));
                    break;
            }
        }

        public function performPayrollTransactionAction(inspector:AbstractInspectorViewModel, payrollRun:PayrollRun, payrollTransaction:PayrollTransaction):void {
            switch (code) {
                case ActionEventCode.TX_STATE_HISTORY:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_HISTORY).activatePage(PayrollTransactionHistoryViewModel.createActivator(payrollTransaction));
                    break;
                case ActionEventCode.ISSUE_REISSUE_REFUND_ER:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_ISSUE_REISSUE_REFUND).activatePage(PayrollTransactionIssueReissueRefundPageViewModel.createActivator(payrollRun, payrollTransaction));
                    break;
                case ActionEventCode.REFUND_REBILL_FEE:
                    inspector.getPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_REFUND_REBILL).activatePage(PayrollRefundRebillViewModel.createActivator(payrollTransaction, payrollRun));
                    break;
            }
        }



    }
}

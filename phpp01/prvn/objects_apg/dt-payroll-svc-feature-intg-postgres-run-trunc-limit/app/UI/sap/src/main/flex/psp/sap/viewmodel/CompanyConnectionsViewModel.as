package psp.sap.viewmodel {
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;

    public class CompanyConnectionsViewModel extends CompositePartViewModel {
        public function CompanyConnectionsViewModel() {
            super();

            bindSaveMessageWithChildren = true;

            this.label = CompanyInspectorPageEnum.COMPANY_CONNECTIONS;

            var taxPtn:PartsTabNavigatorViewModel = addPartsTabNavigator(CompanyInspectorPageEnum.PAYROLL_CONNECTION_LOG);

            var connectionLog:AbstractPartViewModel = taxPtn.addNewPart(PayrollConnectionLogViewModel, CompanyInspectorPageEnum.PAYROLL_CONNECTION_LOG);
            if (SAP.canPerformOperation(OperationsEnum.DATA_SYNC_TOOL)) {
                taxPtn.addNewPart(DataSyncToolsViewModel, CompanyInspectorPageEnum.DATA_SYNC_TOOLS);
            }
            taxPtn.defaultSinglePart = connectionLog;
        }
    }
}

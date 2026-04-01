package psp.sap.viewmodel {
    import psp.sap.application.enums.OperatorPageEnum;

    public class CheckPrintingViewModel extends CompositePartViewModel {
        public function CheckPrintingViewModel() {
            super();
            bindSaveMessageWithChildren = true;
            this.label = OperatorPageEnum.CHECK_PRINTING_VIEW;

            var tabNavigatorVM:PartsTabNavigatorViewModel = addPartsTabNavigator(OperatorPageEnum.CHECK_PRINTING_VIEW);
            var checkPrintingQueueViewModel:CheckPrintingQueueViewModel = tabNavigatorVM.addNewPart(CheckPrintingQueueViewModel, OperatorPageEnum.CHECK_PRINTING_QUEUE) as CheckPrintingQueueViewModel;
            tabNavigatorVM.addNewPart(CheckPrintingAgencyChecksViewModel, OperatorPageEnum.CHECK_PRINTING_AGENCY_CHECKS);
            tabNavigatorVM.defaultSinglePart = checkPrintingQueueViewModel;
        }
    }
}
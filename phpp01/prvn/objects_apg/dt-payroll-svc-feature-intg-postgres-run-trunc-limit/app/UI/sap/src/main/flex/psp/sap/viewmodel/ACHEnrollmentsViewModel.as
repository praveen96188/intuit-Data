package psp.sap.viewmodel {
    import flash.events.Event;

    import psp.sap.application.enums.EnrollmentsPageEnum;
    import psp.sap.model.ACHEnrollmentDetail;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class ACHEnrollmentsViewModel extends CompositePartViewModel {

        public var achHistoryPopUp:PopUpPartViewModel;
        public var achHistoryPopUpViewModel:ACHEnrollmentHistoryViewModel;

        public function ACHEnrollmentsViewModel() {
            super();
            bindSaveMessageWithChildren = true;
            this.label = EnrollmentsPageEnum.ACH_ENROLLMENTS;

            var achPtn:PartsTabNavigatorViewModel = addPartsTabNavigator(EnrollmentsPageEnum.ACH_ENROLLMENTS);
            var achPendingEnrollments:AbstractPartViewModel = achPtn.addNewPart(ACHEnrollmentsPendingViewModel, EnrollmentsPageEnum.ACH_PENDING_ENROLLMENT);
            achPtn.addNewPart(ACHEnrollmentsPendingResponseViewModel, EnrollmentsPageEnum.ACH_PENDING_RESPONSE);
            achPtn.addNewPart(ACHEnrollmentsRejectedViewModel, EnrollmentsPageEnum.ACH_REJECTED);
            achPtn.addNewPart(ACHEnrollmentsDeleteViewModel, EnrollmentsPageEnum.ACH_DELETE);

            achPtn.defaultSinglePart = achPendingEnrollments;

            achHistoryPopUp = addPopUpPart(EnrollmentsPageEnum.ACH_HISTORY);
            achHistoryPopUpViewModel = achHistoryPopUp.addNewPart(ACHEnrollmentHistoryViewModel, EnrollmentsPageEnum.ACH_HISTORY) as ACHEnrollmentHistoryViewModel;
            achHistoryPopUpViewModel.addEventListener(ViewModelEvent.DEACTIVATED, onViewModelDeactivated);
        }

        public function viewACHHistory(data:ACHEnrollmentDetail):void {
            achHistoryPopUpViewModel.targetCompanyKey = data.companyKey;
            achHistoryPopUp.displayPopUp();
        }

        private function onViewModelDeactivated(e:Event):void {
            PopUpPartViewModel.refreshIfSaved(AbstractPartViewModel(e.target), this);
        }
    }
}
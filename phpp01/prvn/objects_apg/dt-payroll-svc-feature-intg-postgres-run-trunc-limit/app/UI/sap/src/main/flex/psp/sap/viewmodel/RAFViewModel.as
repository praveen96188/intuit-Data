package psp.sap.viewmodel {
    import psp.sap.application.enums.EnrollmentsPageEnum;

    public class RAFViewModel extends CompositePartViewModel {
        public function RAFViewModel() {
            super();
            bindSaveMessageWithChildren = true;
            this.label = EnrollmentsPageEnum.RAF_ENROLLMENTS;

            var rafPtn:PartsTabNavigatorViewModel = addPartsTabNavigator(EnrollmentsPageEnum.RAF_ENROLLMENTS);
            var rafPendingEnrollments:AbstractPartViewModel = rafPtn.addNewPart(RAFPendingEnrollmentsViewModel, EnrollmentsPageEnum.RAF_PENDING_ENROLLMENTS);
            rafPtn.addNewPart(RAFPendingTapeViewModel, EnrollmentsPageEnum.RAF_PENDING_TAPE);
            rafPtn.addNewPart(RAFPendingResponseViewModel, EnrollmentsPageEnum.RAF_PENDING_RESPONSE);
            rafPtn.addNewPart(RAFRejectedViewModel, EnrollmentsPageEnum.RAF_REJECTED);
            rafPtn.addNewPart(RAFUnenrollTapeViewModel, EnrollmentsPageEnum.RAF_UNENROLL_TAPE);

            rafPtn.defaultSinglePart = rafPendingEnrollments;
        }
    }
}
/**
 * User: dweinberg
 * Date: 2/11/13
 * Time: 10:42 AM
 */
package psp.sap.viewmodel {
    import psp.sap.application.enums.EnrollmentsPageEnum;

    public class ACHEnrollmentTopicViewModel extends InspectorTopicViewModel {
        public function ACHEnrollmentTopicViewModel(inspector:AbstractInspectorViewModel) {
            super(inspector, EnrollmentsPageEnum.ACH);

            addSinglePart(EnrollmentsPageEnum.ACH_PAGE, ACHEnrollmentsViewModel);
        }
    }
}

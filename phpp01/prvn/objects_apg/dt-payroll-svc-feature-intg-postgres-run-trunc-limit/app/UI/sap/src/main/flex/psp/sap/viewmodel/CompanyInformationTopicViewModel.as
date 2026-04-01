package psp.sap.viewmodel
{
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.CompanyInspectorTopicEnum;

	public class CompanyInformationTopicViewModel extends CompanyInspectorTopicViewModel
	{
		
		public function CompanyInformationTopicViewModel(companyInspector:CompanyInspectorViewModel)
		{
			super(companyInspector, CompanyInspectorTopicEnum.INFORMATION);	
			
			addSinglePart(CompanyInspectorPageEnum.COMPANY_INFO, CompanyInformationViewModel);
			addSinglePart(CompanyInspectorPageEnum.STRIKES, CompanyManageStrikesViewModel, "Manage Company Strikes");

            var subscriptionStatus:SinglePartPageViewModel = addSinglePart(CompanyInspectorPageEnum.SUBSCRIPTION_STATUS, CompanyEditSubscriptionStatusViewModel, "Edit Company Subscription Status");
			subscriptionStatus.maintainCrossTopicHistory = true;

            var limitViolations:SinglePartPageViewModel = addSinglePart(CompanyInspectorPageEnum.LIMIT_VIOLATIONS_HISTORY, CompanyLimitViolationsViewModel);
			limitViolations.maintainCrossTopicHistory = true;

            addSinglePart(CompanyInspectorPageEnum.COMPANY_OFFERS, CompanyOffersViewModel);
			addSinglePart(CompanyInspectorPageEnum.COMPANY_OFFERINGS, CompanyOfferingsViewModel);
 
		}
		
	}
}
package psp.sap.viewmodel
{
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.EmployeeInfo;

    public class EmployeeProfileInfoViewModel
	extends AbstractPartViewModel
	{		
        private var mEmployeeInfo:EmployeeInfo;

		[Bindable]
		public var editSeasonalViewModel:EditSeasonalViewModel;

        [Bindable] [BackingProperty]
		public function get employeeInfo():EmployeeInfo {
			return mEmployeeInfo;
		}
		public function set employeeInfo(value:EmployeeInfo):void {
			mEmployeeInfo = value;
		}

		public function EmployeeProfileInfoViewModel()
		{		
			this.label = CompanyInspectorPageEnum.EMPLOYEE_PROFILE_INFO;
			this.reloadOnSave = true;
		}

        public function goToEmployeeBankScreen():void {
           inspector.findPage(CompanyInspectorPageEnum.BANKS_EMPLOYEE_ACCOUNT_HISTORY).activatePage(BanksEmployeeAccountHistoryViewModel.createActivator(employeeInfo as EmployeeInfo));
        }

	}
}
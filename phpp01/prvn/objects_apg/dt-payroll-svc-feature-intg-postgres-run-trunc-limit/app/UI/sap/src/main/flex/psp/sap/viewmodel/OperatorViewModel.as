package psp.sap.viewmodel
{
	import psp.sap.application.enums.OperatorPageEnum;
	
	public class OperatorViewModel extends CompositePartViewModel
	{
		public function OperatorViewModel () {
			super();
			
			
			addNewPart(ACHOffloadViewModel,OperatorPageEnum.ACH_OFFLOAD);
			//addNewPart(OperatorEnrollmentsViewModel,OperatorPageEnum.ENROLLMENTS, PartAdditionStrategy.COMPOSITE);
		}

	}
}
package psp.sap.viewmodel
{
	import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
	import psp.sap.application.enums.OperationsEnum;

	public class EnrollmentsExplorerViewModel
	extends AbstractExplorer
	{
		public function EnrollmentsExplorerViewModel()
		{
			super(ExplorerEnum.ENROLLMENTS, ExplorerEnum.ENROLLMENTS, true );
			var enrollmentsInspector:EnrollmentsInspectorViewModel = new EnrollmentsInspectorViewModel(this);
			inspectors.addItem(enrollmentsInspector);
		}

		override public function permissionGranted():Boolean {
			return SAP.canPerformOperation(OperationsEnum.VIEW_GLOBAL_ENROLLMENTS);
		}

	}
}

package psp.sap.viewmodel
{
	import mx.events.PropertyChangeEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;

	public class AdministrationInspectorViewModel extends AbstractInspectorViewModel
	{
		public function AdministrationInspectorViewModel(explorer:AbstractExplorer)
		{
			super(explorer);
			
			//Order these in preference to be displayed first
			
			if(SAP.canPerformOperation(OperationsEnum.UPDATE_OR_VIEW_SETTINGS))
			{
				topics.addItem( new AdministrationSettingsTopicViewModel(this) );
				topics.addItem( new FraudSettingsTopicViewModel(this) );
			}
			
			if(SAP.canPerformOperation(OperationsEnum.AUTH_ACCESS_APPLICATION))
			{
				topics.addItem( new UserTopicViewModel(this));
				topics.addItem( new AdministrationRolesTopicViewModel(this) );
			}
            if(SAP.canPerformOperation(OperationsEnum.VIEW_SYSTEM_PARAMETERS))
			{
				topics.addItem( new SystemParametersTopicViewModel(this));
			}
            if (SAP.canPerformOperation(OperationsEnum.EXECUTE_SQL))
            {
                topics.addItem( new SqlAdministrationTopicViewModel(this));
            }
			
			defaultTopic = (topics.length > 0) ? topics.getItemAt(0) as InspectorTopicViewModel : null;
		}
		
		override public function activate(topicToActivate:InspectorTopicViewModel=null):void {
			super.activate(topicToActivate);
			dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "activeTopic", null, this.activeTopic) );
		}
		
	}
}
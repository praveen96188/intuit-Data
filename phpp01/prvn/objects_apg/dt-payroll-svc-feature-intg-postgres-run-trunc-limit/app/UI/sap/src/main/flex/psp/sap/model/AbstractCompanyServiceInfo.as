package psp.sap.model
{
    import flash.events.EventDispatcher;

    import mx.events.PropertyChangeEvent;
	
	import psp.sap.application.SAP;
	
	[Bindable]
	public class AbstractCompanyServiceInfo extends EventDispatcher
	{
		private var mServiceStatusCd:String;
	    private var mServiceSubStatusCd:String;
	    
	    private var mServiceStatus:ServiceStatus = null;
	    private var mServiceSubStatus:ServiceSubStatus = null;	 
		
		public function get serviceStatusCd():String {
	    	return mServiceStatusCd;
	    }
	    
	    public function set serviceStatusCd(value:String):void {
	    	mServiceStatusCd = value;
	    	
	    	var oldValue:ServiceStatus = mServiceStatus;
	    	mServiceStatus = lookupServiceStatus();
	    	dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "serviceStatus", oldValue, mServiceStatus) );
	    }
	    
	    
	    public function get serviceSubStatusCd():String {
	    	return mServiceSubStatusCd;
	    }
	    
	    public function set serviceSubStatusCd(value:String):void {
	    	mServiceSubStatusCd = value;
	
	    	var oldValue:ServiceSubStatus = mServiceSubStatus;
	    	mServiceSubStatus = lookupServiceSubStatus();
	    	dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "serviceSubStatus", oldValue, mServiceSubStatus) );
	    }
	    
	    [Transient]
	    [Bindable("propertyChange")]
	    public function get serviceStatus():ServiceStatus {
	        return mServiceStatus;
	    }
	    
	    [Transient]
	    [Bindable("propertyChange")]
	    public function get serviceSubStatus():ServiceSubStatus {
	        return mServiceSubStatus;
	    }

	    private function lookupServiceStatus():ServiceStatus {
			var serviceStatus:ServiceStatus =
				 	SAP.instance.lookupService.serviceStatuses.getItemByKey(mServiceSubStatusCd)
				 	as ServiceStatus;		
			
			return serviceStatus;	    	
	    }
	    
	    private function lookupServiceSubStatus():ServiceSubStatus {
			var serviceSubStatus:ServiceSubStatus =
				 	SAP.instance.lookupService.serviceSubStatuses.getItemByKey(mServiceSubStatusCd)
				 	as ServiceSubStatus;		
			
			return serviceSubStatus;	    	
	    }


	}
}
package psp.sap.service
{
	import flash.events.EventDispatcher;
	import flash.utils.describeType;
	
	import psp.sap.service.interfaces.IPSPService;
	
	public class PSPService extends EventDispatcher implements IPSPService
	{
		protected var mRemoteObjectPool:RemoteObjectPool;
		
		public function get remoteObjectPool():RemoteObjectPool {
			return mRemoteObjectPool; 
		}
		
		public function set remoteObjectPool(value:RemoteObjectPool):void {
			mRemoteObjectPool = value;
		}
				
		virtual public function connect():void {
			if (mRemoteObjectPool)
				mRemoteObjectPool.connect();				
		}
	}
}
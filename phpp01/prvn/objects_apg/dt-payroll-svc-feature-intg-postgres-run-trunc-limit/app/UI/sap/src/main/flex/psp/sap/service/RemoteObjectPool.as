package psp.sap.service
{
	import mx.collections.ArrayCollection;
	import mx.messaging.Channel;
	import mx.messaging.ChannelSet;
	import mx.messaging.config.ServerConfig;
	import mx.rpc.remoting.mxml.RemoteObject;
	
	public class RemoteObjectPool
	{
		private var mServiceDestination:String;
		private var mRemoteObjects:ArrayCollection = new ArrayCollection();
		private var mNextAvailable:int = -1;
		private var mPoolSize:int = 0;
		private var mShowBusyCursor:Boolean;
		
		public function RemoteObjectPool(serviceDestination:String, poolSize:int = 1, showBusyCursor:Boolean = false):void
		{
			mServiceDestination = serviceDestination;
			mPoolSize = poolSize;
			mShowBusyCursor = showBusyCursor;
			connect();	
		}
		
		public function get serviceDestination():String {
			return mServiceDestination;
		}
		
		public function get poolSize():int {
			return mPoolSize;
		}
		
		public function get showBusyCursor():Boolean { 
			return mShowBusyCursor;
		}
				
		public function nextAvailable():RemoteObject {
			if (mNextAvailable == -1)
				return null;
				
			var i:int = mNextAvailable;
			mNextAvailable++;
			if (mNextAvailable == mRemoteObjects.length)
				mNextAvailable = 0;
			
			return mRemoteObjects.getItemAt(i) as RemoteObject;		
		}
			
		public function connect():void {
			var defaultChannel:Channel = ServerConfig.getChannel("sap-secure-http");
			for (var i:int = 0; i < poolSize; i++) {
				var remoteObject:mx.rpc.remoting.mxml.RemoteObject = new RemoteObject(serviceDestination);
				remoteObject.showBusyCursor = showBusyCursor;
							
				var cs:ChannelSet = new ChannelSet();
				var newChannel:Channel = new SAPSecureHTTPChannel("sap-secure-http-" + serviceDestination + "-" + i, defaultChannel.endpoint);
				cs.addChannel(newChannel);
				
				remoteObject.channelSet = cs;
				mRemoteObjects.addItem(remoteObject);				
			}
			
			if (poolSize > 0)
				mNextAvailable = 0;			
		}
	}
}
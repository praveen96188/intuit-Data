package psp.sap.service
{
	import mx.messaging.MessageAgent;
	import mx.messaging.channels.SecureHTTPChannel;
	import mx.messaging.messages.IMessage;

	public class SAPSecureHTTPChannel extends SecureHTTPChannel
	{
		public function SAPSecureHTTPChannel(id:String=null, uri:String=null)
		{
			super(id, uri);
		}
		
        /**
         * The send method is called when any flex code calls a remote object service method.  This method
         * adds the corpId and authToken of the current user onto the request and then calls the super.send() method
         * to actually do the sending.  The SAPJavaObject.java adapter on the Java-side processes the corpId and
         * authorizationToken information and validates that the request is allowed.
         * @param agent
         * @param message
         */
        override public function send(agent:MessageAgent, message:IMessage):void {
        	SAPChannel.prepareSAPHandshake(agent, message);		
			super.send(agent, message);
		}		
	}
}
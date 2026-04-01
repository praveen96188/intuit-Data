package psp.sap.service
{
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.RemoteObject;
	
	import psp.sap.service.interfaces.IPSPSystemInformationService;
	
	public class PSPSystemInformationService extends PSPService implements IPSPSystemInformationService
	{
		private var pSPSystemInformationRemoteService:RemoteObject = new RemoteObject("pspsysteminformationservice");		
		
		public function PSPSystemInformationService()
		{
		}

		public function getSystemInformation(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(pSPSystemInformationRemoteService.getSystemInformation());
			remoteToken.addResponder(responder);
        }
		
		

	}
}
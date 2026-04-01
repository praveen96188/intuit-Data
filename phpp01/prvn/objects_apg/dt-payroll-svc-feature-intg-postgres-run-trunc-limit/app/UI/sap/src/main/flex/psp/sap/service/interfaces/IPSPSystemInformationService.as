package psp.sap.service.interfaces
{
	import mx.rpc.IResponder;
	
	public interface IPSPSystemInformationService extends IPSPService
	{

		function getSystemInformation(responder:IResponder):void;
		
	}
}
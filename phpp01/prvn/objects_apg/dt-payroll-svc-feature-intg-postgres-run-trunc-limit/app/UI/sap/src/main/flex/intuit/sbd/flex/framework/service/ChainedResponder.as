package intuit.sbd.flex.framework.service
{
	import mx.rpc.Responder;

	public class ChainedResponder extends Responder
	{
		public function ChainedResponder(result:Function, fault:Function)
		{
			super(result, fault);
		}
		
		public var chainedResult:Function;
		public var chainedFault:Function;
	}
}
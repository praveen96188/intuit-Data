package intuit.sbd.flex.framework.service {
import mx.rpc.Responder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

public class IgnoreResponder extends Responder{
        public function IgnoreResponder() {
            super(onResult,onFault);
        }

        private function onResult(e:ResultEvent):void {
			// ignore
		}
		private function onFault(e:FaultEvent):void {
			// ignore
		}
    }
}
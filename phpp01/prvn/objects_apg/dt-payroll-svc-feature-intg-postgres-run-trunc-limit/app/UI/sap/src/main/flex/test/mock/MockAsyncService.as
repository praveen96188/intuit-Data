package test.mock
{
	import flash.events.TimerEvent;
	import flash.utils.Dictionary;
	import flash.utils.Timer;

import mx.rpc.Fault;
import mx.rpc.IResponder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;
	
	import psp.sap.service.interfaces.IPSPService;
	
	public class MockAsyncService extends Mock implements IPSPService
	{
		protected var returnValues:Dictionary = new Dictionary();
		protected var defaultReturnValues:Dictionary = new Dictionary();		 
	
		/**
		 * This function delays the request call so that the
		 * test framework thinks the request was asyncrouns
		 */ 
		protected function sendAsyncRequest(asyncRequest:Function):void {
			var timer:Timer = new Timer(1, 1);
   			timer.addEventListener(TimerEvent.TIMER_COMPLETE, asyncRequest);    				
   			timer.start();
		}

		override public function expects(methodName:String):Mock {
			var mock:Mock = super.expects(methodName);
            if (returnValues[currentMethod.name] == null) {
                returnValues[currentMethod.name] = [];
            }
			return mock;
		}

		override public function willReturnAsync(returnVal:Object):void {
			(returnValues[currentMethod.name] as Array).push(new AsyncReturn(AsyncReturn.RESULT, returnVal));
		}

        //use this if you don't want to willReturnAsync on every invocation
		public function setDefaultReturnVal(methodName:String, returnVal:Object):void {
			defaultReturnValues[methodName] = new AsyncReturn(AsyncReturn.RESULT, returnVal);
		}

        override public function willFaultAsync(error:String):void {
            (returnValues[currentMethod.name] as Array).push(new AsyncReturn(AsyncReturn.FAULT, error));
        }

		protected function getReturnVal(methodName:String):Object {
			var pair:AsyncReturn = getReturnPair(methodName);
            if (pair == null) {
                return null;
            } else {
                return pair.value;
            }
		}

        protected function getReturnPair(methodName:String):AsyncReturn {
			var vals:Array = returnValues[methodName];
			if (vals == null || vals.length == 0) {
				if (! (methodName in defaultReturnValues)) {
                    return null;
                } else {
                    return AsyncReturn(defaultReturnValues[methodName]);
                }
			} else {
                return AsyncReturn(vals.shift());
			}
		}

        protected function sendAsyncResult(responder:IResponder, methodName:String):void {
            sendAsyncRequest(
                function():void {
                    var pair:AsyncReturn = getReturnPair(methodName);
                    if (pair == null) {
                        responder.result(new ResultEvent(ResultEvent.RESULT));
                    } else if (pair.type == AsyncReturn.RESULT) {
                        responder.result(new ResultEvent(ResultEvent.RESULT, false, true, pair.value));
                    } else if (pair.type == AsyncReturn.FAULT) {
                        //this is how it's done on the server for whatever reason; probably not actually used anywhere.
                        responder.fault(new FaultEvent(FaultEvent.FAULT, false, true, new Fault("Server.Processing", "Error in method SomeAdapter." + methodName + "()." + String(pair.value), String(pair.value))));
                    }
                }
            );
        }

	
		public function connect():void { }
		public function close():void { }
		public function reset():void { }		
	}
}

class AsyncReturn {
    public static const RESULT:String = "result";
    public static const FAULT:String = "fault";    

    public function AsyncReturn(type:String, value:Object) {
        this.type = type;
        this.value = value;
    }

    public var type:String;
    public var value:Object;
}
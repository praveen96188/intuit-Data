package test.sap.application
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	
	import flexunit.framework.TestCase;
	
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	import mx.utils.ObjectUtil;

	public class TestCaseEx extends TestCase
	{
		// async event handling
		public static const DEFAULT_ASYNC_TIMEOUT:int = 5000;
		public var asyncTimeout:int = DEFAULT_ASYNC_TIMEOUT;		
		
		// action event tracking
   		protected var mTrackedActionEvents:Array = [];		
		protected var mLastEventFired: Event;
		protected var mActionEventHistory:ArrayCollection;
		
		// change event tracking
		protected var mPropertyChangeEventHistory:ArrayCollection;
		protected var mTrackedProperties:Array = []; 		
		
		public function TestCaseEx(methodName:String=null)
		{
			super(methodName);
		}
		
		override public function setUp():void {
			// action events
			mActionEventHistory = new ArrayCollection();   			
   			
   			// property change events
   			mPropertyChangeEventHistory = new ArrayCollection();			
		}
		
		override public function tearDown():void {
			super.tearDown();
			
			// action events
			mLastEventFired = null;
			mActionEventHistory.removeAll();
			
			// property change events			
   			mPropertyChangeEventHistory.removeAll();
   			mPropertyChangeEventHistory = null;			
		}
		
		public function trackEventsStart(eventDispatcher:EventDispatcher):void {
			trackActionEventsStart(eventDispatcher);
			trackPropertyChangeEventsStart(eventDispatcher);
		}
		
		public function trackEventsStop(eventDispatcher:EventDispatcher):void {
			trackActionEventsStop(eventDispatcher);
			trackPropertyChangeEventsStop(eventDispatcher);
		}			
		
		///------------------------------------------
		/// Action Event Handling
		///------------------------------------------
		public function set trackedEvents(value:Array):void {
			if (value == null) value = [];
			mTrackedActionEvents = value;			
		}
		
		public function trackActionEventsStart(eventDispatcher:EventDispatcher):void {
   			for each (var eventType:String in mTrackedActionEvents)
   				eventDispatcher.addEventListener(eventType, onEvent, false, int.MAX_VALUE, true);
		}
		
		public function trackActionEventsStop(eventDispatcher:EventDispatcher):void {
   			for each (var eventType:String in mTrackedActionEvents)
   				eventDispatcher.removeEventListener(eventType, onEvent);			
		}
			
		public function onEvent(event:Event):void {
			mActionEventHistory.addItem(event);
		}
		
		public function get lastEventCaught():Event {
			if (mActionEventHistory.length == 0) return null;
			return mActionEventHistory.getItemAt(mActionEventHistory.length - 1) as Event;
		}
		
		public function assertEventHistory(eventNames:Array):void {
			if (eventNames == null || eventNames.length == 0) return;
								
			if (mActionEventHistory == null)
				fail("Event tracking not setup.  Did you forget to call super.setUp() in your test case?");
									
			if (mActionEventHistory.length == 0)
				fail("Event history is empty -- did you track the correct events?");									
									
			var i:int = mActionEventHistory.length - eventNames.length;
			if (i < 0)
				fail("Event history length (" + mActionEventHistory.length + ") is less than the number of expected events (" + eventNames.length + ").");
				
			for each (var eventName:String in eventNames) {
				if (i < 0)
					fail("Action event history exhausted.  No record on stack to compare against verification index: " + i + " event: " + eventName);
				
				assertEquals("eventName", eventName, Event(mActionEventHistory.getItemAt(i)).type);
				i++;
			}
		}

        protected function clearEventHistory():void {
   			mActionEventHistory.removeAll();
   		}
		
		///------------------------------------------
		/// PropertyChangeEvent Handling
		///------------------------------------------
		public function trackPropertyChangeEventsStart(eventDispatcher:EventDispatcher):void {
   			eventDispatcher.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChangeEvent, false, 0, true);			
		}
		
		public function trackPropertyChangeEventsStop(eventDispatcher:EventDispatcher):void {
   			eventDispatcher.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onPropertyChangeEvent);			
		}		
				
   		protected function onPropertyChangeEvent(e:PropertyChangeEvent):void {
   			if (mTrackedProperties.indexOf(e.property) != -1)
   				mPropertyChangeEventHistory.addItem(e);
   		}
   		
   		protected function set trackedProperties(value:Array):void {
   			mPropertyChangeEventHistory.removeAll();
   			mTrackedProperties = value;
   		}
   		
   		public function assertPropertyChangeEventHistory(propertiesToVerify:Array, clearHistory:Boolean = false):void {
   			// check from the top of the stack on down
   			var i:int = mPropertyChangeEventHistory.length - 1;  			
   			for each (var verificationValues:Object in propertiesToVerify) {

				if (i < 0)
	   				fail("PropertyChange event history exhausted.  No record on stack to compare against verification index: " + i + " property: " + verificationValues["property"]);   				
	
	   			var e:PropertyChangeEvent = mPropertyChangeEventHistory.getItemAt(i) as PropertyChangeEvent;
	  			
	   			for each (var attribute:String in ["property", "newValue"]) {
		   			if (verificationValues[attribute] != null) {
		   				
		   				//Adding date compare
		   				if(attribute == "newValue" && (verificationValues[attribute] is Date || e[attribute] is Date))
		   				{
		   					//For some reason assertEquals does not work on dates, but the compare on the toString does.
		   					var firstDateString:String = (verificationValues[attribute]).toString();
		   					var secondDateString:String = (e[attribute] != null) ? e.newValue.toString() : null;
		   					
		   					assertEquals(attribute, firstDateString, secondDateString); 
		   				} else {
		   					assertEquals(attribute, verificationValues[attribute], e[attribute]); 	
		   				}
		   				 
		   			}   				
	   			}
	   			
	   			i--;	
   			}
   			
   			if (clearHistory)
   				clearPropertyChangeEventHistory();
   		}
   		
   		protected function clearPropertyChangeEventHistory():void {
   			mPropertyChangeEventHistory.removeAll();	
   		}		
		
		/*
		 * If you happen to see a async verifier call the wrong method check to make sure an event that
         * another async verifier is listening for is not being fired twice. The async helper has an array of event
         * listeners that it calls and it does not check the event type. So it is possible for a handler to get called
         * with an incorrect event type.  
		 */
   		protected virtual function addAsyncVerifier(eventSource:IEventDispatcher, 
   													eventName:String, 
   													callbackFunction:Function,
   													token:Object = null,
   													failFunction:Function = null,
   													multiplier:int = 1):void {
   			   														
   			var asyncCallComplete:Function = this.addAsync(callbackFunction, this.asyncTimeout * multiplier, token, failFunction);
   			eventSource.addEventListener(eventName, asyncCallComplete, false, 0, true);
   		}
   		
   		/*
   		protected virtual function defaultFailFunction(e:Object):void {
   			var message:String = "";
   			if (e != null)
   			 	message = e.toString();
   			fail(message);
   		}
   		*/
	}
}
package psp.sap.viewmodel.events
{
	import flash.events.Event;

	public class ViewModelEvent extends Event
	{
		public static const CLOSE:String = "viewModelCloseEvent";
		
		public static const LOGIN_SUCCEEDED:String = "loginSucceededEvent";
		public static const LOGIN_FAILED:String = "loginFailedEvent";

        public static const PRE_ACTIVATION_COMPLETE:String = "preActivationComplete";
		public static const ACTIVATED:String = "viewModelActivated";
		public static const DEACTIVATED:String = "viewModelDeactivated";
		
		public static const ACTIVE_PAGE_CHANGED:String = "activePageChangedEvent";
		
		public static const SAVE_SUCCEEDED:String = "saveSucceededEvent";
		
		public static const DATA_LOAD_INITIATED:String = "dataLoadInitiated";
		public static const DATA_LOAD_RESULTED:String = "dataLoadResulted";
		public static const DATA_LOAD_FAULTED:String = "dataLoadFaulted";
		
		public static const MODEL_DATA_SETUP_COMPLETED:String = "modelDataSetupCompleted";
		public static const MODEL_DATA_LOADED:String = "modelDataLoaded";		
		
		public static const BACKING_PROPERTIES_INITIALIZED:String = "backingPropertiesInitialized";

        public static const BACKING_PROPERTY_CHANGED:String = "backingPropertyChanged";
        public static const CONTEXT_PROPERTY_CHANGED:String = "contextPropertyChanged";
		public static const ALERT_MSG:String = "alertMsgEvent";
		
		/**
		 * Any additional data the event wishes to carry.
		 */
		public var data:Object = null;		
		
		public function ViewModelEvent(type:String, bubbles:Boolean = false, cancelable:Boolean = false)
		{
			super(type, bubbles, cancelable);
		}
		
		override public function clone():Event {
			var e:ViewModelEvent = new ViewModelEvent(this.type, this.bubbles, this.cancelable);
			e.data = this.data;
			return e;
		}
			
		public static function createCloseEvent():ViewModelEvent {
			return new ViewModelEvent(CLOSE);
		}
		
		public static function createLoginSucceededEvent():ViewModelEvent {
			return new ViewModelEvent(LOGIN_SUCCEEDED);
		}
		
		public static function createLoginFailedEvent():ViewModelEvent {
			return new ViewModelEvent(LOGIN_FAILED);
		}		

		public static function createActivePageChangedEvent():ViewModelEvent {
			return new ViewModelEvent(ACTIVE_PAGE_CHANGED);
		}
		
		public static function createSaveSucceededEvent():ViewModelEvent {
			return new ViewModelEvent(SAVE_SUCCEEDED);
		}

		public static function createDataLoadInitiated():ViewModelEvent {
			return new ViewModelEvent(DATA_LOAD_INITIATED);
		}

		public static function createDataLoadResulted():ViewModelEvent {
			return new ViewModelEvent(DATA_LOAD_RESULTED);
		}

		public static function createDataLoadFaulted():ViewModelEvent {
			return new ViewModelEvent(DATA_LOAD_FAULTED);
		}

        public static function createPreActivationEvent():ViewModelEvent {
            return new ViewModelEvent(PRE_ACTIVATION_COMPLETE);
        }

        public static function createActivatedEvent():ViewModelEvent {
			return new ViewModelEvent(ACTIVATED);
		}
		
		public static function createDeactivatedEvent():ViewModelEvent {
			return new ViewModelEvent(DEACTIVATED);
		}
		
		public static function createModelDataLoadedEvent():ViewModelEvent {
			return new ViewModelEvent(MODEL_DATA_LOADED);
		}			
		
		public static function createModelDataSetupCompletedEvent():ViewModelEvent {
			return new ViewModelEvent(MODEL_DATA_SETUP_COMPLETED);
		}
		
		public static function createBackingPropertiesInitializedEvent():ViewModelEvent {
			return new ViewModelEvent(BACKING_PROPERTIES_INITIALIZED);
		}

        public static function createBackingPropertyChangedEvent():ViewModelEvent {
            return new ViewModelEvent(BACKING_PROPERTY_CHANGED);
        }

        public static function createContextPropertyChangedEvent():ViewModelEvent {
            return new ViewModelEvent(CONTEXT_PROPERTY_CHANGED);
        }

	}
}
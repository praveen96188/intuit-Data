package psp.taxcredits.model {
    import flash.events.Event;

    public class TaxCreditsEvent extends Event {

        public static const ELIGIBILITY_CHANGE:String = "eligibilityChange";
        public static const CATEGORIES_LOADED:String = "categoriesLoaded";
        public static const MODEL_INITIALIZED:String = "modelInitialized";
        public static const APPLICATION_SUBMITTED:String = "applicationSubmitted";


        public function TaxCreditsEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
            super(type, bubbles, cancelable);
        }

        public static function createEligibilityChangeEvent():TaxCreditsEvent {
            return new TaxCreditsEvent(ELIGIBILITY_CHANGE);
        }

        public static function createCategoriesLoadedEvent():TaxCreditsEvent {
            return new TaxCreditsEvent(CATEGORIES_LOADED);
        }

        public static function createApplicationSubmittedEvent():TaxCreditsEvent {
            return new TaxCreditsEvent(APPLICATION_SUBMITTED);
        }

        public static function createModelInitializedEvent():TaxCreditsEvent {
            return new TaxCreditsEvent(MODEL_INITIALIZED);   
        }

    }
}
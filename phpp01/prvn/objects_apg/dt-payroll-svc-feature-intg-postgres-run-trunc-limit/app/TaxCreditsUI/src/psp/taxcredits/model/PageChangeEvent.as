package psp.taxcredits.model {
    import flash.events.Event;

    public class PageChangeEvent extends Event {

        public static const PAGE_CHANGE:String = "PageChange";

        public var page:String;
        public var taxCreditsPage:TaxCreditsPage;

        public function PageChangeEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
            super(type, bubbles, cancelable);
        }

        public static function createPageChangeEvent(taxCreditsPage:TaxCreditsPage):PageChangeEvent {
            var pce:PageChangeEvent = new PageChangeEvent(PAGE_CHANGE);
            pce.taxCreditsPage = taxCreditsPage;
            pce.page = taxCreditsPage.internalName;            
            return pce;
        }

    }
}
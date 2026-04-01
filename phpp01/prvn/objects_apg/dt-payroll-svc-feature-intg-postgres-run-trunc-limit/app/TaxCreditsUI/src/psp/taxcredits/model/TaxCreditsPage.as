package psp.taxcredits.model {
    public class TaxCreditsPage {

        [Bindable] public var internalName:String;
        [Bindable] public var externalName:String;
        [Bindable] public var index:int;
        [Bindable] public var subIndex:int=-1;
        [Bindable] public var visited:Boolean = false;
        [Bindable] public var setsFragment:Boolean = true;

        //which page, if any, to go to when this page is requested?
        [Bindable] public var navigatesTo:Function=defaultNavigatesFunction;

        public function TaxCreditsPage(internalName:String=null, externalName:String=null, index:int=0, subIndex:int=-1, visited:Boolean=false, setsFragment:Boolean=true) {
            this.internalName = internalName;
            this.externalName = externalName;
            this.index = index;
            this.subIndex = subIndex;
            this.visited = visited;
            this.setsFragment = setsFragment;
        }

        public function defaultNavigatesFunction(oldPage:TaxCreditsPage):TaxCreditsPage {
            //default behavior is to require page to have been visited; we are not using deep links

            //also can't move from a different "layer" (i.e. from a popup to the main)
            if (oldPage.subIndex >= 0 && this.subIndex < 0) {
                return null;
            }
            if (oldPage.subIndex < 0 && this.subIndex >= 0) {
                return null
            }
            if (! this.visited) {
                return null;
            }

            return this;
        }

        public function set navigatesFunctionExtra(fun:Function):void {
            navigatesTo = function(oldPage:TaxCreditsPage):TaxCreditsPage {
                var page:TaxCreditsPage = defaultNavigatesFunction(oldPage);
                if (page == null) {
                    return null;
                }
                return fun(oldPage);
            }
        }
    }
}
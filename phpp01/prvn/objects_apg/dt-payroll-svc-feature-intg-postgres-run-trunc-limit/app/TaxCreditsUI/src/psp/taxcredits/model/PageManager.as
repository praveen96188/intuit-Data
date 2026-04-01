package psp.taxcredits.model {
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;
    import mx.resources.ResourceManager;

    import psp.taxcredits.swfaddress.SWFAddress;

    [Event(name="pageChange", type="psp.taxcredits.model.PageChangeEvent")]
    public class PageManager extends EventDispatcher {

        [Bindable] public var currentPage:String;
        [Bindable] public var currentIndex:int=-1;
        [Bindable] public var currentSubIndex:int=-1;

        [ArrayElementType("TaxCreditsPage")]
        private var pages:ArrayCollection;

        //the order it always navigates
        [ArrayElementType("String")]
        private var breadCrumbOrder:Array = [
            TaxCreditsPageEnum.INTRO,
            TaxCreditsPageEnum.ELIGIBILITY,
            TaxCreditsPageEnum.ESTIMATE,
            TaxCreditsPageEnum.PRICING,
            TaxCreditsPageEnum.COMPANY_INFO,
            TaxCreditsPageEnum.FINAL_STEPS];



        public function PageManager() {
            createPages();
        }

        public function initialize():void {
            SWFAddress.onChange = handleSWFAddress;
            setSWFAddress();
        }

        private function handleSWFAddress():void
        {
            var value:String = SWFAddress.getValue();
            value = value.replace(/\//g, "");
            value = value.replace(/-/g, " ");

            var page:TaxCreditsPage = getPageByExternalName(value);

            if (value == null || value == "") {
                page = getPageByInternalName(TaxCreditsPageEnum.INTRO);        
            }


            if (page.internalName != currentPage) {
                var currentTCPage:TaxCreditsPage = getPageByInternalName(currentPage);
                var navigateTo:TaxCreditsPage = page.navigatesTo(currentTCPage);

                if (navigateTo != null) {
                    switchPage(navigateTo.internalName);
                }
            }

        }


        public function setSWFAddress():void
        {
            var page:TaxCreditsPage = getPageByInternalName(currentPage);
            if (page != null && page.setsFragment) {
                if (currentPage == TaxCreditsPageEnum.INTRO) {
                    SWFAddress.setValue("");
                } else {
                    SWFAddress.setValue("/" + formatAsURL(getPageByInternalName(currentPage).externalName) + "/");
                }
            }
        }

        public function switchPage(newPage:String):void {
            var page:TaxCreditsPage = getPageByInternalName(newPage);

            currentPage = page.internalName;
            currentIndex = page.index;
            currentSubIndex = page.subIndex;

            setSWFAddress();

            page.visited = true;

            if (page.internalName == TaxCreditsPageEnum.INTRO) {
                SWFAddress.setTitle(ResourceManager.getInstance().getString("pages", "browser_title_home"));
            } else {
                SWFAddress.setTitle(ResourceManager.getInstance().getString("pages", "browser_title", [formatAsTitle(page.externalName)]));
            }

            dispatchEvent(PageChangeEvent.createPageChangeEvent(page));

            TaxCreditsModel.instance.tracker.trackPageview("/"+newPage);
        }

        public function buildBreadCrumbList(currentPage:String):ArrayCollection {
            var currentPageIndex:int = breadCrumbOrder.indexOf(currentPage);
            if (currentPageIndex < 0) {
                return new ArrayCollection();
            } else {
                return new ArrayCollection(breadCrumbOrder.slice(0, currentPageIndex + 1));
            }
        }

        private function createPages():void {
            pages = new ArrayCollection();

            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.INTRO, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.INTRO),0, -1, true));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.ELIGIBILITY, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.ELIGIBILITY),1, -1, true)); //enable deep linking on this one for future           
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.ESTIMATE, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.ESTIMATE),2));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.PRICING, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.PRICING),3));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.COMPANY_INFO, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.COMPANY_INFO),4));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.FINAL_STEPS, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.FINAL_STEPS),5));

            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewPersonalInformation, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewPersonalInformation),1,0));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionASNAP, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionASNAP),1,1));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionBTANF, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionBTANF),1,2));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionCReferral, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionCReferral),1,3));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionDVeteran, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionDVeteran),1,4));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionEDisconnectedYouth, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionEDisconnectedYouth),1,5));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionFFelon, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionFFelon),1,6));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewSectionGOther, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewSectionGOther),1,7));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewDates, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewDates),1,8));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewLocation, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewLocation),1,9));
            pages.addItem(new TaxCreditsPage(TaxCreditsPageEnum.InterviewChecking, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewChecking),1,10,false,false));

            var interviewIneligiblePage:TaxCreditsPage = new TaxCreditsPage(TaxCreditsPageEnum.InterviewIneligible, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewIneligible),1,11);
            interviewIneligiblePage.navigatesFunctionExtra = function(oldPage:TaxCreditsPage):TaxCreditsPage { return getPageByInternalName(TaxCreditsPageEnum.InterviewChecking) };
            pages.addItem(interviewIneligiblePage);

            var interviewEligiblePage:TaxCreditsPage = new TaxCreditsPage(TaxCreditsPageEnum.InterviewEligible, ResourceManager.getInstance().getString("pages", TaxCreditsPageEnum.InterviewEligible),1,12);
            interviewEligiblePage.navigatesFunctionExtra = function(oldPage:TaxCreditsPage):TaxCreditsPage { return getPageByInternalName(TaxCreditsPageEnum.InterviewChecking) };
            pages.addItem(interviewEligiblePage);
        }

        public function getPageByIndex(index:int, subIndex:int=-1):TaxCreditsPage {
            for each (var page:TaxCreditsPage in pages) {
                if (page.index == index && page.subIndex == subIndex) {
                    return page;
                }
            }
            return null;
        }

        public function getPageByInternalName(label:String):TaxCreditsPage {
            for each (var page:TaxCreditsPage in pages) {
                if (page.internalName == label) {
                    return page;
                }
            }
            return null;
        }

        public function getPageByExternalName(label:String):TaxCreditsPage {
            for each (var page:TaxCreditsPage in pages) {
                if (page.externalName == label) {
                    return page;
                }
            }
            return null;
        }

        private function formatAsURL(t:String):String
        {
            return t.replace(/ /g,"-");
        }

        private function formatAsTitle(t:String):String
        {
            return t.replace(/-/g," ");
        }
        

    }
}
package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;

    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.logging.ILogger;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.collections.PartViewModelCollection;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class InspectorTopicViewModel extends EventDispatcher
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		
		private var mIsActivating:Boolean = false;
		protected var mIdentifier:String = null;
		protected var mLabel:String = null;
		protected var mInspector:AbstractInspectorViewModel;

		protected var mPages:PartViewModelCollection = new PartViewModelCollection();
		protected var mActivePage:AbstractPartViewModel;
		protected var mDefaultPage:AbstractPartViewModel;
		
		public function InspectorTopicViewModel(inspector:AbstractInspectorViewModel, label:String)
		{
			mInspector = inspector;
			mIdentifier = label;
			this.label = label;			
		}
		
		[Bindable]
		public function get label():String {
			return mLabel;
		}
		
		public function set label(value:String):void {
			mLabel = value;
		}
		
		public function get inspector():AbstractInspectorViewModel {
			return mInspector;
		}
		
		public function get pages():PartViewModelCollection {
			return mPages;
		}
			
		public function goToPreviousPage():void {
			inspector.pageHistory.goToPreviousPage();
		}
			
		/**
		 * Activation Use Cases:
		 * 1.0  The topic is not active, and has no activePage (being viewed for the first time), pageToActivate is null
		 *      a) activate topic on inspector
		 * 		b) activate defaultPage
		 * 1.1  The topic is not active, and has no activePage (being viewed for the first time), pageToActivate is not null
		 *      a) activate topic on inspector
		 * 		b) activate pageToActivate		 
		 * 2.0  The topic is not active, and has an activePage, pageToActivate is null
		 * 		a) activate topic on inspector
		 * 		b) activate defaultPage
		 * 2.1  The topic is not active, and has an activePage, pageToActivate is not null
		 * 		a) activate topic on inspector
		 * 		b) activate pageToActivate
		 * 3.0  The topic is active, has an activePage, and the pageToActivate is null
		 * 		a) activate
		 * 3.1  The topic is active, has an activePage, and the pageToActivate is not null   
		 */
		 
		virtual public function activate(pageToActivate:AbstractPartViewModel = null):void {
			var pageLabel:String = (pageToActivate != null) ? pageToActivate.label : "null";
			logger.info("activate called with page: " + pageLabel + " mIsActivating = " + mIsActivating);
			
			if (mIsActivating)
				return;
			
			mIsActivating = true;
			
			//TODO: will need different semantics for 'activate' vs 'open' at some point
			// activate would not set/change the active page, open would allow that specification
			if(pageToActivate == null && activePage != null){
				// must be the trying to activate the "current" page
				pageToActivate = activePage;
				mActivePage = null;
			}
			else if (pageToActivate == null)
				pageToActivate = defaultPage;
			
			activePage = pageToActivate;			
			
			if (!isActive) {
				inspector.activate(this);
			}
									
			mIsActivating = false;
		}
		
		[Bindable]
		public function set activePage(value:AbstractPartViewModel):void {
			
			if (value == mActivePage)
				return;
				
			var pageLabel:String = value != null ? value.label : "null";	
			logger.info("activePage changed page: " + pageLabel);
			
			if (mActivePage != null)
				mActivePage.deactivate();
				
			mActivePage = value;
			
			if (mActivePage != null) {
                mActivePage.activate();
            }

		}
		
		public function get activePage():AbstractPartViewModel {
			return mActivePage;
		}		
				
		virtual public function deactivate():void {
			if (mActivePage != null)
				mActivePage.deactivate();
				
			mActivePage = null;
		}
		
		virtual public function close():void {
			
		}		
		
		public function get isActive():Boolean {
			return inspector.isActive && inspector.activeTopic == this;
		}
		
		[Bindable]
		public function get defaultPage():AbstractPartViewModel {
			if (mDefaultPage == null) {
                if (pages.length == 0) {
                    return null;
                } else {
                    return AbstractPartViewModel(ArrayCollection(pages).getItemAt(0));
                }    
            } else {
                return mDefaultPage;
            }
		}
		
		public function set defaultPage(value:AbstractPartViewModel):void {
			mDefaultPage = value;
		}		
		
		/**
		 * walks the tree to find the part
		 * if there are multiples of the same label, it will just return the first
		 */
		public function findPart(label:String):AbstractPartViewModel {
            for each (var part:AbstractPartViewModel in pages) {
                var subPart:AbstractPartViewModel = part.findPartByLabel(label);
                if (subPart != null) {
                    return subPart;
                }
            }

            return null;
        }

        public function findPage(label:String):SinglePartPageViewModel {
            return findPart(label+"_page") as SinglePartPageViewModel;
        }

        public function addPagePart(
            baseLabel:String,
            pageLabel:String=null
                ):SinglePartPageViewModel {

            var sppvm:SinglePartPageViewModel = new SinglePartPageViewModel();
            sppvm.inspector = this.inspector;
            sppvm.topic = this;
            sppvm.label = baseLabel + "_page";
            if (pageLabel != null) {
                sppvm.pageLabel = pageLabel;
            } else {
                sppvm.pageLabel = baseLabel;
            }


            if ("company" in inspector) {
                BindingUtils.bindProperty(sppvm, "company", inspector, "company");
            }
            if ("companyKey" in inspector) {
                BindingUtils.bindProperty(sppvm, "companyKey", inspector, "companyKey");
            }

            sppvm.addEventListener(ViewModelEvent.ACTIVATED, function(e:ViewModelEvent):void {
                activate(sppvm);
            }, false, 0, false);
            sppvm.addEventListener(ViewModelEvent.CLOSE, function(e:ViewModelEvent):void {
                goToPreviousPage();                
            }, false, 0, false);

            pages.addItem(sppvm);

            return sppvm;
        }

        public function addSinglePart(
                baseLabel:String,
                partClass:Class,
                pageLabel:String=null
                ):SinglePartPageViewModel {
            var sppvm:SinglePartPageViewModel = addPagePart(baseLabel, pageLabel);
            sppvm.addNewPart(partClass, baseLabel);
            return sppvm;
        }
        
	}
}
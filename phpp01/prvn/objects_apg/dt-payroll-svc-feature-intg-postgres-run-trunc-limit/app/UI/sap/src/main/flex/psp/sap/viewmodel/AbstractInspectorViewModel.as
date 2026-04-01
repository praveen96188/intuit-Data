package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;

    import intuit.sbd.flex.framework.model.EntityObject;

    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.collections.CompanyInspectorTopicCollection;
    import psp.sap.application.collections.PageHistoryCollection;
    import psp.sap.application.collections.PartViewModelCollection;
    import psp.sap.application.events.InspectorActionEvent;
    import psp.sap.application.events.InspectorPropertyChangeEvent;

    /**
	 * An Inspector is a 'top level' window for displaying a single application entity.
	 * 
	 * Typically, an inspector has topics (think tabs) and each topic may have
	 * a set of pages (think more tabs, so tabs inside of tabs)			
	 */
	public class AbstractInspectorViewModel
		extends EventDispatcher        
	{
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        public var explorer:AbstractExplorer;

		protected var mApplicationItem:EntityObject;
		protected var mViewModelIsDirty:Boolean = false;
		
		private var mTopics:CompanyInspectorTopicCollection = new CompanyInspectorTopicCollection();
		private var mActiveTopic:InspectorTopicViewModel;
		private var mDefaultTopic:InspectorTopicViewModel;
		
		public var pageHistory:PageHistoryCollection = new PageHistoryCollection();
		
		// this is used to keep disabled inspectors from activating
		public var enabled:Boolean = true;
		
		public function AbstractInspectorViewModel(explorer:AbstractExplorer) {
			super();
            this.explorer = explorer;
			topics.addEventListener(CollectionEvent.COLLECTION_CHANGE, onTopicCollectionChanged, false, 0, true);
		}
		
		//-----------------------------
		// IInspector
		//-----------------------------
		[Bindable]
		public function get applicationItem():EntityObject {
			return mApplicationItem;
		}
		
		public function set applicationItem(value:EntityObject):void {
			mApplicationItem = value;	
		}
		
		[Bindable]
		public function get defaultTopic():InspectorTopicViewModel {
			if (mDefaultTopic != null) {
                return mDefaultTopic;
            } else {
                if (topics.length == 0) {
                    return null;
                } else {
                    return InspectorTopicViewModel((topics as ArrayCollection).getItemAt(0));
                }
            }
		}
		
		public function set defaultTopic(value:InspectorTopicViewModel):void {
			mDefaultTopic = value;
		}

		private var mIsActivating:Boolean = false;
		virtual public function activate(topicToActivate:InspectorTopicViewModel = null):void {
			var topicLabel:String = (topicToActivate != null) ? topicToActivate.label : "null";
			logger.info("activate called with topic: " + topicLabel + " mIsActivating = " + mIsActivating);
			
			if (mIsActivating)
				return;
			
			mIsActivating = true;
			
			if (topicToActivate != null)
				activeTopic = topicToActivate;
			else if (topicToActivate == null && mActiveTopic == null && defaultTopic != null)
				activeTopic = defaultTopic;
			else if (topicToActivate == null && mActiveTopic != null){
				// must be trying to activate the "current" topic
				activeTopic.activate();
			}
			
			if (!isActive) {
				if (explorer == null) {
					//TODO: add a getDefaultExplorer(type:Class) to explorers collection to allowing adding?
					return;
				}	
				explorer.activate(this);
			}
			
			mIsActivating = false;						
		}
			
		[Bindable]
		public function get activeTopic():InspectorTopicViewModel {
			return mActiveTopic;
		}
		
		public function set activeTopic(value:InspectorTopicViewModel):void {
			if (value == mActiveTopic)
				return;
			
			var topicLabel:String = value != null ? value.label : "null";	
			logger.info("activeTopic changed topic: " + topicLabel);

			// this is a hack, see activePageLabel comments
			var oldPage:AbstractPartViewModel = mActiveTopic != null ? mActiveTopic.activePage : null;
					
			if (mActiveTopic != null)
				mActiveTopic.deactivate();
			
			mActiveTopic = value;
			
			if (mActiveTopic != null)
				mActiveTopic.activate();
				
			var newPage:AbstractPartViewModel = mActiveTopic != null ? mActiveTopic.activePage : null;
			dispatchActivePageChanged(oldPage, newPage);
		}		
		
		public function get isActive():Boolean {
			var activeExplorer:AbstractExplorer = SAP.instance.activeExplorer as AbstractExplorer;
			if (activeExplorer == null)
				return false;
				 
			return activeExplorer.activeInspector == this;
		}
		
		virtual public function deactivate():void {
			if (mActiveTopic != null)
				mActiveTopic.deactivate();
		}
		
		virtual public function close():void {										
			mActiveTopic = null;
			topics.removeAll();	
			
			dispatchEvent(InspectorActionEvent.createCloseEvent());					
		}		
		
		[Bindable("propertyChange")]
		public function get topics():CompanyInspectorTopicCollection {
			return mTopics;
		}
		
		private function onTopicCollectionChanged(e:CollectionEvent):void {
			if (e.kind == CollectionEventKind.ADD) {
				for each (var addedTopic:InspectorTopicViewModel in e.items) {
					addedTopic.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onActivePageChanged, false, 0, true);
				}
			}
			else if (e.kind == CollectionEventKind.REMOVE) {
				for each (var removedTopic:InspectorTopicViewModel in e.items) {
					removedTopic.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onActivePageChanged);
				}
			}
		}	
		
		public function getAllPages():PartViewModelCollection {
			var pages:PartViewModelCollection = new PartViewModelCollection();
			for each (var topic:InspectorTopicViewModel in this.topics) {
				for each (var page:AbstractPartViewModel in topic.pages) {
					pages.addItem(page);
				}
			}
			
			return pages;
		}		


		//----------------------------------------
		// hack starts
		//----------------------------------------
		[Bindable("propertyChange")]
		public function get activePage():AbstractPartViewModel {
			if (mActiveTopic == null)
				return null;
				
			return mActiveTopic.activePage;
		}

		/**
		 * This property exists to support the InsecptorView.  The InspectorView
		 * has a viewstack with all pages supported by the inspector.  This
		 * does not match the true model of the application which has an intermediate
		 * 'topic' level so there is a mismatch between the view and application view model.
		 * 
		 * The ViewStack on the InspectorView that controls the visible pageView, is tied
		 * to these properties.  In reality, the inspector view's ViewStack should have
		 * a set of Views, one per topic.  The topic's view should be a ViewStack with
		 * one view per page in the topic.
		 * 
		 * Until these models are synchronized, this property represents the link and it
		 * must be updated any time the active page of a topic changes -- either b/c of
		 * a page flip within a topic or due to a new topic becoming active.
		 */ 
		[Bindable]
		public function get activePageLabel():String {
			return activePage != null ? activePage.label : "";
		}
		
		public function set activePageLabel(value:String):void {
			var page:AbstractPartViewModel = getPage(value);
			if (page == null)
				throw new Error("Page " + value + " not found.");
			
			if (page != null) {
				page.activate();				
			}


		}
		
		private function onActivePageChanged(e:PropertyChangeEvent):void {
			if (e.property != "activePage")
				return;	
				
			if (e.source == mActiveTopic) {
				dispatchActivePageChanged(AbstractPartViewModel(e.oldValue), AbstractPartViewModel(e.newValue));
			}
		}
		
		private function dispatchActivePageChanged(oldPage:AbstractPartViewModel, newPage:AbstractPartViewModel):void {
			var pce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, "activePage", oldPage, newPage);
			dispatchEvent(pce);
			
			var ipce:InspectorPropertyChangeEvent = InspectorPropertyChangeEvent.createActivePageChanged(oldPage, newPage);
			dispatchEvent(ipce);
			
			var oldLabel:String = oldPage != null ? oldPage.label : null;
			var newLabel:String = newPage != null ? newPage.label : null;
			var activePageLabelChanged:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, "activePageLabel", oldLabel, newLabel);
			dispatchEvent(activePageLabelChanged); 			
		}
		
		//----------------------------------------
		// hack ends
		//----------------------------------------		
			
		/**
		 * Return the requested page by searching through each topic's pages in the 
		 * topics collection.
		 */		
		public function getPage(pageEnumValue:String):SinglePartPageViewModel {
			var page:SinglePartPageViewModel = null;
			
			for each (var topic:InspectorTopicViewModel in topics) {
				page = topic.findPage(pageEnumValue);
				if (page != null)
					return page;
			}
			
			return page;			
		}

        public function findPage(pageEnumValue:String):SinglePartPageViewModel {
            return getPage(pageEnumValue);
        }

		public function findPart(pageEnumValue:String):AbstractPartViewModel {
			var page:AbstractPartViewModel = null;

			for each (var topic:InspectorTopicViewModel in topics) {
				page = topic.findPart(pageEnumValue);
				if (page != null)
					return page;
			}

			return page;
		}
		
		virtual public function permissionGranted():Boolean {
			 return (topics.length > 0);
		}
		
		virtual public function refresh():void{}

        public function get persistentLabel():String {
            return "";
        }
		
	}
}
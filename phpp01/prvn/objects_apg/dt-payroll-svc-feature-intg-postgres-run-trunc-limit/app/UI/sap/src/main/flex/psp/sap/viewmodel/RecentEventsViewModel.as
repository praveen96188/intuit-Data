package psp.sap.viewmodel
{
	import flash.events.TextEvent;
	
	import mx.collections.ArrayCollection;
	import mx.formatters.DateFormatter;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.CompanyEventHandler;
	import psp.sap.application.SAP;
	import psp.sap.model.CompanyEventItem;
import psp.sap.model.companyevents.CompanyEventDetail;

public class RecentEventsViewModel extends AbstractPartViewModel
	{
		
		[ArrayElementType("psp.sap.model.CompanyEventItem")]		
		private var recentEvents:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var eventsHtmlText:String;
		
		private var eventHandler:CompanyEventHandler;
		
		private var dateFormatter:DateFormatter;
		
		private var numEvents:int=5;
						
		public function RecentEventsViewModel()
		{					
			dateFormatter = new DateFormatter();
			dateFormatter.formatString = SAP.instance.configuration.dateTimeFormatShort;
		}

		override public function set inspector(value:AbstractInspectorViewModel):void {
			super.inspector = value;
			eventHandler = new CompanyEventHandler(value as CompanyInspectorViewModel);
		}

		public function getMore():void {
			numEvents+=5;
			refresh();
		}
		
		override protected function loadModelData():void {
			SAP.instance.companyService.getRecentCompanyEvents(
						companyKey.sourceSystemCd,
						companyKey.companyId,
						numEvents,
						createLoadModelDataResponder(onEventsLoaded)); 	
		}
		
		private function onEventsLoaded(e:ResultEvent):void {
			recentEvents = e.result as ArrayCollection;
		}
		
		override protected function initializeBackingProperties():void {
			var newText:String = "";
			if (recentEvents.length == 0) {
				newText = "No events found";
			} else {
				for each (var event:CompanyEventItem in recentEvents) {
					newText += "<a href='event:goEvent=" + event.id + "'>" + dateFormatter.format(event.eventDate) + "</a>";
					newText += " ";
					newText += eventHandler.buildDescription(event.eventTypeDescription, event);
                    for each(var eventDetail:CompanyEventDetail in event.companyEventDetails){
                        if(eventDetail.eventDetailTypeCd== "CaseId")
                            newText +=". Case Id is "+eventDetail.value;
                    }
					newText += "<br>";	
				}
			}
			//want it?
			//newText += "<a href='event:more'>More</a>";
			eventsHtmlText = newText;
		}
		
		public function onLink(e:TextEvent):void {
			if (e.text == "more") {
				getMore();
			} else {
				eventHandler.onLink(e);
			}
		}
				

		
	}
}
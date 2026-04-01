package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.model.CompanyEventItem;
	import psp.sap.model.CompanyNote;

	public class CompanyEventLogAddNoteViewModel extends AbstractPartViewModel
	{	
		
		private const MAX_NOTE_LENGTH:Number = 3998;
		
		private var mHasNotes:Boolean;
		private var mNoteText:String = "";	
		private var mCompanyNotes:ArrayCollection;
		private var mNotes:ArrayCollection = new ArrayCollection();
		private var mIsEditing:Boolean = false;
		private var mShowNotes:Boolean = false;
		
		private var newNote:CompanyNote = null;
		
		//context
		public var selectedEvent:CompanyEventItem = null;
		
		private static const DEFAULT_mNoteText:String = "";
		private static const DEFAULT_mIsEditing:Boolean = false;	
		private static const DEFAULT_mNotes:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var charactersRemaining:Number = MAX_NOTE_LENGTH;
		
		public function CompanyEventLogAddNoteViewModel()
		{
			this.label = CompanyInspectorPageEnum.EVENT_LOG_ADD_NOTE;
			
			//Defaults
			hasNotes = false;
			notes = DEFAULT_mNotes;
		}
		
		override protected function onActivated():void {
			
			//On activate
			updateShowNotes();
		}
				
		public function updateShowNotes():void {
			showNotes = selectedEvent != null;
		}		
				
		[Bindable]
		public function get showNotes():Boolean { 
			return mShowNotes;
		}		
		
		public function set showNotes(value:Boolean):void {
			mShowNotes = value;
		}
				
				
		[Bindable]
		public function get notes():ArrayCollection {
			return mNotes;
		}		
		
		public function set notes(value:ArrayCollection):void {
			mNotes = value;
		}
				
		[Bindable]
		public function get hasNotes():Boolean {
			return mHasNotes;
		}		
		
		public function set hasNotes(value:Boolean):void {
			mHasNotes = value;
		}
				
		[Bindable]
		public function get noteText():String {
			return mNoteText;
		}
		
		public function set noteText(value:String):void {
			mNoteText = value;
			charactersRemaining = MAX_NOTE_LENGTH - mNoteText.length;
			updateCanSave();
		}
		
		public function cancelNote():void {
			cancel();
		}
		
		[Bindable]
		public function get isEditing():Boolean {
			return mIsEditing;
		}
		
		public function set isEditing(value:Boolean):void {
			mIsEditing = value;
			updateCanSave();
		}
		
		
		override public function get hasChanged():Boolean {
			return (mNoteText != DEFAULT_mNoteText);
		}
		
		override protected function evaluateCanSave():Boolean {
			//todo verify if the old behavior is needed
			//return super.evaluateCanSave() && isDataLoaded;
			return super.evaluateCanSave();
		}
			
		override protected function initializeBackingProperties():void {
			noteText = DEFAULT_mNoteText;
			isEditing = DEFAULT_mIsEditing;
		}	
		
		private function prepareNewNote():void {
			newNote = new CompanyNote();
			newNote.notes = noteText;
			newNote.insertUserId = SAP.instance.session.user.corpId;
			newNote.createdDate = SAP.instance.PSPDate;
            newNote.alert = false;
		}
		
		override protected function loadModelData():void {			
			
			if(selectedEvent != null) 
			{
				SAP.instance.companyService.findCompanyNotes(company.sourceSystemCd,
															 company.companyId,
															 selectedEvent.id,
                                                             selectedEvent.transmissionId,
															 createLoadModelDataResponder(onLoadNoteSuccess));
			} else {				
				notes = new ArrayCollection();
				updateHasNotes();
				modelDataLoaded();
			}
		}

        public function addCompanyNote():void {
            saveMode = "addCompanyNote";
            save();
        }

        public function removeNoteAlert(noteId:String): void {
            this.noteId = noteId;
            saveMode = "removeNoteAlert";
            forceSave();
        }

        private var saveMode:String;
        private var noteId:String;

        override protected function executeSave():void {
            if (saveMode == "addCompanyNote") {
                isEditing = false;
                prepareNewNote();

                var eventId:String = null;
                var eventTransmissionId:String = null;

                if(selectedEvent != null) {
                    eventId = selectedEvent.id;
                    eventTransmissionId = selectedEvent.transmissionId;
                }

                SAP.instance.companyService.addCompanyNote(company.sourceSystemCd,
                        company.companyId,
                        eventId,
                        eventTransmissionId,
                        newNote,
                        createSaveResponder());
            } else {
                SAP.instance.companyService.removeCompanyNoteAlert(noteId, createSaveResponder());
            }
        }
		


		override public function cancel():void {
			isEditing = false;
			this.noteText = "";			
			super.cancel();
		}
					
		private function onLoadNoteSuccess(e:ResultEvent):void {			
			notes = e.result as ArrayCollection;	
			
			var sort:Sort = new Sort();
			sort.compareFunction = CompanyNote.notesCompareFunction;
			notes.sort = sort;
			notes.refresh();
			
			updateHasNotes();				
		}
						
		protected function updateHasNotes():void {
			hasNotes = (notes != null && notes.length > 0);
		}				
		
	}
}
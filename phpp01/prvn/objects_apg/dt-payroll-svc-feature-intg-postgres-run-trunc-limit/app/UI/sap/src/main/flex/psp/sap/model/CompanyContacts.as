package psp.sap.model
{
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.events.CollectionEvent;
	import mx.events.PropertyChangeEvent;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyContacts")]
	public class CompanyContacts extends EventDispatcher
	{
		public function CompanyContacts()
		{
			contacts = new ArrayCollection();
		}
		
		public var sourceSystemCd:String;
		public var companyId:String;

		private var mHasSecondaryContact:Boolean = false;
        private var mHasPayrollAdminContact:Boolean = false;
		private var mContacts:ArrayCollection;

		[ArrayElementType("psp.sap.model.Contact")]		
		public function get contacts():ArrayCollection {
			return mContacts;
		}
		
		public function set contacts(value:ArrayCollection):void {
			mContacts = value;
			if (mContacts != null) {
				onCollectionChanged(null);
				
				// put collection into a reasonable order
				var sort:Sort = new Sort();
				sort.compareFunction = function(a:Contact, b:Contact, fields:Array = null):int {
                    if (a == null && b == null) {
                        return 0;
                    } else if (a == null) {
                        return 1;
                    } else if (b == null) {
                        return -1;
                    } else if (a.newContactOrder == -1 && b.newContactOrder != -1) {
                        return -1;
                    } else if (b.newContactOrder == -1 && a.newContactOrder != -1) {
                        return 1;
                    } else if (a.newContactOrder < b.newContactOrder) {
                        return -1;
                    } else if (b.newContactOrder < a.newContactOrder) {
                        return 1;
                    } else if (a.contactRole.sortOrder < b.contactRole.sortOrder) {
                        return -1;
                    } else if (b.contactRole.sortOrder < a.contactRole.sortOrder) {
                        return 1;                        
                    } else if (a.getOriginalName() < b.getOriginalName()) { //don't change the order in middle of working since names can change
                        return -1;
                    } else if (b.getOriginalName() < a.getOriginalName()) {
                        return 1;
                    } else if (a.getRandomId() < b.getRandomId()) { //same role, same name; probably won't happen, but need to be orderable if it does
                        return -1;
                    } else if (b.getRandomId() < a.getRandomId()) {
                        return 1;
                    } else {      //same role, name, and random number: punt
                        return 0;
                    }
				};
				mContacts.sort = sort;
				mContacts.refresh();
				
				mContacts.addEventListener(CollectionEvent.COLLECTION_CHANGE, onCollectionChanged, false, 0, true);
			}
			else {
				mContacts = new ArrayCollection();
			}
		}
		
		private function onCollectionChanged(e:CollectionEvent):void {
			setHasSecondaryContact(containsContactType(mContacts, ContactRole.SECONDARY_PRINCIPAL));
            setHasPayrollAdminContact(containsContactType(mContacts, ContactRole.PAYROLL_ADMIN));
		}

        [Bindable ("propertyChange")]
        public function get hasSecondaryContact():Boolean {
        	return mHasSecondaryContact;
        }
        
        private function setHasSecondaryContact(value:Boolean):void {
    		var oldVal:Boolean = mHasSecondaryContact;
        	mHasSecondaryContact = value;
	    	dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "hasSecondaryContact", oldVal, mHasSecondaryContact));
        }

        [Bindable ("propertyChange")]
        public function get hasPayrollAdminContact():Boolean {
        	return mHasPayrollAdminContact;
        }

        private function setHasPayrollAdminContact(value:Boolean):void {
    		var oldVal:Boolean = mHasPayrollAdminContact;
        	mHasPayrollAdminContact = value;
	    	dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "hasPayrollAdminContact", oldVal, mHasPayrollAdminContact));
        }
        
        private function containsContactType(contacts:ArrayCollection, contactType:ContactRole):Boolean {
        	for each(var contact:Contact in contacts){
        		if(contact.contactRole != null && contact.contactRole == contactType){
        			return true;
        		}
        	}
        	return false;        	
        }
	}
}
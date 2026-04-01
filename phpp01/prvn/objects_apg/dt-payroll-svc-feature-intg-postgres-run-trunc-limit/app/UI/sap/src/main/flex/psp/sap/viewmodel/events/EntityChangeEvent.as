package psp.sap.viewmodel.events
{
	import flash.events.Event;
	
	public class EntityChangeEvent extends Event
	{
		// change events
		public static const ENTITY_SAVED:String = "entitySaved";
		public static const PAGE_REFRESH:String = "pageRefresh";

		// entity types
		public static const COMPANY:String = "company";
		public static const COMPANY_CONTACTS:String = "companyContacts";
		public static const SETTINGS:String = "settings";
				
		private var mEntityType:String;
		private var mEntityId:String;
		
		public function EntityChangeEvent(type:String, entityType:String = null, entityId:String = null, bubbles:Boolean = false, cancelable:Boolean = false)
		{
			super(type, bubbles, cancelable);
			this.entityType = entityType;
			this.entityId = entityId;
		}
		
		public function get entityType():String {
			return mEntityType;
		}
		
		public function set entityType(value:String):void {
			mEntityType = value;
		}
		
		public function get entityId():String {
			return mEntityId;
		}
		
		public function set entityId(value:String):void {
			mEntityId = value;
		}
		
		public static function createEvent(type:String, entityType:String = null, entityId:String = null, bubbles:Boolean = false, cancelable:Boolean = false):EntityChangeEvent {
			return new EntityChangeEvent(type, entityType, entityId, bubbles, cancelable);
		}

	}
}
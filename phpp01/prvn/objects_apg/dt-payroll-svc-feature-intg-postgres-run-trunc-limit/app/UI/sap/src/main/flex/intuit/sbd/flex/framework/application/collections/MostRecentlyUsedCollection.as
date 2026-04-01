package intuit.sbd.flex.framework.application.collections
{
	import mx.logging.ILogger;
	
	import psp.sap.application.ClientLoggingTarget;
	import psp.sap.model.Company;

	/**
	 * Specializes ArrayCollection to provide a MRU list.
	 * 
	 * The MRU list:
	 * -- always inserts items into the front of the list
	 * -- only shows the most recent usage of an item (i.e. an item can only appear once in the list)
	 */
	public class MostRecentlyUsedCollection extends ArrayCollectionExt
	{
		public static const DEFAULT_MAX_SIZE:int = 50;
		
		private var logger:ILogger = ClientLoggingTarget.getLogger(this); 
		
		private var mMaxSize:int = DEFAULT_MAX_SIZE;
		
		public function MostRecentlyUsedCollection(maxSize:int = DEFAULT_MAX_SIZE, source:Array=null):void {
			var equalityFunction:Function = function(a:*, b:*):Boolean {
    				return a["key"] === b["key"];
  			}

			super(Object, source, equalityFunction);
			mMaxSize = maxSize;
		}
		
		override public function addItem(item:Object):void {
			// always add to the first position in the list
			this.addItemAt(item, 0);			
		}
		
		override public function addItemAt(item:Object, index:int): void {
			logger.debug("Item UID: " + item["key"]);
			
			var currentIndex:int = this.getItemIndex(item);
			if (currentIndex != -1) {
				this.removeItemAt(currentIndex);
			}
			
			super.addItemAt(item, index);
			
			// if the max limit has been reach, remove the oldest entry
			if (this.length > mMaxSize) {
				this.removeItemAt(this.length - 1);
			}
		}
		
		override public function contains(item:Object):Boolean {
			for each(var company:Company in this){
				if(company.sourceSystemCd == Company(item).sourceSystemCd && company.companyId == Company(item).companyId){
					return true;
				} 
			}
			return false;
		}
		
		override public function removeItem(item:Object):Boolean {
			for each(var company:Company in this){
				if(company.sourceSystemCd == Company(item).sourceSystemCd && company.companyId == Company(item).companyId){
					super.removeItem(company);
					return true;
				} 
			}
			return false;
		}
	}
}
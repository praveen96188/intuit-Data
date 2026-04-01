package psp.sap.application.collections
{
	import flash.utils.Dictionary;
	import flash.utils.describeType;
	
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;

	public class LookupCollection extends ArrayCollectionExt
	{
		private var mLookupKey:String = null;
		private var mDefaultPropertyName:String = null;
		private var mDictionary:Dictionary = new Dictionary(true);
		
		public function LookupCollection(itemType:Class, source:Array=null, lookupKey:String = null, defaultPropertyName:String = null) {
			mLookupKey = lookupKey;
			super(itemType, source);
			mDefaultPropertyName = defaultPropertyName;

			// verify type has property
			verifyPropertyExists(itemType, mLookupKey);
			verifyPropertyExists(itemType, mDefaultPropertyName);
		}
		
		private function verifyPropertyExists(itemType:Class, propertyName:String):void {
			var typeInfo:XML = describeType(itemType);
			var propertyExists:Boolean = 
					typeInfo..factory.accessor.(@name == propertyName).length != 0
					|| typeInfo..factory.variable.(@name == propertyName).length != 0;
			if (!propertyExists)
				throw new Error("Could not find " + propertyName + " as an accessor or variable on type " + typeInfo.type.@name);			
		}
		
		public function get lookupKey():String {
			return mLookupKey;
		}
		
		override public function addItem(item:Object):void {
			super.addItem(item);
			
			if (mLookupKey != null) {
				 mDictionary[item[mLookupKey]] = item;
			}
		}
		
		override public function addItemAt(item:Object, index:int):void {
			super.addItemAt(item, index);
			
			if (mLookupKey != null) {
				 mDictionary[item[mLookupKey]] = item;
			}
		}
		
		override public function set source(s:Array):void {
			super.source = s;
			
			if (mLookupKey != null) {
				mDictionary = new Dictionary(true);
				for each (var item:Object in s) {
					mDictionary[item[mLookupKey]] = item;
				}
			}
		}
		
		public function getItemByKey(keyValue:Object):Object {
			return mDictionary[keyValue];
		}
		
		public function getStringByKey(keyValue:Object):Object {
			if (mDefaultPropertyName == null)
				return null;
		
			return getItemByKey(keyValue)[mDefaultPropertyName];
		}
	}
}
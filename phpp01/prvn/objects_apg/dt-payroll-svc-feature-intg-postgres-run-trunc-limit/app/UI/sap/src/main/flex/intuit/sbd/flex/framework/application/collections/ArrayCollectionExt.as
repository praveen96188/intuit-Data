package intuit.sbd.flex.framework.application.collections
{
	import mx.collections.ArrayCollection;

	public class ArrayCollectionExt extends ArrayCollection
	{
		private var mItemType:Class = null;
		
		private var mEqualityFunction:Function = null;
        private var idField:String = null;
		
		/**
		 * itemType the type of object this collection can hold.  Check is performed against
		 * 			this type using ActionScript 'is' operator when an item is added.
		 */
		public function ArrayCollectionExt(itemType:Class, source:Array=null, equalityFunction:Function = null, idField:String = null)
		{
			super(source);
			if (itemType == null) throw new Error("itemType must be specified");
			mItemType = itemType;
			
			mEqualityFunction = equalityFunction;
            this.idField = idField;
		}
		
		
		override public function getItemIndex(item:Object):int {
			if (mEqualityFunction != null) {
				for (var i:int = 0; i < this.length; i++) {
					if (mEqualityFunction(item, getItemAt(i))) {
						return i;
					}
				}
				return -1;
			} else if (idField != null) {
                return getItemIndexById(getId(item));
            } else {
                return super.getItemIndex(item);
			}
		}

        public function getItemIndexById(id:String):int {
            for (var i:int = 0; i < this.length; i++) {
                if (getId(getItemAt(i)) == id) {
                    return i;
                }
            }
            return -1;
        }

        private function getId(item:Object):String {
            return String(getId_internal(item,  idField));
        }

        private function getId_internal(item:Object, propertyString:String):Object {
            var propertyArray:Array = propertyString.split(".", 2);
            if (propertyArray.length == 1) {
                return item[propertyString];
            }
            return getId_internal(item[propertyArray[0]], propertyArray[1]);
        }

        public function getItemById(id:String):Object {
            var index:int = getItemIndexById(id);
            if (index < 0) {
                return null;
            }
            return getItemAt(index);
        }

		/**
		 * Type-safe add
		 */
		override public function addItemAt(item:Object, index:int):void {
			if (!(item is mItemType)) {
				throw new Error("Incorrect item type - must be of type " + mItemType.toString());
			}
			super.addItemAt(item, index);
		}

		/**
		 * Removes the item from the collection using the steps:
		 * 1. retrieves item index using getItemIndex(item)
		 * 2. remove item using removeItemAt(index)
		 * 
		 * Returns true if the object was found and removed,
		 * false otherwise.
		 */
		public function removeItem(item:Object):Boolean {
			var i:int = this.getItemIndex(item);
			var found:Boolean = i != -1;
			
			if (found) {
				this.removeItemAt(i);
			}
			
			return found;
		}

        /**
         *
         * @param item Item being checked for existence
         * @return true if item is found, false other wise.
         * Uses getItemIndex(item), added for improved readability only.
         *
         */
        override public function contains(item:Object):Boolean {
            return getItemIndex(item) >= 0;
        }

        public function containsId(id:String):Boolean {
            return getItemIndexById(id) >= 0;
        }
		
	}
}

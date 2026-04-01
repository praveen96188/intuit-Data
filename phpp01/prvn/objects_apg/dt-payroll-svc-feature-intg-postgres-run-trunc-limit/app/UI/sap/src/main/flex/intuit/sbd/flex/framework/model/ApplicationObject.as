package intuit.sbd.flex.framework.model
{
	
	import flash.events.EventDispatcher;
	import flash.utils.Dictionary;
	import flash.utils.describeType;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	import flash.utils.getQualifiedSuperclassName;
	
	import intuit.sbd.flex.framework.validation.PropertyValidatorCollection;
	
	import mx.collections.ArrayCollection;
	import mx.events.PropertyChangeEvent;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;
	
	[ValueEquals(exclude="validators",exclude="isValid",exclude="isDirty",exclude="uid")]
	public class ApplicationObject
		extends EventDispatcher
	{	
		private static function isPrimitive(typeName:String):Boolean {
			return typeName == "String"
					|| typeName == "int"
					|| typeName == "Boolean";
		}		
		
		private static function isType(instanceTypeName:String, targetTypeName:String):Boolean {
			
			if (instanceTypeName.indexOf(targetTypeName) != -1)
				return true;
			
			var classRef:Class = getDefinitionByName(instanceTypeName) as Class;
			
			var superclassName:String = getQualifiedSuperclassName(classRef);
			while (superclassName != null && superclassName.indexOf(targetTypeName) == -1) {
				classRef = getDefinitionByName(superclassName) as Class;
				superclassName = getQualifiedSuperclassName(classRef);
			}
			
			return (superclassName != null && superclassName.indexOf(targetTypeName) != -1);
		}
		
		//TODO: this is garbage and will be removed when we discern why LCDS does not like to serialize ArrayCollection derived classes
		public function valueEquals(other:Object):Boolean {
			
			if (other == null)
				return false;

			if (getQualifiedClassName(this) != getQualifiedClassName(other))
				return false;
			
			var typeInfo:XML = describeType(this);
			
			for each (var n:* in typeInfo..accessor) {
				// avoid endless recursion
				if (n.@name == "isDirty")
					continue;
				
				trace(n.@name);
				if (n.@name == "uid") {
					trace("  skipping internal");
					continue;
				}
				
				if (!isPrimitive(n.@type)) {
					
					if (this[n.@name] == null && other[n.@name] == null)
						continue;					
					
					if (this[n.@name] == null && other[n.@name] != null)
						return false;
					
					if (this[n.@name] != null && other[n.@name] == null)
						return false;
					
					if (isType(n.@type, "ApplicationObject")) {
						var areValueEqual:Boolean = ApplicationObject(this[n.@name]).valueEquals(other[n.@name]);
						if (!areValueEqual)
							return false;
					}
					else if (isType(n.@type, "ArrayCollection")) {
						var thisArrayCollection:ArrayCollection = ArrayCollection(this[n.@name]);
						var otherArrayCollection:ArrayCollection = ArrayCollection(other[n.@name]);
						if (thisArrayCollection.length != otherArrayCollection.length)
							return false;
							
						for each (var o:Object in thisArrayCollection) {
							
						}
					}
					else {
						trace("   skipping unhandled type");
					}
				}
				else {
					trace("  this  -> " + this[n.@name]);
					trace("  other -> " + other[n.@name]);
				
					if (this[n.@name] != other[n.@name])
						return false;
				}
			}
			return true;
			
		}		

	}
}
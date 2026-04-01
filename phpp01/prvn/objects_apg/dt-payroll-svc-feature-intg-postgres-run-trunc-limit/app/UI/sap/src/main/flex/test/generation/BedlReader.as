package test.generation
{
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	
	import test.data.EnumData;
	
	public class BedlReader
	{
		private var mXMLString:String;
		private var mXMLObject:XML;
		private var mLastParseError:String;
		
		private var enumerations:Dictionary;
	
		[Bindable]
		public var enumKeys:ArrayCollection = new ArrayCollection();
		
		protected function updateEnumKeys():void
	    {
	      var a:ArrayCollection = new ArrayCollection();	     
	      
	      for (var key:Object in enumerations)
	      {
	        a.addItem(key);
	      }
	      
	      var sort:Sort = new Sort();
     	  sort.fields = [new SortField(null, true)];
     	  a.sort = sort;
          a.refresh();
	      
	      enumKeys = a;
	    }
		
		public function BedlReader(xmlString:String)
		{
			super();
			mXMLString = xmlString;
			enumerations = new Dictionary;
		}
		
		public function get lastParseError():String {
			return mLastParseError;
		}
		
		public function set lastParseError(value:String):void {
			mLastParseError = value;
		}
		
		public function get XMLObject():XML {
			return mXMLObject;
		}
		
		public function set XMLObject(value:XML):void {
			mXMLObject = value;
		}
		
		public function get XMLString():String {
			return mXMLString;
		}
		
		public function set XMLString(value:String):void {
			mXMLString = value;
		}
		
		public function processXML():void {
			XMLObject = new XML(XMLString);
			
			var xmlList:XMLList = XMLObject.Enumerations.Enumeration;
			
			for each(var node:XML in xmlList){
				trace("Enumeration: " + node.@Name.toString());
				enumerations[node.@Name.toString()] = new ArrayCollection();
				
				var valList:XMLList = node.Fields.Field;
				
				for each(var subnode:XML in valList)
				{
					var val:String = subnode.@Name.toString();
					trace("value: " + val);
					(enumerations[node.@Name.toString()] as ArrayCollection).addItem(new EnumData(val, val));
				}
			}
			
			updateEnumKeys();
		}

		public function generateEnumeration(enumValue:String):String {
			var retText:String = "package psp.sap.model\n{\n\n";
			retText+= "\tpublic class " + enumValue +"\n\t{\n";

			
			var valText:String = "\t\tpublic static const values:Array = [";
			var enumConsts:String = "";
			
			for each(var enumData:EnumData in enumerations[enumValue])
			{
				if(enumConsts != "") valText+= ", ";
				enumConsts+= "\t\tpublic static const " + getReasonableConstName(enumData.code) + ":" + enumValue + " = new " + enumValue + "(\"" + enumData.code + "\");\n";
				valText+= getReasonableConstName(enumData.code)
			}
			valText+= "];\n\n";
			
			retText+= enumConsts + "\n\n" + valText;
			
			
			retText+= "\t\tprivate var mCode:String;\n"
            retText+= "\t\tprivate var mLabel:String;\n\n"
            
            retText+= "\t\tpublic function " + enumValue + "(code:String = null, label:String = null)\n";
            retText+= "\t\t{\n";
            retText+= "\t\t\tmCode = code;\n";
            retText+= "\t\t\tmLabel = (label != null ? label : code);\n";
            retText+= "\t\t}\n\n";
            
            retText+= "\t\tpublic function get code():String {\n";
            retText+= "\t\t\treturn mCode;\n";
            retText+= "\t\t}\n\n";
            
            retText+= "\t\tpublic function get label():String {\n";
            retText+= "\t\t\treturn mLabel;\n";
            retText+= "\t\t}\n\n";
            
            retText+= "\t\tpublic function toString():String {\n";
            retText+= "\t\t\treturn label;\n";
            retText+= "\t\t}\n\n";
            
            retText+= "\t\tpublic static function fromLabel(label:String):" + enumValue + " {\n";
            retText+= "\t\t\tfor each (var enum:" + enumValue + " in values) {\n";
            retText+= "\t\t\t\tif (enum.label == label)\n";
            retText+= "\t\t\t\t\treturn enum;\n";      
            retText+= "\t\t\t}\n\n";
            retText+= "\t\t\treturn null;\n";
            retText+= "\t\t}\n\n";
            
            retText+= "\t\tpublic static function valueOf(value:String):" + enumValue + " {\n";
            retText+= "\t\t\tfor each (var enum:" + enumValue + " in values) {\n";
            retText+= "\t\t\t\tif (enum.code == value)\n";
            retText+= "\t\t\t\t\treturn enum;\n";      
            retText+= "\t\t\t}\n\n";
            retText+= "\t\t\treturn null;\n";
            retText+= "\t\t}\n\n";
			
			retText+="\t}\n}";
			
			return retText;
		}
		
		private function getReasonableConstName(enumCd:String):String {
			var goodName:String = "";
			
			var trendUpper:Boolean = true;
			var trendSymbol:Boolean = false;
			
			for(var i:int = 0; i < enumCd.length; i++)
			{
				var workingChar:String = enumCd.charAt(i);
				
				if(i > 0)
				{
					var lastChar:String = enumCd.charAt(i-1);
					
					var lastWasAcronymn:Boolean = false;
					
					if((isSymbol(lastChar) && !isSymbol(workingChar)) || (!isSymbol(lastChar) && isSymbol(workingChar)))
					{
						goodName+= "_";
					} else {
						if(i + 1 < enumCd.length)
						{
							var nextChar:String = enumCd.charAt(i+1);
							
							//Special case
							if(isLowerCase(nextChar) && isUpperCase(workingChar) && isUpperCase(lastChar))
							{
								lastWasAcronymn = true;
								goodName+= "_";
							}
						}
						if(!lastWasAcronymn && isUpperCase(workingChar) && isLowerCase(lastChar))
						{
							goodName+= "_";
						}
					}
					
					goodName+= workingChar.toUpperCase();
				} else {
					goodName+= workingChar.toUpperCase();
				}
				
			}
			
			return goodName;
		}
		
				
		private function isLowerCase(val:String):Boolean{
			var c:int = val.charCodeAt(0);
			return (c > 96 && c < 123);
		}
		private function isUpperCase(val:String):Boolean {
			var c:int = val.charCodeAt(0);
			return (c > 64 && c < 91);
		}
		private function isSymbol(val:String):Boolean{
			return (!isUpperCase(val) && !isLowerCase(val));
		}

	}
}
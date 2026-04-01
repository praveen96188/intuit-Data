package test.data
{
	public class MockSearchFilter
	{
		private var mInput:String;
		private var mTerms:Array;
		
		function MockSearchFilter(input:String):void {
			mInput = input;
			// this would need to be more sophisticated but since this is just
			// for temporary purposes, it will remain lame
			mTerms = [mInput];
		}
		
		public function filter(item:Object):Boolean {
			for (var i:int = 0; i < mTerms.length; i++) {
				var term:String = mTerms[i] as String;
				var keyValue:Array = term.split(":");
				
				if (keyValue.length == 2 && (item[keyValue[0]] != keyValue[1]))
					return false;	
			}
			return true;
		}		
	}
}
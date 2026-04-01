package psp.sap.application.collections
{
	import intuit.sbd.flex.framework.application.collections.ArrayCollectionExt;
	
	import psp.sap.viewmodel.AbstractExplorer;
	import psp.sap.viewmodel.AbstractInspectorViewModel;
	
	public class ExplorersCollection extends ArrayCollectionExt
	{
		public function ExplorersCollection(source:Array=null)
		{
			super(AbstractExplorer, source);
		}
		
		public function containsExplorer(explorerName:String): Boolean {
			return _getExplorer(explorerName) != null;
		}

		public function getExplorer(explorerName:String):AbstractExplorer {
			var explorer:AbstractExplorer = _getExplorer(explorerName);
			
			if (explorer == null) {
				//since this is a getXxx (instead of a findXxx) should throw Error
				throw new Error("Could not find explorer with name " + explorerName);
			}
			
			return explorer;
		}

        public function hasExplorer(explorerName:String):Boolean {
			return _getExplorer(explorerName) != null;
		}
		
		private function _getExplorer(explorerName:String):AbstractExplorer {
			for each (var explorer:AbstractExplorer in this) {
				if (explorer.name == explorerName)
					return explorer;
			}
			
			return null;
		}
		
		public function findByInspector(inspector:AbstractInspectorViewModel):AbstractExplorer {
			for each (var explorer:AbstractExplorer in this) {
				if (explorer.inspectors.contains(inspector))
					return explorer;
			}
			
			return null;
		}		
	}
}
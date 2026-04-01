package psp.sap.application.collections
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.utils.ObjectUtil;
	
	import psp.sap.model.NachaFile;
	
	[Bindable]
	public class NachaFileGroup
	{
		
		public var finalizedTime:Date;
		public var transmissionTime:Date;
		
		[ArrayElementType("psp.sap.model.NachaFile")]
        public var achFiles:ArrayCollection=new ArrayCollection();
           
        	
		/**
		 * Group a collection of nacha files into NachaFileGroups by finalized and transmission times
		 */
		public static function group(achFiles:ArrayCollection):ArrayCollection {
			var nachaGroups:ArrayCollection = new ArrayCollection();
			
			//this crap keeps it so that if the dates are the same, it preserves the ordering
			var index:int=0;
			for each (var file:NachaFile in achFiles) {
				file.index = index;
				index++;
			} 
			
			//first sort by the two fields
			
			var sort:Sort = new Sort();
            sort.fields = [new SortField("finalizedTime", false, true), 
            				new SortField("transmissionTime",false,true),
            				new SortField("index",false,false,true)];	
			
			achFiles.sort = sort;
			achFiles.refresh();
			
			//now find the groups
			
								
			var currentGroup:NachaFileGroup=null;			
			
			for each (var nachaFile:NachaFile in achFiles) {
				if (currentGroup == null || 
					(ObjectUtil.compare(currentGroup.finalizedTime, nachaFile.finalizedTime)!=0) ||
					(ObjectUtil.compare(currentGroup.transmissionTime, nachaFile.transmissionTime)!=0)){
					//i.e. belongs to new group			
					if (currentGroup != null) {
						nachaGroups.addItem(currentGroup);
					}						
					currentGroup = new NachaFileGroup();
					currentGroup.finalizedTime = nachaFile.finalizedTime;
					currentGroup.transmissionTime = nachaFile.transmissionTime;													
				} 
				currentGroup.achFiles.addItem(nachaFile);				
			}
			if (currentGroup != null) {
				nachaGroups.addItem(currentGroup);
			}
			
			
			return nachaGroups;
		}

	}
}
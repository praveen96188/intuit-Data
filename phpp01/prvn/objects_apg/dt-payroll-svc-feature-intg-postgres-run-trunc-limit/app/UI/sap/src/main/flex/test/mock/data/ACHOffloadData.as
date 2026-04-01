package test.mock.data
{
	import mx.collections.ArrayCollection;
	
	import psp.sap.model.NachaFile;
	
	public class ACHOffloadData
	{
		
		public static function getLongOffloadData():ArrayCollection {
			var files:ArrayCollection = new ArrayCollection();
			//group 4
			files.addItem(new NachaFile("id1","a/b/c/d/e/f/g.txt",2421.12,2421.12,null,new Date(null,1,3,2,1,4,5), new Date(null,1,6,4,2,1,5)));
			files.addItem(new NachaFile("id2","a\\b\\c\\d\\e\\f\\G.txt",6324.12,6324.12,null,new Date(null,1,3,2,1,4,5), new Date(null,1,6,4,2,1,5)));
			files.addItem(new NachaFile("id3","a\\b\\c\\d\\e\\f\\h.txt",6324.12,6324.12,null,new Date(null,1,3,2,1,4,5), new Date(null,1,6,4,2,1,5)));
			files.addItem(new NachaFile("id4","a\\b\\c\\d\\e\\f\\i.txt",6324.12,6324.12,null,new Date(null,1,3,2,1,4,5), new Date(null,1,6,4,2,1,5)));
			//group 3
			files.addItem(new NachaFile("id5","a\\b\\c\\d\\e\\f\\j.txt",1017220.90,1017220.90,null,new Date(null,1,3,2,1,4,5), new Date(null,1,6,6,2,1,5)));
			//group 2
			files.addItem(new NachaFile("id6","a\\b\\c\\d\\e\\f\\k.txt",1017220.90,1017220.90,null,new Date(null,1,3,3,1,4,5), new Date(null,1,6,6,2,1,5)));
			files.addItem(new NachaFile("id7","a\\b\\c\\d\\e\\f\\k.txt",1017220.90,1017220.90,null,new Date(null,1,3,3,1,4,5), new Date(null,1,6,6,2,1,5)));
			//group 1
			files.addItem(new NachaFile("id8","a\\b\\c\\d\\e\\f\\k.txt",1017220.90,1017220.90,null,new Date(null,1,3,3,1,4,5), null));
			
			return files;
			
		}

	}
}
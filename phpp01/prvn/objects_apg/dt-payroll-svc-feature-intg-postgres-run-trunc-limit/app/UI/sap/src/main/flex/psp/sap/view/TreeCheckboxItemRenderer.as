package psp.sap.view
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ICollectionView;
	import mx.collections.IViewCursor;
	import mx.controls.CheckBox;
	import mx.controls.Tree;
	import mx.controls.treeClasses.ITreeDataDescriptor;
	import mx.controls.treeClasses.TreeItemRenderer;
	import mx.controls.treeClasses.TreeListData;

	public class TreeCheckboxItemRenderer extends TreeItemRenderer
	{
		protected var myCheckBox:CheckBox;
		
		public function TreeCheckboxItemRenderer() {
			super();
		}
		
		override protected function createChildren():void
		{
		   super.createChildren();
		   myCheckBox = new CheckBox();
		   myCheckBox.setStyle( "verticalAlign", "middle" );
		   myCheckBox.addEventListener( MouseEvent.CLICK, checkBoxToggleHandler, false, 0, true );
		   addChild(myCheckBox);
		} 
	
		protected function isParent():Boolean {
			return TreeListData(super.listData).item.hasOwnProperty("children");
		}
	
		private function imageToggleHandler(event:MouseEvent):void
		{
		   myCheckBox.selected = !myCheckBox.selected;
		   checkBoxToggleHandler(event);
		}
		
		protected function checkBoxToggleHandler(event:Event):void {
			data["checked"] = myCheckBox.selected;
			
			var myListData:TreeListData = TreeListData(this.listData);
			var tree:Tree =  Tree(myListData.owner);
			var treeData:ITreeDataDescriptor = tree.dataDescriptor;
			
		    var children:ICollectionView = null;
			var cursor:IViewCursor = null;
			 
			if(isParent()) {
				//Propogate lowers
			     if (treeData.hasChildren(data))
			      {
			         children = treeData.getChildren (data);
			         cursor = children.createCursor();
			         while (!cursor.afterLast)
			         {
			            cursor.current["checked"] = (myCheckBox.selected);
			            cursor.moveNext();
			         }
			      }
			} else {
				var parentData:Object = tree.getParentItem(data);
				if (treeData.hasChildren(parentData))
			      {
			         children = treeData.getChildren (parentData);
			         cursor = children.createCursor();
			         
			         var allChecked:Boolean = true;
			         
			         while (!cursor.afterLast)
			         {
			         	allChecked = allChecked && cursor.current["checked"];
			            cursor.moveNext();
			         }
			         
			         parentData["checked"] = allChecked;
			      }
				
			}
		}	
	
		override public function set data(value:Object):void
		{
		   super.data = value;
		   
		   if(value == null) return;
		   
		   
		   
		   if(this.parent == null) return;
		   if(super.listData == null) return;
		   
		   var _tree:Tree = Tree(this.parent.parent);
		   
		   
		   if(TreeListData(super.listData).item.hasOwnProperty("children"))
		   {
		      _tree.setStyle("defaultLeafIcon", null);
		   }
		} 
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
		   super.updateDisplayList(unscaledWidth, unscaledHeight);
		   
		    if(super.data)
		   {
		      if (super.icon != null)
		      {
		         myCheckBox.x = super.icon.x;
		         myCheckBox.y = 10;
		         super.icon.x = myCheckBox.x + myCheckBox.width + 17;
		         super.label.x = super.icon.x + super.icon.width + 3;
		      }
		      else
		      {
		         myCheckBox.x = super.label.x;
		         myCheckBox.y = 10;
		         super.label.x = myCheckBox.x + myCheckBox.width + 17;
		      }
		     
		     myCheckBox.selected = data["checked"];
		      
		   }
		}
		   
	}
}	
package psp.sap.view.controls
{
    import flash.events.Event;
    import flash.utils.Dictionary;

    import mx.collections.ArrayCollection;
    import mx.controls.DataGrid;
    import mx.controls.dataGridClasses.DataGridColumn;
    import mx.core.Container;
    import mx.core.IFlexDisplayObject;
    import mx.core.IInvalidating;
    import mx.core.ScrollPolicy;
    import mx.core.UIComponent;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;
    import mx.events.ResizeEvent;
    import mx.logging.ILogger;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.formatters.SAPCurrencyFormatters;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.formatters.SAPRateFormatter;

    public class SAPDataGrid extends DataGrid
	{
        //noinspection JSFieldCanBeLocalInspection
        private var logger:ILogger = ClientLoggingTarget.getLogger(this);

		private var mMaxRows:Number = NaN;

        override public function get draggableColumns():Boolean
        {
            return false;
        }

		/*
		Set this to get a data grid that behaves like it should when inlined on a page.
		The amount of rows being displayed is 
			min(maxRows, dataProvider.length)
		and it won't scroll when there is nothing below.	
		*/
		[Bindable]
		public function get maxRows():Number {
			return mMaxRows;
		}
		
		public function set maxRows(value:Number):void {
			mMaxRows = value;
			adjustRowCount();
		}
		 
		
		[Bindable]
		public var autoResizeToFitContent:Boolean = false;
		
		private var mAutoResizeHeightBuffer:int = 260;

        [Bindable]
        public var autoResizeWidthToFitContent:Boolean = false;

        private var mAutoResizeWidthBuffer:int = 200;

        [Bindable]
        public var maxResizeWidth:Number = NaN;


        [Bindable]
        public function get autoResizeWidthBuffer():int {
            return mAutoResizeWidthBuffer;
        }

        public function set autoResizeWidthBuffer(value:int):void {
            mAutoResizeWidthBuffer = value;
            resizeGrid();
        }

        [Bindable]
		public function get autoResizeHeightBuffer():int {
			return mAutoResizeHeightBuffer;
		}
		
		public function set autoResizeHeightBuffer(value:int):void {
			mAutoResizeHeightBuffer = value;
			resizeGrid();
		}
		
		private var m_setSwitch:Boolean = false;
		
		//Specifically for binding a variable to the datagrid to change when var is changed.
		public function get resizeSwitch():Boolean {
			return m_setSwitch;
		}
		
		public function set resizeSwitch(value:Boolean):void {
			resizeGrid();
			m_setSwitch = value;
		}
		
		public function SAPDataGrid()
		{
			super();
			
			this.addEventListener(ResizeEvent.RESIZE, function(e:Event):void {resizeGrid()}, false, 0, false);
		}

        public function resizeGrid():void {
            if (fixWidths && originalColumnWidths != null) {
                //flex sucks
                var oldScrollPolicy:String = this.horizontalScrollPolicy;
                this.horizontalScrollPolicy = ScrollPolicy.ON;

                var fixedWidthTotal:int =0;
                var numberOfFloatingColumns:int = 0;
                for (var i:int=0; i < columns.length; i++) {
                    if (!columns[i].visible) {
                        DataGridColumn(columns[i]).width = 0;
                    } else if (originalColumnWidths[i] != -1) { //-1 = float
                        DataGridColumn(columns[i]).width = originalColumnWidths[i];
                        fixedWidthTotal+= originalColumnWidths[i];
                    } else {
                        numberOfFloatingColumns++;
                    }
                }
                for (i=0; i < columns.length; i++) {
                    if (originalColumnWidths[i] == -1) { //-1 = do not fix width
                        DataGridColumn(columns[i]).width = (this.width - fixedWidthTotal) / numberOfFloatingColumns;   //can easily expand this to be percentages if needed
                    }
                }

                this.horizontalScrollPolicy = oldScrollPolicy;
            }

            var p:UIComponent = this.parent as UIComponent;
            if (autoResizeWidthToFitContent || autoResizeToFitContent) {
                var newWidth:Number = NaN;
                var newHeight:Number = NaN;
                while (p != null && p.parent != null) {
                    if (autoResizeWidthToFitContent) {
                        if ((p.width - autoResizeWidthBuffer) > 0) {
                            newWidth = p.width - autoResizeWidthBuffer;
                        } else if ((p.explicitWidth - autoResizeWidthBuffer) > 0) {
                            newWidth = p.width - autoResizeWidthBuffer;
                        }
                    }
                    if (autoResizeToFitContent) {
                        if ((p.height - autoResizeHeightBuffer) > 0) {
                            newHeight = p.height - autoResizeHeightBuffer;
                        } else if ((p.explicitHeight - autoResizeHeightBuffer) > 0) {
                            newHeight = p.explicitHeight - autoResizeHeightBuffer;
                        }
                    }
                    p = p.parent as UIComponent;
                }
                if (!isNaN(newHeight)) {
                    this.height = newHeight;
                }
                if (!isNaN(newWidth)) {
                    //flex gazes into the abyss that is the vertical scroll bar and the abyss gazes back.  18 seems to work, though.  So... yay?
                    this.width = isNaN(maxResizeWidth) ? newWidth : Math.min(newWidth, maxResizeWidth) + 18;
                }
            }
        }

        [Bindable] public var fixWidths:Boolean = false;
        private var originalColumnWidths:Array=[];
        override public function set columns(columns:Array):void {
            for (var i:int = originalColumnWidths.length; i < columns.length; i++) {
                if (columns[i] is SAPDataGridColumn && !SAPDataGridColumn(columns[i]).fixWidth) {
                    originalColumnWidths.push(-1);
                } else {
                    originalColumnWidths.push(columns[i].width);
                }
                DataGridColumn(columns[i]).addEventListener(FlexEvent.HIDE, function(e:Event):void {resizeGrid();}, false, 0, true);
                DataGridColumn(columns[i]).addEventListener(FlexEvent.SHOW, function(e:Event):void {resizeGrid();}, false, 0, true);
            }
            super.columns = columns;
            resizeGrid();
        }
		
		
		
		/* All of this is for exporting data */
				
		protected function getColumnKey(dgc:DataGridColumn):String
		{
			var cKey:String = (dgc.headerText == null ? dgc.dataField : dgc.headerText);
			if(cKey != null) cKey = cKey.replace(/\n/g, ' ');
			return cKey;
		}
		
		
		public function getOrderArray():Array {
			//Create order array
			var orderArray:Array = new Array();
			
			//Fill in those that are order specified first.
			for(var j:int = 0; j < this.columns.length; j++)
			{
				var dg:SAPDataGridColumn = this.columns[i] as SAPDataGridColumn;
				
				//If we specify an order, fill it in here
				if(dg != null && dg.exportOrder != -1 && dg.useInExport)
				{
					orderArray[orderArray.length] = getColumnKey(this.columns[i] as DataGridColumn);
				}
			}
				
			//Next, fill in the others.
			for(var i:int =0; i < this.columns.length; i++)
			{
				var dgc:SAPDataGridColumn = this.columns[i] as SAPDataGridColumn;
				if(dgc != null && dgc.exportOrder == -1 && dg.useInExport)
				{
					//Fill in free value into order array
					orderArray[orderArray.length] = getColumnKey(this.columns[i] as DataGridColumn);
				}
			}
			
			return orderArray;
		}
		
		public function getExportHeaders():String {
			var headerRetText:String = "";
			
			var orderArray:Array = getOrderArray();
			
			//Setup headers
			for(var ia:int =0; ia < orderArray.length; ia++)
			{
				var key:String = orderArray[ia];
				
				if(key != "totalGridRecordCount")
				{
					if(headerRetText != "") headerRetText+= ",";
					headerRetText+= "\"" + getHeaderNameFromColumn(getHeaderFromKey(key)) + "\"";
				}
			}
			
			headerRetText+= "\n";
			
			return headerRetText;
		}
		
		protected function getHeaderFromKey(columnKey:String):SAPDataGridColumn {
			for(var i:int =0; i < this.columns.length; i++)
			{
				var key:String = getColumnKey(this.columns[i] as DataGridColumn);
				if(key == columnKey) return this.columns[i];
			}
			return null;
		}
		
		protected function getHeaderNameFromColumn(dgc:SAPDataGridColumn):String {
			var key:String = getColumnKey(dgc);
			return (dgc.exportTitle != null) ? dgc.exportTitle : key;
		}
		
		public function getExportData():String {
			var dataDictionary:Dictionary = getGridData();
			var dataRecordCount:Number = dataDictionary["totalGridRecordCount"] as Number;
			
			var dataRetText:String = "";
			var orderArray:Array = getOrderArray();
			
			for (var i:int = 0; i < dataRecordCount; i++)
			{
				var lDataRetText:String = "";
				
				for(var j:int =0; j < orderArray.length; j++)
				{
					var dkey:String = orderArray[j];
				
					if(dkey != "totalGridRecordCount")
					{
						if(lDataRetText != "") lDataRetText+= ",";
						lDataRetText+= "\"" + (dataDictionary[dkey] as ArrayCollection).getItemAt(i) + "\"";
					}
				}
				
				dataRetText+= lDataRetText + "\n";
			}	
			
			return dataRetText;		
		}		
			
			
		//listItems		
		//protected function getListItemPointForRow(rowIndoex:Number):
			
		protected function getGridData():Dictionary {
			var countSize:Number = 0;
			
			this.verticalScrollPosition = 0;
			this.invalidateDisplayList();
			
			var gridData:Dictionary = new Dictionary(true);

			for(var i:int =0; i < this.columns.length; i++)
			{
				gridData[getColumnKey(this.columns[i] as DataGridColumn)] = new ArrayCollection();
			}
			
			//countSize = rowIndex
			for each(var dataObject:Object in this.dataProvider)
			{	
				//listItems[actualRowIndex][actualColIndex]
				for(var j:int = 0; j < this.columns.length; j++)
				{					
					var foundColumnData:Boolean = false;
					var dSingleColumn:DataGridColumn = this.columns[j] as DataGridColumn;
			
					var listItemOffset:int = (countSize > this.maxVerticalScrollPosition) ? countSize - this.maxVerticalScrollPosition : 0;
			
					if((this.listItems[listItemOffset][j] is Container))
					{
						var container:Container = this.listItems[listItemOffset][j] as Container;
						
						if(container != null)
						{
							for(var k:int = 0; k < container.getChildren().length; k++)
							{
								if(container.getChildren()[k] is SAPColumnData)
								{
									var sapColumnData:SAPColumnData = container.getChildren()[k] as SAPColumnData;
									foundColumnData = true;
									(gridData[getColumnKey(dSingleColumn)] as ArrayCollection).addItem(sapColumnData.displayData);
								}
							}
						}
					}
				
					if(!foundColumnData)
					{
						if(dSingleColumn.dataField != null && dataObject.hasOwnProperty(dSingleColumn.dataField))
						{
							(gridData[getColumnKey(dSingleColumn)] as ArrayCollection).addItem(dataObject[dSingleColumn.dataField]);
						} else {
							(gridData[getColumnKey(dSingleColumn)] as ArrayCollection).addItem('');
							logger.error("Error getting dataField off object '" + dSingleColumn.dataField + "'");
						}	
					}
				}
				
				countSize++;
				
				//Change values to invalidate and force invalidate.
				this.verticalScrollPosition = (countSize > this.maxVerticalScrollPosition) ? this.maxVerticalScrollPosition : countSize;
				this.selectedIndex = countSize - 1;
				this.invalidateDisplayList();
				
				//Synchronously update the display
				if (this is IFlexDisplayObject)
		        {
		            if (this is IInvalidating)
		            {
		                IInvalidating(this).invalidateDisplayList();
		                IInvalidating(this).validateNow();
		            }
		        }
			}
			
			
			gridData["totalGridRecordCount"] = countSize;
		
			return gridData;
		}
		

        /*
         This monstrosity is because we had this other hack in PagingDataGrid that made it fire a PropertyChange on DataProvider on any CollectionChange.
         I don't know why, but I gather it is important.  Anyhow, a side effect of that, is when the DataProvider changes, a Reset CollectionChange event is fired.
         When a reset is fired, Flex resets the position to 0 when this happens.
         This behavior is not desired, so when we make that fake PropertyChange, we can set this ignoreReset which changes the collection event to one that
         is friendlier and doesn't reset the scroll position.  What could go wrong?
         */
        public var ignoreReset:Boolean = false;
		override protected function collectionChangeHandler(event:Event):void {
			if (ignoreReset && event is CollectionEvent && CollectionEvent(event).kind == CollectionEventKind.RESET) {
                ignoreReset = false;
                CollectionEvent(event).kind = CollectionEventKind.REFRESH;
            }
            super.collectionChangeHandler(event);
            adjustRowCount();
		}
		
		protected function adjustRowCount():void {
			if (! isNaN(maxRows)) {
				if (collection != null) {
					this.rowCount = Math.max(1, Math.min(maxRows, collection.length));
					this.verticalScrollPolicy = (collection.length > this.rowCount ? ScrollPolicy.AUTO : ScrollPolicy.OFF);
				} else {
					this.rowCount = 1;
					this.verticalScrollPolicy = ScrollPolicy.OFF;
				}			
			}
		}

        public static function formatCurrency(item:Object, dgc:DataGridColumn):String {
            return SAPCurrencyFormatters.defaultFormatter.format(item[dgc.dataField]);
        }
        public static function formatDateShort(item:Object,  dgc:DataGridColumn):String {
            return SAPDateFormatters.dateFormatShort.format(item[dgc.dataField]);
        }
        public static function formatDateMedium(item:Object,  dgc:DataGridColumn):String {
            return SAPDateFormatters.dateFormatMedium.format(item[dgc.dataField]);
        }
        public static function formatDateTimeMedium(item:Object, dgc:DataGridColumn):String {
            return SAPDateFormatters.dateTimeFormatMedium.format(item[dgc.dataField]);
        }
		public static function formatDateTimeFormatDateOverTime(item:Object,  dgc:DataGridColumn):String {
            return SAPDateFormatters.dateTimeFormatDateOverTime.format(item[dgc.dataField]);
        }
        public static function formatRateAsPercentage(item:Object, column:DataGridColumn):String {
            if (isNaN(item[column.dataField])) {
                return "";
            } else {
                return SAPRateFormatter.formatRateAsPercentage(item[column.dataField]);
            }
        }

    }
}

package psp.sap.application.collections
{
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	
	import psp.sap.application.SAP;
    import psp.sap.model.SearchResults;

    public class PaginationCollection extends ArrayCollection
	{		
		private var mSort:Sort;
        private var _compareFunction:Function;
		
		public function PaginationCollection(source:Array=null,
											 pageSize:int=SAP.instance.configuration.DEFAULT_GRID_PAGE_SIZE,
											 startIndex:int=0,
											 lastIndex:int=0,
											 totalRecords:int=0,
											 sortBy:String=null,
											 sortDesc:Boolean=false,
                                             totalAmount:Number=0)
		{	
			this.source = source;		
			this.pageSize = pageSize;
			this.startIndex = startIndex;
			this.lastIndex = lastIndex;
			this.totalRecords = totalRecords;
			this.sortBy = sortBy;
			this.sortDesc = sortDesc;
			mSort = new Sort();
            this.totalAmount = totalAmount;
		}

        public function get searchResults():SearchResults {
            var searchResults:SearchResults = new SearchResults();
            searchResults.returnsList = new ArrayCollection(source);
            searchResults.totalRecords = totalRecords;
            searchResults.totalAmount = totalAmount;
            return searchResults;
        }

        public function set searchResults(searchResults:SearchResults):void {
            source = searchResults.returnsList.source;
            totalRecords = searchResults.totalRecords;
            totalAmount = searchResults.totalAmount;
        }

		/**
		 * Number of records to retrieve at a time
		 */ 
		[Bindable]
		public var pageSize:int;
		
		/**
		 * The first record in the results set
		 */ 
		[Bindable]
		public var startIndex:int;
		
		/**
		 * The last record in the results set
		 */ 
		[Bindable]
		public var lastIndex:int;
		
		/**
		 * The total number of records 
		 */ 
		[Bindable]
		public var totalRecords:int;

        /**
         * Total amount of all records
         */
        [Bindable]
	    public var totalAmount:Number;
		
		/**
		 * Column to sort by
		 */ 
		[Bindable]
		public var sortBy:String;
		
		/**
		 * Which direction to sort
		 */ 
		[Bindable]
		public var sortDesc:Boolean;
		
		[Bindable("listChanged")]
		override public function set source(s:Array):void {
			super.source = s;
			if(s != null){
				lastIndex = startIndex + s.length;
				if(sortBy != null){
					// results will already be sorted, but this should set the arrow on the data grid
					mSort.fields = [new SortField(sortBy, false, sortDesc)];
                    if(_compareFunction != null) {
                        mSort.compareFunction = _compareFunction;
                    }
					sort = mSort;
					refresh();
				}
			}
		}		
		
		public function previousPage():Boolean {
			if(startIndex - pageSize >= 0){
				startIndex -= pageSize;
				return true;
			}
			return false;
		}
		
		public function nextPage():Boolean {
			if(startIndex + pageSize <= totalRecords){
				startIndex += pageSize;
				return true;
			}
			return false;
		}
		
		public function reset():void {					
			this.startIndex = 0;			
			this.sortBy = null;
			this.sortDesc = false;
			sort = null;
			mSort = new Sort();
		}

        public function set compareFunction(value:Function):void {
            _compareFunction = value;
            if(_compareFunction != null) {
                mSort.compareFunction = _compareFunction;
            }
        }
    }
}

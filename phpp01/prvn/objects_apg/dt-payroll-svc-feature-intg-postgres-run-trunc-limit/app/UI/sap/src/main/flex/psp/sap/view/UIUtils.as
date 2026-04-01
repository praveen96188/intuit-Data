package psp.sap.view
{
	import mx.collections.ICollectionView;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.core.UIComponent;
	import mx.utils.ObjectUtil;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.OperationsEnum;
	import psp.sap.model.PropertyAudit;
	
	public class UIUtils
	{
		
		public static const COLOR_RED:uint = 0xff0000;
		public static const COLOR_GREEN:uint = 0x009900;
		public static const COLOR_BLACK:uint = 0x000000;
		
		public static const EVEN_ROW:uint = 0xF7F7F7;
		public static const ODD_ROW:uint = 0xFFFFFF;
		public static const MS_PER_DAY:uint = 1000 * 60 * 60 * 24;
		
		public static function setInvisible(value:UIComponent):void {
			value.visible = false;
			value.includeInLayout = false;
		}

		public static function setVisible(value:UIComponent):void {
			value.includeInLayout = true;
			value.visible = true;
		}
				
		public static function getTransactionStatusColor(value:Date):uint {
			return (value == null) ? COLOR_RED : COLOR_BLACK;
		}
		
		
		public static function getRowColor(rowNumber:int):uint{
			if(rowNumber % 2 == 0){
				return EVEN_ROW;
			}
			return ODD_ROW; 
		}

        /**
         *
         * @param value
         * @return
         *
         * Used to transform a SSN (value) from "172635468907" to "********8907"
         */
        public static function maskSSN(value:String):String {
            if (value == null) {
                return "";
            }
			if(SAP.canPerformOperation(OperationsEnum.VIEW_EE_PII)) {
				return value;
            }
            var digitsToShow:int = 4;
            if (value.length <= digitsToShow)
				return value;
            return maskText(value);
        }
		
		/**
		 * This function will mask account numbers based on the users access 
		 */
		public static function maskBankAccountNumbers(value:String):String {
			if(canViewFullBankAccountNumbers()){
				return value;
			}
			else{
				return protectBankAccountNumber(value);
			}
		}
		
		//Can this user view full bank account numbers?
		private static function canViewFullBankAccountNumbers():Boolean {
			return SAP.canPerformOperation(OperationsEnum.VIEW_FULL_BANK_ACCOUNT_NUMBERS);
		}
		
		//Used to transform a bank account from "172635468907" to "********8907"
		private static function protectBankAccountNumber(value:String):String {
			var retString:String = "";
			if(value == null) 
				return retString;
			var strLen:int = value.length;
			if(strLen < 5) 
				return value;
            return maskText(value);
		}

        protected static function maskText(value:String):String {
            var digitsToShow:int = 4;
            var strLen:int = value.length;
            var retString:String = "";
            for (var i:int = 0; i < (strLen - digitsToShow); i++)
                retString += "*";
            retString += value.substring(strLen - digitsToShow);
            return retString;
        }


		//Use this on date controls to set the selectableRange on dates
		//Example: settlementDate.selectableRange = UIUtils.getSelectableDateRange();
		//		   example will gray out days prior to 45 days before fromDate and all days after fromDate
	   public static function getSelectableDateRange(daysBeforeAllowed:int = 45, daysAfterAllowed:int = 0, fromDate:Date = null):Object {
	   		if(fromDate == null) fromDate = SAP.instance.PSPDate;
	   		
	   		var startRangeDate:Date = SAP.instance.PSPDate;
	   		var endRangeDate:Date = SAP.instance.PSPDate;
	   		
	   		startRangeDate.time-= MS_PER_DAY * daysBeforeAllowed;
	   		endRangeDate.time+= MS_PER_DAY * daysAfterAllowed;
	   		
	   		return {rangeStart: startRangeDate, 
                    rangeEnd: endRangeDate};
	   }
	   
	   	public static function _sortCompareFunc(itemA:Object, itemB:Object):int {
			// null check
			if(itemA == null || itemB == null){
            	return 0;
            }
            
            // string
            if(itemA is String && itemB is String){
            	return ObjectUtil.stringCompare(itemA as String, itemB as String);
            }
            
            // date
            if(itemA is Date && itemB is Date){
            	return ObjectUtil.dateCompare(itemA as Date, itemB as Date);
            }
            
            // number
            if(itemA is Number && itemB is Number){
            	return ObjectUtil.numericCompare(itemA as Number, itemB as Number);
            }
            
            // other objects
            return ObjectUtil.compare(itemA, itemB);
     	}
     	
     	/*
     	@deprecated use Controls/SAPDataGrid
     	*/
     	public static function getRowCountFor(c:ICollectionView, max:int=5):int {
     		if (c.length < 1) {
     			return 1;
     		} 
     		if (c.length > max) {
     			return max;
     		}
     		return c.length;
     	}

        public static function singleSort(sortField:SortField):Sort {
            var sort:Sort = new Sort();
            sort.fields = [sortField];
            return sort;
        }
	}
}
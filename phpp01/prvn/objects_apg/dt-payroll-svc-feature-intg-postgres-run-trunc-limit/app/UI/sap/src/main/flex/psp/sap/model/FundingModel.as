package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPFundingModel")]
	public class FundingModel
	{
		/**
		 * ENUMERATED FUNDING MODEL CD VALUES ARE HARD-CODED; 
		 * MUST STAY IN SYNC WITH DB VALUES
		 */
		public static var TWO_DAY:FundingModel = FundingModel.createInstance("2 day", "2 day funding model", "2D");
		public static var FIVE_DAY:FundingModel = FundingModel.createInstance("5 day", "5 day funding model", "5D");
		
		public function FundingModel():void {
						
		}
		
		public static function createInstance(pName:String, pDescription:String, pFundingModelCd:String):FundingModel {
			var newFundingModel:FundingModel = new FundingModel();
			newFundingModel.name = pName;
			newFundingModel.description = pDescription;
			newFundingModel.fundingModelCd = pFundingModelCd;
			return newFundingModel;
		}
		
		public var name: String;
		
		public var description: String;
		
		public var fundingModelCd: String;
		
		public function toString():String {
			return name;
		}
		
		[Transient]
		public function isValidForCompanySourceSystem(sourceSystem:String):Boolean {
            /* PSRV001157: removed check for source system since all now allow 5-day funding */
			return true;
		}
		
		[Transient]
		public function toolTip(sourceSystem:String):String {
			if(!isValidForCompanySourceSystem(sourceSystem)){
				return name + " is not valid for a " + sourceSystem + " company.";
			}
			return "";
		}
	}
}
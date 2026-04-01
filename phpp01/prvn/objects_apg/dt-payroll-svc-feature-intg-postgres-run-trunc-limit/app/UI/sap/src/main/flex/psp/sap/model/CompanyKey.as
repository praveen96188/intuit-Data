package psp.sap.model
{
	
	
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.ExplorerEnum;
	import psp.sap.viewmodel.CompanyInspectorViewModel;
	
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyKey")]
	public class CompanyKey
	{
		public var sourceSystemCd:String;
		public var companyId:String;
	
		public function CompanyKey(sourceSystemCd:String = null, companyId:String = null):void {
			this.sourceSystemCd = sourceSystemCd;
			this.companyId = companyId;
		}
		
		public static function create(entity:Object):CompanyKey {
			if (entity == null)
				return null;
				
			if (entity.hasOwnProperty('sourceSystemCd') && entity.hasOwnProperty('companyId'))
				return new CompanyKey(entity['sourceSystemCd'], entity['companyId']);

			return null;
		}
		
		public function equals(value:Object):Boolean {
			if (value == null)
				return false;

			if (value is String) {
				// expected format <sourceSystemCd>:<companyId>
				return StringUtil.trim(String(value)) == this.toString();
			}

			if (!value.hasOwnProperty('sourceSystemCd') || !value.hasOwnProperty('companyId'))
				return false;
				
			return (sourceSystemCd == value['sourceSystemCd'] && companyId == value['companyId']);
		}
		
		public function toString():String {
			return sourceSystemCd + ":" + companyId;
		}
		
		public function display():void {
			var companyToDisplay:Company = null;
			
			//if we already have the company, display that			
			for each (var inspector:CompanyInspectorViewModel in (SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY).inspectors)) {				
				if (inspector.company != null && inspector.company.companyKey.equals(this)) {
					companyToDisplay = null;
				}
			}
			
			if (companyToDisplay != null) {
				companyToDisplay.display();
			} else {
				SAP.instance.companyService.findCompany(this.sourceSystemCd,
																		this.companyId,
																		new Responder(onCompanyResults,onCompanyLoadFaulted));
			}	
		}
				
		public function onCompanyResults(e:ResultEvent):void {
			(e.result as Company).display();		 	 			
		}
				
		public function onCompanyLoadFaulted(e:FaultEvent, token:Object=null):void {
			//TODO: what to do if load faults?
			//answer: the same thing we do everywhere else: pretend it never happened :)
		}

		
	}
}
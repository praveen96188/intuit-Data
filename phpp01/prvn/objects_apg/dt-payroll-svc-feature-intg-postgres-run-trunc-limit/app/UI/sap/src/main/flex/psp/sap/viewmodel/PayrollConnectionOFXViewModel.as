package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
    import mx.formatters.DateFormatter;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
	import psp.sap.application.enums.OperationsEnum;
	import psp.sap.model.Transmission;
	import psp.sap.view.UIUtils;
	
	public class PayrollConnectionOFXViewModel extends AbstractPartViewModel
	{
        [Bindable] [BackingProperty(context=true)] public var payrollTransmissionId:String;
                
		private var mTransmission:Transmission;
		private var mTransmissionText:String;
		private var mDateFormatter:DateFormatter = new DateFormatter();
		
		public function PayrollConnectionOFXViewModel()
        {
            this.label = CompanyInspectorPageEnum.PAYROLL_CONNECTION_OFX;     	
            
            mDateFormatter.formatString = SAP.instance.configuration.dateTimeFormatMedium;				            
        }
        public static function createActivator(payrollTransmissionId:String):Object {
            return {"payrollTransmissionId":payrollTransmissionId};
        }

        [Bindable]
		public function set transmission(value:Transmission):void {
			mTransmission = value;
			updateTransmissionText();
		}
		
		public function get transmission():Transmission {
			return mTransmission;
		}																		
		
		[Bindable]
		public function set transmissionText(value:String):void {
			mTransmissionText = value;
		}
		
		public function get transmissionText():String {
			return mTransmissionText;
		}		
		
		
		public function get dateFormatter():DateFormatter {
			return mDateFormatter;					
		}							
		
		
		public function get requestDocument():String {
			return transmission.requestDocument;
		}
		
		public function get responseDocument():String {
			return transmission.responseDocument;
		}
		
        protected function updateTransmissionText():void {
            transmissionText = "Request\n=====================================================\n\n" +
                    requestDocument + "\n\nResponse\n=====================================================\n\n" +
                    responseDocument + "\n";
		}		
		
		
		override protected function loadModelData():void {
			SAP.instance.companyService.findTransmissionById(payrollTransmissionId,
					createLoadModelDataResponder(onTransmissionLoaded));
		}
		
		public function onTransmissionLoaded(e:ResultEvent):void {
			transmission = e.result as Transmission;
		}

		
		override public function get hasChanged():Boolean {
			return true;
		} 


	}
}

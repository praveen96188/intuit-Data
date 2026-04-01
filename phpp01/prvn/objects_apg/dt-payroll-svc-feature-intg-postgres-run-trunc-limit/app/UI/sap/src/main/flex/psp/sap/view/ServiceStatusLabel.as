package psp.sap.view
{
    import flash.events.MouseEvent;

    import mx.controls.Label;

    import psp.sap.application.CompanyInspectorLinkHandler;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.DisplayStatus;
    import psp.sap.viewmodel.CompanyExplorerViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;

    /*
	for the new searches using companysearchresult	
	*/
	public class ServiceStatusLabel extends Label
	{

		private var mDisplayStatus:DisplayStatus;
 		
		public static const COLOR_RED:uint = 0xff0000;
		public static const COLOR_GREEN:uint = 0x009900;

        [Bindable] public var linkToSubscriptionStatus:Boolean = false;
        [Bindable] public var serviceCode:String = null;

        private var canLinkToSubscriptionStatus:Boolean = false;
		
		public function ServiceStatusLabel()
		{
			super();
			this.setStyle("textAlign", "left");

            this.addEventListener(MouseEvent.CLICK, onClick, false, 0, true);
            this.addEventListener(MouseEvent.ROLL_OVER, onRollover, false, 0, true);
            this.addEventListener(MouseEvent.ROLL_OUT, onRollout, false, 0, true);
		}
		
		public function set displayStatus(value:DisplayStatus):void {			
			mDisplayStatus = value;
			
			super.text = value.displayStatus;
			setStatusColor(value.displayStatus);
			this.toolTip = value.displayDetails;

            if(linkToSubscriptionStatus && SAP.canPerformOperation(OperationsEnum.STATUS_UPDATE)) {
                useHandCursor = true;
                canLinkToSubscriptionStatus = true;
                this.selectable = false;
            }
            else {
                this.selectable = true;
            }
									
		}
							
		override public function set text(value:String):void {
			// disallow direct setting of text
		}
							
		private function setStatusColor(statusCd:String):void {			
			if(mDisplayStatus.displayStatus == "On Hold") {
				this.setStyle("color", COLOR_RED);
			} else {
				if(statusCd != "") {
					if(statusCd.indexOf("Active") > -1) {
						this.setStyle("color", COLOR_GREEN);
					} else {
						this.setStyle("color", COLOR_RED);
					}
				} else {
					this.setStyle("color", COLOR_RED);
				}
			}			
		}

        protected function onClick(e:MouseEvent):void {
            if(canLinkToSubscriptionStatus) {
                var explorer:CompanyExplorerViewModel = SAP.instance.activeExplorer as CompanyExplorerViewModel;
                var inspector:CompanyInspectorViewModel =  explorer.activeInspector as CompanyInspectorViewModel;
                new CompanyInspectorLinkHandler(inspector).goToEditSubscriptionStatus(serviceCode);
            }
        }

        protected function onRollover(e:MouseEvent):void {
            if(canLinkToSubscriptionStatus){
                this.setStyle("textDecoration", "underline");
            }
        }

        protected function onRollout(e:MouseEvent):void {
            if(canLinkToSubscriptionStatus){
                this.setStyle("textDecoration", "none");
            }
        }
    }
}
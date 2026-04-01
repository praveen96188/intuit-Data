package intuit.sbd.flex.framework.service {
    import mx.rpc.events.FaultEvent;

    public class ServiceUtils {

        public static function getFaultMessage(e:FaultEvent):String {
            var faultMessage:String = "ERROR - Save Failed";
            if (e != null && e.fault != null) {
                if (e.fault.faultDetail != null)
                    faultMessage = e.fault.faultDetail;
                else if (e.fault.faultString != null)
                    faultMessage = e.fault.faultString;
            }
            return faultMessage;
        }

        //for whatever reason, the details are actually in the string
        //so for logging, we want to get as much as we can get, so we'll get the details
        //that is to say the string
        public static function getFaultDetails(e:FaultEvent):String {
            var faultMessage:String = "ERROR - Save Failed";
            if (e != null && e.fault != null) {
                if (e.fault.faultString != null)
                    faultMessage = e.fault.faultString;
                else if (e.fault.faultDetail != null)
                    faultMessage = e.fault.faultDetail;
            }
            return faultMessage;
        }

    }
}
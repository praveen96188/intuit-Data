package psp.sap.model.companyevents
{
	import mx.collections.ArrayCollection;
    import psp.sap.application.SAP;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyEventEmail")]
	public class CompanyEventEmail
	{
		public var id:String;
        public var templateType:String;
		public var status:String;
		public var effectiveDate:String;
        public var mtlEnabled:Boolean;

        [ArrayElementType("psp.sap.model.companyevents.CompanyEventEmailParam")]
        public var emailParams:ArrayCollection;

        [Transient]
        public function get summaryText():String {
            var noteString:String = "";

            var status:String = (status == null) ? "" : status;
            var effectiveDate:String = (effectiveDate == null) ? "" : effectiveDate;

            if (noteString.length > 0) {
                noteString += "\n"; // add a line to make display more readable
            }

            noteString += "Email Status - " + status + " - " + effectiveDate;

            if (status == "Sent" || status == "SendFailed") {
                noteString += " <a href='event:resendEmail=" + id + "'>Resend</a>";
            }

            var mtlEnabled:Boolean = (mtlEnabled == null || mtlEnabled == false) ? false : true;

            var sessionUserEmailAddress = SAP.instance.session.user.emailAddress;
            sessionUserEmailAddress = (sessionUserEmailAddress == "") ? null : sessionUserEmailAddress;

            if(mtlEnabled && sessionUserEmailAddress != null) {
                noteString += "\t<a href='event:sendEmailToMtl=" + id + "'>Regenerate Email (MTL exam)</a>"
            }

            noteString += "\n";

            if (emailParams != null) {
                for each (var emailParam:CompanyEventEmailParam in emailParams) {
                    var paramName:String = (emailParam.paramType == null) ? "" : emailParam.paramType;
                    var paramValue:String = (emailParam.paramValue == null) ? "" : emailParam.paramValue;
                    noteString += "\t" + paramName + " - " + paramValue + "\n";
                }
            }
            
            return noteString;
        }
	}
}
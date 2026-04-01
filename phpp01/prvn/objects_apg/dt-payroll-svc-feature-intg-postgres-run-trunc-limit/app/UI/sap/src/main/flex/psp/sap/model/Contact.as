package psp.sap.model
{
    import flash.events.EventDispatcher;

    import mx.events.PropertyChangeEvent;
    import mx.utils.StringUtil;

    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPContact")]
    public class Contact extends EventDispatcher
    {

        public var description:String; //used for importing contacts, but otherwise not used

        [Transient] [BackingProperty(hasChanged=false)] public var newContactOrder:int=-1;
        [Transient] [BackingProperty(hasChanged=false)] public var isDeleted:Boolean = false;
        [Transient] [BackingProperty(hasChanged=false)] public var errors:int = 0;
        [Transient] [BackingProperty(hasChanged=false)] public var changed:Boolean = false;

        private var mContactRoleCd : String;
        private var mContactRole : ContactRole;

        public var contactId:String;
        public var firstName : String;

        private var mMiddleName : String;
        public function get middleName():String {
            return isEmptyString(mMiddleName) ? null : mMiddleName;
        }

        public function set middleName(value:String):void {
            mMiddleName = value;
        }

        public var lastName : String;
        public var email : String;
        public var phoneNumber : String;
        public var address : Address;
        public var accountSignatory : Boolean;
        public var hasInvalidEmail : Boolean;
        public var socialSecurityNumber: String;
        public var dateOfBirth: Date;


        private var mFaxNumber : String;
        public function get faxNumber():String {
            return isEmptyString(mFaxNumber) ? null : mFaxNumber;
        }

        public function set faxNumber(value:String):void {
            mFaxNumber = value;
        }

        private var mPrefix : String;
        public function get prefix():String {
            return isEmptyString(mPrefix) ? null : mPrefix;
        }

        public function set prefix(value:String):void {
            mPrefix = value;
        }

        private var mSuffix : String;
        public function get suffix():String {
            return isEmptyString(mSuffix) ? null : mSuffix;
        }

        public function set suffix(value:String):void {
            mSuffix = value;
        }

        private var mJobTitle : String;
        public function get jobTitle():String {
            return isEmptyString(mJobTitle) ? null : mJobTitle;
        }

        public function set jobTitle(value:String):void {
            mJobTitle = value;
        }

        public var communicationTypeCd:String;

        public function get contactRoleCd():String {
            return mContactRoleCd;
        }

        public function set contactRoleCd(value:String):void {
            mContactRoleCd = value;

            var oldValue:ContactRole = mContactRole;
            mContactRole = ContactRole.fromCode(value);
            dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "contactRole", oldValue, mContactRole) );
        }

        [Transient]
        [Bindable("propertyChange")]
        public function get contactRole():ContactRole {
            return mContactRole;
        }

        [Transient]
        public function get communicationPref():CommunicationPrefEnum {
            return CommunicationPrefEnum.getEnumForCode(this.communicationTypeCd);
        }

        public function set communicationPref(value:CommunicationPrefEnum):void {
            if (value != null) {
                this.communicationTypeCd = value.code;
            }
        }

        override public function toString():String {
            var contact:String = "";

            contact = firstName;
            if (middleName != null) contact += " " + middleName;
            contact += " " + lastName;
            contact += "\n" + "  Phone: " + (phoneNumber != null ? phoneNumber : "");
            contact += "\n" + "  Email: " + (email != null ? email : "");
            contact += "\n" + address.toString();

            return contact;
        }

        public function get contactName():String {
            var contact:String = "";
            contact = firstName;
            if (middleName != null) contact += " " + middleName;
            contact += " " + lastName;
            return contact;
        }

        public function get phoneString():String {
            return "Phone: " + (phoneNumber != null ? phoneNumber : "");
        }

        public function get emailString():String {
            return "Email: " + (email != null ? email : "");
        }

        private function isEmptyString(value:String):Boolean {
            return (value == null || StringUtil.trim(value).length == 0);
        }

        [Bindable(event="propertyChange")]
        public function get label():String {
            var newLabel:String = contactRole != null ? contactRole.name : "";
            if (newContactOrder > -1) {
                newLabel += " (new)";
            }
            return newLabel;
        }

        [Bindable(event="propertyChange")]
        public function get toolTip():String {
            return firstName + " " + lastName;
        }

        [Bindable(event="propertyChange")]
        public function get menuLabel():String {
            var label:String = firstName ? firstName + " " : "";            
            label += lastName ? lastName : "";
            label += "(";
            label += description ? description : contactRole.name;
            label += ")";

            return label;
        }

        [Bindable(event="propertyChange")]
        public function get canDelete():Boolean {
            return contactRole != ContactRole.PAYROLL_ADMIN && contactRole != ContactRole.PRIMARY_PRINCIPAL;
        }

        //for sorting
        private var mOriginalName:String=null;
        public function getOriginalName():String {
            if (mOriginalName == null) {
                mOriginalName = contactName;
            }            
            return mOriginalName;
        }

        //for really last chance sorting
        //don't include in equality checking
        private var mRandomId:Number = Math.random();
        public function getRandomId():Number {
            return mRandomId;
        }

    }
}
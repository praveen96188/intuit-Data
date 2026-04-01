package psp.sap.model
{
	[Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPContactRole")]
	public class ContactRole
	{
		public static const PRIMARY_PRINCIPAL:ContactRole = new ContactRole("Primary Principal","Primary Principal", "PrimaryPrincipal",1);
        public static const SECONDARY_PRINCIPAL:ContactRole = new ContactRole("Secondary Principal","Secondary Principal","SecondaryPrincipal",2);
        public static const PAYROLL_ADMIN:ContactRole = new ContactRole("Payroll Admin", "Payroll Admin","PayrollAdmin",0);
        public static const OTHER:ContactRole = new ContactRole("Other","Other","Other",3);

        public static const values:Array = [PRIMARY_PRINCIPAL, SECONDARY_PRINCIPAL, PAYROLL_ADMIN, OTHER];

        public var name: String;
		public var description: String;
		public var contactRoleCd: String;
        public var sortOrder:int;

        public function ContactRole(name:String=null, description:String=null, contactRoleCd:String=null, sortOrder:int=0) {
            this.name = name;
            this.description = description;
            this.contactRoleCd = contactRoleCd;
            this.sortOrder = sortOrder;
        }

        public static function fromCode(code:String):ContactRole {
            for each (var contactRole:ContactRole in values) {
                if (contactRole.contactRoleCd == code) {
                    return contactRole;
                }
            }
            return null;
        }
        
    }
}
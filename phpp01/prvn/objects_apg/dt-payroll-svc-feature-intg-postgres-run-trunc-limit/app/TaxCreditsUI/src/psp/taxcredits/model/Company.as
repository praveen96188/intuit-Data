package psp.taxcredits.model {
    import mx.resources.ResourceManager;

    public class Company {

        public var profitLabel:String = ResourceManager.getInstance().getString('eligibility','type_of_business_profit');
        public var nonProfitLabel:String = ResourceManager.getInstance().getString('eligibility','type_of_business_non_profit');
        [Bindable] public var businessTypes:Array = ["", profitLabel , nonProfitLabel];
        [Bindable] public var businessType:String="";

        [Bindable] public var companyEligible:Boolean;
        [Bindable] public var companyEligibleSet:Boolean=false;

        [Bindable] public var legalName:String;
        [Bindable] public var ein:String;
        [Bindable] public var address1:String;
        [Bindable] public var address2:String;
        [Bindable] public var city:String;
        [Bindable] public var state:String;
        [Bindable] public var zip:String;
        [Bindable] public var contactName:String;
        [Bindable] public var phone1:String;
        [Bindable] public var phone2:String;
        [Bindable] public var phone3:String;
        [Bindable] public var phoneX:String;
        [Bindable] public var contactEmail:String;

        public var corporationLabel:String = ResourceManager.getInstance().getString('companyInfo','company_type_corporation');
        public var partnershipLabel:String = ResourceManager.getInstance().getString('companyInfo','company_type_partnership');
        public var soleLabel:String = ResourceManager.getInstance().getString('companyInfo','company_type_sole');
        [Bindable] public var companyTypes:Array = ["", corporationLabel, partnershipLabel, soleLabel];
        private var mCompanyType:String;

        [Bindable] public var authSignerLabel:String;
        [Bindable] public var authSignerHelp:String;
        
        private var mAuthorizedSignerEmail:String;
        [Bindable] public var isAuthorizedText:String;
        [Bindable] public var isAuthorized:Boolean=false;        

        [Bindable] public var fiscalYearStartDateMonth:String;
        [Bindable] public var fiscalYearStartDateDay:String;

        public function Company() {
            companyType = "";
        }

        public function determineCompanyEligibility():void {
            if (businessType != "") {
                companyEligibleSet = true;
                companyEligible = (businessType == profitLabel);
            } else {
                companyEligibleSet = false;
            }
        }

        [Bindable]
        public function get companyType():String {
            return mCompanyType;
        }

        public function set companyType(value:String):void {
            if (mCompanyType != value) {
                isAuthorized = false;
            }

            mCompanyType = value;


            if (value == "") {
                authSignerLabel = "";
                authSignerHelp = "";
                isAuthorizedText = "";
            } else {
                var authSignerTitle:String;
                var authSignerDefinite:String;
                var authSignerIndefinite:String;
                if (companyType == corporationLabel) {
                    authSignerTitle = ResourceManager.getInstance().getString('companyInfo','authorized_signer_corporation_title');
                    authSignerDefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_corporation_title_definite');
                    authSignerIndefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_corporation_title_indefinite');
                } else if (companyType == partnershipLabel) {
                    authSignerTitle = ResourceManager.getInstance().getString('companyInfo','authorized_signer_partnership_title');
                    authSignerDefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_partnership_title_definite');
                    authSignerIndefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_partnership_title_indefinite');
                } else if (companyType == soleLabel) {
                    authSignerTitle = ResourceManager.getInstance().getString('companyInfo','authorized_signer_sole_title');
                    authSignerDefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_sole_title_definite');
                    authSignerIndefinite = ResourceManager.getInstance().getString('companyInfo','authorized_signer_sole_title_indefinite');
                }
                authSignerLabel = "(" + authSignerTitle + ")";
                authSignerHelp =  ResourceManager.getInstance().getString('companyInfo','authorized_signer_required_help', [authSignerIndefinite, authSignerDefinite]);
                isAuthorizedText = ResourceManager.getInstance().getString('companyInfo','authorized_signer_required', [authSignerIndefinite]); 
            }

        }


        [Bindable]
        public function get authorizedSignerEmail():String {
            return mAuthorizedSignerEmail;
        }

        public function set authorizedSignerEmail(value:String):void {
            if (value != mAuthorizedSignerEmail) {
                isAuthorized = false;
            }
            mAuthorizedSignerEmail = value;
        }
    }
}
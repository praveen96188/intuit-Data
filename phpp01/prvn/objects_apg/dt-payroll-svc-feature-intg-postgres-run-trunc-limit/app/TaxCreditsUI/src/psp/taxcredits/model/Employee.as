package psp.taxcredits.model {
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.ProgressEvent;

    import mx.events.PropertyChangeEvent;
    import mx.formatters.DateFormatter;

    [Event(name="eligibilityChange", type="psp.taxcredits.model.TaxCreditsEvent")]
    public class Employee extends EventDispatcher {

        public static const INELIGIBLE_MAJORITY_OWNER:String = "ineligibleMajorityOwner";
        public static const INELIGIBLE_RELATIVE:String = "ineligibleRelative";
        public static const INELIGIBLE_PREVIOUSLY_EMPLOYED:String = "ineligiblePreviouslyEmployed";
        public static const INELIGIBLE_TOO_LATE:String = "ineligibleTooLate";

        private var receivedByDateFormatter:DateFormatter;

        //pre-screen
        [Bindable] public var majorityOwner:Boolean;
        [Bindable] public var majorityOwnerSet:Boolean=false;
        [Bindable] public var relative:Boolean;
        [Bindable] public var relativeSet:Boolean=false;
        [Bindable] public var previouslyHired:Boolean;
        [Bindable] public var previouslyHiredSet:Boolean;

        private var mHireDate:Date;

        [Bindable] public var employeePreEligible:Boolean;
        [Bindable] public var employeePreEligibleSet:Boolean=false;
        [Bindable] public var employeePreIneligibleType:String;

        //backing properties
        //personal info
        [Bindable] public var firstName:String;
        [Bindable] public var middleInitial:String;
        [Bindable] public var lastName:String;
        [Bindable] public var ssn:String;
        [Bindable] public var dobMonth:String;
        [Bindable] public var dobDay:String;
        [Bindable] public var dobYear:String;
        [Bindable] public var address:String;
        [Bindable] public var city:String;
        [Bindable] public var state:String;
        [Bindable] public var zip:String;
        [Bindable] public var county:String;
        [Bindable] public var phone1:String;
        [Bindable] public var phone2:String;
        [Bindable] public var phone3:String;
        [Bindable] public var phoneX:String;
        [Bindable] public var email:String;
        [Bindable] public var startingWage:String;
        [Bindable] public var position:String;
        [Bindable] public var offeredDate:Date;
        [Bindable] public var startDate:Date;
        [Bindable] public var workState:String;

        //snap
        [Bindable] public var snapEver:Boolean;
        [Bindable] public var snapEverSet:Boolean=false;
        [Bindable] public var snapLast6:Boolean;
        [Bindable] public var snapLast6Set:Boolean=false;
        [Bindable] public var snap3of5:Boolean;
        [Bindable] public var snap3of5Set:Boolean=false;
        [Bindable] public var snap3Benefits:Benefits = new Benefits();
        [Bindable] public var snap5Benefits:Benefits = new Benefits();

        //tanf
        [Bindable] public var tanfEver:Boolean;
        [Bindable] public var tanfEverSet:Boolean=false;
        [Bindable] public var tanfLast18:Boolean;
        [Bindable] public var tanfLast18Set:Boolean=false;
        [Bindable] public var tanfStateLaw:Boolean;
        [Bindable] public var tanfStateLawSet:Boolean = false;
        [Bindable] public var tanf9of18:Boolean;
        [Bindable] public var tanf9of18Set:Boolean = false;
        [Bindable] public var tanf3Benefits:Benefits = new Benefits();
        [Bindable] public var tanf5Benefits:Benefits = new Benefits();
        [Bindable] public var tanf7Benefits:Benefits = new Benefits();

        //referral
        [Bindable] public var referralRehab:Boolean;
        [Bindable] public var referralRehabSet:Boolean=false;
        [Bindable] public var referralNetwork:Boolean;
        [Bindable] public var referralNetworkSet:Boolean=false;
        [Bindable] public var referralDva:Boolean;
        [Bindable] public var referralDvaSet:Boolean=false;

        //veteran
        [Bindable] public var veteranEver:Boolean;
        [Bindable] public var veteranEverSet:Boolean=false;
        [Bindable] public var veteranSnap:Boolean;
        [Bindable] public var veteranSnapSet:Boolean=false;
        [Bindable] public var veteranDisabled:Boolean;
        [Bindable] public var veteranDisabledSet:Boolean = false;
        [Bindable] public var veteranDischargedYear:Boolean;
        [Bindable] public var veteranDischargedYearSet:Boolean=false;
        [Bindable] public var veteranUnemployed6:Boolean;
        [Bindable] public var veteranUnemployed6Set:Boolean = false;
        [Bindable] public var veteranActive180:Boolean;
        [Bindable] public var veteranActive180Set:Boolean=false;
        [Bindable] public var veteranDischargedDisability:Boolean;
        [Bindable] public var veteranDischargedDisabilitySet:Boolean=false;
        [Bindable] public var veteranDischarged5:Boolean;
        [Bindable] public var veteranDischarged5Set:Boolean = false;
        [Bindable] public var veteranUnemployment4:Boolean;
        [Bindable] public var veteranUnemployment4Set:Boolean=false;
        [Bindable] public var veteranBenefits:Benefits = new Benefits();

        //disconnected youth
        [Bindable] public var disconnectedEver:Boolean;
        [Bindable] public var disconnectedEverSet:Boolean=false;
        [Bindable] public var disconnectedHighSchool:Boolean;
        [Bindable] public var disconnectedHighSchoolSet:Boolean=false;
        [Bindable] public var disconnectedAttend:Boolean;
        [Bindable] public var disconnectedAttendSet:Boolean=false;
        [Bindable] public var disconnectedAdmitted:Boolean;
        [Bindable] public var disconnectedAdmittedSet:Boolean=false;
        [Bindable] public var disconnectedJob6:Boolean;
        [Bindable] public var disconnectedJob6Set:Boolean;

        //felon
        [Bindable] public var felonEver:Boolean;
        [Bindable] public var felonEverSet:Boolean=false;
        [Bindable] public var felonConvictionYear:String;
        [Bindable] public var felonConvictionMonth:String;
        [Bindable] public var felonConvictionDay:String;
        [Bindable] public var felonReleaseYear:String;
        [Bindable] public var felonReleaseMonth:String;
        [Bindable] public var felonReleaseDay:String;
        [Bindable] public var felonFederal:Boolean;
        [Bindable] public var felonFederalSet:Boolean=false;

        //other
        [Bindable] public var ssi:Boolean;
        [Bindable] public var ssiSet:Boolean=false;
        [Bindable] public var summerYouth:Boolean;
        [Bindable] public var summerYouthSet:Boolean=false;
        [Bindable] public var conditional:Boolean;
        [Bindable] public var conditionalSet:Boolean=false;
        [Bindable] public var unemployed60:Boolean;
        [Bindable] public var unemployed60Set:Boolean=false;



        //location
        [Bindable] public var ezRC:Boolean;
        [Bindable] public var ezRCSet:Boolean=false; //was this set by the employee?
        [Bindable] public var ezRCHUD:Boolean=false; //was this set from HUD?        

        public function Employee() {
            receivedByDateFormatter = new DateFormatter();
            receivedByDateFormatter.formatString = "MMMM D, YYYY";
        }

        [Bindable]
        public function get hireDate():Date {
            return mHireDate;
        }
        public function set hireDate(value:Date):void {
            //the date field likes to pass null in whenever it feels like ("I had a bad day and we're out of Ben & Jerry's so I think I'll just write down some null values")
            // and since we don't allow it to be cleared, we'll just do this
            if (value == null) {
                return;
            }
            mHireDate = value;            
        }

        public function get locationPageEnabled():Boolean {
            return !this.ezRCHUD && TaxCreditsModel.instance.pspDate.getFullYear() - dob.getFullYear() < 51; //close enough--will be exact to qualify but not to ask
        }


        public function get dob():Date {
            var dobYearInt:int = parseInt(dobYear);
            var dobMonthInt:int = parseInt(dobMonth);
            var dobDayInt:int = parseInt(dobDay);

            if (isNaN(dobYearInt) || isNaN(dobMonthInt) || isNaN(dobDayInt)) {
                return null
            } else {
                return new Date(dobYearInt, dobMonthInt-1, dobDayInt);
            }
        }

        public function get felonConvictionDate():Date {
            var yearInt:int = parseInt(felonConvictionYear);
            var monthInt:int = parseInt(felonConvictionMonth);
            var dayInt:int = parseInt(felonConvictionDay);

            if (isNaN(yearInt) || isNaN(monthInt) || isNaN(dayInt)) {
                return null
            } else {
                return new Date(yearInt, monthInt-1, dayInt);
            }
        }

        public function get felonReleaseDate():Date {
            var yearInt:int = parseInt(felonReleaseYear);
            var monthInt:int = parseInt(felonReleaseMonth);
            var dayInt:int = parseInt(felonReleaseDay);

            if (isNaN(yearInt) || isNaN(monthInt) || isNaN(dayInt)) {
                return null
            } else {
                return new Date(yearInt, monthInt-1, dayInt);
            }
        }

        public function determineEmployeeEligibility():void {
            if (hireDate != null && ModelUtils.dayDifference(hireDate, TaxCreditsModel.instance.pspDate) > TaxCreditsModel.DAY_LIMIT ) {
                employeePreEligible = false;
                employeePreIneligibleType = INELIGIBLE_TOO_LATE;
                employeePreEligibleSet = true;
            } else if (majorityOwnerSet && majorityOwner) {
                employeePreEligible = false;
                employeePreIneligibleType = INELIGIBLE_MAJORITY_OWNER;
                employeePreEligibleSet = true;
            } else if (relativeSet && relative) {
                employeePreEligible = false;
                employeePreIneligibleType = INELIGIBLE_RELATIVE;
                employeePreEligibleSet = true;
            } else if (previouslyHiredSet && previouslyHired) {
                employeePreEligible = false;
                employeePreIneligibleType = INELIGIBLE_PREVIOUSLY_EMPLOYED;
                employeePreEligibleSet = true;
            } else if (majorityOwnerSet && relativeSet && previouslyHiredSet && hireDate != null ) {
                employeePreEligible = true;
                employeePreEligibleSet = true;
                employeePreIneligibleType = "";
            } else {
                employeePreEligibleSet = false;
                employeePreIneligibleType = "";                
            }

            dispatchEvent(TaxCreditsEvent.createEligibilityChangeEvent());
        }

        public function getPostmarkedByDate():String {
            return receivedByDateFormatter.format(ModelUtils.addDays(hireDate, TaxCreditsModel.DAY_LIMIT));    
        }



    }

}


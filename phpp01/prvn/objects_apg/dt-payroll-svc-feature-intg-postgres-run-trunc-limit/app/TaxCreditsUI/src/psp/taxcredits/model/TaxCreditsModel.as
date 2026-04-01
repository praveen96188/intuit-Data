package psp.taxcredits.model {
    import com.google.analytics.AnalyticsTracker;

    import flash.events.EventDispatcher;
    import flash.external.ExternalInterface;

    import flash.net.URLRequest;

    import flash.net.URLVariables;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.resources.ResourceManager;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;

    import psp.taxcredits.dto.Address;
    import psp.taxcredits.dto.ApplicationInfo;
    import psp.taxcredits.dto.EligibilityInfo;
    import psp.taxcredits.dto.EmployeeInfo;
    import psp.taxcredits.dto.EmployerInfo;
    import psp.taxcredits.dto.WOTCCategory;
    import psp.taxcredits.service.TaxCreditsService;

    public class TaxCreditsModel extends EventDispatcher {
        
        [Bindable] public var initialized:Boolean = false;

        [Bindable] public var taxCreditsService:TaxCreditsService;

        [Bindable] public var pageManager:PageManager;
        [Bindable] public var tracker:AnalyticsTracker;

        [Bindable] public var company:Company;
        [Bindable] public var employee:Employee;

        [Bindable] public var eligibleCategories:ArrayCollection;


        //assuming that this won't "roll over" during visit
        [Bindable] public var pspDate:Date;

        [Bindable] public var estimatorModel:EstimatorModel;

        [Bindable] public var applicationInfo:ApplicationInfo;

        [Bindable] public var offer:String;
        [Bindable] public var offerCode:String; //same as offer, but never empty
        [Bindable] public var price:String;

        private var isSubmitting:Boolean = false;

        private var cachedEmployerInfo:EmployerInfo = null;
        private var cachedEmployeeInfo:EmployeeInfo = null;
        private var cachedEligibilityInfo:EligibilityInfo = null;

        [Bindable] public var errors:ArrayCollection = new ArrayCollection();

        //maximum number of days between hiring and receiving forms
        public static var DAY_LIMIT:int = 23;

        //Singleton pattern implementation
        private static var mInstance:TaxCreditsModel;
        public static function get instance():TaxCreditsModel
        {
            if (mInstance == null) {
                mInstance = new TaxCreditsModel(new Private());
            }
            return mInstance;
        }
        public function TaxCreditsModel(accessPrivate:Private) {
            taxCreditsService = new TaxCreditsService();
            pageManager = new PageManager();
            company = new Company();
            employee = new Employee();
            estimatorModel = new EstimatorModel(this);
        }

        public function preInitialize():void {
            //test params for locale and deep linking
            //not passing to flash vars, so will rely on external interface (js)
            var queryString:String = ExternalInterface.call("window.location.search.substring", 1);
            var params:Array = queryString.split("&");

            var urlParams:Object = [];
            for (var i:uint=0,index:int=-1; i<length; i++)
            {
                var kvPair:String = params[i];
                if ((index = kvPair.indexOf("=")) > 0)
                {
                    var key:String = kvPair.substring(0,index);
                    urlParams[key] = kvPair.substring(index+1);
                }
            }

            offer = urlParams.offer;

            ResourceManager.getInstance().localeChain = ["en_US"];

            offerCode = offer;
            switch (offer) {
                case "TC800":
                    price = "$99";
                    break;
                case "TC600":
                    price = "$149";
                    break;
                case "TC400":
                default:
                    price = "$199";
                    offerCode = "TC400";
            }


            
        }

        public function initialize():void {

            //simulate a new page for tracking
            if (offer && offer != "") {
                tracker.trackPageview("/"+offer);
            }

            pageManager.initialize();
                        
            pageManager.switchPage(TaxCreditsPageEnum.INTRO);
            
                       
            taxCreditsService.getPSPDate(new Responder(onPSPDateLoaded, onFail));
        }

        public function onFail(e:FaultEvent):void {
            Alert.show("An error occured while processing your request: " + e.fault.faultString);
        }

        private function onPSPDateLoaded(e:ResultEvent):void {
            pspDate = e.result as Date;
            initialized = true;
            dispatchEvent(TaxCreditsEvent.createModelInitializedEvent());
        }

        public function checkEZRC():void {            
            employee.ezRCHUD = false;
            taxCreditsService.isAddressInRCorEZ(employee.address, employee.zip, new Responder(onCheckEZRCSuccess, onCheckEZRCFail));
        }

        private function onCheckEZRCSuccess(e:ResultEvent):void {
            employee.ezRC = e.result as Boolean;
            employee.ezRCHUD = true;
        }

        private function onCheckEZRCFail(e:FaultEvent):void {
            employee.ezRCHUD = false;            
        }

        public function getCategories():void {
            taxCreditsService.getCategories(createEligibilityInfo(), createEmployeeInfo(), new Responder(onCategoriesSuccess, onFail));
        }

        public function onCategoriesSuccess(e:ResultEvent):void {
            eligibleCategories = e.result as ArrayCollection;
            dispatchEvent(TaxCreditsEvent.createCategoriesLoadedEvent());
            dispatchEvent(TaxCreditsEvent.createEligibilityChangeEvent());
        }

        public function submitApplication():void {

            var employerInfo:EmployerInfo = createEmployerInfo();
            var employeeInfo:EmployeeInfo = createEmployeeInfo();
            var eligibilityInfo:EligibilityInfo = createEligibilityInfo();

            if (isSubmitting) {

            } else if (ObjectUtil.compare(employerInfo, cachedEmployerInfo) != 0
                    || ObjectUtil.compare(employeeInfo, cachedEmployeeInfo) != 0
                    || ObjectUtil.compare(eligibilityInfo, cachedEligibilityInfo) != 0) {
                //don't want to create a new PDF (packet/9061 in db) if nothing has changed

                cachedEmployerInfo = employerInfo;
                cachedEmployeeInfo = employeeInfo;
                cachedEligibilityInfo = eligibilityInfo;

                applicationInfo = null;

                taxCreditsService.submitApplication(employerInfo, employeeInfo, eligibilityInfo, new Responder(onDownloadComplete, onSubmitError));
            } else {
                dispatchEvent(TaxCreditsEvent.createApplicationSubmittedEvent());
            }


        }

        private function onDownloadComplete(e:ResultEvent):void {
            applicationInfo = ApplicationInfo(e.result);
            isSubmitting = false;            
            dispatchEvent(TaxCreditsEvent.createApplicationSubmittedEvent());
        }

        private function onSubmitError(e:FaultEvent):void {
            addError(e.toString());
            Alert.show("Error submitting application: " + e.toString());
            isSubmitting = false;
        }


        public function createEmployeeInfo():EmployeeInfo {
            var employeeInfo:EmployeeInfo = new EmployeeInfo();

            employeeInfo.dateOfBirth = employee.dob;

            employeeInfo.firstName = employee.firstName;
            employeeInfo.hireDate = employee.hireDate;
            employeeInfo.jobOfferDate = employee.offeredDate;
            employeeInfo.lastName = employee.lastName;            

            employeeInfo.middleInitial = employee.middleInitial;

            employeeInfo.position = employee.position;           
            employeeInfo.ssn = employee.ssn == null ? null : employee.ssn.replace(new RegExp("-", "g"), "");
            employeeInfo.startDate = employee.startDate;
            employeeInfo.startingWage = employee.startingWage;
            employeeInfo.telephoneNumber = employee.phone1 + employee.phone2 + employee.phone3;
            employeeInfo.telephoneExtension = employee.phoneX;
            employeeInfo.email = employee.email;
            employeeInfo.workState = employee.workState;
            
            var liveAddress:Address = new Address();
            liveAddress.address1 = employee.address;
            liveAddress.address2 = "";
            liveAddress.city = employee.city;
            liveAddress.state = employee.state;
            liveAddress.zip = employee.zip;
            liveAddress.county = employee.county;
            employeeInfo.liveAddress = liveAddress;

            return employeeInfo;
        }

        public function createEmployerInfo():EmployerInfo {
            var employerInfo:EmployerInfo = new EmployerInfo();
            employerInfo.ein = company.ein == null ? null : company.ein.replace(new RegExp("-", "g"), "");
            employerInfo.contactName = company.contactName;            
            employerInfo.telephoneNumber = company.phone1 + company.phone2 + company.phone3;
            employerInfo.telephoneExtension = company.phoneX;
            employerInfo.companyLegalName = company.legalName;
            employerInfo.contactEmail = company.contactEmail;
            employerInfo.offerCode = offer;
            employerInfo.companyType = company.companyType;
            employerInfo.authSignerEmail = company.authorizedSignerEmail;
            
            employerInfo.fiscalYearStartDateString = ModelUtils.pad2(company.fiscalYearStartDateMonth) + ModelUtils.pad2(company.fiscalYearStartDateDay);

            var legalAddress:Address = new Address();
            legalAddress.address1 = company.address1;
            legalAddress.address2 = company.address2;
            legalAddress.city = company.city;
            legalAddress.state = company.state;
            legalAddress.zip = company.zip;
            employerInfo.legalAddress = legalAddress;

            return employerInfo;
        }

        public function createEligibilityInfo():EligibilityInfo {
            var eligibilityInfo:EligibilityInfo = new EligibilityInfo();

            if (employee.snapEverSet && employee.snapEver) {
                if (employee.snapLast6Set && employee.snapLast6) {
                    eligibilityInfo.snapLast6Months = true;
                    eligibilityInfo.snapPrimaryRecipient = employee.snap3Benefits.name;
                    eligibilityInfo.snapCityStateBenefitsReceived = employee.snap3Benefits.city + ", " + employee.snap3Benefits.state;
                } else if (employee.snap3of5Set && employee.snap3of5) {
                    eligibilityInfo.snapLast3of5MonthsNoLongerEligible = true;
                    eligibilityInfo.snapPrimaryRecipient = employee.snap5Benefits.name;
                    eligibilityInfo.snapCityStateBenefitsReceived = employee.snap5Benefits.cityState;
                }
            }


            if (employee.tanfEverSet && employee.tanfEver) {
                if (employee.tanfLast18Set && employee.tanfLast18) {
                    eligibilityInfo.tanfLast18Months = true;
                    eligibilityInfo.tanfPrimaryRecipient = employee.tanf3Benefits.name;
                    eligibilityInfo.tanfCityStateBenefitsReceived = employee.tanf3Benefits.cityState;
                } else if (employee.tanfStateLawSet && employee.tanfStateLaw) {
                    eligibilityInfo.tanfStopLaw2Years = true;
                    eligibilityInfo.tanfPrimaryRecipient = employee.tanf5Benefits.name;
                    eligibilityInfo.tanfCityStateBenefitsReceived = employee.tanf5Benefits.cityState;
                } else if (employee.tanf9of18Set && employee.tanf9of18) {
                    eligibilityInfo.tanf9of18Months = true;
                    eligibilityInfo.tanfPrimaryRecipient = employee.tanf7Benefits.name;
                    eligibilityInfo.tanfCityStateBenefitsReceived = employee.tanf7Benefits.cityState;
                }
            }


            eligibilityInfo.referralVocationalRehabilitationAgency = employee.referralRehabSet && employee.referralRehab;
            eligibilityInfo.referralEmploymentNetwork = employee.referralNetworkSet && employee.referralNetwork;
            eligibilityInfo.referralDepartmentVeteranAffairs = employee.referralDvaSet && employee.referralDva;

            if (employee.veteranEverSet && employee.veteranEver) {
                eligibilityInfo.veteran = true;
                if (employee.veteranSnapSet && employee.veteranSnap) {
                    eligibilityInfo.veteranSnap3of15Months = true;
                    eligibilityInfo.veteranSnapPrimaryRecipient = employee.veteranBenefits.name;
                    eligibilityInfo.veteranSnapCityStateBenefitsReceived = employee.veteranBenefits.cityState;
                }
                if (employee.veteranDisabledSet && employee.veteranDisabled) {
                    eligibilityInfo.veteranDisability = true;
                    if (employee.veteranDischargedYearSet && employee.veteranDischargedYear) {
                        eligibilityInfo.veteranDischargedBeforeHired = true;
                    }
                    if (employee.veteranUnemployed6Set && employee.veteranUnemployed6) {
                        eligibilityInfo.veteranUnemployed6MonthsBeforeHired = true;
                    }
                }
                if (employee.veteranActive180Set && employee.veteranActive180) {
                    eligibilityInfo.veteranServed180Days = true;
                }
                if (employee.veteranDischargedDisabilitySet && employee.veteranDischargedDisability) {
                    eligibilityInfo.veteranDischargedServiceRelatedDisability = true;
                }
                if (eligibilityInfo.veteranServed180Days || eligibilityInfo.veteranDischargedServiceRelatedDisability) {
                    if (employee.veteranDischarged5Set && employee.veteranDischarged5) {
                        eligibilityInfo.veteranDischargedPast5Years = true;
                    }
                    if (employee.veteranUnemployment4Set && employee.veteranUnemployment4) {
                        eligibilityInfo.veteranUnemploymentCompensation4weeksOfLastYear = true
                    }
                }
            }

            eligibilityInfo.designatedEZorRC = employee.ezRC;

            //will do age calculation on the server, but want to not count answers if they calculated differently
            if (employee.disconnectedEverSet && employee.disconnectedEver) {
                if (employee.disconnectedHighSchoolSet && employee.disconnectedHighSchool) {
                    eligibilityInfo.disconnectedGraduateGED = true;
                }
                eligibilityInfo.disconnectedSchoolLessThan10Hours = !(employee.disconnectedAttendSet && employee.disconnectedAttend);
                eligibilityInfo.disconnectedAdmittedSinceCertificate = employee.disconnectedAdmittedSet && employee.disconnectedAdmitted;
                eligibilityInfo.disconnectedNotRegularlyEmployedLast6Months = !(employee.disconnectedJob6Set && employee.disconnectedJob6);
            }

            if (employee.felonEverSet && employee.felonEver) {
                eligibilityInfo.felonLastYear = true;
                eligibilityInfo.felonConvictionDate = employee.felonConvictionDate;
                eligibilityInfo.felonReleaseDate = employee.felonReleaseDate;
                eligibilityInfo.felonFederal = employee.felonFederalSet && employee.felonFederal;
            }

            eligibilityInfo.ssiWithin60Days = employee.ssiSet && employee.ssi;

            eligibilityInfo.summerYouthEmployedBetween1Mayand15September = employee.summerYouthSet && employee.summerYouth;

            eligibilityInfo.conditionalCertification = employee.conditionalSet && employee.conditional;

            eligibilityInfo.unemployed60 = employee.unemployed60Set && employee.unemployed60;

            return eligibilityInfo;
        }

        public function addError(error:String):void {
            errors.addItem(new Date().toString() +": "+ error);
        }

        public function getPrintInstructionsURL():URLRequest {
            var request:URLRequest = new URLRequest("printInstructions");
            var variables:URLVariables = new URLVariables();
            variables.password = applicationInfo.password;

            var categoryString:String = "";
            for each (var category:WOTCCategory in eligibleCategories) {
                categoryString+=category.category+",";
            }            
            variables.categories = categoryString.substr(0, categoryString.length-1);
            variables.eeEmail = employee.email;
            variables.erEmail = company.authorizedSignerEmail;
            variables.submitDate = employee.getPostmarkedByDate();
            variables.eeName = employee.firstName + " " + employee.lastName;
            variables.signerType = company.authSignerLabel.substr(1, company.authSignerLabel.length-2);
            request.data = variables;
            return request;
        }

    }
}



class Private {}

package psp.sap.viewmodel
{
    import flash.events.Event;
    import flash.utils.Dictionary;

    import mx.collections.ArrayCollection;
    import mx.formatters.PhoneFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;
    import mx.validators.EmailValidator;
    import mx.validators.PhoneNumberValidator;
    import mx.validators.StringValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.collections.LookupCollection;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.Address;
    import psp.sap.model.CommunicationPrefEnum;
    import psp.sap.model.CompanyContacts;
    import psp.sap.model.CompanyServiceState;
    import psp.sap.model.Contact;
    import psp.sap.model.ContactRole;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPEinValidator;
    import psp.sap.validators.SAPSsnValidator;
    import psp.sap.validators.SAPSsnValidator;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.EntityChangeEvent;

    public class CompanyEditContactInfoViewModel
	extends AbstractPartViewModel
	{
        public static const CONTACTS_CHANGED:String = "contactsChanged";

		public const PREFIX_LIST:ArrayCollection = new ArrayCollection(["", "Mr.", "Ms.", "Mrs.", "Miss", "Dr.", "Sir", "Hon.", "Rev."]);
		public const SUFFIX_LIST:ArrayCollection = new ArrayCollection(["", "Jr.", "Sr.", "III.", "CPA", "DC", "DDS", "DR", "DVM", "EA", "ESQ", "FATHER", "HON", "JD", "MD", "MISS", "MR", "MRS", "MS", "PROF", "RABBI", "REV", "RN", "PhD", "SIR"]);

        private var mPhoneFormatter:PhoneFormatter;

        [Bindable] public var selectedContactIndex:int;

        [ArrayElementType("psp.sap.model.Contact")]
        [Bindable] [BackingProperty(context=true, required=false, recursive=true)] public var contacts:ArrayCollection = new ArrayCollection();
        public var contactValidators:Dictionary = new Dictionary(true);

        [BackingProperty(context=true)] public var clearValues:Boolean = true;
        [ArrayElementType("psp.sap.model.Contact")]
        [Bindable] [BackingProperty(context=true, required=false)] public var additionalContacts:ArrayCollection = new ArrayCollection();

        [ArrayElementType("psp.sap.model.ContactRole")]
        [Bindable] public var availableRolesToAdd:ArrayCollection = new ArrayCollection();

        [Bindable] public var preferenceTypes:ArrayCollection = new ArrayCollection(CommunicationPrefEnum.values);

        [Bindable]
        [BackingProperty]
        public var dateOfBirth:String;

        [Bindable] public var showSaveButtons:Boolean = true;

        private var hasDeleted:Boolean = false;

        //noinspection JSFieldCanBeLocalInspection
        private var newContacts:int=0;

        [Bindable]
        public var caseId:String;

        private var contactsSnapshot:ArrayCollection = new ArrayCollection();

		public function CompanyEditContactInfoViewModel()
		{
			super();

			this.label = CompanyInspectorPageEnum.EDIT_COMPANY_CONTACT_INFO;
			this.reloadOnSave = true;

			mPhoneFormatter = new PhoneFormatter();
			mPhoneFormatter.validPatternChars = "()- .+";
		}


		public function formatPhone(contact:Contact):void {
			if (contact.phoneNumber != null) {
				var tempPhone:String = mPhoneFormatter.format(stripPhoneNonNumeric(contact.phoneNumber));
                if(tempPhone != null && tempPhone != ""){
                    contact.phoneNumber = tempPhone;
                }
			}
		}

		public function formatFax(contact:Contact):void {
            if (contact.faxNumber != null) {
				var tempPhone:String = mPhoneFormatter.format(stripPhoneNonNumeric(contact.faxNumber));
                if(tempPhone != null && tempPhone != ""){
                    contact.faxNumber = tempPhone;
                }
			}
		}

		public function formatState(contact:Contact):void {
            if (contact.address.state != null) {
				var tempState:String = contact.address.state.toUpperCase();
                if(tempState != null && tempState != ""){
                    contact.address.state = tempState;
                }
			}
		}

		private function stripPhoneNonNumeric(value:String):String {
			if (value != null) {
			   value = value.replace("(", "");
			   value = value.replace(")", "");
			   var dash:RegExp = /-/g;
			   value = value.replace(dash, "");
			   var space:RegExp = / /g;
			   value = value.replace(space, "");
			   return value;
			}
			return "";
		}

		override protected function initializeBackingProperties():void {

            hasDeleted = false;
            createValidatorsFromContacts();

            availableRolesToAdd.removeAll();

            //this shouldn't be the case, but empirically can be
            var paFound:Boolean = false;
            var ppFound:Boolean = false;

            for each (var roleContact:Contact in contacts) {
                if (roleContact.contactRole == ContactRole.PAYROLL_ADMIN) {
                    paFound = true;
                } else if (roleContact.contactRole == ContactRole.PRIMARY_PRINCIPAL) {
                    ppFound = true;

                }
            }

            if (company == null) {
                addPrincipalContactOptions(ppFound);
                addOtherContactOptions(paFound);
            } else if (company.companyServiceState == CompanyServiceState.DIYDD || company.companyServiceState == CompanyServiceState.DIYOnly) {
                addPrincipalContactOptions(ppFound);
                addOtherContactOptions(paFound);
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                if(SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS_PENDING_ACTIVATION)) {
                    addPrincipalContactOptions(ppFound);
                }
                if (SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_PENDING)) {
                    addOtherContactOptions(paFound);
                }
            } else if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                if(SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS)) {
                    addPrincipalContactOptions(ppFound);
                }
                if(SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_ACTIVE)) {
                    addOtherContactOptions(paFound);
                }
            }

        }

        private function addPrincipalContactOptions(ppFound:Boolean):void {
            if (!ppFound) {
                availableRolesToAdd.addItem(ContactRole.PRIMARY_PRINCIPAL);
            }
            availableRolesToAdd.addItem(ContactRole.SECONDARY_PRINCIPAL);
        }

        private function addOtherContactOptions(paFound:Boolean):void {
            if (! paFound ){
                availableRolesToAdd.addItem(ContactRole.PAYROLL_ADMIN);
            }
            availableRolesToAdd.addItem(ContactRole.OTHER);
        }

        private function createValidatorsFromContacts():void {
            clearValidators();
            contactValidators = new Dictionary(true);

            for each (var contact:Contact in contacts) {
                if (contact.isDeleted) {
                    continue;
                }
                if(contact.communicationTypeCd == null){
                    contact.communicationPref = CommunicationPrefEnum.EMAIL;
                }
                var thisContactValidators:ArrayCollection = new LookupCollection(Validator, null, "property");
                contactValidators[contact] = thisContactValidators;

                var firstNameValidator:Validator = SAPValidators.createRequiredFieldValidator(contact, "firstName", true);
                validators.push(firstNameValidator);
                thisContactValidators.addItem(firstNameValidator);

                var lastNameValidator:Validator = SAPValidators.createRequiredFieldValidator(contact, "lastName", true);
                validators.push(lastNameValidator);
                thisContactValidators.addItem(lastNameValidator);

                var phoneValidator:PhoneNumberValidator = new PhoneNumberValidator();
                phoneValidator.source = contact;
                phoneValidator.allowedFormatChars = "()- ";
                phoneValidator.required = true;
                phoneValidator.property = "phoneNumber";
                validators.push(phoneValidator);
                thisContactValidators.addItem(phoneValidator);

                var faxValidator:PhoneNumberValidator = new PhoneNumberValidator();
                faxValidator.source = contact;
                faxValidator.allowedFormatChars = "()- ";
                faxValidator.required = false;
                faxValidator.property = "faxNumber";
                validators.push(faxValidator);
                thisContactValidators.addItem(faxValidator);

                var emailValidator:EmailValidator = new EmailValidator();
                emailValidator.required = true;
                emailValidator.source = contact;
                emailValidator.property = "email";
                validators.push(emailValidator);
                thisContactValidators.addItem(emailValidator);

                //Required field validators for JPMC changes

                var socialSecurityNumberValidator:SAPSsnValidator = new SAPSsnValidator();
                socialSecurityNumberValidator.required = false;
                socialSecurityNumberValidator.source = contact;
                socialSecurityNumberValidator.enabled = (contact.contactRoleCd == "PrimaryPrincipal" && isDirectDepositCustomer());
                socialSecurityNumberValidator.property = "socialSecurityNumber";
                validators.push(socialSecurityNumberValidator);
                thisContactValidators.addItem(socialSecurityNumberValidator);

                var dobValidator:SAPDateValidator =  SAPValidators.createDateValidator(contact, "dateOfBirth", false, 36500, 0, SAP.instance.PSPDate);
                dobValidator.enabled = (contact.contactRoleCd == "PrimaryPrincipal" && isDirectDepositCustomer());
                validators.push(dobValidator);
                thisContactValidators.addItem(dobValidator);

                var address1Validator:Validator = SAPValidators.createRequiredFieldValidator(contact.address, "addressLine1", true);
                address1Validator.enabled = false;
                address1Validator.requiredFieldError = "This field is required to specify an address.";

                validators.push(address1Validator);
                thisContactValidators.addItem(address1Validator);

                var cityValidator:Validator = SAPValidators.createRequiredFieldValidator(contact.address, "city", true);
                cityValidator.enabled = false;
                cityValidator.requiredFieldError = "This field is required to specify an address.";
                validators.push(cityValidator);
                thisContactValidators.addItem(cityValidator);

                var stateValidator:StringValidator = SAPValidators.createStringValidator(contact.address, "state", true, 2);
                stateValidator.enabled = false;
                stateValidator.requiredFieldError = "This field is required to specify an address.";
                stateValidator.tooShortError = "State must contain 2 letters.";
                validators.push(stateValidator);
                thisContactValidators.addItem(stateValidator);

                var zipValidator:StringValidator = SAPValidators.createStringValidator(contact.address, "zipCode", true, 5);
                zipValidator.enabled = false;
                zipValidator.requiredFieldError = "This field is required to specify an address.";
                zipValidator.tooShortError = "Zip code must be at least 5 digits long.";
                validators.push(zipValidator);
                thisContactValidators.addItem(zipValidator);

                var zipExtValidator:StringValidator = SAPValidators.createStringValidator(contact.address, "zipCodeExtension", false, 4);
                zipExtValidator.enabled = false;
                zipExtValidator.tooShortError = "Zip code Extension must be at least 4 digits long.";
                validators.push(zipExtValidator);
                thisContactValidators.addItem(zipExtValidator);
            }
            updateCanSave();
            dispatchEvent(new Event(CONTACTS_CHANGED));
        }

        public function contactsUpdated():Boolean{
            var i:int = 0;
            contactsSnapshot = backingPropertiesSnapshot["contacts"];
            for each (var contact:Contact in contacts) {
                var changed:int = 0;
                if (contactsSnapshot != null) {
                    if (contactsSnapshot.length > i) {
                        changed = ObjectUtil.compare(contact, contactsSnapshot.getItemAt(i));
                        if(changed)
                            return true;
                    } else if(contact.contactRole== ContactRole.SECONDARY_PRINCIPAL) {
                        return true;
                    }
                }
                i++;
            }
            return false;
        }
        //address fields required if (any part of the) address present, or else not required
        //also find out which contacts changed for displaying to the user
        override protected function evaluateCanSave():Boolean {
            var i:int = 0;
            for each (var contact:Contact in contacts) {
                var changed:int = 0;
                if (backingPropertiesSnapshot && backingPropertiesSnapshot["contacts"] != null) {
                    if (ArrayCollection(backingPropertiesSnapshot["contacts"]).length > i) {
                        Contact(ArrayCollection(backingPropertiesSnapshot["contacts"]).getItemAt(i)).changed = contact.changed;   //stupid hack.  flex won't ignore [transient] or anything. Jerks.
                        changed = ObjectUtil.compare(contact, ArrayCollection(backingPropertiesSnapshot["contacts"]).getItemAt(i));
                    } else {
                        changed = 1;
                    }
                }
                contact.changed = changed != 0;
                i++;


                if (contactValidators[contact] != null) {
                    if (
                            (contact.address.addressLine1 != null && contact.address.addressLine1 != "") ||
                                    (contact.address.addressLine2 != null && contact.address.addressLine2 != "") ||
                                    (contact.address.city != null && contact.address.city != "") ||
                                    (contact.address.state != null && contact.address.state != "") ||
                                    (contact.address.zipCode != null && contact.address.zipCode != "") ||
                                    (contact.address.zipCodeExtension != null && contact.address.zipCodeExtension != "")
                            ) {
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("addressLine1")).enabled = true;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("city")).enabled = true;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("state")).enabled = true;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("zipCode")).enabled = true;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("zipCodeExtension")).enabled = true;
                    } else {
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("addressLine1")).enabled = false;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("city")).enabled = false;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("state")).enabled = false;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("zipCode")).enabled = false;
                        Validator(LookupCollection(contactValidators[contact]).getItemByKey("zipCodeExtension")).enabled = false;
                    }
                }
            }

            dispatchEvent(new Event(CONTACTS_CHANGED));

            return super.evaluateCanSave();
        }

        //split out by tab to inform user better
        override protected function evaluateIsValid(fireEvents:Boolean = true):Boolean {
            if (validators.length > 0) {
                var totalFailures:int = 0;
                for each (var contact:Contact in contacts) {
                    var failures:int = 0;
                    if (contactValidators[contact] != null) {
                        failures = SAPValidators.validateAll(LookupCollection(contactValidators[contact]).source, !fireEvents).length;
                    }
                    contact.errors = failures;
                    totalFailures+=failures;
                }
                dispatchEvent(new Event(CONTACTS_CHANGED));
                return totalFailures == 0;
            } else {
                return super.evaluateIsValid(fireEvents);
            }
        }

        override public function get hasChanged():Boolean {
            return hasDeleted || super.hasChanged;
        }

		override protected function loadModelData():void {

			if (companyKey != null) {
				SAP.instance.companyService.getCompanyContacts(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onContactsLoaded));
            } else {
                if (clearValues) {
                    var newCompanyContacts:CompanyContacts = new CompanyContacts();
                    var newContactCollection:ArrayCollection = new ArrayCollection();
                    newContactCollection.addItem(createNewContact(ContactRole.PAYROLL_ADMIN));
                    newContactCollection.addItem(createNewContact(ContactRole.PRIMARY_PRINCIPAL));
                    newCompanyContacts.contacts = newContactCollection;

                    contacts = newCompanyContacts.contacts;
                }
                modelDataLoaded();
            }

		}

        private function createNewContact(role:ContactRole):Contact {
            var newContact:Contact = new Contact();
            newContact.contactRoleCd = role.contactRoleCd;
            newContact.address = new Address();
            //if these are null, validators won't handle it correctly
            newContact.address.addressLine1="";
            newContact.address.city="";
            newContact.address.state="";
            newContact.address.zipCode="";
            newContact.newContactOrder = newContacts++;
            newContact.firstName = "";
            newContact.lastName = "";
            newContact.socialSecurityNumber = "";
            newContact.dateOfBirth = null;
            return newContact;
        }

		public function onContactsLoaded(e:ResultEvent):void {
			for each (var contact:Contact in CompanyContacts(e.result).contacts) {
                if (contact.address == null) {
                    contact.address = new Address();
                }
            }
            CompanyContacts(e.result).contacts.filterFunction = function(contact:Contact):Boolean {
                return !contact.isDeleted;
            };

            contacts = CompanyContacts(e.result).contacts as ArrayCollection;
            contactsSnapshot = contacts;
		}

        public function addContact(contactRole:ContactRole):void {
            var newContact:Contact = createNewContact(contactRole);
            contacts.addItem(newContact);
            contacts.refresh();
            createValidatorsFromContacts();
            loadMetadata();
            updateCanSave();
            selectedContactIndex = contacts.length - 1;
        }

        public function getAvailableRolesToCopy(from:Contact):ArrayCollection {
            var availableRolesToCopy:ArrayCollection = new ArrayCollection();
            if (additionalContacts != null) {
                for each (var additionalContact:Contact in additionalContacts) {
                    availableRolesToCopy.addItem(additionalContact);
                }
            }
            for each (var contact:Contact in contacts) {
                if (contact != from) {
                    availableRolesToCopy.addItem(contact);
                }
            }
            return availableRolesToCopy;
        }

        public function copy(source:Contact, target:Contact):void {
            target.prefix = source.prefix;
            target.firstName = source.firstName;
            target.middleName = source.middleName;
            target.lastName = source.lastName;
            target.jobTitle = source.jobTitle;
            target.suffix = source.suffix;
            target.phoneNumber = source.phoneNumber;
            target.faxNumber = source.faxNumber;
            target.email = source.email;

            if(target.contactRoleCd == "PrimaryPrincipal")
            {
                target.socialSecurityNumber = source.socialSecurityNumber;
                target.dateOfBirth = source.dateOfBirth;
            }

            if (source.address != null) {
                target.address.addressLine1 = source.address.addressLine1;
                target.address.addressLine2 = source.address.addressLine2;
                target.address.city = source.address.city;
                target.address.state = source.address.state;
                target.address.zipCode = source.address.zipCode;
                target.address.zipCodeExtension = source.address.zipCodeExtension;
            } else {
                target.address.addressLine1 = "";
                target.address.addressLine2 = "";
                target.address.city = "";
                target.address.state = "";
                target.address.zipCode = "";
                target.address.zipCodeExtension = "";
            }
            target.communicationPref = source.communicationPref;
        }

        public function deleteContact(contact:Contact):void {
            //contactIndexDetached = true;
            contact.isDeleted = true;
            contacts.refresh();

            hasDeleted = true;

            contactsSnapshot.removeItemAt(selectedContactIndex);
            selectedContactIndex = Math.max(0, selectedContactIndex - 1);
            createValidatorsFromContacts();
        }

        public function clearContact(contact:Contact):void {
            contact.prefix="";
            contact.firstName="";
            contact.middleName="";
            contact.lastName="";
            contact.jobTitle="";
            contact.suffix="";
            contact.phoneNumber="";
            contact.faxNumber="";
            contact.email="";
            contact.address.addressLine1="";
            contact.address.addressLine2="";
            contact.address.addressLine3="";
            contact.address.city="";
            contact.address.state="";
            contact.address.zipCode="";
            contact.address.zipCodeExtension="";
            contact.socialSecurityNumber = "";
            contact.dateOfBirth = null;
            contact.communicationPref=CommunicationPrefEnum.PHONE;
        }

        override public function cancel():void {
            if (Contact(contacts.getItemAt(selectedContactIndex)).newContactOrder > -1) {
                selectedContactIndex = 0;
            }
            contacts = backingPropertiesSnapshot["contacts"];
            contactsSnapshot = contacts;
            super.cancel();
        }

		override protected function executeSave():void {
			var contactsToSave:ArrayCollection = new ArrayCollection();

            for each (var contact:Contact in contacts) {
                if (!contact.isDeleted) {
                    contact.address.replaceEmptyWithNull();
                    contactsToSave.addItem(contact);
                }
            }
            SAP.instance.companyService.updateCompanyContacts(companyKey.sourceSystemCd, companyKey.companyId, contactsToSave,caseId, createSaveResponder(onContactsSaved));
            caseId = null;
		}

		public function onContactsSaved(e:ResultEvent):void {
			SAP.instance.dispatchEvent(EntityChangeEvent.createEvent(
											EntityChangeEvent.ENTITY_SAVED,
											EntityChangeEvent.COMPANY_CONTACTS,
											companyKey.toString()));
		}

        public function isDirectDepositCustomer():Boolean{
            if (company == null) {
                return false;
            }

            if(company.companyServiceState == CompanyServiceState.AssistedPending || company.companyServiceState == CompanyServiceState.AssistedActive || company.companyServiceState == CompanyServiceState.DIYDD){
                return true;
            }
            return false;
        }



        public function mayEditContact(data:Contact):Boolean {
            if(company == null) {
                return true;
            } else if(company.companyServiceState == CompanyServiceState.DIYDD) {
                return SAP.canPerformOperation(OperationsEnum.EDIT_COMPANY_CONTACT_INFORMATION)
            } else if (company.companyServiceState == CompanyServiceState.DIYOnly) {
                return SAP.canPerformOperation(OperationsEnum.EDIT_PRINCIPAL_CONTACTS_DIY)
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                if (data.contactRole == ContactRole.PRIMARY_PRINCIPAL || data.contactRole == ContactRole.SECONDARY_PRINCIPAL) {
                    return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS_PENDING_ACTIVATION);
                } else {
                    return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_PENDING);
                }
            } else if (company.companyServiceState == CompanyServiceState.AssistedActive) {
                if (data.contactRole == ContactRole.PRIMARY_PRINCIPAL || data.contactRole == ContactRole.SECONDARY_PRINCIPAL) {
                    return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_COMPANY_PRINCIPAL_CONTACTS);
                } else {
                    return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_CONTACTS_ACTIVE);
                }
            }
            return false;
        }


	}
}

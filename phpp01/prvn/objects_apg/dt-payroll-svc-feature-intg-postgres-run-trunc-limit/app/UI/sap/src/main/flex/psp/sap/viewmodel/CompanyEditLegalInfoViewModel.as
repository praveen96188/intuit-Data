package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.core.UIComponent;
import mx.rpc.events.ResultEvent;
import mx.validators.StringValidator;
import mx.validators.Validator;

import psp.sap.application.SAP;
import psp.sap.application.enums.CompanyInspectorPageEnum;
import psp.sap.application.enums.OperationsEnum;
import psp.sap.model.Address;
import psp.sap.model.CompanyLegalInfo;
import psp.sap.model.CompanyServiceState;
import psp.sap.validators.SAPValidators;
import psp.sap.view.SAPDateField;
import psp.sap.viewmodel.events.EntityChangeEvent;

public class CompanyEditLegalInfoViewModel extends AbstractPartViewModel {

    [Bindable]
    [BackingProperty(context=true, required=false, recursive=true)]
    public var legalInfo:CompanyLegalInfo;

    [BackingProperty(context=true)]
    public var clearValues:Boolean = true;
    [ArrayElementType("psp.sap.model.Address")]
    [Bindable]
    [BackingProperty(context=true, required=false)]
    public var additionalAddresses:ArrayCollection = new ArrayCollection();

    [Bindable]
    public var IndustryTypes:ArrayCollection;

    public var industryTemp:String;

    [Bindable]
    public var showSaveButtons:Boolean = true;
    [Bindable]
    public var effectiveDateValidator:Validator;
    [Bindable]
    [BackingProperty]
    public var effectiveDate:String ="";
    private var mCanEditEffectiveDate:Boolean = false;
    [Bindable]
    [BackingProperty(context=true)]
    public var includeEffectiveDate:Boolean = true;
    private var mEin:String = null;
    private var mCurrentEin:String;
    [Bindable]
    [BackingProperty]
    public var isError:Boolean = false;

    [Bindable]
    public var caseId:String;

    public function CompanyEditLegalInfoViewModel() {
        effectiveDate = "";
        var fromValidationDate:Date = SAP.instance.PSPDate;
        fromValidationDate.setDate(fromValidationDate.getDate() - (9 * 365));
        effectiveDateValidator = SAPValidators.createDateValidator(this, "effectiveDate", true, 0, -1, fromValidationDate, "Effective Date is way in the past.", null);
        this.label = CompanyInspectorPageEnum.COMPANY_LEGAL_INFO;
        this.reloadOnSave = true;

    }

    override protected function initializeBackingProperties():void {
        clearValidators();
        effectiveDate = "";
        isError = false;

        validators.push(SAPValidators.createRequiredFieldValidator(legalInfo, "legalName", true));
        validators.push(SAPValidators.createRequiredFieldValidator(legalInfo.address, "addressLine1", true));
        validators.push(SAPValidators.createRequiredFieldValidator(legalInfo.address, "city", true));
        validators.push(SAPValidators.createRequiredFieldValidator(legalInfo.address, "state", true));
        validators.push(SAPValidators.createRequiredFieldValidator(legalInfo.address, "zipCode", true));

        //For Direct Deposit Customer Industry type is mandatory
        if (isDirectDepositCustomer()) {
            validators.push(SAPValidators.createRequiredFieldValidator(legalInfo, "industryType", true));
        }

        var zipValidator:StringValidator = SAPValidators.createStringValidator(legalInfo.address, "zipCode", true, 5);
        zipValidator.tooShortError = "Zipcode must be at least 5 digits long.";
        validators.push(zipValidator);
        validators.push(SAPValidators.createEinValidator(this, "ein", true));
        validators.push(effectiveDateValidator);
        includeEffectiveDate = (company != null && company.isAssisted && company.companyServiceState != CompanyServiceState.AssistedPending);
        canEditEffectiveDate = false;
        effectiveDateValidator.enabled = false;
    }


    [Bindable]
    [BackingProperty]
    public function get canEditEffectiveDate():Boolean {
        return mCanEditEffectiveDate;
    }

    public function set canEditEffectiveDate(value:Boolean):void {
        mCanEditEffectiveDate = includeEffectiveDate && value;
        effectiveDateValidator.enabled = mCanEditEffectiveDate;
        if (!effectiveDateValidator.enabled && effectiveDateValidator.listener != null) {
            UIComponent(effectiveDateValidator.listener).errorString = "";
        }
    }


    [Bindable]
    [BackingProperty]
    public function get ein():String {
        return mEin;
    }

    public function set ein(value:String):void {
        mEin = value;
        if (includeEffectiveDate) {
            if (mCurrentEin == value) {
                canEditEffectiveDate = false;
            } else {
                canEditEffectiveDate = true;
            }
        }
    }


    override protected function loadModelData():void {


        if (companyKey != null) {

            SAP.instance.companyService.getCompanyLegalInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onLegalInfoLoaded));
        } else {
            if (clearValues) {
                var newLegalInfo:CompanyLegalInfo = new CompanyLegalInfo();
                newLegalInfo.address = new Address();
                legalInfo = newLegalInfo;
            }
            modelDataLoaded();
        }
    }

    public function populateIndustryType():void {
        SAP.instance.companyService.getIndustryTypes(createLoadModelDataResponder(onIndustryTypesLoaded));
    }

    private function onLegalInfoLoaded(e:ResultEvent):void {

        legalInfo = e.result as CompanyLegalInfo;
        mCurrentEin = legalInfo.ein;
        ein = mCurrentEin;
        effectiveDate = "";
        industryTemp = legalInfo.industryType;
    }

    private function onIndustryTypesLoaded(e:ResultEvent):void {
        if (e.result) {
            IndustryTypes = new ArrayCollection();
            IndustryTypes.addItem(null);

            var industryList = e.result

            for (var ind in industryList) {
                IndustryTypes.addItem(industryList[ind]);
            }


        }
        legalInfo.industryType = industryTemp;
    }

    public function copy(from:Address):void {
        legalInfo.address.addressLine1 = from.addressLine1;
        legalInfo.address.addressLine2 = from.addressLine2;
        legalInfo.address.addressLine3 = from.addressLine3;
        legalInfo.address.city = from.city;
        legalInfo.address.state = from.state;
        legalInfo.address.zipCode = from.zipCode;
        legalInfo.address.zipCodeExtension = from.zipCodeExtension;
    }

    protected override function executeSave():void {
        legalInfo.ein = ein;
        legalInfo.isOldEinError = isError;
        legalInfo.ein = legalInfo.ein.replace(/-/g, "");
        if (canEditEffectiveDate) {
            legalInfo.einEffectiveDate = SAPDateField.stringToDate(effectiveDate, SAP.instance.configuration.dateFormatShort);
        }

        SAP.instance.companyService.updateCompanyLegalInfo(company.sourceSystemCd, company.companyId, legalInfo, caseId, createSaveResponder(onSaveSucceeded));
    }

    protected function onSaveSucceeded(e:ResultEvent):void {
        // publish event on successful save
        SAP.instance.dispatchEvent(EntityChangeEvent.createEvent(
                EntityChangeEvent.ENTITY_SAVED,
                EntityChangeEvent.COMPANY,
                company.sourceSystemCd + ":" + company.companyId));
        effectiveDate = "";
    }

    public function mayEditEin():Boolean {
        if (company == null) {
            return true;
        }
        if (company.companyServiceState == CompanyServiceState.AssistedPending) {
            return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_EIN_PENDING);
        } else if (company.companyServiceState == CompanyServiceState.AssistedActive) {
            return SAP.canPerformOperation(OperationsEnum.EDIT_ASSISTED_EIN_ACTIVE);
        }

        return true; //i.e. if can view this page
    }


    //Revisit the method.

    public function isDirectDepositCustomer():Boolean {
        if (company == null) {
            return false;
        }
        if (company.companyServiceState == CompanyServiceState.AssistedPending || company.companyServiceState == CompanyServiceState.AssistedActive || company.companyServiceState == CompanyServiceState.DIYDD) {
            return true;
        }
        return false;
    }
}
}
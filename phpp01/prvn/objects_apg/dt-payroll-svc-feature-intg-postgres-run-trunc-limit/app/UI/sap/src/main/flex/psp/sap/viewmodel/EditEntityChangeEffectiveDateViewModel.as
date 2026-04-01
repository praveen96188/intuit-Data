package psp.sap.viewmodel {
import mx.rpc.events.ResultEvent;
import mx.validators.Validator;

import psp.sap.application.SAP;
import psp.sap.application.enums.CompanyInspectorPageEnum;
import psp.sap.model.EntityChange;
import psp.sap.validators.SAPValidators;
import psp.sap.view.SAPDateField;

public class EditEntityChangeEffectiveDateViewModel extends AbstractPartViewModel {
    public static const SUCCESSOR:String = "Successor";
    public static const NON_SUCCESSOR:String = "NonSuccessor";
    [Bindable]
    [BackingProperty]
    public var entityChange:EntityChange;
    [Bindable]
    [BackingProperty]
    public var effectiveDate:String;
    [Bindable]
    public var effectiveDateValidator:Validator;
    private var mSuccessor:String;
    public var oldEin:String;
    public var newEin:String;

    public function EditEntityChangeEffectiveDateViewModel() {
        this.label = CompanyInspectorPageEnum.ENTITY_CHANGE_EDIT_EFFECTIVE_DATE;
        var fromValidationDate:Date = SAP.instance.PSPDate;
        fromValidationDate.setDate(fromValidationDate.getDate() - (9 * 365));
        effectiveDateValidator = SAPValidators.createDateValidator(this, "effectiveDate", true, 0, -1, fromValidationDate, "Effective Date is way in the past.", null);
        validators.push(SAPValidators.createRequiredFieldValidator(this, "successor", true));
    }

    override protected function loadModelData():void {
        SAP.instance.companyService.getEntityChange(companyKey.sourceSystemCd, companyKey.companyId, oldEin, newEin, createLoadModelDataResponder(onEntityChangeLoaded));
    }

    protected function onEntityChangeLoaded(e:ResultEvent):void {
        entityChange = e.result as EntityChange;
        if (entityChange.isSuccessor) {
            successor = SUCCESSOR;
        } else {
            successor = NON_SUCCESSOR;
        }
    }

    override protected function initializeBackingProperties():void {
        validators.push(effectiveDateValidator);
        validators.push(SAPValidators.createRequiredFieldValidator(this, "successor", true));
    }

    protected override function executeSave():void {
        entityChange.effectiveDate = SAPDateField.stringToDate(effectiveDate, SAP.instance.configuration.dateFormatShort);
        if (successor == SUCCESSOR) {
            entityChange.isSuccessor = true;
        } else {
            entityChange.isSuccessor = false;
        }
        SAP.instance.companyService.updateEntityChange(company.sourceSystemCd, company.companyId, entityChange, createSaveResponder());
    }

    [Bindable]
    [BackingProperty]
    public function get successor():String {
        return mSuccessor;
    }

    public function set successor(value:String):void {
        mSuccessor = value;
    }
}
}
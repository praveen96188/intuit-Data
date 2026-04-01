package psp.sap.viewmodel
{
import mx.collections.ArrayCollection;
import mx.rpc.events.ResultEvent;

import psp.sap.application.SAP;
import psp.sap.application.enums.FraudInspectorPageEnum;

public class FraudEmployeeBankAccountMatchViewModel extends AbstractPartViewModel
{
    private var mEmployeeFraudBankAccounts:ArrayCollection;

    public var payrollRunId:String;

    [Bindable]
    public function get employeeFraudBankAccounts():ArrayCollection{
        return mEmployeeFraudBankAccounts;
    }
    public function set employeeFraudBankAccounts(value:ArrayCollection):void{
        mEmployeeFraudBankAccounts = value;
    }

    override protected function loadModelData():void{
        if(payrollRunId != null){
            SAP.instance.employeeService.checkEmployeeBankAccountFraud(companyKey.sourceSystemCd, companyKey.companyId, payrollRunId, createLoadModelDataResponder(onEmployeeFraudBankAccountsLoaded));
        }
    }

    private function onEmployeeFraudBankAccountsLoaded(e:ResultEvent):void{
        employeeFraudBankAccounts = e.result as ArrayCollection;
    }
}
}
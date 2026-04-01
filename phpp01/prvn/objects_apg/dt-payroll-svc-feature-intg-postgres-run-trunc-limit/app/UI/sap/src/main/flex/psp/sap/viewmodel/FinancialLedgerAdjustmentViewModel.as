/**
 * User: dweinberg
 * Date: 1/10/12
 * Time: 9:50 AM
 */
package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.collections.Sort;
import mx.collections.SortField;
import mx.rpc.Responder;
import mx.rpc.events.ResultEvent;
import mx.validators.Validator;

import psp.sap.application.SAP;
import psp.sap.formatters.SAPDateFormatters;
import psp.sap.model.CompanyLedgerAccount;
import psp.sap.model.LawItem;
import psp.sap.model.PayrollRun;
import psp.sap.model.ResultBalance;
import psp.sap.validators.SAPNotSameValidator;
import psp.sap.validators.SAPValidators;

public class FinancialLedgerAdjustmentViewModel extends AbstractPartViewModel {

    static public const MAX_AMOUNT:Number = 99999.99;

    [Bindable]
    public var sourcePayrollRunId:String; //optionally set by caller

    [Bindable]
    public var paycheckDate:String;

    private var payrollRun:PayrollRun;

    [Bindable]
    [BackingProperty]
    [ArrayElementType("psp.sap.model.CompanyLedgerAccount")]
    public var ledgerAccounts:ArrayCollection = new ArrayCollection();

    [Bindable]
    [ArrayElementType("psp.sap.model.LawItem")]
    public var laws:ArrayCollection = new ArrayCollection();

    [ArrayElementType("psp.sap.model.CompanyLedgerAccount")]
    private var companyBalances:ArrayCollection = new ArrayCollection();

    [ArrayElementType("psp.sap.model.CompanyLedgerAccount")]
    private var payrollBalances:ArrayCollection = new ArrayCollection();

    [Bindable]
    [BackingProperty]
    [ArrayElementType("psp.sap.model.ResultBalance")]
    public var accountBalances:ArrayCollection;


    private var mSelectedDebit:CompanyLedgerAccount;
    [Bindable]
    [BackingProperty]
    public function get selectedDebit():CompanyLedgerAccount {
        return mSelectedDebit;
    }
    public function set selectedDebit(value:CompanyLedgerAccount):void {
        mSelectedDebit = value;
        if (mSelectedDebit != null) {
            onChangeLedgerAccount();
        }
    }
    /*Defining String getter to bind validator to selected Combo box item */
    public function get debitAccCode():String {
        if (selectedDebit != null && selectedDebit.ledgerAccountCode != null && "" != selectedDebit.ledgerAccountCode) {
            return(selectedDebit.ledgerAccountCode);
        } else {
            return "";
        }
    }

    private var mSelectedCredit:CompanyLedgerAccount = null;
    [Bindable]
    [BackingProperty]
    public function set selectedCredit(value:CompanyLedgerAccount):void {
        mSelectedCredit = value;
        if (mSelectedCredit != null) {
            onChangeLedgerAccount();
        }
    }
    public function get selectedCredit():CompanyLedgerAccount {
        return mSelectedCredit;
    }
    public function get creditAccCode():String {
        if (selectedCredit != null && selectedCredit.ledgerAccountCode != null && "" != selectedCredit.ledgerAccountCode) {
            return(selectedCredit.ledgerAccountCode);
        } else {
            return "";
        }
    }


    private var mLaw:LawItem = null;
    [Bindable]
    [BackingProperty]
    public function get law():LawItem {
        return mLaw;
    }
    public function set law(value:LawItem):void {
        mLaw = value;
        if (mLaw != null && mLaw.lawId != null && mLaw.lawId.length > 0) {
            onChangeLaw();
            showLawBalance = true;
        } else {
            showLawBalance = false;
        }
    }
    public function get selectedLawId():String {
        if (law != null && law.lawId != null && law.lawId != "") {
            return(law.lawId);
        } else {
            return "";
        }
    }


    private var mShowLawDropDown:Boolean = false;

    [Bindable]
    public function get showLawDropDown():Boolean {
        return mShowLawDropDown;
    }

    public function set showLawDropDown(value:Boolean):void {
        mShowLawDropDown = value;
        lawValidator.enabled = mShowLawDropDown;
    }


    private var mAmount:String = "";
    [Bindable]
    [BackingProperty]
    public function get amount():String {
        return mAmount;
    }
    public function set amount(value:String):void {
        mAmount = value;
        if (mAmount != null && mAmount.length > 0) {
            onChangeAmount();
        } else {
            resetAccountBalances();
        }
    }

    [Bindable]
    [BackingProperty]
    public var notes:String = "";

    [Bindable]
    public var showPayrollBalance:Boolean;
    [Bindable]
    public var showLawBalance:Boolean;
    [Bindable]
    public var showDetailPanel:Boolean;

    /*Validators*/
    [Bindable]
    public var debitRequiredValidator:Validator;
    [Bindable]
    public var creditRequiredValidator:Validator;
    [Bindable]
    public var notesValidator:Validator;
    [Bindable]
    public var lawValidator:Validator;
    [Bindable]
    public var amountValidator:Validator;
    [Bindable]
    public var sameDebitAndCreditAccountValidator:SAPNotSameValidator;

    private var mSort:Sort;

    public static function createActivator(sourcePayrollRunId:String):Object {
        return {"sourcePayrollRunId":sourcePayrollRunId};
    }

    public function FinancialLedgerAdjustmentViewModel() {
        super();
        debitRequiredValidator = SAPValidators.createStringValidator(this, "debitAccCode", true, 1);
        validators.push(debitRequiredValidator);
        creditRequiredValidator = SAPValidators.createStringValidator(this, "creditAccCode", true, 1);
        validators.push(creditRequiredValidator);
        lawValidator = SAPValidators.createStringValidator(this, "selectedLawId", true, 1);
        lawValidator.enabled = false;
        validators.push(lawValidator);
        amountValidator = SAPValidators.createNumberValidator(this, "amount", true, 0.01, MAX_AMOUNT, false, 2);
        validators.push(amountValidator);
        notesValidator = SAPValidators.createStringValidator(this, "notes", true, 1, 3900);
        validators.push(notesValidator);
        sameDebitAndCreditAccountValidator = SAPValidators.createSAPNotSameValidator(this, this, "debitAccCode", "creditAccCode", false);
        validators.push(sameDebitAndCreditAccountValidator);
        showDetailPanel = false;
        showPayrollBalance = false;
        showLawBalance = false;
        mSort = new Sort();
        mSort.fields = [new SortField("name", true)];
    }

    override protected function loadModelData():void {
        loadCount = 1;
        SAP.instance.payrollRunService.findLedgerAccounts(
                company.companyId, company.sourceSystemCd,
                createLoadModelDataResponder(onCompanyLedgerAccountsLoaded));
        if (sourcePayrollRunId != null && sourcePayrollRunId != "") {
            loadCount += 3;
            SAP.instance.payrollRunService.findLedgerAccountsByPayroll(
                    company.companyId, company.sourceSystemCd, sourcePayrollRunId,
                    createLoadModelDataResponder(onPayrollLedgerAccountsLoaded));
            SAP.instance.payrollRunService.findPayrollRun(company.sourceSystemCd, company.companyId, sourcePayrollRunId,
                                                          createLoadModelDataResponder(onPayrollRunLoaded));
            SAP.instance.payrollRunService.getPayrollLaws(company.companyId, company.sourceSystemCd, sourcePayrollRunId,
                                                          createLoadModelDataResponder(onLawsLoaded));
        }
    }

    public function onCompanyLedgerAccountsLoaded(e:ResultEvent):void {
        companyBalances = e.result as ArrayCollection;
        companyBalances.sort = mSort;
        companyBalances.refresh()
    }

    public function onPayrollLedgerAccountsLoaded(e:ResultEvent):void {
        payrollBalances = e.result as ArrayCollection;
        payrollBalances.sort = mSort;
        payrollBalances.refresh();
    }

    public function onPayrollRunLoaded(e:ResultEvent):void {
        payrollRun = e.result as PayrollRun;
    }

    public function onLawsLoaded(e:ResultEvent):void {
        var lawsTemp:ArrayCollection = e.result as ArrayCollection;
        var blankLaw:LawItem = new LawItem();
        blankLaw.lawId = "";
        blankLaw.name = "";
        lawsTemp.addItem(blankLaw);
        lawsTemp.sort = mSort;
        lawsTemp.refresh();
        laws = lawsTemp;
    }

    public function requiresLawFilter(item:CompanyLedgerAccount):Boolean {
        return (!item.requiresQuarterLaw);
    }


    override protected function initializeBackingProperties():void {
        paycheckDate = payrollRun != null ? SAPDateFormatters.dateFormatShort.format(payrollRun.paycheckDate).toString() : "";

        /*Initialize ledger accounts for credit/debit drop down*/
        var ledgerAccountsTemp:ArrayCollection = new ArrayCollection();

        var blankLedgerAcc:CompanyLedgerAccount = new CompanyLedgerAccount();
        blankLedgerAcc.name = "";
        blankLedgerAcc.balance = 0;
        blankLedgerAcc.ledgerAccountCode = "";
        blankLedgerAcc.description = "";
        ledgerAccountsTemp.addItem(blankLedgerAcc);
        for each(var account:CompanyLedgerAccount in companyBalances) {
            ledgerAccountsTemp.addItem(account);
        }
        ledgerAccountsTemp.sort = mSort;
        ledgerAccountsTemp.refresh();

        if (sourcePayrollRunId == null || sourcePayrollRunId == "") {
            ledgerAccountsTemp.filterFunction = requiresLawFilter;
            ledgerAccountsTemp.refresh();
        }

        ledgerAccounts = ledgerAccountsTemp;

        selectedCredit = (ledgerAccounts != null && ledgerAccounts.length > 0) ?
                ledgerAccounts.getItemAt(0) as CompanyLedgerAccount : null;
        selectedDebit = (ledgerAccounts != null && ledgerAccounts.length > 0) ?
                ledgerAccounts.getItemAt(0) as CompanyLedgerAccount : null;


        /*Update account balances for company Balance*/

        var resBalanceTemp:ResultBalance;
        var resultingBalanceListTemp:ArrayCollection = new ArrayCollection();
        var balance:Number;
        for each(var companyAccount:CompanyLedgerAccount in companyBalances) {
            resBalanceTemp = new ResultBalance();
            resBalanceTemp.accountCode = companyAccount.ledgerAccountCode;
            resBalanceTemp.accountName = companyAccount.name;
            resBalanceTemp.accountType = companyAccount.accountType;
            resBalanceTemp.credit = companyAccount.credit;

            balance = companyAccount.balance;

            resBalanceTemp.companyBalance = balance;
            resBalanceTemp.curCompanyBalance = balance;
            resultingBalanceListTemp.addItem(resBalanceTemp);
        }
        accountBalances = resultingBalanceListTemp;

        /*Update account Balances for payroll*/
        if (sourcePayrollRunId != null && sourcePayrollRunId != "") {
            showPayrollBalance = true;
            var tempAccountBalances:ArrayCollection = accountBalances;
            if (tempAccountBalances != null || tempAccountBalances.length > 0) {

                for (var counter:int = 0; counter < tempAccountBalances.length; counter++) {

                    resBalanceTemp = tempAccountBalances.getItemAt(counter) as ResultBalance;
                    var ledgerAccTemp:CompanyLedgerAccount = payrollBalances.getItemAt(counter) as CompanyLedgerAccount;

                    balance = ledgerAccTemp.balance;

                    resBalanceTemp.payrollBalance = balance;
                    resBalanceTemp.curPayrollBalance = balance;
                    tempAccountBalances.setItemAt(resBalanceTemp, counter);
                }

                accountBalances = tempAccountBalances;
            }
        }

        amount = "";
        notes = "";
    }


    public function onChangeLedgerAccount():void {
        showLawDropDown = (sourcePayrollRunId != null && sourcePayrollRunId != "") &&
                (selectedCredit != null && selectedCredit.name != "" && selectedDebit != null && selectedDebit.name != "") &&
                (selectedCredit.requiresQuarterLaw || selectedDebit.requiresQuarterLaw);

        if (showLawDropDown) {
            law = laws != null && laws.length > 0 ? laws.getItemAt(0) as LawItem : null;
        } else {
            showLawBalance = false;
        }
        isShowDetailSection();
    }

    public function isShowDetailSection():void {
        showDetailPanel = (selectedCredit != null && selectedDebit != null && selectedCredit.name != "" && selectedDebit.name != "")
                && (selectedCredit.requiresQuarterLaw || selectedDebit.requiresQuarterLaw ? (law != null && law.name != "") : true  );
    }

    public function onChangeLaw():void {
        if (law != null && law.name != "") {
            SAP.instance.payrollRunService.getLedgerAccountBalanceForLaw(company.companyId,
                                                                         company.sourceSystemCd,
                                                                         sourcePayrollRunId,
                                                                         law.lawId,
                                                                         new Responder(onLedgerAccountsLawBalanceLoaded,
                                                                                       onLoadModelDataFaulted));
        }
        isShowDetailSection();
    }

    public function onLedgerAccountsLawBalanceLoaded(e:ResultEvent):void {
        var ledgerAccountsTemp:ArrayCollection = e.result as ArrayCollection;
        ledgerAccountsTemp.sort = mSort;
        ledgerAccountsTemp.refresh();
        updateLawBalances(ledgerAccountsTemp);
    }

    public function updateLawBalances(ledgerAccountsTemp:ArrayCollection):void {
        var tempAccountBalances:ArrayCollection = accountBalances;
        if (tempAccountBalances != null || tempAccountBalances.length > 0) {
            for (var counter:int = 0; counter < tempAccountBalances.length; counter++) {

                var resBalanceTemp:ResultBalance = tempAccountBalances.getItemAt(counter) as ResultBalance;
                var lawBalanceTemp:CompanyLedgerAccount = ledgerAccountsTemp.getItemAt(counter) as CompanyLedgerAccount;

                var lawBalanceForQuarter:Number = lawBalanceTemp.requiresQuarterLaw ?
                        lawBalanceTemp.balance : Number.NaN;

                resBalanceTemp.lawBalance = lawBalanceForQuarter;
                resBalanceTemp.curLawBalance = lawBalanceForQuarter;
                tempAccountBalances.setItemAt(resBalanceTemp, counter);
            }
            accountBalances = tempAccountBalances;
        }
    }

    public function onChangeAmount():void {
        var creditAccCode:String = selectedCredit.ledgerAccountCode;
        var debitAccCode:String = selectedDebit.ledgerAccountCode;
        var creditAccIndex:int;
        var debitAccIndex:int;
        resetAccountBalances();
        if (accountBalances.length < 1) {
            return;
        }
        for (var i:int = 0; i < accountBalances.length; i++) {
            var tempResultingBal:ResultBalance = accountBalances[i] as ResultBalance;
            if (tempResultingBal.accountCode == creditAccCode) {
                creditAccIndex = i;
            }
            if (tempResultingBal.accountCode == debitAccCode) {
                debitAccIndex = i;
            }
        }
        var creditAcc:ResultBalance = accountBalances.getItemAt(creditAccIndex) as ResultBalance;
        var debitAcc:ResultBalance = accountBalances.getItemAt(debitAccIndex) as ResultBalance;
        var tempAmount:Number = parseFloat(amount);

        var debitAccBalanceSign:Number = (selectedDebit.creditAddsToBalance ? -1 : 1) * (selectedDebit.credit ? -1 : 1);
        var creditAccBalanceSign:Number = (selectedCredit.creditAddsToBalance ? -1 : 1) * (selectedCredit.credit ? -1 : 1);
        var debitAccBalanceRule:Number = selectedDebit.creditAddsToBalance ? -1 : 1;
        var creditAccBalanceRule:Number = selectedCredit.creditAddsToBalance ? 1 : -1;


        creditAcc.lawBalance = Math.abs((creditAcc.curLawBalance * creditAccBalanceSign)  + (tempAmount * creditAccBalanceRule));
        creditAcc.payrollBalance = Math.abs((creditAcc.curPayrollBalance  * creditAccBalanceSign) + (tempAmount * creditAccBalanceRule));
        creditAcc.companyBalance = Math.abs((creditAcc.curCompanyBalance * creditAccBalanceSign) + (tempAmount * creditAccBalanceRule));
        debitAcc.lawBalance = Math.abs((debitAcc.curLawBalance * debitAccBalanceSign) + (tempAmount * debitAccBalanceRule));
        debitAcc.payrollBalance = Math.abs((debitAcc.curPayrollBalance * debitAccBalanceSign) + (tempAmount * debitAccBalanceRule));
        debitAcc.companyBalance = Math.abs((debitAcc.curCompanyBalance * debitAccBalanceSign) + (tempAmount * debitAccBalanceRule));
        accountBalances.setItemAt(creditAcc, creditAccIndex);
        accountBalances.setItemAt(debitAcc, debitAccIndex);
        accountBalances.refresh();

    }

    public function resetAccountBalances():void {
        if (accountBalances != null && accountBalances.length > 0) {
            for each(var acc:ResultBalance in accountBalances) {
                acc.companyBalance = acc.curCompanyBalance;
                acc.payrollBalance = acc.curPayrollBalance;
                acc.lawBalance = acc.curLawBalance;

            }
        }

    }

    override protected function executeSave():void {
        SAP.instance.payrollRunService.addFinancialLedgerAdjustmentTransaction(company.companyId,
                                                                               company.sourceSystemCd,
                                                                               payrollRun == null ? null : payrollRun.id,
                                                                               selectedDebit.ledgerAccountCode,
                                                                               selectedCredit.ledgerAccountCode,
                                                                               parseFloat(mAmount),
                                                                               (law == null || law.lawId == "") ? null : law.lawId,
                                                                               notes, createSaveResponder());


    }
}
}

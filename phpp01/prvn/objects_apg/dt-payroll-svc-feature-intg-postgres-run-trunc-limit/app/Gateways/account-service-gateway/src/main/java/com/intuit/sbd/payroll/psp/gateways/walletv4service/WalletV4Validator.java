package com.intuit.sbd.payroll.psp.gateways.walletv4service;

import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.WalletV4CloneModel;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class WalletV4Validator {

    private Pattern validPhonePattern;
    private Pattern invalidPhonePattern;
    private Pattern invalidNamePattern;
    private Pattern invalidAccountPattern;

    private static final String PHONE = "phone";
    private static final String NAME = "name";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static final String INVALID_ACCOUNT_NUMBER_REGEX = "[\\s-]+";
    public static final String INVALID_NAME_REGEX = "[^a-zA-Z0-9ÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝàáâãäåçèéêëìíîïñòóôõöùúûüýÿŸŒœ.,/'&()?@!#~*_;+ -]";
    public static final String VALID_NAME_REGEX = "^[a-zA-Z0-9ÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝàáâãäåçèéêëìíîïñòóôõöùúûüýÿŸŒœ.,/'&()?@!#~*_;+ -]{0,200}$";
    public static final String INVALID_PHONE_REGEX = "[^0-9-+()]";
    public static final String VALID_PHONE_REGEX = "[0-9-+()]{10,20}";

    @Autowired
    public WalletV4Validator() {
        this.validPhonePattern = Pattern.compile(VALID_PHONE_REGEX);
        this.invalidPhonePattern = Pattern.compile(INVALID_PHONE_REGEX);
        this.invalidNamePattern = Pattern.compile(INVALID_NAME_REGEX);
        this.invalidAccountPattern = Pattern.compile(INVALID_ACCOUNT_NUMBER_REGEX);
    }

    public void validateWalletCreateResponse(WalletBankAccount walletBankAccountRequest, WalletBankAccount walletBankAccountResponse, String realmId) {
        boolean nameMismatch = false;
        boolean parentIdMismatch = false;
        boolean last4DigitMimatch = false;

        //validate Name
        if(!walletBankAccountRequest.getName().equals(walletBankAccountResponse.getName())) {
            nameMismatch = true;
        }
        //validate parent id
        if(!walletBankAccountRequest.getParentId().equals(walletBankAccountResponse.getParentId())) {
            parentIdMismatch = true;
        }
        //validating last four digit of wallet and accountNumber
        String accountNumber = walletBankAccountRequest.getAccountNumber();
        String walletId = walletBankAccountResponse.getId().getLocalId();
        String accountNumberLastFourDigit = accountNumber.substring(accountNumber.length() - 4);
        String responseWalletLastFourDigit = walletId.substring(walletId.length() - 4);
        //Last 4 digit of Wallet and Account number match for all accounts should match
        //Exception - If account number is 4 digit, wallet
        if(accountNumber.length() > 4 && !accountNumberLastFourDigit.equals(responseWalletLastFourDigit)) {
            last4DigitMimatch = true;
        }
        log.info("Action=WalletCreateResponseCheck NameMismatch={} ParentIdMismatch={} Last4DigitMismatch={} ParentId={} ParentType={} WalletId={} " +
                        "RealmId={}", nameMismatch, parentIdMismatch, last4DigitMimatch, walletBankAccountRequest.getParentId(), walletBankAccountRequest.getParentType(), walletId,realmId);
    }

    public WalletBankAccount sanitiseFieldsInWalletBankAccount(WalletBankAccount walletBankAccount) {
        String fullName = validateAndSanitiseStringsUsingRegex(walletBankAccount.getName(), NAME);
        String phone = validateAndSanitiseStringsUsingRegex(walletBankAccount.getPhone(), PHONE);
        String accountNumber = validateAndSanitiseStringsUsingRegex(walletBankAccount.getAccountNumber(), ACCOUNT_NUMBER);

        walletBankAccount.setName(fullName);
        walletBankAccount.setPhone(!StringUtil.isNullOrEmpty(phone) ? phone : null);
        walletBankAccount.setAccountNumber(accountNumber);
        return walletBankAccount;
    }

    public String validateAndSanitiseStringsUsingRegex(String givenString, String validateFor) {
        String result = null;
        if(!StringUtil.isNullOrEmpty(givenString)) {
            switch(validateFor) {
                case PHONE:
                    result = invalidPhonePattern.matcher(givenString).replaceAll("");
                    result = validPhonePattern.matcher(result).matches() ? result : null;
                    break;
                case NAME:
                    result = invalidNamePattern.matcher(givenString).replaceAll("?");
                    break;
                case ACCOUNT_NUMBER:
                    result = invalidAccountPattern.matcher(givenString).replaceAll("");
                    break;
            }
        }
        return result;
    }

    public void validateWalletCloneResponse(WalletV4CloneModel walletV4CloneModel, WalletBankAccount walletBankAccount) {
        boolean parentIdMismatch = false;
        boolean last4DigitMimatch = false;

        //validate parent id
        if(!walletV4CloneModel.getParentId().equals(walletBankAccount.getParentId())){
            parentIdMismatch = true;
        }

        //validating last four digit of new and old wallet id
        String oldWalletId = walletV4CloneModel.getWalletIds().get(0);
        String newWalletId = walletBankAccount.getId().getLocalId();
        String oldWalletIdLastFourDigit = oldWalletId.substring(oldWalletId.length() - 4);
        String newWalletIdLastFourDigit = newWalletId.substring(newWalletId.length() - 4);

        if(!oldWalletIdLastFourDigit.equals(newWalletIdLastFourDigit)) {
            last4DigitMimatch = true;
        }

        log.info("Action=WalletCloneResponseCheck ParentIdMismatch={} Last4DigitMismatch={}" +
                        " ParentId={} ParentType={} OldWalletId={} NewWalletId={} OldRealmId={} NewRealmId={} ",
                parentIdMismatch, last4DigitMimatch, walletV4CloneModel.getParentId(), walletV4CloneModel.getParentType(),
                oldWalletId, newWalletId, walletV4CloneModel.getOldRealmId(), walletV4CloneModel.getNewRealmId());
    }

}

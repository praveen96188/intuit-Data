/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/BankAccountDTO.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.ACHBankAccountType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.EntryClassCode;

/**
 * @author Marcela Villani
 */
public class BankAccountDTO {

    public static final int ROUTING_NUMBER_LENGTH = 9;
    public static final int ACCOUNT_NUMBER_LENGTH = 17;

    private String accountNumber;
    private String routingNumber;
    private BankAccountType accountType;
    private ACHBankAccountType achAccountType;
    private String bankName;
    private EntryClassCode achEntryClass;
    private String sessionId;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String pAccountNumber) {
        if(pAccountNumber != null) {
            pAccountNumber = pAccountNumber.trim();
        }
        this.accountNumber = pAccountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String pRoutingNumber) {
        if(pRoutingNumber != null) {
            pRoutingNumber = pRoutingNumber.trim();
        }
        this.routingNumber = pRoutingNumber;
    }

    public BankAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(BankAccountType pAccountType) {
        this.accountType = pAccountType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String pBankName) {
        this.bankName = pBankName;
    }

    public EntryClassCode getAchEntryClass() {
        return achEntryClass;
    }

    public void setAchEntryClass(EntryClassCode achEntryClass) {
        this.achEntryClass = achEntryClass;
    }

    public ACHBankAccountType getAchAccountType() {
        return achAccountType;
    }

    public void setAchAccountType(ACHBankAccountType pAchAccountType) {
        achAccountType = pAchAccountType;
    }

    /**
     * Validates a Bank Account DTO
     *
     * @return  ProcessResult with errors
     */
    public ProcessResult validateBankAccountDTO() {
        ProcessResult validationResult = new ProcessResult();
        String accountId = null;

        if (accountType == null) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "AccountType");
        }

        if (accountNumber == null) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "AcctNum");
        } else if (accountNumber.length() == 0 || !isValidBankAccountNumber(accountNumber)) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "AcctNum");
        }

        if (routingNumber == null) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, accountId, "RoutingNum");
        } else if (routingNumber.length() == 0 || !isValidRoutingNumber(routingNumber)) {
            validationResult.getMessages()
                    .BankAccountRoutingNumberInvalid(EntityName.BankAccount, accountNumber, routingNumber);
        }

        if (bankName != null && !Validator.isValidLength(bankName, 1, 255)) {
            validationResult.getMessages().InvalidValue(EntityName.BankAccount, bankName, "BankName");
        }

        return validationResult;
    }


    public static boolean isValidRoutingNumber(String pRoutingNumber) {
        char[] routingNumberArray;
        if (pRoutingNumber == null ||
                (routingNumberArray = pRoutingNumber.toCharArray()).length != ROUTING_NUMBER_LENGTH) {
            return false;
        }

        // Run through each digit and calculate the total.
        int total = 0, digit0, digit1, digit2;
        for (int i = 0; i < routingNumberArray.length; i += 3) {
            digit0 = Character.digit(routingNumberArray[i], 10);
            digit1 = Character.digit(routingNumberArray[i + 1], 10);
            digit2 = Character.digit(routingNumberArray[i + 2], 10);
            if (digit0 == -1 || digit1 == -1 || digit2 == -1) {
                return false;
            }
            total += digit0 * 3 + digit1 * 7 + digit2;
        }

        // If the resulting sum is a multiple of ten (but not zero),
        // the aba routing number is good.
        return (total > 0 && total % 10 == 0);
    }


    /**
     * @param pAccountNumber
     * @return  boolean indicating if bank account number is valid
     */
    public static boolean isValidBankAccountNumber(String pAccountNumber) {
        boolean valid = (pAccountNumber != null &&
                pAccountNumber.length() > 0 &&
                pAccountNumber.length() <= ACCOUNT_NUMBER_LENGTH);
        for (int i = 0; valid && i < pAccountNumber.length(); i++) {
            char ch = pAccountNumber.charAt(i);
            // Any printable characters which have an EBCDIC value greater than hexadecimal 3F - ASCII 32-126
            valid = (ch > 31 && ch < 127);
        }
        return valid;
    }

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


}

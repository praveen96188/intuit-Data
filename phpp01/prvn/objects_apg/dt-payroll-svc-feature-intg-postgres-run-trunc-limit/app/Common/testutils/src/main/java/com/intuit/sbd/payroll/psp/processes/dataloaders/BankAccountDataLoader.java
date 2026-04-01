package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Marcela Villani
 */
public class BankAccountDataLoader {

    public static BankAccount generateBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(getRandomAccountNumber(6));
        bankAccount.setAccountTypeCd(BankAccountType.Checking);
        bankAccount.setBankName(getRandomBankName());
        bankAccount.setEffectiveDate(SpcfCalendar.getNow());
        bankAccount.setRoutingNumber(generateRoutingNumber());

        return Application.save(bankAccount);
    }

    public static BankAccountDTO generateBankAccountDTO() {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();
        bankAccountDTO.setAccountNumber(getRandomAccountNumber(6));
        bankAccountDTO.setAccountType(BankAccountType.Checking);
        bankAccountDTO.setBankName(getRandomBankName());
        bankAccountDTO.setRoutingNumber(generateRoutingNumber());

        return bankAccountDTO;
    }

    public static BankAccount getBankAccountFromDTO(BankAccountDTO pBankAccountDTO) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountNumber(pBankAccountDTO.getAccountNumber());
        BankAccountType domainBAType = BankAccountType.valueOf(pBankAccountDTO.getAccountType().toString());
        bankAccount.setAccountTypeCd(domainBAType);
        bankAccount.setEffectiveDate(PSPDate.getPSPTime());
        bankAccount.setBankName(pBankAccountDTO.getBankName());
        bankAccount.setRoutingNumber(pBankAccountDTO.getRoutingNumber());

        return bankAccount;
    }

    public static BankAccountDTO getBankAccountDTOFromBankAccount(BankAccount pBankAccount) {
        BankAccountDTO bankAccountDTO = new BankAccountDTO();

        bankAccountDTO.setAccountNumber(pBankAccount.getAccountNumber());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccount.getAccountTypeCd().toString()));
        bankAccountDTO.setBankName(pBankAccount.getBankName());
        bankAccountDTO.setRoutingNumber(pBankAccount.getRoutingNumber());

        return bankAccountDTO;
    }

    public static String getRandomAccountNumber(int pNumberOfDigits) {
        String accountNumber = "";
        double digit = Math.random();
        accountNumber = String.valueOf(digit);
        return accountNumber.substring(3, pNumberOfDigits + 3);
    }

    public static String generateRoutingNumber() {

        int total = 0;
        String routingNumber = "";
        int multiplier[] = {3, 7, 1, 3, 7, 1, 3, 7};

        for (int i = 0; i < 8; i++) {
            String strDigit = String.valueOf(Math.random()).substring(3, 4);
            strDigit = strDigit.equals(null) ? "1" : strDigit;
            routingNumber += strDigit;
            total += Integer.parseInt(strDigit) * multiplier[i];
        }

        // Find checksum digit
        int checkSumDigit = (total % 10) == 0 ? 0 : 10 - (total % 10);
        routingNumber += checkSumDigit;

        return routingNumber;

    }

    public static String getRandomBankName() {
        String bankNames[] = {"Bank of America", "Wachovia", "Wells Fargo", "HSBC", "Bank of Nevada", "Citibank"};
        List<String> bankNamesList = Arrays.asList(bankNames);
        Collections.shuffle(bankNamesList);
        return bankNamesList.get(0);
    }


}

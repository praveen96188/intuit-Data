package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentSplitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 9, 2009
 * Time: 8:29:38 AM
 */
public class BillPaymentTranslator {
    public static SpcfLogger logger = SpcfLogManager.getLogger(BillPaymentTranslator.class);

    public static PayeeDTO buildPayeeDTOFromPayeeWSDTO(QBPayee pQBPayee) {
        if (pQBPayee == null) {
            return null;
        }

        PayeeDTO payeeDTO = new PayeeDTO();
        payeeDTO.setEmail(pQBPayee.getEmailAddress());
        payeeDTO.setName(pQBPayee.getName());
        payeeDTO.setSourcePayeeId(pQBPayee.getPayeeSourceId());
        payeeDTO.setTaxId(pQBPayee.getTaxId());
        if(pQBPayee.getIs1099() != null) {
            payeeDTO.setIs1099(pQBPayee.getIs1099());
        } else {
            payeeDTO.setIs1099(false);
        }

        payeeDTO.setPhone(pQBPayee.getPhoneNumber());
        payeeDTO.setMailingAddress(buildAddressDTOFromAddressWSDTO(pQBPayee.getAddress()));
        payeeDTO.setAccountNumber(pQBPayee.getAccountNumber());
        return payeeDTO;
    }

    public static AddressDTO buildAddressDTOFromAddressWSDTO(QBAddress pQBAddress) {
        if (pQBAddress == null) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1(pQBAddress.getAddressLine1());
        addressDTO.setAddressLine2(pQBAddress.getAddressLine2());
        addressDTO.setAddressLine3(pQBAddress.getAddressLine3());
        addressDTO.setCity(pQBAddress.getCity());
        addressDTO.setCountry(pQBAddress.getCountry());
        addressDTO.setState(pQBAddress.getState());
        addressDTO.setZipCode(pQBAddress.getZipCode());
        addressDTO.setZipCodeExtension(pQBAddress.getZipCodeExtension());
        return addressDTO;
    }

    public static PayeeBankAccountDTO buildPayeeBankAccountDTOFromBillPaymentSplitWSDTO(PayeeBankAccount pPayeeBankAccount, QBBankAccount pBankAccountWSDTO, String sessionId) {
        if (pBankAccountWSDTO == null) {
            return null;
        }

        BankAccountDTO bankAccountDTO;
        PayeeBankAccountDTO payeeBankAccountDTO;
        if (pPayeeBankAccount == null) {
            bankAccountDTO = new BankAccountDTO();
            payeeBankAccountDTO = new PayeeBankAccountDTO();
            payeeBankAccountDTO.setPayeeBankAccountId(pBankAccountWSDTO.getSourceBankAccountId());
            //payeeBankAccountDTO.setSessionId();
            payeeBankAccountDTO.setBankAccount(bankAccountDTO);
        } else {
            payeeBankAccountDTO = PayrollServices.dtoFactory.create(pPayeeBankAccount);
            bankAccountDTO = payeeBankAccountDTO.getBankAccount();
        }

        bankAccountDTO.setAccountNumber(pBankAccountWSDTO.getAccountNumber());
        bankAccountDTO.setRoutingNumber(pBankAccountWSDTO.getRoutingNumber());
        bankAccountDTO.setAccountType(BankAccountType.valueOf(pBankAccountWSDTO.getAccountType().value()));
        bankAccountDTO.setBankName(pBankAccountWSDTO.getBankName());
        bankAccountDTO.setSessionId(sessionId);
        return payeeBankAccountDTO;
    }

    public static BillPaymentDTO buildBillPaymentDTO(PaymentTransaction pPaymentTransaction,
                                                     PayeeDTO pPayeeDTO,
                                                     Collection<BillPaymentSplitDTO> pBillPaymentDTOs,
                                                     DateDTO pDepositeDate,
                                                     SpcfMoney pNetAmount) {
        BillPaymentDTO billPaymentDTO = new BillPaymentDTO();
        billPaymentDTO.setBillPaymentId(pPaymentTransaction.getTransactionId());
        billPaymentDTO.setPayeeDTO(pPayeeDTO);
        billPaymentDTO.setPaymentTransactions(pBillPaymentDTOs);
        billPaymentDTO.setAmount(pNetAmount);
        billPaymentDTO.setDepositDate(pDepositeDate);
        billPaymentDTO.setMemo(pPaymentTransaction.getMemo());
        billPaymentDTO.setSessionId(pPaymentTransaction.getSessionId());
        if(pPaymentTransaction.getTransactionType() != null)
            billPaymentDTO.setTransactionType(BillPaymentTransactionType.valueOf(pPaymentTransaction.getTransactionType().toString()));
        return billPaymentDTO;
    }

    public static FeeTransaction buildFeeTransaction(BillingDetail pBillingDetail, Boolean pHasOffloaded, ArrayList<String> pAssociatedTransactionIds) {
        FeeTransaction feeTransaction = new FeeTransaction();
        feeTransaction.setFeeAmount(SpcfUtils.convertToBigDecimal((SpcfMoney) pBillingDetail.getItemTotal().subtract(pBillingDetail.getTaxAmount())));
        feeTransaction.setTaxAmount(SpcfUtils.convertToBigDecimal(pBillingDetail.getTaxAmount()));
        //Todo Change this in v1.5
        if (pBillingDetail.getOfferingServiceChargeType().equals(OfferingServiceChargeType.PerPayment)) {
            feeTransaction.setFeeType(FeeTypeEnum.valueOf(OfferingServiceChargeType.PerPaycheck.toString()));
        } else {
            feeTransaction.setFeeType(FeeTypeEnum.valueOf(pBillingDetail.getOfferingServiceChargeType().toString()));
        }
        feeTransaction.setNumberOfTransactions(pBillingDetail.getQuantity());
        feeTransaction.setSettlementDate(SpcfUtils.convertSpcfCalendarToDate(getBillingDetailSettlemetDate(pBillingDetail.getFinancialTransactionCollection())));
        feeTransaction.setTransactionId(pBillingDetail.getId().toString());
        feeTransaction.setAssociatedTransactionIds(pAssociatedTransactionIds);
        feeTransaction.setHasOffloaded(pHasOffloaded);
        return feeTransaction;
    }

    private static SpcfCalendar getBillingDetailSettlemetDate(Collection<FinancialTransaction> pFinancialTransactions) {
        for (FinancialTransaction financialTransaction : pFinancialTransactions) {
            FinancialTransactionState financialTransactionState = financialTransaction.getCurrentFinancialTransactionState();
            if (financialTransactionState != null && !financialTransactionState.getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Voided) &&
                    !financialTransactionState.getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
                return financialTransaction.getSettlementDate();
            }
        }
        return null;
    }
}

package com.intuit.sbd.payroll.psp.adapters.lt;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Mar 8, 2010
 * Time: 10:03:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class LtPayee {




       public ArrayList<LtPayeeDTO> getPayees(String pCompanyId)throws Exception{

        //Create an empty ArrayList<LtCompanyDTO>
        ArrayList<LtPayeeDTO> dtoList = new ArrayList<LtPayeeDTO>();

        try{
            PayrollServices.beginUnitOfWork();

            Expression<PayeeBankAccount> query =
                    new Query<PayeeBankAccount>()
                            .Where(PayeeBankAccount.Payee().Company().SourceCompanyId().equalTo(pCompanyId)
                                    .And(PayeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));

            DomainEntitySet<PayeeBankAccount> payeeList = Application.find(PayeeBankAccount.class, query);


            for (PayeeBankAccount ba : payeeList){
                LtPayeeDTO tempPayee = buildDTO(ba);

                //Add the company to the list for return
                dtoList.add(tempPayee);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(ex.getMessage());
         }

        return dtoList;
    }


    /**
     * Purpose: Builds a single LtPayeeDTO from the PayeeBankAccount object provided.
     * @param pba - PayeeBankAccount object to build the LtPayeeDTO
     * @return completed LtPayeeDTO for the provided employee bank account
     */
    private LtPayeeDTO buildDTO(PayeeBankAccount pba){
        LtPayeeDTO eeDTO = new LtPayeeDTO();

        eeDTO.setPayeeId(pba.getPayee().getSourcePayeeId());
        eeDTO.setPayeeBankAccountId(pba.getSourceBankAccountId());
        eeDTO.setPayeeName(pba.getPayee().getName());
        eeDTO.setPayeeBankAccountNumber(pba.getBankAccount().getAccountNumber());

        return eeDTO;

    }

}

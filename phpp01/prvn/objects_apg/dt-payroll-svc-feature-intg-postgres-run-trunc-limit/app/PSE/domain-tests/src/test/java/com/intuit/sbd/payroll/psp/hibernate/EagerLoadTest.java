package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.Ignore;
import org.junit.Test;
@Ignore
public class EagerLoadTest {

    //@Test // works fine
    public void Test1_EagerLoadSingleProperty(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company()
                .equalTo(MoneyMovementTransaction.Company()));

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works fine
    public void Test2_EagerLoadTwoProperties(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company()
                .equalTo(MoneyMovementTransaction.Company())).EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company().equalTo(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company()));

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // throws Exception
    public void Test3_EagerLoadSingleProperties_WithScalerCondition(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().Id()
                .equalTo(MoneyMovementTransaction.Company().Id()));

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // As of Now NotImplemented
    public void Test4_EagerLoadSingleProperties_WithANDConditionOnDifferentProperty(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company()
                .equalTo(MoneyMovementTransaction.Company())
                .And(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company().equalTo(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company())));

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test //throws exception
    public void Test5_EagerLoadSingleProperties_WithMoreThanSingleHierarchy(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company().equalTo(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company()));

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works fine
    public void Test6_EagerLoadSingleNonCollectionProperty(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.Company()));

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }


    //@Test // works fine left
    public void Test7_EagerLoadSingleNonCollectionProperty_WHEREOnEagerlyLoadedEntity(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.Company()));

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.MoneyMovementTransaction().Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // hibernate throws exception
    public void Test8_EagerLoadSingleProperty_EagerlyLoadedEntityOnRHS(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.Company().equalTo(FinancialTransaction.MoneyMovementTransaction().Company()));

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works fine
    public void Test9_EagerLoadPath(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction());

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works but converts to inner
    public void Test10_EagerLoadPath_WithWHEREOnEagerEntity(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction());

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.MoneyMovementTransaction().Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // hibernate throws exception
    public void Test11_EagerLoadSingleProperty_EagerlyLoadedEntityOnRHS(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.MoneyMovementTransaction().QbdtTransactionInfo().Company()));

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // hibernate throws exception
    public void Test12_EagerLoadSingleProperty_EagerlyLoadedPathCompany(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.MoneyMovementTransaction().QbdtTransactionInfo().Company()));


        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works but no eager loading
    public void Test13_EagerLoadPath_MultiHierarchy(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.OrderBy()
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company());

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works but no eager loading
    public void Test14_EagerLoadPath_MultiHierarchy2(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query
                .EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company());

        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(FinancialTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test // works fine
    public void Test15_EagerLoadPath_MultiHierarchy(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company());

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test16_EagerLoadPath_EagerLoadCriteria_SameEntity(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet());

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test17_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet());

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test18_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded2(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Paycheck().Company().equalTo(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit().Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PaycheckSplit());

        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.generateRandomUniqueId()));

        Application.find(MoneyMovementTransaction.class, query);

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test19_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded2(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()));


        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance("1c6047a1-dedc-42eb-bbed-ae4c53a4345e")));

        DomainEntitySet<MoneyMovementTransaction> mmtSet = Application.find(MoneyMovementTransaction.class, query);

        MoneyMovementTransaction mmt = mmtSet.get(0);
        DomainEntitySet<FinancialTransaction> ftSet = mmt.getFinancialTransactionCollection();

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test21_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded2(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>();
        query = (Query<MoneyMovementTransaction>) query.EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().Company().equalTo(MoneyMovementTransaction.Company()))
                .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().PayrollRun().Company().equalTo(MoneyMovementTransaction.Company()));


        query = (Query<MoneyMovementTransaction>) query.Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance("d9684e23-18b1-4db2-8994-427c3a4cccd1")));

        DomainEntitySet<MoneyMovementTransaction> mmtSet = Application.find(MoneyMovementTransaction.class, query);

        MoneyMovementTransaction mmt = mmtSet.get(0);
        DomainEntitySet<FinancialTransaction> ftSet = mmt.getFinancialTransactionCollection();
        PayrollRun pr = ftSet.get(0).getPayrollRun();
        Company c = pr.getCompany();
        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test20_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded2(){
        Application.initialize();
        Application.beginUnitOfWork();
        Query<FinancialTransaction> query = new Query<FinancialTransaction>();
        query = (Query<FinancialTransaction>) query.EagerLoad(FinancialTransaction.MoneyMovementTransaction().Company().equalTo(FinancialTransaction.Company()));


        query = (Query<FinancialTransaction>) query.Where(FinancialTransaction.Id().equalTo(SpcfUniqueId.createInstance("72523638-3b1b-4705-a60a-fa5dee2b7072")));

        DomainEntitySet<FinancialTransaction> ftSet = Application.find(FinancialTransaction.class, query);

        FinancialTransaction ft = ftSet.get(0);
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

        Application.rollbackUnitOfWork();

    }

    //@Test
    public void Test22_EagerLoadPath_EagerLoadCriteria_OneEntityDirectlyLoaded2(){
        Application.initialize();
        Application.beginUnitOfWork();

        Company comp = Application.findById(Company.class, SpcfUniqueId.createInstance("cadaad17-32a4-4c57-9f5b-c737095b2528"));
        IntuitBankAccount ibc = new IntuitBankAccount();
        SpcfCalendar date = SpcfCalendar.getNow();

        Expression<EntryDetailRecord> query = new Query<EntryDetailRecord>()
                .Where(EntryDetailRecord.IntuitBankAccount().equalTo(ibc)
                        .And(EntryDetailRecord.InitiationDate().equalTo(date))
                        .And(EntryDetailRecord.MoneyMovementTransaction().InitiationDate().equalTo(date))
                        .And(EntryDetailRecord.MoneyMovementTransaction().MoneyMovementPaymentMethod().in(PaymentMethod.ACHCredit))
                        .And(EntryDetailRecord.MoneyMovementTransaction().Status().equalTo(PaymentStatus.Executed)))
                .EagerLoad(EntryDetailRecord.MoneyMovementTransaction().Company().equalTo(EntryDetailRecord.Company()))
                .EagerLoad(EntryDetailRecord.MoneyMovementTransaction().QbdtTransactionInfo().Company().equalTo(EntryDetailRecord.Company()))
                .EagerLoad(EntryDetailRecord.Company())
                .ReadOnly(true);
        DomainEntitySet<EntryDetailRecord> edr = (DomainEntitySet<EntryDetailRecord>)Application.findScrollable(EntryDetailRecord.class, query);


        Application.rollbackUnitOfWork();

    }

}
